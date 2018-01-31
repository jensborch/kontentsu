/*
 * The MIT License
 *
 * Copyright 2016 Jens Borch Christiansen.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package dk.kontentsu.upload;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import dk.kontentsu.externalization.ExternalizerService;
import dk.kontentsu.model.Content;
import dk.kontentsu.model.Host;
import dk.kontentsu.model.Item;
import dk.kontentsu.model.Term;
import dk.kontentsu.model.Version;
import dk.kontentsu.model.processing.InjectableContentProcessingScope;
import dk.kontentsu.parsers.ContentParser;
import dk.kontentsu.parsers.Link;
import dk.kontentsu.repository.HostRepository;
import dk.kontentsu.repository.ItemRepository;
import dk.kontentsu.repository.TermRepository;
import dk.kontentsu.spi.ContentProcessingMimeType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Service facade for performing various operations on CDN items - like uploading new items.
 *
 * @author Jens Borch Christiansen
 */
@Stateless
public class Uploader {

    private static final Logger LOGGER = LogManager.getLogger();

    @Inject
    private ItemRepository itemRepo;

    @Inject
    private HostRepository hostRepo;

    @Inject
    private TermRepository termRepo;

    @Inject
    private ExternalizerService externalizer;

    @Inject
    private Uploader self;

    @Inject
    private BeanManager bm;

    public UUID upload(@Valid final UploadItem uploadItem) {
        Version version = self.save(uploadItem);
        externalizer.externalize(version.getUuid());
        return version.getItem().getUuid();
    }

    public UUID uploadSync(@Valid final UploadItem uploadItem) {
        Version version = self.save(uploadItem);
        try {
            externalizer.externalize(version.getUuid()).get();
        } catch (InterruptedException | ExecutionException ex) {
            throw new UploadException("Error externalizing version : " + version.getUuid(), ex);
        }
        return version.getItem().getUuid();
    }

    /**
     * Overwrite a existing item with a given UUID. The will if needed change the internals of the existing active versions of the item, to make room for the new item.
     *
     * @param itemId     the UUID of the item to replace
     * @param uploadItem the new item to replace existing.
     * @return a list of the identifiers for the new versions created
     */
    public Set<UUID> overwrite(@NotNull final UUID itemId, @Valid final UploadItem uploadItem) {
        Set<UUID> externalized = self.overwriteAndSave(itemId, uploadItem);
        externalized.forEach(u -> externalizer.externalize(u));
        return externalized;
    }

    public Set<UUID> overwriteSync(final UUID itemId, @Valid final UploadItem uploadItem) {
        Set<UUID> externalized = self.overwriteAndSave(itemId, uploadItem);
        externalized.forEach(u -> {
            try {
                externalizer.externalize(u).get();
            } catch (InterruptedException | ExecutionException ex) {
                throw new UploadException("Error externalizing version : " + u, ex);
            }
        });
        return externalized;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Set<UUID> overwriteAndSave(final UUID itemId, @Valid final UploadItem uploadItem) {
        Item item = itemRepo.get(itemId);
        Set<UUID> toExternalize = new HashSet<>();
        //We need to create tmp set to avoid ConcurrentModificationException. HasSet do not support that we loop and modify at the same time.
        Set<Version> toAdd = new HashSet<>();
        item.getVersions()
            .stream()
            .filter(Version::isActive)
            .filter(i -> i.getInterval().overlaps(uploadItem.getInterval()))
            .forEach(v -> {
                LOGGER.debug("Deleting version {} with interval {}", v.getUuid(), v.getInterval());
                v.delete();
                v.getInterval().disjunctiveUnion(uploadItem.getInterval())
                    .stream()
                    .filter(i -> !i.overlaps(uploadItem.getInterval()))
                    .forEach(i -> {
                        Version n = Version.builder()
                            .version(v)
                            .interval(i)
                            .active()
                            .build();
                        LOGGER.debug("Adding new version {} with interval {}", n.getUuid(), n.getInterval());
                        toAdd.add(n);
                        toExternalize.add(n.getUuid());
                    });
            });
        toAdd.forEach(item::addVersion);
        Version version = addVersion(item, uploadItem);
        addHosts(item, uploadItem);
        toExternalize.add(version.getUuid());
        return toExternalize;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Version save(@Valid final UploadItem uploadItem) {
        Item item = findOrCreateItem(uploadItem);
        Version version = addVersion(item, uploadItem);
        addHosts(item, uploadItem);
        itemRepo.save(item);
        return version;
    }

    private Item findOrCreateItem(final UploadItem uploadItem) {
        Item.URI itemUri = uploadItem.getUri();
        Term term = termRepo.create(itemUri);
        return term.getItems()
            .stream().filter(i -> i.getEdition().equals(itemUri.getEdition()))
            .findAny()
            .orElse(new Item(term, itemUri.getEdition().orElse(null), uploadItem.getMimeType()));
    }

    private void addHosts(final Item item, final UploadItem uploadItem) {
        List<Host> hosts = hostRepo.findAll();
        if (uploadItem.getHosts().isEmpty()) {
            hosts.forEach(item::addHost);
        } else {
            hosts.stream()
                .filter(h -> uploadItem.getHosts().stream()
                    .anyMatch(n -> h.getName().equals(n)))
                .forEach(item::addHost);
        }
    }

    private ContentParser getContentParser(final Bean<?> b) {
        CreationalContext<?> ctx = bm.createCreationalContext(b);
        return (ContentParser) bm.getReference(b, ContentParser.class, ctx);
    }

    private Set<Bean<?>> findAllContentParserBeans() {
        return bm.getBeans(ContentParser.class, new AnnotationLiteral<Any>() {
            private static final long serialVersionUID = 1L;
        });
    }

    private Version addVersion(final Item item, final UploadItem uploadItem) {
        Content content = itemRepo.saveContent(uploadItem.getContent(), uploadItem.getEncoding(), uploadItem.getMimeType());

        Version.Builder builder = Version.builder()
            .content(content)
            .from(uploadItem.getInterval().getFrom())
            .to(uploadItem.getInterval().getTo());

        InjectableContentProcessingScope.execute(
            () -> findAllContentParserBeans().forEach(
                bean -> Arrays.stream(bean.getBeanClass().getAnnotationsByType(ContentProcessingMimeType.class)).forEach(a -> {
                    if (item.getMimeType().matches(a).isMatch()) {
                        parse(getContentParser(bean), item.getUri(), builder);
                    }
                })),
            content);

        Version version = builder.build();
        LOGGER.debug("Adding newly uploaded version {} to item with interval {}", version.getUuid(), version.getInterval());
        item.addVersion(version);
        return version;
    }

    private void parse(final ContentParser parser, final Item.URI uri, final Version.Builder builder) {
        ContentParser.Results parsedContent = parser.parse();
        parsedContent.getLinks()
            .stream()
            .filter(link -> !link.getUri().equals(uri))
            .forEach(link -> {
                Item i = findOrCreate(link);
                builder.reference(i, link.getType());
            });
        parsedContent.getMetadata().forEach((k, v) -> {
            LOGGER.debug("Found metadata in content with key {} and value {}", k, v);
            builder.metadata(k, v);
        });
    }

    private Item findOrCreate(final Link link) {
        return itemRepo.findByUri(link.getUri())
            .orElseGet(() -> {
                Term path = termRepo.create(link.getUri());
                LOGGER.debug("Found link in content with path {} and name {}", path, link.getUri().getEdition());
                Item tmpItem = new Item(path, link.getUri().getEdition().orElse(null), link.getUri().getMimeType());
                return itemRepo.save(tmpItem);
            });
    }
}
