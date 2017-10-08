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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
                query = "SELECT t FROM Term t LEFT JOIN FETCH t.children WHERE t.uuid = :uuid")})
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
    private Set<Term> children = new HashSet<Term>();

    @NotNull
    @Size(min = 1, max = 200)
    @Column(name = "name", length = 200)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    private Term parent;

    @Transient
    private String[] pathNames;

    @Lob
    @Column(name = "path")
    private String path;

    protected Term() {
        //Needed by JPA
    }

    public Term(final String name) {
        this.name = name.toLowerCase();
    }

    public static Term parse(final String path) {
        String[] terms = parseFullPath(path);
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
        pathNames = parseFullPath(path);
    }

    private static List<String> parsePath(final String path) {
        if (SEPARATOR.equals(path)) {
            return new ArrayList<>(0);
        } else {
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
    }

    private static String[] parseFullPath(final String fullPath) {
        Matcher m = FULL_PATH_REGEX_PATTERN.matcher(fullPath);
        List<String> result = new ArrayList<>(10);
        if (m.matches()) {
            result.add(m.group("tax"));
            result.addAll(parsePath(m.group("term")));

        } else {
            throw new IllegalArgumentException("Illegal path: " + fullPath
                    + ". Path must match regular expression: " + FULL_PATH_REGEX);
        }
        if (result.size() < 1) {
            throw new IllegalArgumentException("Illegal path: " + fullPath
                    + ". Path must contain both taxonomy and path");
        }
        return result.toArray(new String[result.size()]);
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

    private String buildPath(final String[] p) {
        if (p.length > 1) {
            StringJoiner joiner = new StringJoiner(SEPARATOR, SEPARATOR, SEPARATOR);
            Arrays.stream(getPathPart(p)).forEach(joiner::add);
            return joiner.toString();
        } else {
            return SEPARATOR;
        }
    }

    private String buildFullPath(final String[] p) {
        return getTaxonomyPart(p) + TAXONOMY_SEPARATOR + buildPath(p);
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

    public Term remove(final Term child) {
        if (children.remove(child)) {
            child.setParent(null);
            child.resetPath();
        }
        return this;
    }

    void resetPath() {
        this.pathNames = null;
        this.path = null;
    }

    public Term getParent() {
        return parent;
    }

    private void setParent(final Term parent) {
        this.resetPath();
        this.parent = parent;
    }

    @Override
    public String toString() {
        return getFullPath();
    }

    void addItem(Item item) {
        items.add(item);
    }

    void removeItem(final Item item) {
        items.remove(item);
    }

    public Set<Item> getItems() {
        return Collections.unmodifiableSet(items);
    }

}
