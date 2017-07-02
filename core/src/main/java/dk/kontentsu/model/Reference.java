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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;


/**
 *
 * @author Jens Borch Christiansen
 */
@Entity
@Table(name = "reference")
public class Reference extends AbstractBaseEntity {

    private static final long serialVersionUID = 7618024206144088305L;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "version_id")
    private Version version;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;

    /**
     * Column for optimizing JPA queries.
     */
    @SuppressWarnings("PMD")
    @NotNull
    @Column(name = "item_path")
    private String itemPath;

    /**
     * Column for optimizing JPA queries.
     */
    @SuppressWarnings("PMD")
    @NotNull
    @Column(name = "item_name")
    private String itemName;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 50)
    private ReferenceType type;

    protected Reference() {
        //Needed by JPA...
    }

    public Reference(final Version version, final Item item, final ReferenceType type) {
        this.version = version;
        this.item = item;
        this.itemPath = item.getUri().getPath().toString();
        this.itemName = item.getName();
        this.type = type;
    }

    public Version getVersion() {
        return version;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(final Item item) {
        this.item = item;
    }

    public ReferenceType getType() {
        return type;
    }

}
