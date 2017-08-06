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
package dk.kontentsu.upload;

import dk.kontentsu.model.Interval;
import dk.kontentsu.model.MimeType;
import dk.kontentsu.model.SemanticUri;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.validation.constraints.NotNull;

/**
 * Class representing the information and data needed to upload a item to the CDN.
 *
 * @author Jens Borch Christiansen
 */
public final class UploadItem {

    @NotNull
    private final SemanticUri uri;

    @NotNull
    private final Interval interval;

    @NotNull
    private final MimeType mimeType;

    private final Charset encoding;

    @NotNull
    private final Content content;

    @NotNull
    private final List<String> hosts;

    private UploadItem(final Builder builder) {
        this.interval = builder.interval;
        this.uri = builder.uri;
        this.mimeType = builder.mimeType;
        this.encoding = builder.encoding;
        this.content = builder.content;
        this.hosts = new ArrayList<>(builder.hosts);
    }

    public static Builder builder() {
        return new Builder();
    }

    public SemanticUri getUri() {
        return uri;
    }

    public Interval getInterval() {
        return interval;
    }

    public MimeType getMimeType() {
        return mimeType;
    }

    public Charset getEncoding() {
        return encoding;
    }

    public InputStream getContent() {
        return content.getContent();
    }

    public List<String> getHosts() {
        return Collections.unmodifiableList(hosts);
    }

    /**
     * Representation of the content to uploaded.
     */
    public interface Content {

        String getReference();

        InputStream getContent();
    }

    /**
     * Content that should be downloaded by the CDN application.
     */
    public static class DownloadableContent implements Content {

        private final URL url;

        public DownloadableContent(final URL url) {
            this.url = url;
        }

        @Override
        public InputStream getContent() {
            try {
                return url.openStream();
            } catch (IOException ex) {
                throw new UploadException("Error retrieving content from URL: " + getReference(), ex);
            }
        }

        @Override
        public String getReference() {
            return url.toExternalForm();
        }

    }

    /**
     * Content is part of multipart message and can thus be retrieved directly.
     */
    public static class MultipartContent implements Content {

        private final InputStream is;

        private final String ref;

        public MultipartContent(final String ref, final InputStream is) {
            this.ref = ref;
            this.is = is;
        }

        @Override
        public InputStream getContent() {
            return is;
        }

        @Override
        public String getReference() {
            return ref;
        }
    }

    /**
     * Builder for CDN upload item.
     */
    public static final class Builder {

        private Content content;
        private SemanticUri uri;
        private Interval interval;
        private MimeType mimeType;
        private Charset encoding;
        private final List<String> hosts = new ArrayList<>();

        private Builder() {
        }

        public Builder uri(final SemanticUri uri) {
            this.uri = uri;
            return this;
        }

        public Builder content(final URL url) {
            this.content = new DownloadableContent(url);
            return this;
        }

        public Builder content(final String ref, final InputStream is) {
            this.content = new MultipartContent(ref, is);
            return this;
        }

        public Builder mimeType(final MimeType mimeType) {
            this.mimeType = mimeType;
            return this;
        }

        public Builder encoding(final Charset encoding) {
            this.encoding = encoding;
            return this;
        }

        public Builder interval(final Interval interval) {
            this.interval = interval;
            return this;
        }

        public Builder host(final String host) {
            this.hosts.add(host);
            return this;
        }

        public UploadItem build() {
            return new UploadItem(this);
        }

    }

}
