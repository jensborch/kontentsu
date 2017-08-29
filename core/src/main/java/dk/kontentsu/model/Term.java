package dk.kontentsu.model;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import dk.kontentsu.repository.Repository;

/**
 *
 * @author Jens Borch Christiansen
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "node",
        uniqueConstraints = {
            @UniqueConstraint(columnNames = {"parent_id", "name"}, name = "node_constraint")})
@NamedQueries({
    @NamedQuery(name = Repository.TERM_FIND_ALL,
            query = "SELECT t FROM Term t LEFT JOIN FETCH t.children"),
    @NamedQuery(name = Repository.TERM_GET,
            query = "SELECT t FROM Term t LEFT JOIN FETCH t.children WHERE uuid = :uuid")})
public class Term extends AbstractBaseEntity {

    public static final String URI_TAXONOMY = "uri";
    public static final char SEPARATOR_CHAR = '/';
    public static final String SEPARATOR = String.valueOf(SEPARATOR_CHAR);
    private static final long serialVersionUID = -3974244017445628292L;

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
    private String[] path;

    public Term(String name) {
        this.name = name.toLowerCase();
    }

    @PostLoad
    public void updatePath() {
        if (path == null) {
            Deque<String> result = new ArrayDeque<>();
            Term last = this;
            while (last != null) {
                result.addFirst(last.getName());
                last = last.getParent();
            }
            path = result.toArray(new String[result.size()]);
        }
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
        String[] termNames = split(path);
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
        updatePath();
        if (path.length > 0) {
            return path[0] + ":" + toString();
        } else {
            return null;
        }
    }

    public String getName() {
        return name;
    }

    public String[] getNames() {
        updatePath();
        if (path.length > 0) {
            return Arrays.copyOfRange(path, 1, path.length);
        } else {
            return new String[0];
        }
    }

    public boolean isUriTaxonomy() {
        return getTaxonomy().equals(URI_TAXONOMY);
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
        StringJoiner joiner = new StringJoiner(SEPARATOR, SEPARATOR, "");
        Arrays.stream(getNames()).forEach(joiner::add);
        return joiner.toString();
    }

    void addItem(Item item) {
        items.add(item);
    }

    void removeItem(Item item) {
        items.remove(item);
    }

    private void setParent(Term parent) {
        this.parent = parent;
    }

    private String[] split(final String path) {
        String tmp = path.toLowerCase();
        if (tmp.startsWith(SEPARATOR)) {
            tmp = tmp.substring(1);
        }
        if (tmp.endsWith(SEPARATOR)) {
            tmp = tmp.substring(0, tmp.length() - 1);
        }
        return tmp.split(SEPARATOR);
    }

}
