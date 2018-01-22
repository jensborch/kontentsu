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

import java.io.InputStream;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.hibernate.internal.SessionImpl;

import dk.kontentsu.model.Content;
import dk.kontentsu.model.ContentException;
import dk.kontentsu.model.Item;
import dk.kontentsu.model.MimeType;
import dk.kontentsu.model.State;
import dk.kontentsu.model.Version;

/**
 * Repository for performing CRUD operations on CDN items.
 *
 * @author Jens Borch Christiansen
 */
@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.MANDATORY)
public class ItemRepository extends Repository<Item> {

    @Inject
    private TermRepository termRepo;

    public List<Item> find(final Item.Criteria criteria) {
        return criteria.fetch(em);
    }

    @Override
    public List<Item> findAll() {
        TypedQuery<Item> query = em.createNamedQuery(ITEM_FIND_ALL, Item.class);
        query.setParameter("state", Arrays.asList(State.ACTIVE, State.DRAFT));
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

    public Optional<Item> findByUri(final Item.URI uri, State... states) {
        if (states.length == 0) {
            states = new State[]{State.ACTIVE, State.DRAFT};
        }
        try {
            TypedQuery<Item> query = em.createNamedQuery(ITEM_FIND_BY_URI, Item.class);
            query.setParameter("path", uri.toTerm());
            query.setParameter("edition", uri.getEdition().orElse(null));
            query.setParameter("state", Arrays.asList(states));

            List<Item> tmp = query.getResultList();
            if (tmp.size() > 1) {
                tmp.stream().forEach(i -> System.out.println("###" + i));
            }
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public List<Item> findByTerm(final UUID uuid) {
        TypedQuery<Item> query = em.createNamedQuery(ITEM_FIND_BY_TERM, Item.class);
        query.setParameter("uuid", uuid);
        return query.getResultList();
    }

    public Content getContent(final UUID uuid) {
        TypedQuery<Content> query = em.createNamedQuery(CONTENT_GET, Content.class);
        query.setParameter("uuid", uuid);
        return query.getSingleResult();
    }

    public Content saveContent(final InputStream content, final Charset encoding, final MimeType mimeType) {
        UUID uuid = saveContentUsingJDBC(content, encoding, mimeType);
        return getContent(uuid);
    }

    private UUID saveContentUsingJDBC(final InputStream content, final Charset encoding, final MimeType mimeType) {
        UUID uuid = UUID.randomUUID();
        String sql = "INSERT INTO content (uuid, data, encoding) VALUES(?, ?, ?)";
        Connection con = getConnection();
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            int i = 1;
            statement.setObject(i++, uuid);
            statement.setBinaryStream(i++, content);
            statement.setString(i++, (encoding == null) ? null : encoding.name());
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new ContentException("Error saving content to database", ex);
        }
        return uuid;
    }

    private Connection getConnection() {
        return em.unwrap(SessionImpl.class).connection();
    }
}
