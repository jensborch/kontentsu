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

import static dk.kontentsu.model.Item.URI.ALLOWED_CHARS_REGEX;
import static dk.kontentsu.model.Item.URI.PATH_SEPARATOR;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import dk.kontentsu.repository.Repository;

/**
 * A term in a taxonomy used to categorize items.
 *
 * @author Jens Borch Christiansen
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "term",
        uniqueConstraints = {
            @UniqueConstraint(columnNames = {"parent_id", "name"}, name = "term_constraint"),
            @UniqueConstraint(columnNames = {"path"}, name = "path_constraint")})
@NamedQueries({
    @NamedQuery(name = Repository.TERM_FIND_BY_URI,
            query = "SELECT t FROM Term t WHERE t.path = :path"),
    @NamedQuery(name = Repository.TERM_FIND_ALL,
            query = "SELECT DISTINCT t FROM Term t LEFT JOIN FETCH t.children WHERE t.parent IS NULL"),
    @NamedQuery(name = Repository.TERM_FIND,
            query = "SELECT DISTINCT t FROM Term t LEFT JOIN FETCH t.children WHERE t.path = :path"),
    @NamedQuery(name = Repository.TERM_GET,
            query = "SELECT DISTINCT t FROM Term t LEFT JOIN FETCH t.children WHERE t.uuid = :uuid")})
public class Term extends AbstractBaseEntity {

    public static final String URI_TAXONOMY = "uri";
    public static final String TAXONOMY_SEPARATOR = ":";
    private static final long serialVersionUID = -3974244017445628292L;

    private static final String FULL_PATH_REGEX = String.format("^(?<tax>%s+):(?<term>\\/(?:%s+\\/)*)$", ALLOWED_CHARS_REGEX, ALLOWED_CHARS_REGEX);
    private static final String PATH_REGEX = String.format("^\\/?(?:%s+\\/)*%s+\\/?$", ALLOWED_CHARS_REGEX, ALLOWED_CHARS_REGEX);
    private static final Pattern FULL_PATH_REGEX_PATTERN = Pattern.compile(FULL_PATH_REGEX);
    private static final Pattern PATH_REGEX_PATTERN = Pattern.compile(PATH_REGEX);

    @ManyToMany(mappedBy = "terms")
    private Set<Item> items = new HashSet<>();

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, mappedBy = "parent", fetch = FetchType.LAZY)
    @OrderColumn
    private Set<Term> children = new HashSet<>();

    @NotNull
    @Size(min = 1, max = 200)
    @Column(name = "name", length = 200)
    private String name;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Term parent;

    @Transient
    private String[] pathElements;

    @Lob
    @Column(name = "path")
    private String path;

    public Term(final String name) {
        //TODO: chnage this
        this.name = name.toLowerCase();
    }

    public Term() {
        this.name = URI_TAXONOMY;
    }

    public static Term parse(final String path) {
        String[] terms = splitPathWithTaxonomy(path);
        Term term = null;
        if (terms.length > 0) {
            term = new Term(terms[0]);
            if (terms.length > 1) {
                for (int i = 1; i < terms.length; i++) {
                    term = term.append(new Term(terms[i]));
                }
            }
        }
        return term;
    }

    public static String toPath(final String taxonomy, final String path) {
        return taxonomy
                + Term.TAXONOMY_SEPARATOR
                + (path.startsWith(Item.URI.PATH_SEPARATOR) ? "" : Item.URI.PATH_SEPARATOR)
                + path
                + (path.endsWith(Item.URI.PATH_SEPARATOR) ? "" : Item.URI.PATH_SEPARATOR);
    }

    @PostLoad
    public void updatePathElements() {
        pathElements = splitPathWithTaxonomy(path);
    }

    @PrePersist
    @PreUpdate
    public void initPath() {
        if (pathElements == null) {
            pathElements = initElements();
        }
        if (path == null) {
            path = initPath(pathElements);
        }
    }

    private String[] initElements() {
        Deque<String> result = new ArrayDeque<>();
        Optional<Term> last = Optional.of(this);
        while (last.isPresent()) {
            result.addFirst(last.get().getName());
            last = last.get().getParent();
        }
        return result.toArray(new String[result.size()]);
    }

    private String initPath(final String[] p) {
        return getTaxonomyPart(p) + TAXONOMY_SEPARATOR + toPathString(p);
    }

    public static List<String> splitPath(final String p) {
        if (PATH_SEPARATOR.equals(p)) {
            return new ArrayList<>(0);
        } else {
            Matcher m = PATH_REGEX_PATTERN.matcher(p);
            if (!m.matches()) {
                throw new IllegalArgumentException("Path '" + p + "' mustch match regular expression " + PATH_REGEX);
            }
            return Arrays.stream(p.split("\\" + PATH_SEPARATOR))
                    .filter(Objects::nonNull)
                    .filter(t -> !t.isEmpty())
                    .collect(Collectors.toList());
        }
    }

    public static String[] splitPathWithTaxonomy(final String fullPath) {
        Matcher m = FULL_PATH_REGEX_PATTERN.matcher(fullPath);
        List<String> result = new ArrayList<>(10);
        if (m.matches()) {
            result.add(m.group("tax"));
            result.addAll(splitPath(m.group("term")));
        } else {
            throw new IllegalArgumentException("Path '" + fullPath
                    + "' must match regular expression " + FULL_PATH_REGEX);
        }
        if (result.size() < 1) {
            throw new IllegalArgumentException("Path '" + fullPath
                    + "' must contain both taxonomy and path");
        }
        return result.toArray(new String[result.size()]);
    }

    private String[] getPathPart(final String[] p) {
        if (p.length > 0) {
            return Arrays.copyOfRange(p, 1, p.length);
        } else {
            throw new IllegalArgumentException("Path array must contain taxonomy - length must thus be > 0");
        }
    }

    private String getTaxonomyPart(final String[] p) {
        if (p.length > 0) {
            return p[0];
        } else {
            throw new IllegalArgumentException("Path array must contain taxonomy - length must thus be > 0");
        }
    }

    private String toPathString(final String[] elements) {
        if (elements.length > 1) {
            StringJoiner joiner = new StringJoiner(PATH_SEPARATOR, PATH_SEPARATOR, PATH_SEPARATOR);
            Arrays.stream(getPathPart(elements)).forEach(joiner::add);
            return joiner.toString();
        } else {
            return PATH_SEPARATOR;
        }
    }

    public Term append(final Term child) {
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
        List<String> termNames = splitPath(path);
        return append(termNames);
    }

    public Term append(final String... path) {
        return append(Arrays.asList(path));
    }

    public Term append(final List<String> path) {
        Term term = this;
        for (String e : path) {
            Optional<Term> found = term.getChildren().stream()
                    .filter(t -> t.getName().equals(e))
                    .findAny();
            if (found.isPresent()) {
                term = found.get();
            } else {
                term = term.append(new Term(e));
            }
        }
        return term;
    }

    public Set<Term> getChildren() {
        return Collections.unmodifiableSet(children);
    }

    public Set<Term> getChildren(final boolean recursive) {
        if (recursive) {
            Set<Term> result = new HashSet<>();
            for (Term t : getChildren()) {
                result.add(t);
                result.addAll(t.getChildren(true));
            }
            return result;
        } else {
            return getChildren();
        }
    }

    public String getPathWithTaxonomy() {
        initPath();
        return path;
    }

    public String getPath() {
        initPath();
        return toPathString(pathElements);
    }

    public String getURIPath() {
        StringJoiner joiner = new StringJoiner(PATH_SEPARATOR);
        Arrays.stream(getPathPart(pathElements)).forEach(joiner::add);
        return joiner.toString();
    }

    public String getName() {
        return name;
    }

    public String[] getElements() {
        initPath();
        return getPathPart(pathElements);
    }

    public boolean isUri() {
        return getTaxonomy().getName().equals(URI_TAXONOMY);
    }

    public boolean isTaxonomy() {
        return !getParent().isPresent();
    }

    @SuppressWarnings("squid:S3655")
    public Term getTaxonomy() {
        Term result = this;
        while (result.getParent().isPresent()) {
            result = result.getParent().get();
        }
        return result;
    }

    public Term remove(final Term child) {
        if (children.remove(child)) {
            child.setParent(null);
            child.resetPath();
        }
        return this;
    }

    void resetPath() {
        this.pathElements = null;
        this.path = null;
    }

    public Optional<Term> getParent() {
        return Optional.ofNullable(parent);
    }

    private void setParent(final Term parent) {
        this.resetPath();
        this.parent = parent;
    }

    @Override
    public String toString() {
        return getPathWithTaxonomy();
    }

    void addItem(final Item item) {
        items.add(item);
    }

    void removeItem(final Item item) {
        items.remove(item);
    }

    public Set<Item> getItems() {
        return Collections.unmodifiableSet(items);
    }

}
