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
package dk.kontentsu.cdn.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import dk.kontentsu.cdn.model.internal.Item;
import dk.kontentsu.cdn.repository.Repository;

/**
 * A classification of a item on the CDN in a concrete taxonomy.
 *
 * @author Jens Borch Christiansen
 */
@Entity
@Table(name = "category",
        uniqueConstraints = {
            @UniqueConstraint(columnNames = {"taxonomy_id", "path"})})
@NamedQueries({
    @NamedQuery(name = Repository.TAXON_FIND_ALL,
            query = "SELECT t FROM Taxon t"),
    @NamedQuery(name = Repository.TAXON_GET,
            query = "SELECT t FROM Taxon t WHERE uuid = :uuid"),
    @NamedQuery(name = Repository.TAXON_FIND_BY_TAXONOMY,
            query = "SELECT t FROM Taxon t WHERE t.taxonomy = :taxonomy"),
    @NamedQuery(name = Repository.TAXON_FIND_BY_TAXONOMY_PATH,
            query = "SELECT t FROM Taxon t WHERE t.taxonomy = :taxonomy AND t.path = :path")})
public class Taxon extends Category {

    private static final long serialVersionUID = -670011005746563610L;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "category_item",
            joinColumns = @JoinColumn(name = "cat_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "item_id", referencedColumnName = "id"))
    private Set<Item> items;

    @ManyToOne(cascade = {})
    @JoinColumn(name = "taxonomy_id")
    private Taxonomy taxonomy;

    protected Taxon() {
        //Needed by JPA
    }

    public Taxon(final Taxonomy taxonomy, final String... elements) {
        super(elements);
        this.taxonomy = taxonomy;
        this.items = new HashSet<>();
    }

    public static Taxon parse(final Taxonomy taxonomy, final String uri) {
        return new Taxon(taxonomy, split(uri));
    }

    public Set<Item> getItems() {
        return Collections.unmodifiableSet(items);
    }

    public Optional<Item> getItem(final String name) {
        return items.stream()
                .filter(i -> i.getName().equals(name))
                .findFirst();
    }

    public final void addItem(final Item item) {
        items.add(item);
    }

    @Override
    public Taxonomy getTaxonomy() {
        return taxonomy;
    }

}
