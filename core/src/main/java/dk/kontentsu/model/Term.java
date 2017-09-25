package dk.kontentsu.model;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import dk.kontentsu.repository.Repository;
import dk.kontentsu.util.MatcherSpliterator;

/**
 * @author Jens Borch Christiansen
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "node",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"parent_id", "name"}, name = "node_constraint")})
@NamedQueries({
        @NamedQuery(name = Repository.TERM_FIND_ALL,
                query = "SELECT t FROM Term t LEFT JOIN FETCH t.children WHERE t.parent IS NULL"),
        @NamedQuery(name = Repository.TERM_GET,
                query = "SELECT t FROM Term t LEFT JOIN FETCH t.children WHERE uuid = :uuid")})
public class Term extends AbstractBaseEntity {

    public static final String URI_TAXONOMY = "uri";
    public static final char SEPARATOR_CHAR = '/';
    public static final char TAXONOMY_SEPARATOR_CHAR = ':';
    public static final String SEPARATOR = String.valueOf(SEPARATOR_CHAR);
    public static final String TAXONOMY_SEPARATOR = String.valueOf(TAXONOMY_SEPARATOR_CHAR);
    private static final long serialVersionUID = -3974244017445628292L;
    private static final String FULL_PATH_REGEX = "^(?<tax>\\p{L}[\\p{L}\\d]*):(?<term>\\/(?:\\p{L}[\\p{L}\\d\\s]*\\/)*)$";
    private static final String PATH_REGEX = "^\\/?(?:\\p{L}[\\p{L}\\d\\s]*\\/)*\\p{L}[\\p{L}\\d\\s]*\\/?$";
    private static final Pattern FULL_PATH_REGEX_PATTERN = Pattern.compile(FULL_PATH_REGEX);
    private static final Pattern PATH_REGEX_PATTERN = Pattern.compile(PATH_REGEX);

    @ManyToMany(mappedBy = "terms")
    private Set<Item> items = new HashSet<>();

    @OneToMany
    @OrderColumn
    private final Set<Term> children = new HashSet<Term>();

    @NotNull
    @Size(min = 1, max = 200)
    @Column(name = "name", length = 200)
    private final String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    private Term parent;

    @Transient
    private String[] pathNames;

    @Lob
    @Column(name = "path")
    private String path;

    @PrePersist
    @PreUpdate
    public void initPath() {
        if (pathNames == null) {
            pathNames = buildPathNames();
        }
        if (path == null) {
            path = buildFullPath(pathNames);
        }
    }

    @PostLoad
    public void updatePathNames() {
        pathNames = parse(path);
    }

    private String[] buildPathNames() {
        Deque<String> result = new ArrayDeque<>();
        Term last = this;
        while (last != null) {
            result.addFirst(last.getName());
            last = last.getParent();
        }
        return result.toArray(new String[result.size()]);
    }

    private String[] getPathPart(String[] p) {
        if (p.length > 0) {
            return Arrays.copyOfRange(p, 1, p.length);
        } else {
            throw new IllegalArgumentException("Path array must contain taxonomy - length must thus be > 0");
        }
    }

    private String getTaxonomyPart(String[] p) {
        if (p.length > 0) {
            return p[0];
        } else {
            throw new IllegalArgumentException("Path array must contain taxonomy - length must thus be > 0");
        }
    }

    private String buildPath(String[] p) {
        if (p.length > 1) {
            StringJoiner joiner = new StringJoiner(SEPARATOR, SEPARATOR, SEPARATOR);
            Arrays.stream(getPathPart(p)).forEach(joiner::add);
            return joiner.toString();
        } else {
            return SEPARATOR;
        }
    }

    private String buildFullPath(String[] p) {
        return getTaxonomyPart(p) + TAXONOMY_SEPARATOR + buildPath(p);
    }

    public Term(String name) {
        this.name = name.toLowerCase();
    }

    public Term append(Term child) {
        if (child == this || child.hasParent()) {
            throw new IllegalArgumentException("Child term must be different from current term and must not have parent");
        }
        children.add(child);
        child.setParent(this);
        return child;
    }

    private boolean hasParent() {
        return parent != null;
    }

    public Term append(final String path) {
        List<String> termNames = parsePath(path);
        Term term = this;
        for (String termName : termNames) {
            for (Term c : term.getChildren()) {
                if (c.getName().equals(termName)) {
                    term = c;
                }
            }
            if (!term.getName().equals(termName)) {
                term = term.append(new Term(termName));
            }

        }
        return term;
    }

    public Set<Term> getChildren() {
        return Collections.unmodifiableSet(children);
    }

    public String getFullPath() {
        initPath();
        return path;
    }

    public String getPath() {
        initPath();
        return buildPath(pathNames);
    }

    public String getName() {
        return name;
    }

    public String[] getNames() {
        initPath();
        return getPathPart(pathNames);
    }

    public boolean isUri() {
        return getTaxonomy().getName().equals(URI_TAXONOMY);
    }

    public boolean isTaxonomy() {
        return getParent() == null;
    }

    public Term getTaxonomy() {
        Term result = this;
        while (result.getParent() != null) {
            result = result.getParent();
        }
        return result;
    }

    public Term remove(Term child) {
        if (children.remove(child)) {
            child.setParent(null);
        }
        return this;
    }

    public Term getParent() {
        return parent;
    }

    @Override
    public String toString() {
        return getFullPath();
    }

    void addItem(Item item) {
        items.add(item);
    }

    void removeItem(Item item) {
        items.remove(item);
    }

    public Set<Item> getItems() {
        return Collections.unmodifiableSet(items);
    }

    private void setParent(Term parent) {
        this.parent = parent;
    }

    private List<String> parsePath(final String path) {
        Matcher m = PATH_REGEX_PATTERN.matcher(path);
        if (!m.matches()) {
            throw new IllegalArgumentException("Illegal path: " + path);
        }

        List<String> result = Arrays.stream(path.split("\\/"))
                .filter(Objects::nonNull)
                .filter(t -> !t.isEmpty())
                .collect(Collectors.toList());
        return result;
    }

    private String[] parse(final String fullPath) {
        Matcher m = FULL_PATH_REGEX_PATTERN.matcher(fullPath);
        List<String> result = new ArrayList<>(10);
        if (m.matches()) {
            result.add(m.group("tax"));
            result.addAll(parsePath(m.group("term")));

        }
        if (result.size() < 1) {
            throw new IllegalArgumentException("Illegal path: " + fullPath);
        }
        return result.toArray(new String[result.size()]);
    }


}
