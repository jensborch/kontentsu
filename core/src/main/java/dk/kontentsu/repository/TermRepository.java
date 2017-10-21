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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import dk.kontentsu.model.Item;
import dk.kontentsu.model.Term;
import java.util.HashSet;
import java.util.Set;

/**
 * Repository for performing CRUD operations on term objects.
 *
 * @author Jens Borch Christiansen
 */
@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.MANDATORY)
public class TermRepository extends Repository<Term> {

    @Override
    public List<Term> findAll() {
        TypedQuery<Term> query = em.createNamedQuery(TERM_FIND_ALL, Term.class);
        return query.getResultList();
    }


    @Override
    public Term get(final UUID uuid) {
        TypedQuery<Term> query = em.createNamedQuery(TERM_GET, Term.class);
        query.setParameter("uuid", uuid);
        return query.getSingleResult();
    }

    public Term create(final Item.URI path) {
        Term term = findAll().stream().filter(Term::isUri).findAny().orElse(new Term());

        String[] elements = path.getPathElements();
        List<String> p = Arrays.stream(elements).limit(elements.length - 1).collect(Collectors.toList());

        for (String e :p) {
            Optional<Term> found = term.getChildren().stream()
                    .filter(t -> t.getName().equals(e))
                    .findAny();
            if (found.isPresent()) {
                term = found.get();
            } else {
                term = term.append(new Term(e));
            }
        }
        return term;
    }

    public Optional<Term> findByUri(final Item.URI path) {
        try {
            TypedQuery<Term> query = em.createNamedQuery(TERM_FIND_BY_URI, Term.class);
            query.setParameter("path", path.getFullPath());
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public void delete(final UUID uuid) {
        Term t = get(uuid);
        Set<Term> remove = new HashSet<>(t.getChildren());
        remove.forEach(t::remove);
        t.getParent().ifPresent(p -> p.remove(t));
        em.remove(t);
    }

}
