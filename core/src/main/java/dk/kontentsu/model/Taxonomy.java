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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import dk.kontentsu.repository.Repository;

/**
 * User defined taxonomy for items in Kontentsu. A given item in Kontentsu can be
 * categorized according to different taxonomic schemes - i.e. different user defined
 * classification systems as needed.
 *
 * @author Jens Borch Christiansen
 */
@Entity
@Table(name = "taxonomy")
@NamedQueries({
    @NamedQuery(name = Repository.TAXONOMY_FIND_ALL,
            query = "SELECT t FROM Taxonomy t"),
    @NamedQuery(name = Repository.TAXONOMY_GET_BY_NAME,
            query = "SELECT t FROM Taxonomy t WHERE t.name = :name"),
    @NamedQuery(name = Repository.TAXONOMY_GET,
            query = "SELECT t FROM Taxonomy t WHERE t.uuid = :uuid")})
public class Taxonomy extends AbstractBaseEntity {

    private static final long serialVersionUID = 7168960688451082158L;

    @NotNull
    @Size(min = 1, max = 200)
    @Column(name = "name", unique = true, length = 200)
    private String name;

    public Taxonomy() {
        //Needed by JPA
    }

    public Taxonomy(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

}
