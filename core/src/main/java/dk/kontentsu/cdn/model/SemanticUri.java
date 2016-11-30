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

import dk.kontentsu.cdn.spi.MimeType;
import java.io.Serializable;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Represents an URI for an item in the CDN.
 *
 * @author Jens Borch Christiansen
 */
@Embeddable
public class SemanticUri implements Serializable {

    private static final String REGEX = "^(?<elements>([^\\/\\s]+)(\\/(?<element>[^\\/\\s\\.]+))+?)(\\/(?<name>\\k<element>[^\\/\\s\\.]*))?(\\.[a-zA-Z]+)?$";
    private static final Pattern REGEX_PATTERN = Pattern.compile(REGEX);
    private static final long serialVersionUID = -6517163140576869876L;

    @NotNull
    @Size(min = 1, max = 200)
    @Column(name = "name", length = 200)
    private String name;

    @NotNull
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER, targetEntity = SemanticUriPath.class)
    @JoinColumn(name = "path_id")
    private SemanticUriPath path;

    protected SemanticUri() {
        //Needed by JPA
    }

    public SemanticUri(final SemanticUriPath path, final String name) {
        this.path = path;
        this.name = name;
    }

    public static SemanticUri parse(final String uri) {
        Matcher m = REGEX_PATTERN.matcher(uri);
        if (m.matches()) {
            String newname = (m.group("name") == null) ? m.group("element") : m.group("name");
            return new SemanticUri(SemanticUriPath.parse(m.group("elements")), newname);
        } else {
            throw new IllegalArgumentException("Illegal URI: " + uri);
        }
    }

    public String getName() {
        return name;
    }

    public SemanticUriPath getPath() {
        return path;
    }

    public String[] getElements() {
        String[] pathElements = path.getElements();
        String[] result = Arrays.copyOf(pathElements, pathElements.length + 1);
        result[pathElements.length] = name;
        return result;
    }

    public java.nio.file.Path toPath(final MimeType mimetype) {
        String[] elements = getElements();
        String[] tail = (elements.length > 1) ? Arrays.copyOfRange(elements, 1, elements.length) : new String[0];
        java.nio.file.Path p = Paths.get(elements[0], tail);
        String filename = p.getFileName().toString() + "." + mimetype.getFileExtension();
        return p.getParent().resolve(filename);
    }

    @Override
    public String toString() {
        return path.toString() + SemanticUriPath.SEPERATOR_CHAR + name;
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
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SemanticUri other = (SemanticUri) obj;
        return this.getPath().equals(other.getPath()) && this.getName().equals(other.getName());
    }

    public boolean matches(final String uri) {
        SemanticUri other = parse(uri);
        return equals(other);
    }

}
