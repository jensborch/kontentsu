package dk.kontentsu.model;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 * @author Jens Borch Christiansen
 */
@MappedSuperclass
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Node extends AbstractBaseEntity {

    public static final char SEPERATOR_CHAR = '/';
    public static final String SEPERATOR = String.valueOf(SEPERATOR_CHAR);
    private static final long serialVersionUID = -3974244017445628292L;

    @NotNull
    @Size(min = 1, max = 200)
    @Column(name = "name", length = 200)
    private String name;

    @OneToMany
    @OrderColumn
    private Set<Node> children = new HashSet<Node>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    private Node parent;

    public Node(String name) {
        this.name = name;
    }

    public abstract Taxonomy getTaxonomy();

    public Node addChild(Node child) {
        if (child == this) {
            throw new IllegalArgumentException("");
        }
        children.add(child);
        child.setParent(this);
        return this;
    }

    public Node removeChild(Node child) {
        child.setParent(null);
        children.remove(child);
        return this;
    }

    private void setParent(Node parent) {
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public Set<Node> getChildren() {
        return Collections.unmodifiableSet(children);
    }

    public Node getParent() {
        return parent;
    }

    public String[] getElements() {
        Deque<String> result = new ArrayDeque<>();
        Node last = parent;
        while (last != null) {
            result.addFirst(last.getName());
            last = last.getParent();
        }
        return result.toArray(new String[result.size()]);
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(SEPERATOR);
        Arrays.stream(getElements()).forEach(joiner::add);
        return joiner.toString();
    }

}
