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

import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import dk.kontentsu.repository.Repository;

/**
 * Host server for a CDN item.
 *
 * @author Jens Borch Christiansen
 */
@Entity
@Table(name = "host")
@NamedQueries({
    @NamedQuery(name = Repository.HOST_FIND_ALL,
            query = "SELECT h FROM Host h"),
    @NamedQuery(name = Repository.HOST_GET_BY_NAME,
            query = "SELECT h FROM Host h WHERE h.name = :name"),
    @NamedQuery(name = Repository.HOST_GET,
            query = "SELECT h FROM Host h WHERE h.uuid = :uuid")})
public class Host extends AbstractBaseEntity {

    private static final long serialVersionUID = 6648972303846562274L;

    @NotNull
    @Size(min = 2, max = 200)
    @Column(name = "name", length = 200, unique = true)
    private String name;

    @Size(max = 1000)
    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "filesystem", length = 1000)
    private URI filesystem;

    @NotNull
    @Pattern(regexp = "^[^\b\f\n\r\t]+$")
    @Column(name = "path", length = 1000)
    private String path;

    protected Host() {
        //Needed by JPA
    }

    public Host(final String name, final String description, final URI filesystem, final String path) {
        this.name = name;
        this.description = description;
        this.filesystem = filesystem;
        this.path = path;
    }

    public Host(final String name, final String description, final String path) {
        this(name, description, null, path);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Optional<URI> getFilesystemURI() {
        return Optional.ofNullable(filesystem);
    }

    @JsonIgnore
    public FileSystem getFilesystem() {
        return getFilesystemURI().map(u -> FileSystems.getFileSystem(u)).orElse(FileSystems.getDefault());
    }

    @JsonSerialize(using = ToStringSerializer.class)
    public Path getPath() {
        return getFilesystem().getPath(path);
    }

}
