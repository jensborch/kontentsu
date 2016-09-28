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
package dk.kontentsu.cdn.model.internal;


import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.Size;

/**
 *
 * @author Jens Borch Christiansen
 */
@Embeddable
public class Metadata implements Serializable {

    private static final long serialVersionUID = -3801848852252393124L;

    @Column(name = "value", length = 2000)
    @Size(min = 1, max = 2000)
    private String value;

    protected Metadata() {
        //Needed by JPA
    }

    public Metadata(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Key for metadata values that includes at type.
     */
    @Embeddable
    public static class Key implements Serializable {

        private static final long serialVersionUID = 6439651967780093035L;

        @Column(name = "type", length = 50)
        @Enumerated(EnumType.STRING)
        private MetadataType type;

        @Column(name = "key", length = 100)
        @Size(min = 1, max = 100)
        private String key;

        protected Key() {
            //Needed by JPA
        }

        public Key(final MetadataType type, final String key) {
            this.type = type;
            this.key = key;
        }

        public Key(final String type, final String key) {
            this.type = MetadataType.parse(type);
            this.key = key;
        }

        public MetadataType getType() {
            return type;
        }

        public String getKey() {
            return key;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 47 * hash + Objects.hashCode(this.type);
            hash = 47 * hash + Objects.hashCode(this.key);
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
            Key other = (Key) obj;
            return Objects.equals(this.key, other.key) && this.type == other.type;
        }

    }
}
