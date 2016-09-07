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

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import dk.kontentsu.cdn.model.ExternalFile;
import dk.kontentsu.cdn.model.Interval;
import dk.kontentsu.cdn.model.SemanticUri;
import dk.kontentsu.cdn.model.State;

/**
 * Repository class for e.g. persisting external files that can be published directly to the CDN.
 *
 * @author Jens Borch Christiansen
 */
@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.MANDATORY)
public class ExternalFileRepository extends Repository<ExternalFile> {

    @Override
    public ExternalFile get(final UUID uuid) {
        TypedQuery<ExternalFile> query = em.createNamedQuery(EXTERNAL_FILE_GET, ExternalFile.class);
        query.setParameter("uuid", uuid);
        return query.getSingleResult();
    }

    public Optional<ExternalFile> findByUri(final SemanticUri uri, final ZonedDateTime at) {
        try {
            return Optional.of(getByUri(uri, at));
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public ExternalFile getByUri(final SemanticUri uri, final ZonedDateTime at) {
        TypedQuery<ExternalFile> query = em.createNamedQuery(EXTERNAL_FILE_FIND_BY_URI, ExternalFile.class);
        query.setParameter("name", uri.getName());
        query.setParameter("path", uri.getPath().toString());
        query.setParameter("state", State.ACTIVE);
        query.setParameter("at", (at == null) ? ZonedDateTime.now() : at);
        return query.getSingleResult();
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

    public List<ZonedDateTime> getSchedule() {
        TypedQuery<Interval> query = em.createNamedQuery(EXTERNAL_FILE_SCHEDULE, Interval.class);
        query.setParameter("state", State.ACTIVE);

        //TODO: Remove ZonedDateTimes that are to close to each other
        List<ZonedDateTime> result = Stream.concat(
                query.getResultList().stream().map(i -> i.getFrom()),
                query.getResultList().stream().map(i -> i.getTo())
        )
                .distinct()
                .filter(t -> t.isAfter(ZonedDateTime.now()))
                .sorted()
                .collect(Collectors.toList());
        return result;
    }

}
