
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
package dk.kontentsu.cdn.model.internal;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import dk.kontentsu.cdn.exception.ValidationException;
import dk.kontentsu.cdn.jpa.AbstractBaseEntity;
import dk.kontentsu.cdn.model.ExternalFile;
import dk.kontentsu.cdn.model.Interval;
import dk.kontentsu.cdn.model.SemanticUri;
import dk.kontentsu.cdn.model.Taxon;
import dk.kontentsu.cdn.repository.Repository;

/**
 * An item that can externalized to the CDN - i.e. a file on the CDN.
 *
 * @author Jens Borch Christiansen
 */
@Entity
@Table(name = "item",
        uniqueConstraints = {
            @UniqueConstraint(columnNames = {"path_id", "name"})})
@NamedQueries({
    @NamedQuery(name = Repository.ITEM_FIND_ALL,
            query = "SELECT DISTINCT i FROM Item i JOIN i.versions v WHERE v.state = :state"),
    @NamedQuery(name = Repository.ITEM_GET,
            query = "SELECT i FROM Item i WHERE i.uuid = :uuid"),
    @NamedQuery(name = Repository.ITEM_FIND_BY_URI,
            query = "SELECT DISTINCT i FROM Item i JOIN i.uri.path p WHERE i.uri.name = :name AND p.path = :path")})
public class Item extends AbstractBaseEntity {

    private static final long serialVersionUID = 1457687095382268401L;

    @NotNull
    @Valid
    @Column(name = "uri")
    private SemanticUri uri;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY, mappedBy = "items")
    private Set<Taxon> categories;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "item")
    private Set<Version> versions;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "item")
    private Set<ExternalFile> files;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER, targetEntity = Provider.class)
    @JoinColumn(name = "provider_id")
    private Provider provider;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    private Set<Host> hosts;

    protected Item() {
        //Needed by JPA
    }

    public Item(final SemanticUri uri) {
        this.categories = new HashSet<>();
        this.versions = new HashSet<>();
        this.files = new HashSet<>();
        this.hosts = new HashSet<>();
        this.uri = uri;
        uri.getPath().addItem(this);
    }

    public Item(final SemanticUri uri, final Provider provider) {
        this(uri);
        this.provider = provider;
    }

    public Set<Host> getHosts() {
        return Collections.unmodifiableSet(hosts);
    }

    public void addHost(final Host host) {
        hosts.add(host);
    }

    public void removeHost(final Host host) {
        hosts.remove(host);
    }

    public Optional<Provider> getProvider() {
        return Optional.ofNullable(provider);
    }

    public void setProvider(final Provider provider) {
        this.provider = provider;
    }

    public String getName() {
        return uri.getName();
    }

    public SemanticUri getUri() {
        return uri;
    }

    public Set<Taxon> getCategories() {
        return Collections.unmodifiableSet(categories);
    }

    public void addCategory(final Taxon category) {
        categories.add(category);
    }

    public Set<Version> getVersions() {
        return Collections.unmodifiableSet(versions);
    }

    public List<Version> getVersions(final Interval interval) {
        return versions.stream()
                .filter(v -> v.isActivate())
                .filter(v -> v.getInterval().overlaps(interval))
                .collect(Collectors.toList());
    }

    public void addVersion(final Version version) {
        if (overlaps(version)) {
            throw new ValidationException("Version with interval " + version.getInterval().toString() + " overlaps existing version for item with URI: " + getUri().toString());
        }
        versions.add(version);
        version.setItem(this);
    }

    public Set<ExternalFile> getFiles() {
        return Collections.unmodifiableSet(files);
    }

    public final void addFile(@Valid final ExternalFile file) {
        files.add(file);
    }

    public boolean overlaps(final Version other) {
        return getVersions()
                .stream()
                .filter(v -> !v.equals(other))
                .filter(v -> v.isActivate())
                .filter(v -> v.getInterval().overlaps(other.getInterval()))
                .findAny()
                .isPresent();
    }

    public void delete() {
        getVersions().forEach(v -> v.delete());
    }

}
