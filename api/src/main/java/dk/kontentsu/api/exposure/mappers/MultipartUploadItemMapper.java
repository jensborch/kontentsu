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
package dk.kontentsu.api.exposure.mappers;

import java.io.InputStream;
import java.util.function.Function;

import dk.kontentsu.api.exposure.mappers.MultipartUploadItemMapper.MultipartUploadItem;
import dk.kontentsu.api.exposure.model.MultipartUploadItemRepresentation;
import dk.kontentsu.exception.ValidationException;
import dk.kontentsu.model.Interval;
import dk.kontentsu.model.MimeType;
import dk.kontentsu.upload.UploadItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Map upload item from REST API to domain object.
 *
 * @author Jens Borch Christiansen
 */
public class MultipartUploadItemMapper implements Function<MultipartUploadItem, UploadItem> {

    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public UploadItem apply(final MultipartUploadItem from) {
        final UploadItem.Builder builder = UploadItem.builder()
                .uri(from.item.getUri())
                .mimeType(from.mimetype)
                .content(from.item.getContentRef(), from.is);

        if (from.item.getInterval() == null) {
            LOGGER.debug("Using default interval for item: {}", from.item.getUri());
            builder.interval(new Interval());
        } else {
            builder.interval(from.item.getInterval());
        }

        from.item.getHosts().forEach(builder::host);

        if (from.mimetype.isText()) {
            builder.encoding(from.mimetype.getCharset()
                    .orElseThrow(() -> new ValidationException("Encoding must be part of attachment mime type for upload item: " + from.item.getUri().toString())));
        }

        return builder.build();
    }

    /**
     * Representation of a multipart upload.
     */
    public static class MultipartUploadItem {

        private final MultipartUploadItemRepresentation item;
        private final MimeType mimetype;
        private final InputStream is;

        public MultipartUploadItem(final MultipartUploadItemRepresentation item, final MimeType mimetype, final InputStream is) {
            this.item = item;
            this.mimetype = mimetype;
            this.is = is;
        }

    }

}
