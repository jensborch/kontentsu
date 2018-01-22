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

import java.time.ZonedDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import dk.kontentsu.model.ExternalFile;
import dk.kontentsu.model.Interval;
import dk.kontentsu.model.Item;
import dk.kontentsu.model.State;

/**
 * Repository class for e.g. persisting external files that can be published directly to the CDN.
 *
 * @author Jens Borch Christiansen
 */
@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.MANDATORY)
public class ExternalFileRepository extends Repository<ExternalFile> {

    private static final int MIN_SCHEDULING_INTERVAL = 5;

    @Override
    public ExternalFile get(final UUID uuid) {
        TypedQuery<ExternalFile> query = em.createNamedQuery(EXTERNAL_FILE_GET, ExternalFile.class);
        query.setParameter("uuid", uuid);
        return query.getSingleResult();
    }

    public Optional<ExternalFile> findByUri(final Item.URI uri, final ZonedDateTime at) {
        try {
            return Optional.of(getByUri(uri, at));
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public ExternalFile getByUri(final String uri, final ZonedDateTime at) {
        try {
            return getByUri(new Item.URI(uri), at);
        } catch (IllegalArgumentException e) {
            throw new NoResultException(e.getMessage());
        }
    }

    public ExternalFile getByUri(final Item.URI uri, final ZonedDateTime at) {
        TypedQuery<ExternalFile> query = em.createNamedQuery(EXTERNAL_FILE_FIND_BY_URI, ExternalFile.class);
        query.setParameter("edition", uri.getEdition().orElse(null));
        query.setParameter("path", uri.toTerm());
        query.setParameter("state", State.ACTIVE);
        query.setParameter("at", (at == null) ? ZonedDateTime.now() : at);
        return query.getSingleResult();
    }

    public List<ExternalFile> findByUri(final Item.URI uri, final Interval interval) {
        TypedQuery<ExternalFile> query = em.createNamedQuery(EXTERNAL_FILE_FIND_IN_INTERVAL, ExternalFile.class);
        query.setParameter("edition", uri.getEdition().orElse(null));
        query.setParameter("path", uri.toTerm());
        query.setParameter("state", State.ACTIVE);
        query.setParameter("from", interval.getFrom());
        query.setParameter("to", interval.getTo());
        return query.getResultList();
    }

    @Override
    public List<ExternalFile> findAll() {
        TypedQuery<ExternalFile> query = em.createNamedQuery(EXTERNAL_FILE_FIND_ALL, ExternalFile.class);
        query.setParameter("state", State.ACTIVE);
        return query.getResultList();
    }

    public List<ExternalFile> findAll(final ZonedDateTime at) {
        TypedQuery<ExternalFile> query = em.createNamedQuery(EXTERNAL_FILE_FIND_ALL_AT_DATE_TIME, ExternalFile.class);
        query.setParameter("state", State.ACTIVE);
        query.setParameter("at", (at == null) ? ZonedDateTime.now() : at);
        return query.getResultList();
    }

    public List<ExternalFile> findAll(final Interval interval) {
        TypedQuery<ExternalFile> query = em.createNamedQuery(EXTERNAL_FILE_FIND_ALL_IN_INTERVAL, ExternalFile.class);
        query.setParameter("state", State.ACTIVE);
        query.setParameter("from", interval.getFrom());
        query.setParameter("to", interval.getTo());
        return query.getResultList();
    }

    public Set<ZonedDateTime> getSchedule() {
        return getSchedule(ZonedDateTime.now());
    }

    public Set<ZonedDateTime> getSchedule(final ZonedDateTime from) {
        TypedQuery<Interval> query = em.createNamedQuery(EXTERNAL_FILE_SCHEDULE, Interval.class);
        query.setParameter("from", from);
        query.setParameter("state", State.ACTIVE);

        List<Interval> intervals = query.getResultList();

        List<ZonedDateTime> tmp = Stream.concat(
                intervals.stream().map(Interval::getFrom),
                intervals.stream().map(Interval::getTo)
        )
                .filter(t -> t.isAfter(from))
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        Set<ZonedDateTime> result = new LinkedHashSet<>();
        ZonedDateTime previous = null;
        for (ZonedDateTime t : tmp) {
            if (previous != null && previous.plusMinutes(MIN_SCHEDULING_INTERVAL).isAfter(t)) {
                result.remove(previous);
            }
            result.add(t);
            previous = t;
        }

        return result;
    }

}
