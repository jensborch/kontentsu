package dk.kontentsu.model;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
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

    public static final char SEPERATOR_CHAR = '/';
    public static final String SEPERATOR = String.valueOf(SEPERATOR_CHAR);
    private static final long serialVersionUID = -3974244017445628292L;

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
        this.name = name;
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
        if (child == this) {
            throw new IllegalArgumentException("Child term must be different from current term");
        }
        children.add(child);
        child.setParent(this);
        return child;
    }

    public Term append(final String path) {
        String[] termNames = split(path);
        Term term = getTaxonomy();
        for (String termName : termNames) {
            for (Term t : term.getChildren()) {
                if (t.getName().equals(termName)) {
                    term = t;
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
        StringJoiner joiner = new StringJoiner(SEPERATOR, SEPERATOR, "");
        Arrays.stream(getNames()).forEach(joiner::add);
        return joiner.toString();
    }

    private void setParent(Term parent) {
        this.parent = parent;
    }

    private String[] split(final String uri) {
        return uri.split(SEPERATOR);
    }
}
