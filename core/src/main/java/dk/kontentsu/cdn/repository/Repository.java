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
package dk.kontentsu.cdn.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.validation.Valid;

/**
 * Repository interface used for data access to CDN database.
 *
 * @author Jens Borch Christiansen
 *
 * @param <E> type of entities managed by repository
 */
public abstract class Repository<E> {

    public static final String TAXONOMY_FIND_ALL = "Taxonomy.findAll";
    public static final String TAXONOMY_GET = "Taxonomy.get";
    public static final String TAXONOMY_GET_BY_NAME = "Taxonomy.getByName";
    public static final String HOST_FIND_ALL = "Host.findAll";
    public static final String HOST_GET = "Host.get";
    public static final String HOST_GET_BY_NAME = "Host.getByName";
    public static final String URI_FIND_ALL = "Uri.findAll";
    public static final String URI_GET = "Uri.get";
    public static final String URI_FIND_ACTIVE = "Uri.findActive";
    public static final String URI_FIND_BY_PATH = "Uri.findByUri";
    public static final String TAXON_FIND_ALL = "Taxon.findAll";
    public static final String TAXON_GET = "Taxon.get";
    public static final String TAXON_FIND_BY_TAXONOMY_PATH = "Taxon.findByTaxonomyAndPath";
    public static final String TAXON_FIND_BY_TAXONOMY = "Taxon.findByTaxonomy";
    public static final String ITEM_GET = "Item.get";
    public static final String ITEM_FIND_ALL = "Item.findAll";
    public static final String ITEM_FIND_BY_URI = "Item.findByUri";
    public static final String CONTENT_GET = "Content.get";
    public static final String VERSION_GET = "Version.get";
    public static final String EXTERNAL_FILE_GET = "ExternalFile.get";
    public static final String EXTERNAL_FILE_FIND_ALL = "ExternalFile.findAll";
    public static final String EXTERNAL_FILE_FIND_BY_URI = "ExternalFile.findByUri";
    public static final String EXTERNAL_FILE_FIND_ALL_AT_DATE_TIME = "ExternalFile.findAllAt";
    public static final String EXTERNAL_FILE_SCHEDULE = "ExternalFile.schedule";
    public static final String EXTERNAL_FILE_FIND_ALL_IN_INTERVAL = "ExternalFile.findAllInInterval";

    @Inject
    protected EntityManager em;

    public abstract E get(UUID uuid);

    public Optional<E> find(final UUID uuid) {
        try {
            return Optional.of(get(uuid));
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public abstract List<E> findAll();

    public E save(@Valid final E entity) {
        em.persist(entity);
        em.flush();
        return entity;
    }

}
