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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import dk.kontentsu.cdn.repository.Repository;

/**
 * Container for the content distributed to the CDN server.
 *
 * @author Jens Borch Christiansen
 */
@Entity
@Table(name = "content")
@NamedQueries({
    @NamedQuery(name = Repository.CONTENT_GET,
            query = "SELECT c FROM Content c WHERE c.uuid = :uuid")})
public class Content implements Serializable {

    private static final long serialVersionUID = 2169103680138791403L;
    private static final String HASH_ALGORITHM = "MD5";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "uuid", length = 32, unique = true)
    @NotNull
    private UUID uuid;

    @Lob
    @NotNull
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "data", length = 1073741824)
    private byte[] data;

    @Column(name = "hash", length = 32)
    private String hash;

    @Column(name = "encoding", length = 50)
    private String encoding;

    @NotNull
    @Column(name = "mimetype")
    private MimeType mimeType;

    protected Content() {
        //Needed by JPA
    }

    public Content(final byte[] content, final Charset encoding, final MimeType mimeType) {
        this.uuid = UUID.randomUUID();
        setEncoding(encoding);
        this.mimeType = mimeType;
        this.data = Arrays.copyOf(content, content.length);
        initHash();
    }

    public Integer getId() {
        return id;
    }

    public UUID getUuid() {
        return uuid;
    }

    private void setEncoding(final Charset encoding) {
        this.encoding = encoding == null ? null : encoding.name();
    }

    private static MessageDigest getContentHashDigester() {
        try {
            return MessageDigest.getInstance(HASH_ALGORITHM);
        } catch (NoSuchAlgorithmException ex) {
            throw new ContentException("Wrong content hash algorithm " + HASH_ALGORITHM + "... should never happen", ex);
        }
    }

    public int getSize() {
        return (int) data.length;
    }

    public String getHash() {
        initHash();
        return hash;
    }

    private void initHash() {
        if (hash == null) {
            MessageDigest digest = getContentHashDigester();
            this.hash = toHex(digest.digest(data));
        }
    }

    public InputStream getDataAsBinaryStream() {
        return new ByteArrayInputStream(data);
    }

    private byte[] getDataAsBytes() {
        return data;
    }

    public String getData() {
        Charset e = getEncoding().orElseThrow(() -> new ContentException("Encoding is null"));
        return new String(getDataAsBytes(), e);
    }

    /**
     * Hash to hex string - verified using http://hash.online-convert.com/md5-generator
     */
    private static String toHex(final byte[] digest) {
        return String.format("%0" + (digest.length << 1) + "x", new BigInteger(1, digest));
    }

    public Optional<Charset> getEncoding() {
        return (encoding == null) ? Optional.empty() : Optional.of(Charset.forName(encoding));
    }

    public MimeType getMimeType() {
        return mimeType;
    }

    @Override
    public int hashCode() {
        return getUuid().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Content)) {
            return false;
        }
        Content other = (Content) obj;
        return getUuid().equals(other.getUuid());
    }

}
