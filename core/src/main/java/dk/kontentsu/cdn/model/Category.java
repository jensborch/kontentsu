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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import dk.kontentsu.cdn.jpa.AbstractBaseEntity;

/**
 * Category for item on the CDN. Can be used categorized CDN items for e.g. web personalization or for finding items.
 *
 * @author Jens Borch Christiansen
 */
@MappedSuperclass
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Category extends AbstractBaseEntity {

    public static final char SEPERATOR_CHAR = '/';
    public static final String SEPERATOR = SEPERATOR_CHAR + "";
    private static final long serialVersionUID = -2277423385038369623L;

    @NotNull
    @Size(min = 1, max = 2000)
    @Column(name = "path", length = 2000)
    private String path;

    @Transient
    private String[] elements;

    protected Category() {
        //Needed by JPA
    }

    public Category(final String... elements) {
        List<String> tmpuri = new ArrayList<>(Arrays.asList(elements));
        if (tmpuri.isEmpty() || tmpuri.contains(null) || tmpuri.contains("")) {
            throw new IllegalArgumentException("Invalid category element(s): " + toString(tmpuri));
        }
        this.elements = elements;
        this.path = toString(Arrays.asList(elements));
    }

    public abstract Taxonomy getTaxonomy();

    protected static String[] split(final String uri) {
        return uri.split(SEPERATOR);
    }

    public String[] getElements() {
        if (elements == null) {
            elements = path.split(SEPERATOR);
        }
        return Arrays.copyOf(elements, elements.length);
    }

    private static String toString(final List<String> semanticUri) {
        return semanticUri.stream().map(s -> Objects.toString(s)).collect(Collectors.joining(SEPERATOR));
    }

    @Override
    public String toString() {
        return path;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.path);
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Category other = (Category) obj;
        return path.equals(other.path);
    }

}
