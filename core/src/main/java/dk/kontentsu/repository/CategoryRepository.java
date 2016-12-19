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
package dk.kontentsu.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import dk.kontentsu.model.Category;
import dk.kontentsu.model.SemanticUriPath;
import dk.kontentsu.model.SemanticUriTaxonomy;
import dk.kontentsu.model.State;
import dk.kontentsu.model.Taxon;
import dk.kontentsu.model.Taxonomy;

/**
 * Repository for performing CRUD operations on category objects.
 *
 * @author Jens Borch Christiansen
 */
@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.MANDATORY)
public class CategoryRepository extends Repository<Category> {

    @Override
    public List<Category> findAll() {
        TypedQuery<SemanticUriPath> uriQuery = em.createNamedQuery(URI_FIND_ALL, SemanticUriPath.class);
        TypedQuery<Taxon> taxQuery = em.createNamedQuery(TAXON_FIND_ALL, Taxon.class);
        List<Category> result = new ArrayList<>(uriQuery.getResultList());
        result.addAll(taxQuery.getResultList());
        return result;
    }

    public List<SemanticUriPath> findActive() {
        TypedQuery<SemanticUriPath> query = em.createNamedQuery(URI_FIND_ACTIVE, SemanticUriPath.class);
        query.setParameter("state", State.ACTIVE);
        return new ArrayList<>(query.getResultList());
    }

    @Override
    public Category get(final UUID uuid) {
        TypedQuery<SemanticUriPath> uriQuery = em.createNamedQuery(URI_GET, SemanticUriPath.class);
        uriQuery.setParameter("uuid", uuid);
        try {
            return uriQuery.getSingleResult();
        } catch (NoResultException e) {
            TypedQuery<Taxon> taxQuery = em.createNamedQuery(TAXON_GET, Taxon.class);
            taxQuery.setParameter("uuid", uuid);
            return taxQuery.getSingleResult();
        }
    }

    public Optional<SemanticUriPath> findByUri(final SemanticUriPath path) {
        try {
            TypedQuery<SemanticUriPath> query = em.createNamedQuery(URI_FIND_BY_PATH, SemanticUriPath.class);
            query.setParameter("path", path.toString());
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public List<Category> getByTaxonomy(final Taxonomy taxonomy) {
        if (taxonomy.equals(SemanticUriTaxonomy.create())) {
            TypedQuery<SemanticUriPath> query = em.createNamedQuery(URI_FIND_ALL, SemanticUriPath.class);
            return new ArrayList<>(query.getResultList());
        } else {
            TypedQuery<Taxon> query = em.createNamedQuery(TAXON_FIND_BY_TAXONOMY, Taxon.class);
            query.setParameter("taxonomy", taxonomy);
            return new ArrayList<>(query.getResultList());
        }
    }

    public Category getByTaxonomy(final Taxonomy taxonomy, final String path) {
        if (taxonomy.equals(SemanticUriTaxonomy.create())) {
            TypedQuery<SemanticUriPath> query = em.createNamedQuery(URI_FIND_BY_PATH, SemanticUriPath.class);
            query.setParameter("path", path);
            return query.getSingleResult();
        } else {
            TypedQuery<Taxon> query = em.createNamedQuery(TAXON_FIND_BY_TAXONOMY_PATH, Taxon.class);
            query.setParameter("taxonomy", taxonomy);
            query.setParameter("path", path);
            return query.getSingleResult();
        }
    }

    public Optional<Category> findByTaxonomy(final Taxonomy taxonomy, final String path) {
        try {
            return Optional.of(getByTaxonomy(taxonomy, path));
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public void delete(final UUID uuid) {
        Category c = get(uuid);
        em.remove(c);
    }

}
