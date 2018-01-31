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
import java.util.SortedSet;
import java.util.StringJoiner;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.querydsl.core.Tuple;
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
        @UniqueConstraint(columnNames = {"path_id", "edition"}, name = "item_constraint")})
@NamedQueries( {
    @NamedQuery(name = Repository.ITEM_FIND_BY_TERM,
        query = "SELECT i FROM Item i JOIN i.path p WHERE p.uuid = :uuid ORDER BY i.created, i.edition"),
    @NamedQuery(name = Repository.ITEM_FIND_ALL,
        query = "SELECT DISTINCT i FROM Item i JOIN i.versions v "
            + "WHERE v.state IN :state "
            + "ORDER BY i.created, i.edition"),
    @NamedQuery(name = Repository.ITEM_GET,
        query = "SELECT i FROM Item i WHERE i.uuid = :uuid"),
    @NamedQuery(name = Repository.ITEM_FIND_BY_URI,
        query = "SELECT DISTINCT i FROM Item i "
            + "JOIN i.path p "
            + "JOIN i.versions v "
            + "WHERE p.path = :path "
            + "AND ((:edition IS NULL AND i.edition IS NULL) OR i.edition = :edition) "
            + "AND v.state IN :state "
            + "ORDER BY i.created, i.edition")})
public class Item extends AbstractBaseEntity {

    private static final long serialVersionUID = 1457687095382268401L;
    private static final Logger LOGGER = LogManager.getLogger();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(name = "item_terms",
        joinColumns = @JoinColumn(name = "item_id"),
        inverseJoinColumns = @JoinColumn(name = "term_id")
    )
    private Set<Term> terms = new HashSet<>();

    @Column(name = "edition")
    private String edition;

    @NotNull
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER, targetEntity = Term.class)
    @JoinColumn(name = "path_id")
    private Term path;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "item")
    @OrderBy("interval.from, interval.to")
    private SortedSet<Version> versions = new TreeSet<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "item")
    private Set<ExternalFile> files = new HashSet<>();

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER, targetEntity = Provider.class)
    @JoinColumn(name = "provider_id")
    private Provider provider;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    private Set<Host> hosts = new HashSet<>();

    @NotNull
    @Column(name = "mimetype")
    private MimeType mimeType;

    protected Item() {
        //Needed by JPA
    }

    public Item(final Term path, final MimeType mimeType) {
        this(path, null, mimeType);
    }

    public Item(final Term path, final String edition, final MimeType mimeType) {
        if (!path.isUri()) {
            throw new IllegalArgumentException("Path must be a URI");
        }
        this.path = path;
        this.terms.add(path);
        this.edition = edition;
        this.mimeType = mimeType;
        this.path.addItem(this);
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

    public URI getUri() {
        return new URI(this);
    }

    public SortedSet<Version> getVersions() {
        return Collections.unmodifiableSortedSet(versions);
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

    public void addTerm(final Term term) {
        if (term.isUri() || term.isTaxonomy()) {
            throw new IllegalArgumentException("Must not be a URI or a taxonomy");
        }
        terms.add(term);
        term.addItem(this);
    }

    public void removeTerm(final Term term) {
        if (term.isUri()) {
            throw new IllegalArgumentException("Can not remove a URI");
        }
        terms.remove(term);
        term.removeItem(this);
    }

    public MimeType getMimeType() {
        return mimeType;
    }

    public Optional<String> getEdition() {
        return Optional.ofNullable(edition);
    }

    public Set<Term> getTerms() {
        return Collections.unmodifiableSet(terms);
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
        private final QTerm path = QTerm.term;
        private final JPAQuery<Tuple> query;

        private Optional<ZonedDateTime> from = Optional.empty();
        private ZonedDateTime to = INFINITE;

        static {
            STATES.add(State.ACTIVE);
        }

        private Criteria(final boolean fetch) {
            query = new JPAQuery<>();
            query.from(item)
                .select(item, item.path)
                .distinct()
                .leftJoin(item.versions, version)
                .join(item.path, path);
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

        public Criteria term(final String term) {
            query.where(path.path.eq(term)).orderBy(path.path.desc());
            return this;
        }

        public Criteria edition(final String edition) {
            query.where(item.edition.eq(edition));
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

        public Criteria host(final String name) {
            QHost host = QHost.host;
            query
                .join(item.hosts, host)
                .where(host.name.eq(name));
            return this;
        }

        public Criteria reference(final URI uri, final ReferenceType type) {
            QReference reference = QReference.reference;
            query
                .join(version.references, reference)
                .where(reference.type.eq(type),
                    reference.itemPath.eq(uri.getFolder()));
            uri.getEdition().ifPresent(e -> query.where(reference.itemName.eq(e)));
            return this;
        }

        public Criteria mineType(final MimeType mimeType) {
            query.where(item.mimeType.eq(mimeType));
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

        public Criteria offset(final int offset) {
            query.offset(offset);
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
                .orderBy(item.created.desc(), item.edition.desc())
                .fetch()
                .stream()
                .map(t -> t.get(item))
                .collect(Collectors.toList());
        }
    }

    /**
     * URI that can be used to access an item in Kontentsu.
     */
    public static class URI implements Comparable<URI> {

        public static final String PATH_SEPARATOR = "/";
        public static final String ALLOWED_CHARS_REGEX = "[\\p{L}\\d-]";
        private static final String REGEX = String.format(
            "^\\/?(?<elements>(?:(?<element>%s+)\\/)+)(?:\\k<element>)?(?:-(?<edition>%s+))?(?:\\.(?<ext>\\w{3,4}))?$",
            ALLOWED_CHARS_REGEX,
            ALLOWED_CHARS_REGEX);
        private static final Pattern REGEX_PATTERN = Pattern.compile(REGEX);

        private final Optional<String> edition;
        private final String[] pathElements;
        private final MimeType mimeType;

        public URI(final Item item) {
            this.pathElements = item.path.getElements();
            this.edition = item.getEdition();
            this.mimeType = item.getMimeType();
        }

        public URI(final String uri) {
            Matcher m = REGEX_PATTERN.matcher(uri);
            if (!m.matches()) {
                throw new IllegalArgumentException("URI '" + uri + "' must match regular expression " + REGEX);
            }
            this.edition = Optional.ofNullable(m.group("edition"));
            List<String> p = Term.splitPath(m.group("elements"));
            this.pathElements = p.toArray(new String[p.size()]);
            this.mimeType = (m.group("ext") == null) ? MimeType.APPLICATION_JSON_TYPE : MimeType.formExtension(m.group("ext"));
        }

        public MimeType getMimeType() {
            return mimeType;
        }

        public Optional<String> getEdition() {
            return edition;
        }

        public String getFileName() {
            return pathElements[pathElements.length - 1] + edition.map("-"::concat).orElse("") + "." + mimeType.getFileExtension();
        }

        public String[] getPathElements() {
            String[] result = Arrays.copyOf(pathElements, pathElements.length + 1);
            result[pathElements.length] = getFileName();
            return result;
        }

        public String[] getFolderElements() {
            return Arrays.copyOf(pathElements, pathElements.length);
        }

        public String getFolder() {
            StringJoiner joiner = new StringJoiner(PATH_SEPARATOR, PATH_SEPARATOR, PATH_SEPARATOR);
            Arrays.stream(pathElements).forEach(joiner::add);
            return joiner.toString();
        }

        public String getPath() {
            StringJoiner joiner = new StringJoiner(PATH_SEPARATOR, PATH_SEPARATOR, "");
            Arrays.stream(getPathElements()).forEach(joiner::add);
            return joiner.toString();
        }

        public java.nio.file.Path toPath() {
            String[] tail = (pathElements.length > 1) ? Arrays.copyOfRange(pathElements, 1, pathElements.length) : new String[0];
            return Paths.get(pathElements[0], tail).resolve(getFileName());
        }

        public String toTerm() {
            return Term.URI_TAXONOMY + Term.TAXONOMY_SEPARATOR + getFolder();
        }

        public boolean matches(final String uri) {
            try {
                return uri != null && new URI(uri).equals(this);
            } catch (IllegalArgumentException e) {
                LOGGER.debug("URI {} is not legal");
                return false;
            }
        }

        @Override
        public int compareTo(final URI uri) {
            return getPath().compareTo(uri.getPath());
        }

        @Override
        public String toString() {
            return getPath();
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 83 * hash + Objects.hashCode(getPath());
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
            return this.getPath().equals(other.getPath());
        }
    }

    @Override
    public String toString() {
        return "Item{" + "terms=" + terms
            + ", edition=" + edition
            + ", path=" + path
            + ", versions=" + versions
            + ", files=" + files
            + ", provider=" + provider
            + ", hosts=" + hosts
            + ", mimeType=" + mimeType + '}';
    }

}
