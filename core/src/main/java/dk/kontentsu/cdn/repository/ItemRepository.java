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

import java.io.InputStream;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.validation.Valid;

import com.querydsl.jpa.impl.JPAQuery;
import dk.kontentsu.cdn.model.Content;
import dk.kontentsu.cdn.model.ContentException;
import dk.kontentsu.cdn.model.Interval;
import dk.kontentsu.cdn.model.MimeType;
import dk.kontentsu.cdn.model.QContent;
import dk.kontentsu.cdn.model.QSemanticUriPath;
import dk.kontentsu.cdn.model.SemanticUri;
import dk.kontentsu.cdn.model.SemanticUriPath;
import dk.kontentsu.cdn.model.State;
import dk.kontentsu.cdn.model.internal.Item;
import dk.kontentsu.cdn.model.internal.QHost;
import dk.kontentsu.cdn.model.internal.QItem;
import dk.kontentsu.cdn.model.internal.QReference;
import dk.kontentsu.cdn.model.internal.QVersion;
import dk.kontentsu.cdn.model.internal.ReferenceType;
import dk.kontentsu.cdn.model.internal.Version;
import org.hibernate.internal.SessionImpl;

/**
 * Repository for performing CRUD operations on CDN items.
 *
 * @author Jens Borch Christiansen
 */
@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.MANDATORY)
public class ItemRepository extends Repository<Item> {

    public List<Item> find(final Criteria criteria) {
        QItem item = QItem.item;
        QSemanticUriPath path = QSemanticUriPath.semanticUriPath;
        JPAQuery<Item> query = new JPAQuery<>(em);
        query.distinct().from(item).join(item.uri.path, path);

        if (criteria.reference.isPresent()) {
            QReference reference = QReference.reference;
            QVersion version = QVersion.version;
            query
                    .join(item.versions, version)
                    .join(version.references, reference);
            query.where(reference.type.eq(criteria.referenceType),
                    reference.itemName.eq(criteria.reference.get().getName()),
                    reference.itemPath.eq(criteria.reference.get().getPath().toString()));
        }

        if (criteria.host.isPresent()) {
            QHost host = QHost.host;
            query.join(item.hosts, host);
            query.where(host.name.eq(criteria.host.get()));
        }

        //At takes precedent over from and to
        if (criteria.at.isPresent()) {
            QVersion version = QVersion.version;
            query.join(item.versions, version);
            query.where(version.interval.from.loe(criteria.at.get()), version.interval.to.gt(criteria.at.get()));
        } else if (criteria.from.isPresent()) {
            QVersion version = QVersion.version;
            query.join(item.versions, version);
            query.where(version.interval.to.goe(criteria.from.get()), version.interval.from.loe(criteria.to));
        }

        //Uri takes precedent over path
        if (criteria.uri.isPresent()) {
            criteria.uri.ifPresent(u -> query
                    .where(item.uri.path.path.eq(u.getPath().toString()),
                            item.uri.name.eq(u.getName())));
        } else {
            criteria.path.ifPresent(p -> query.where(path.path.eq(p.toString())));
        }

        criteria.mimetype.ifPresent(m -> {
            QVersion version = QVersion.version;
            query.join(item.versions, version);
            QContent content = QContent.content;
            query
                    .join(item.versions, version)
                    .join(version.content, content);
            query.where(content.mimeType.eq(m));
        });
        criteria.offset.ifPresent(o -> query.offset(o));
        criteria.limit.ifPresent(l -> query.limit(l));

        QVersion version = QVersion.version;
        query.join(item.versions, version);
        query.where(version.state.in(criteria.STATES));

        return query.fetch();
    }

    @Override
    public List<Item> findAll() {
        TypedQuery<Item> query = em.createNamedQuery(ITEM_FIND_ALL, Item.class);
        query.setParameter("state", new State[]{State.ACTIVE, State.DRAFT});
        return query.getResultList();
    }

    @Override
    public Item get(final UUID uuid) {
        TypedQuery<Item> query = em.createNamedQuery(ITEM_GET, Item.class);
        query.setParameter("uuid", uuid);
        return query.getSingleResult();
    }

    public Version getVersion(final UUID uuid) {
        TypedQuery<Version> query = em.createNamedQuery(VERSION_GET, Version.class);
        query.setParameter("uuid", uuid);
        return query.getSingleResult();
    }

    public Optional<Item> findByUri(final SemanticUri uri) {
        try {
            TypedQuery<Item> query = em.createNamedQuery(ITEM_FIND_BY_URI, Item.class);
            query.setParameter("name", uri.getName());
            query.setParameter("path", uri.getPath().toString());
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public Item save(@Valid final Item item) {
        em.persist(item);
        em.flush();
        return item;
    }

    public Content saveContent(final InputStream content, final Charset encoding, final MimeType mimeType) {
        UUID uuid = saveContentUsingJDBC(content, encoding, mimeType);
        TypedQuery<Content> query = em.createNamedQuery(CONTENT_GET, Content.class);
        query.setParameter("uuid", uuid);
        return query.getSingleResult();
    }

    private UUID saveContentUsingJDBC(final InputStream content, final Charset encoding, final MimeType mimeType) {
        UUID uuid = null;
        String sql = "INSERT INTO content (uuid, data, encoding, mimetype_type, mimetype_subtype) VALUES(?, ?, ?, ?, ?)";
        Connection con = getConnection();
        try (PreparedStatement statment = con.prepareStatement(sql)) {
            uuid = UUID.randomUUID();
            int i = 1;
            statment.setObject(i++, uuid);
            statment.setBinaryStream(i++, content);
            statment.setString(i++, (encoding != null) ? encoding.name() : null);
            statment.setString(i++, mimeType.getType());
            statment.setString(i++, mimeType.getSubType());
            statment.executeUpdate();
        } catch (SQLException ex) {
            throw new ContentException("Error saving content to database", ex);
        }
        return uuid;
    }

    private Connection getConnection() {
        return em.unwrap(SessionImpl.class).connection();
    }

    /**
     * Search criteria for finding items.
     *
     * @author Jens Borch Christiansen
     */
    public static class Criteria {

        private static final int DEFAULT_FROM_COMPENSATION_SECONDS = 1;
        private static final Set<State> STATES = new HashSet<>();

        private Optional<ZonedDateTime> at = Optional.empty();
        private Optional<ZonedDateTime> from = Optional.empty();
        private Optional<SemanticUri> reference = Optional.empty();
        private ReferenceType referenceType = ReferenceType.COMPOSITION;
        private ZonedDateTime to = Interval.INFINIT.plusSeconds(DEFAULT_FROM_COMPENSATION_SECONDS);
        private Optional<SemanticUriPath> path = Optional.empty();
        private Optional<SemanticUri> uri = Optional.empty();
        private Optional<MimeType> mimetype = Optional.empty();
        private Optional<Integer> offset = Optional.empty();
        private Optional<Integer> limit = Optional.empty();
        private Optional<String> host = Optional.empty();

        static {
            STATES.add(State.ACTIVE);
        }

        public static Criteria create() {
            return new Criteria();
        }

        public Criteria at(final ZonedDateTime at) {
            this.at = Optional.of(at);
            return this;
        }

        public Criteria from(final ZonedDateTime from) {
            this.from = Optional.of(from);
            return this;
        }

        public Criteria to(final ZonedDateTime to) {
            this.to = to;
            return this;
        }

        public Criteria interval(final Interval interval) {
            this.to = interval.getTo();
            this.from = Optional.of(interval.getFrom());
            return this;
        }

        public Criteria path(final SemanticUriPath path) {
            this.path = Optional.of(path);
            return this;
        }

        public Criteria uri(final SemanticUri uri) {
            this.uri = Optional.of(uri);
            return this;
        }

        public Criteria host(final String host) {
            this.host = Optional.of(host);
            return this;
        }

        public Criteria reference(final SemanticUri uri) {
            this.reference = Optional.of(uri);
            return this;
        }

        public Criteria referenceType(final ReferenceType referenceType) {
            this.referenceType = referenceType;
            return this;
        }

        public Criteria mineType(final MimeType mimeType) {
            this.mimetype = Optional.of(mimeType);
            return this;
        }

        public Criteria active() {
            this.STATES.add(State.ACTIVE);
            return this;
        }

        public Criteria inactive() {
            this.STATES.remove(State.ACTIVE);
            return this;
        }

        public Criteria draft() {
            this.STATES.remove(State.DRAFT);
            return this;
        }

        public Criteria deleted() {
            this.STATES.remove(State.DELETED);
            return this;
        }

        public Criteria offset(final int offecet) {
            this.offset = Optional.of(offecet);
            return this;
        }

        public Criteria limit(final int limit) {
            this.limit = Optional.of(limit);
            return this;
        }

    }

}
