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
package dk.kontentsu.model;

import dk.kontentsu.model.internal.Item;
import dk.kontentsu.repository.Repository;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.Valid;

/**
 * Represents the physical path of a item on the CDN.
 *
 * @author Jens Borch Christiansen
 */
@Entity
@Table(name = "path",
        uniqueConstraints = {
            @UniqueConstraint(columnNames = {"path"}, name = "path_constraint")})
@NamedQueries({
    @NamedQuery(name = Repository.URI_FIND_ALL,
            query = "SELECT p FROM SemanticUriPath p"),
    @NamedQuery(name = Repository.URI_GET,
            query = "SELECT p FROM SemanticUriPath p WHERE p.uuid = :uuid"),
    @NamedQuery(name = Repository.URI_FIND_BY_PATH,
            query = "SELECT p FROM SemanticUriPath p WHERE p.path = :path"),
    @NamedQuery(name = Repository.URI_FIND_ACTIVE,
            query = "SELECT NEW dk.kontentsu.model.SemanticUriPath(p.path, count(i.id)) "
            + "FROM SemanticUriPath p "
            + "JOIN p.items i "
            + "JOIN i.versions v "
            + "WHERE v.state = :state "
            + "GROUP BY p.path")})
public class SemanticUriPath extends Category {

    private static final long serialVersionUID = 2921353322551114756L;

    @OneToMany(fetch = FetchType.LAZY, targetEntity = Item.class, mappedBy = "uri.path")
    private Set<Item> items;

    @Transient
    private Long versions;

    protected SemanticUriPath() {
        //Needed by JPA
    }

    /**
     * Needed by JPA query to create a <code>SemanticUriPath</code> with a count
     * of the version for that particular URI path. <b>Should not be use
     * directly</b>.
     *
     * @param path the path of the URI
     * @param versions number of versions found
     *
     */
    public SemanticUriPath(final String path, final Long versions) {
        this(split(path));
        this.versions = versions;
        this.items = new HashSet<>();
    }

    public SemanticUriPath(final String... elements) {
        super(elements);
        this.items = new HashSet<>();
    }

    public static SemanticUriPath parse(final String path) {
        return new SemanticUriPath(split(path));
    }

    @Override
    public Taxonomy getTaxonomy() {
        return SemanticUriTaxonomy.create();
    }

    public Long getVersionCount() {
        return versions;
    }

    public Set<Item> getItems() {
        return Collections.unmodifiableSet(items);
    }

    public Optional<Item> getItem(final String name) {
        return items.stream()
                .filter(i -> i.getName().equals(name))
                .findFirst();
    }

    public final void addItem(@Valid final Item item) {
        items.add(item);
    }

    public boolean exists(final Item other) {
        return getItems()
                .stream()
                .filter(i -> !i.equals(other))
                .filter(i -> i.getName().equals(other.getName()))
                .count() > 0;
    }

}
