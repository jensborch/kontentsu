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

import java.util.List;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.transaction.UserTransaction;
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

/**
 * Service facade for performing various operations on CDN items - like uploading new items.
 *
 * @author Jens Borch Christiansen
 */
@Stateless
@TransactionManagement(value = TransactionManagementType.BEAN)
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

    @Resource
    private UserTransaction userTransaction;

    public Item upload(@Valid final UploadItem uploadeItem) {
        try {
            userTransaction.begin();

            Item item = findOrCreateItem(uploadeItem);
            Version version = addVersion(item, uploadeItem);
            addHosts(item, uploadeItem);
            itemRepo.save(item);
            userTransaction.commit();

            externalizer.externalize(version.getUuid());

            return item;
        } catch (Exception ex) {
            try {
                userTransaction.rollback();
            } catch (Exception e) {
                LOGGER.info("Error rolling back transaction", e);
            }
            throw new UploadException("Error uploading - transaction has been rolled back", ex);
        }
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

        ContentParser.Results parsedContent = ContentParser.create(content).parse();
        parsedContent.getComposition().stream().forEach(link -> {
            Item i = itemRepo.findByUri(link.getUri()).orElseGet(() -> {
                SemanticUriPath tmpPath = catRepo.findByUri(link.getPath()).orElse(link.getPath());
                Item tmpItem = new Item(new SemanticUri(tmpPath, link.getUri().getName()));
                return itemRepo.save(tmpItem);
            });
            builder.composition(i, link.getType());
        });

        Version version = builder.build();
        item.addVersion(version);
        return version;
    }

}
