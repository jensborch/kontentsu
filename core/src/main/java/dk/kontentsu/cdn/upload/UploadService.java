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
package dk.kontentsu.cdn.upload;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.kontentsu.cdn.externalization.ExternalizerService;
import dk.kontentsu.cdn.model.Content;
import dk.kontentsu.cdn.model.SemanticUri;
import dk.kontentsu.cdn.model.SemanticUriPath;
import dk.kontentsu.cdn.model.internal.Host;
import dk.kontentsu.cdn.model.internal.Item;
import dk.kontentsu.cdn.model.internal.Version;
import dk.kontentsu.cdn.parsers.ContentParser;
import dk.kontentsu.cdn.repository.CategoryRepository;
import dk.kontentsu.cdn.repository.HostRepository;
import dk.kontentsu.cdn.repository.ItemRepository;
import dk.kontentsu.cdn.spi.ContentContext;
import dk.kontentsu.cdn.spi.MimeType;

/**
 * Service facade for performing various operations on CDN items - like uploading new items.
 *
 * @author Jens Borch Christiansen
 */
@Stateless
public class UploadService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadService.class);

    @Inject
    private ItemRepository itemRepo;

    @Inject
    private HostRepository hostRepo;

    @Inject
    private CategoryRepository catRepo;

    @Inject
    private ExternalizerService externalizer;

    @Inject
    private UploadService self;

    @Inject
    private BeanManager bm;

    @TransactionAttribute(TransactionAttributeType.NEVER)

    public UUID upload(@Valid final UploadItem uploadeItem) {
        Version version = self.save(uploadeItem);
        externalizer.externalize(version.getUuid());
        return version.getItem().getUuid();
    }

    @TransactionAttribute(TransactionAttributeType.NEVER)
    public UUID overwrite(final UUID itemId, @Valid final UploadItem uploadeItem) {
        Set<UUID> externalized = self.overwriteAndSave(itemId, uploadeItem);
        externalized.stream().forEach(u -> externalizer.externalize(u));
        return itemId;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Set<UUID> overwriteAndSave(final UUID itemId, @Valid final UploadItem uploadeItem) {
        Item item = itemRepo.get(itemId);
        Set<UUID> externalized = new HashSet<>();
        //We need to create tmp set to avoid ConcurrentModificationException. HasSet do not support that we loop and modify at the same time.
        Set<Version> toAdd = new HashSet<>();
        item.getVersions()
                .stream()
                .filter(i -> i.isActive())
                .filter(i -> i.getInterval().overlaps(uploadeItem.getInterval()))
                .forEach(v -> {
                    LOGGER.debug("Deleting version {} with interval {}", v.getUuid(), v.getInterval());
                    v.delete();
                    v.getInterval().disjunctiveUnion(uploadeItem.getInterval())
                            .stream().forEach(i -> {
                                Version n = Version.builder()
                                        .version(v)
                                        .interval(i)
                                        .active()
                                        .build();
                                LOGGER.debug("Adding new version {} with interval {}", n.getUuid(), n.getInterval());
                                toAdd.add(n);
                                externalized.add(n.getUuid());
                            });
                });
        toAdd.forEach(v -> item.addVersion(v));
        Version version = addVersion(item, uploadeItem);
        addHosts(item, uploadeItem);
        externalized.add(version.getUuid());
        return externalized;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Version save(@Valid final UploadItem uploadeItem) {
        Item item = findOrCreateItem(uploadeItem);
        Version version = addVersion(item, uploadeItem);
        addHosts(item, uploadeItem);
        itemRepo.save(item);
        return version;
    }

    private Item findOrCreateItem(final UploadItem uploadeItem) {
        SemanticUriPath path = catRepo.findByUri(uploadeItem.getUri().getPath())
                .orElse(uploadeItem.getUri().getPath());
        Item item = path.getItem(uploadeItem.getUri().getName())
                .orElse(new Item(new SemanticUri(path, uploadeItem.getUri().getName())));

        return item;
    }

    private void addHosts(final Item item, final UploadItem uploadeItem) {
        List<Host> hosts = hostRepo.findAll();
        if (uploadeItem.getHosts().isEmpty()) {
            hosts.stream().forEach(h -> item.addHost(h));
        } else {
            hosts.stream()
                    .filter(h -> uploadeItem.getHosts().stream()
                    .filter(n -> h.getName().equals(n))
                    .findAny()
                    .isPresent())
                    .forEach(h -> item.addHost(h));
        }
    }

    private Version addVersion(final Item item, final UploadItem uploadeItem) {
        Content content = itemRepo.saveContent(uploadeItem.getContent(), uploadeItem.getEncoding(), uploadeItem.getMimeType());

        Version.Builder builder = Version.builder()
                .content(content)
                .from(uploadeItem.getInterval().getFrom())
                .to(uploadeItem.getInterval().getTo());

        ContentContext.execute(() -> {
            Set<Bean<?>> beans = bm.getBeans(ContentParser.class, new AnnotationLiteral<Any>() { });
            beans.forEach(b -> {
                Set<Annotation> qualifiers = b.getQualifiers();
                qualifiers.forEach(q -> {
                    if (q.annotationType() == MimeType.class) {
                        String type = ((MimeType) q).type();
                        if (type.equals(content.getMimeType().toString())) {
                            CreationalContext<?> ctx = bm.createCreationalContext(b);
                            ContentParser p = (ContentParser) bm.getReference(b, ContentParser.class, ctx);
                            ContentParser.Results parsedContent = p.parse();
                            parsedContent.getLinks().stream().forEach(link -> {
                                Item i = itemRepo.findByUri(link.getUri()).orElseGet(() -> {
                                    SemanticUriPath tmpPath = catRepo.findByUri(link.getPath()).orElse(link.getPath());
                                    LOGGER.debug("Found link in content with path {} and name {}", tmpPath, link.getUri().getName());
                                    Item tmpItem = new Item(new SemanticUri(tmpPath, link.getUri().getName()));
                                    return itemRepo.save(tmpItem);
                                });
                                builder.reference(i, link.getType());
                            });
                            parsedContent.getMetadata().forEach((k, v) -> {
                                LOGGER.debug("Found metadata in content with key {} and value {}", k, v);
                                builder.metadata(k, v);
                            });
                        }
                    }
                });
            });
            return null;
        }, content);

        Version version = builder.build();
        LOGGER.debug("Adding newly uploadet version {} to item with interval {}", version.getUuid(), version.getInterval());
        item.addVersion(version);
        return version;
    }

}
