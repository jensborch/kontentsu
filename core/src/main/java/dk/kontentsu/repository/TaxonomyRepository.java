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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import dk.kontentsu.model.Taxonomy;

/**
 * Repository for performing CRUD operations on taxonomy objects.
 *
 * @author Jens Borch Christiansen
 */
@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.MANDATORY)
public class TaxonomyRepository extends Repository<Taxonomy> {

    @Override
    public List<Taxonomy> findAll() {
        TypedQuery<Taxonomy> query = em.createNamedQuery(TAXONOMY_FIND_ALL, Taxonomy.class);
        return query.getResultList();
    }

    @Override
    public Taxonomy get(final UUID uuid) {
        TypedQuery<Taxonomy> query = em.createNamedQuery(TAXONOMY_GET, Taxonomy.class);
        query.setParameter("uuid", uuid);
        return query.getSingleResult();

    }

    public Taxonomy getByName(final String name) {
        TypedQuery<Taxonomy> query = em.createNamedQuery(TAXONOMY_GET_BY_NAME, Taxonomy.class);
        query.setParameter("name", name);
        return query.getSingleResult();
    }

    public Optional<Taxonomy> findByName(final String name) {
        try {
            return Optional.of(getByName(name));
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public void delete(final UUID uuid) {
        Taxonomy t = get(uuid);
        em.remove(t);
    }

}
