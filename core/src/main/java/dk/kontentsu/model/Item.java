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

import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.querydsl.jpa.impl.JPAQuery;
import dk.kontentsu.exception.ValidationException;
import dk.kontentsu.repository.Repository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * An item that can externalized to the CDN - i.e. a file on the CDN.
 *
 * @author Jens Borch Christiansen
 */
@Entity
@Table(name = "item",
        uniqueConstraints = {
            @UniqueConstraint(columnNames = {"path_id", "name"}, name = "item_constraint")})
@NamedQueries({
    @NamedQuery(name = Repository.ITEM_FIND_ALL,
            query = "SELECT DISTINCT i FROM Item i JOIN i.versions v WHERE v.state IN :state"),
    @NamedQuery(name = Repository.ITEM_GET,
            query = "SELECT i FROM Item i WHERE i.uuid = :uuid"),
    @NamedQuery(name = Repository.ITEM_FIND_BY_URI,
            query = "SELECT DISTINCT i FROM Item i JOIN i.uri.path p WHERE i.uri.name = :name AND p.path = :path")})
public class Item extends AbstractBaseEntity {

    private static final long serialVersionUID = 1457687095382268401L;
    private static final Logger LOGGER = LogManager.getLogger();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(name = "item_terms",
            joinColumns = @JoinColumn(name = "item_id"),
            inverseJoinColumns = @JoinColumn(name = "term_id")
    )
    private Set<Term> terms = new HashSet<>();

    @NotNull
    @Valid
    @Column(name = "uri")
    private SemanticUri uri;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY, mappedBy = "items")
    private Set<Taxon> categories = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "item")
    private Set<Version> versions = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "item")
    private Set<ExternalFile> files = new HashSet<>();

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER, targetEntity = Provider.class)
    @JoinColumn(name = "provider_id")
    private Provider provider;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    private Set<Host> hosts = new HashSet<>();

    protected Item() {
        //Needed by JPA
    }

    public Item(final SemanticUri uri) {
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
                .filter(Version::isActive)
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
        boolean result = !getVersions().isEmpty() && getVersions()
                .stream()
                .filter(v -> !v.equals(other))
                .filter(Version::isActive)
                .anyMatch(v -> v.getInterval().overlaps(other.getInterval()));
        LOGGER.debug("Overlaps between {} and {} returned {}", other.getUuid(), this.getUuid(), result);
        return result;
    }

    public void delete() {
        getVersions().forEach(Version::delete);
    }

    public void addTerm(Term term) {
        if (term.isUri() || term.isTaxonomy()) {
            throw new IllegalArgumentException("Must not be a URI or a taxonomy");
        }
        terms.add(term);
        term.addItem(this);
    }

    public void removeTerm(Term term) {
        if (term.isUri()) {
            throw new IllegalArgumentException("Can not remove a URI");
        }
        terms.remove(term);
        term.removeItem(this);
    }

    /**
     * Search criteria for finding items.
     *
     * @author Jens Borch Christiansen
     */
    public static final class Criteria {

        private static final List<State> STATES = new ArrayList<>();
        private static final int DEFAULT_FROM_COMPENSATION_SECONDS = 1;
        private static final ZonedDateTime INFINITE = Interval.INFINITE.plusSeconds(DEFAULT_FROM_COMPENSATION_SECONDS);

        private final QItem item = QItem.item;
        private final QVersion version = QVersion.version;
        private final QSemanticUriPath path = QSemanticUriPath.semanticUriPath;
        private final JPAQuery<Item> query;

        private Optional<ZonedDateTime> from = Optional.empty();
        private ZonedDateTime to = INFINITE;

        static {
            STATES.add(State.ACTIVE);
        }

        private Criteria(final boolean fetch) {
            query = new JPAQuery<>();
            query.distinct().from(item).join(item.uri.path, path).fetchJoin().leftJoin(item.versions, version);
            if (fetch) {
                query.fetchJoin();
            }
        }

        public static Criteria create(final boolean fetch) {
            return new Criteria(fetch);
        }

        public static Criteria create() {
            return new Criteria(false);
        }

        public Criteria at(final ZonedDateTime at) {
            query.where(version.interval.from.loe(at), version.interval.to.gt(at));
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
            this.from = Optional.of(interval.getFrom());
            this.to = interval.isInfinite() ? INFINITE : interval.getFrom();
            return this;
        }

        public Criteria path(final SemanticUriPath uri) {
            query.where(path.path.eq(uri.toString()));
            return this;
        }

        public Criteria uri(final SemanticUri uri) {
            query.where(
                    path.path.eq(uri.getPath().toString()),
                    item.uri.name.eq(uri.getName())
            );
            return this;
        }

        public Criteria host(final String name) {
            QHost host = QHost.host;
            query
                    .join(item.hosts, host)
                    .where(host.name.eq(name));
            return this;
        }

        public Criteria reference(final SemanticUri uri, final ReferenceType type) {
            QReference reference = QReference.reference;
            query
                    .join(version.references, reference)
                    .where(reference.type.eq(type),
                            reference.itemName.eq(uri.getName()),
                            reference.itemPath.eq(uri.getPath().toString()));
            return this;
        }

        public Criteria mineType(final MimeType mimeType) {
            QContent content = QContent.content;
            query
                    .join(version.content, content)
                    .where(content.mimeType.eq(mimeType));
            return this;
        }

        public Criteria inactive() {
            STATES.remove(State.ACTIVE);
            return this;
        }

        public Criteria draft() {
            STATES.remove(State.DRAFT);
            return this;
        }

        public Criteria deleted() {
            STATES.remove(State.DELETED);
            return this;
        }

        public Criteria offset(final int offecet) {
            query.offset(offecet);
            return this;
        }

        public Criteria limit(final int limit) {
            query.limit(limit);
            return this;
        }

        public List<Item> fetch(final EntityManager em) {

            from.ifPresent(f -> query.where(version.interval.to.goe(f), version.interval.from.loe(to)));

            return query.clone(em)
                    .where(version.state.in(STATES).or(version.state.isNull()))
                    .orderBy(path.path.asc(), item.uri.name.asc())
                    .fetch();
        }
    }

    public class URI {

        private final String name;
        private final Term path;

        public URI(Term path, String name) {
            this.name = name;
            this.path = path;
        }

        public String getName() {
            return name;
        }

        public Term getPath() {
            return path;
        }

        public String[] getElements() {
            String[] pathElements = path.getNames();
            String[] result = Arrays.copyOf(pathElements, pathElements.length + 1);
            result[pathElements.length] = name;
            return result;
        }

        public java.nio.file.Path toPath(final MimeType mimetype) {
            String[] elements = getElements();
            String[] tail = (elements.length > 1) ? Arrays.copyOfRange(elements, 1, elements.length) : new String[0];
            java.nio.file.Path p = Paths.get(elements[0], tail);
            if (p.getFileName() == null) {
                throw new ContentException("Filename for path " + p + " is null");
            }
            String filename = p.getFileName().toString() + "." + mimetype.getFileExtension();
            return p.getParent().resolve(filename);
        }

        @Override
        public String toString() {
            return path.toString() + SemanticUriPath.SEPARATOR_CHAR + name;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 83 * hash + Objects.hashCode(this.name);
            hash = 83 * hash + Objects.hashCode(this.path);
            return hash;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            URI other = (URI) obj;
            return this.getPath().equals(other.getPath()) && this.getName().equals(other.getName());
        }

        public boolean matches(final String uri) {
            return uri != null && toString().equals(uri.trim());
        }

    }
}
