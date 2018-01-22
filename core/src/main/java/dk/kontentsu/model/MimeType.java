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

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dk.kontentsu.spi.ContentProcessingMimeType;

/**
 * Class representing the content Mime Type of a item in the CDN, but also
 * serves the purpose of parsing accept headers.
 *
 * @see <a href="http://tools.ietf.org/html/rfc6838">RFC Media Type
 * Specification</a>
 *
 * @author Jens Borch Christiansen
 */
@Embeddable
public class MimeType implements Serializable {

    public static final MimeType APPLICATION_XML_TYPE = new MimeType("application", "xml");
    public static final String APPLICATION_HAL_JSON = "application/hal+json";
    public static final MimeType APPLICATION_HAL_JSON_TYPE = new MimeType("application", "hal+json");
    public static final MimeType APPLICATION_JSON_TYPE = new MimeType("application", "json");
    public static final MimeType IMAGE_PNG_TYPE = new MimeType("image", "png");
    public static final MimeType IMAGE_GIF_TYPE = new MimeType("image", "gif");
    public static final MimeType IMAGE_JPG_TYPE = new MimeType("image", "jpg");
    public static final MimeType IMAGE_ANY_TYPE = new MimeType("image");
    public static final MimeType VIDEO_ANY_TYPE = new MimeType("video");

    public static final Map<String, MimeType> EXTENSIONS = new HashMap<>(10);

    private static final String PARAM_CHARSET = "charset";

    private static final String SEPARATOR = "/";
    private static final String WILDCARD = "*";

    // See http://www.ietf.org/rfc/rfc2045.txt for valid mime-type characters.
    private static final String VALID_MIMETYPE_CHARS = "[^\\c\\(\\)<>@,;:\\\\\"/\\[\\]\\?=\\s]";
    private static final String MIME_PARAMS_REGEX_STR = ";\\s*(" + VALID_MIMETYPE_CHARS + "+)\\s*=\\s*(" + VALID_MIMETYPE_CHARS + "+)\\s*";
    private static final String MIME_REGEX_STR = "^\\s*((?<type>" + VALID_MIMETYPE_CHARS + "+|\\*)/(?<subtype>" + VALID_MIMETYPE_CHARS + "+|\\*)|(?<wildcard>\\*))"
            + "\\s*(" + MIME_PARAMS_REGEX_STR + ")*$";

    private static final Pattern MIME_REGEX = Pattern.compile(MIME_REGEX_STR);
    private static final Pattern MIME_PARAMS_REGEX = Pattern.compile(MIME_PARAMS_REGEX_STR);
    private static final long serialVersionUID = -6748129010939249437L;

    private static final Logger LOGGER = LogManager.getLogger();

    static {
        EXTENSIONS.put("xml", APPLICATION_XML_TYPE);
        EXTENSIONS.put("json", APPLICATION_JSON_TYPE);
        EXTENSIONS.put("json", APPLICATION_HAL_JSON_TYPE);
        EXTENSIONS.put("gif", IMAGE_GIF_TYPE);
        EXTENSIONS.put("jpeg", IMAGE_JPG_TYPE);
        EXTENSIONS.put("jpg", IMAGE_JPG_TYPE);
        EXTENSIONS.put("png", IMAGE_PNG_TYPE);
    }

    @Size(min = 1, max = 64)
    @Column(name = "mimetype_type", length = 64)
    @NotNull
    private String type;

    @Size(min = 1, max = 64)
    @Column(name = "mimetype_subtype", length = 64)
    @NotNull
    private String subType;

    @Transient
    private final Map<String, String> params = new HashMap<>();

    protected MimeType() {
        //Needed by JPA
    }

    public MimeType(final String type) {
        this.type = type.trim().toLowerCase(Locale.ENGLISH);
        this.subType = "*";
    }

    public MimeType(final String type, final String subtype) {
        this.type = type.trim().toLowerCase(Locale.ENGLISH);
        this.subType = subtype.trim().toLowerCase(Locale.ENGLISH);
    }

    public MimeType(final String type, final String subtype, final Map<String, String> params) {
        this(type, subtype);
        this.params.putAll(params);
    }

    public static MimeType formExtension(String ext) {
        return Optional.ofNullable(EXTENSIONS.get(ext))
                .orElseThrow(() -> new IllegalArgumentException("Unknown MIME type extension: " + ext));
    }

    public String getType() {
        return type;
    }

    public String getSubType() {
        return subType;
    }

    /**
     * Parses a mime type string including media range and returns a MimeType
     * object. For example, the media range 'application/*;q=0.5' would get
     * parsed into: ('application', '*', {'q', '0.5'}).
     *
     * @param mimeType the mime type to parse
     * @return a MimeType object
     */
    public static MimeType parse(final String mimeType) {
        if (mimeType == null || "".equals(mimeType.trim())) {
            throw new IllegalArgumentException("Mime Type can not be empty");
        }
        final Matcher mimeMatcher = MIME_REGEX.matcher(mimeType);
        if (!mimeMatcher.matches()) {
            throw new IllegalArgumentException(mimeType + " is not a valid Mime Type");
        }

        final boolean oneWildcard = mimeMatcher.group("wildcard") != null;
        final String type = oneWildcard ? "*" : mimeMatcher.group("type");
        final String subType = oneWildcard ? "*" : mimeMatcher.group("subtype");

        Map<String, String> params = new HashMap<>();

        if (mimeMatcher.group("subtype") != null) {
            params = parseParams(mimeType);
        }

        return new MimeType(type, subType, params);
    }

    public static List<MimeType> parseHeader(final String header) {
        final List<MimeType> types = new ArrayList<>();

        Pattern.compile(",").splitAsStream(header).forEach(r -> {
            try {
                types.add(MimeType.parse(r));
            } catch (IllegalArgumentException e) {
                LOGGER.debug("Unable to parse header: {}", header);
            }
        });
        return types;
    }

    public static Map<String, String> parseParams(final String mimeType) {
        final Map<String, String> params = new HashMap<>();
        final Matcher paramMatcher = MIME_PARAMS_REGEX.matcher(mimeType);

        while (paramMatcher.find()) {
            final String key = paramMatcher.group(1);
            if (key != null) {
                final String value = paramMatcher.group(2);
                params.put(key, value);
            }
        }
        return params;
    }

    public Map<String, String> getParams() {
        return Collections.unmodifiableMap(params);
    }

    public Optional<Charset> getCharset() {
        final String charset = params.get(PARAM_CHARSET);
        return (charset == null) ? Optional.empty() : Optional.of(Charset.forName(charset));
    }

    private Match matches(final String type, final String subType) {
        if (this.type.equals(type) && this.subType.equals(subType)) {
            return Match.EXACT;
        } else if (this.type.equals(type) && (WILDCARD.equals(this.subType) || WILDCARD.equals(subType))) {
            return Match.SUBTYPE_WILDCARD;
        } else if (WILDCARD.equals(this.type) || WILDCARD.equals(type)) {
            return Match.WILDCARD;
        } else {
            return Match.NONE;
        }
    }

    public Match matches(final String type) {
        try {
            return matches(MimeType.parse(type));
        } catch (IllegalArgumentException e) {
            return Match.NONE;
        }
    }

    /**
     * Check if this mime type matches another mime type. This will perform a
     * match using wildcards, thus application&#47;* will match
     * application&#47;json and *&#47;* will match everything.
     *
     * @param other the mim type to match
     * @return true if the types matches
     */
    public Match matches(final MimeType other) {
        if (other == null) {
            return Match.NONE;
        } else {
            return matches(other.getType(), other.getSubType());
        }
    }

    public static List<MimeType> create(final Annotation annotation) {
        if (annotation != null && annotation.annotationType() == ContentProcessingMimeType.class) {
            return Arrays.stream(((ContentProcessingMimeType) annotation).value())
                    .map(MimeType::parse)
                    .collect(Collectors.toList());
        } else {
            throw new IllegalArgumentException("");
        }
    }

    public Match matches(final Annotation annotation) {
        if (annotation != null && annotation.annotationType() == ContentProcessingMimeType.class) {
            return Arrays.stream(((ContentProcessingMimeType) annotation).value())
                    .map(this::matches)
                    .filter(m -> m != Match.NONE)
                    .findAny()
                    .orElse(Match.NONE);
        } else {
            return Match.NONE;
        }
    }

    /**
     * Check if a request header matches the given mime type.
     *
     * @param header the header string to check
     * @return true if the header matches the mime type
     */
    public boolean matchesHeader(final String header) {
        if (header == null || header.trim().isEmpty()) {
            return true;
        } else {
            return Pattern.compile(",").splitAsStream(header).anyMatch(h -> matches(h).isMatch());
        }
    }

    public boolean isText() {
        return isJson() || isXML();
    }

    public boolean isJson() {
        return equalsIgnoreParams(MimeType.APPLICATION_JSON_TYPE) || equalsIgnoreParams(MimeType.APPLICATION_HAL_JSON_TYPE);
    }

    public boolean isXML() {
        return equalsIgnoreParams(MimeType.APPLICATION_XML_TYPE);
    }

    public boolean isHal() {
        return equalsIgnoreParams(MimeType.APPLICATION_HAL_JSON_TYPE);
    }

    public boolean isImage() {
        return matches(IMAGE_ANY_TYPE).isMatch();
    }

    public boolean isVideo() {
        return matches(VIDEO_ANY_TYPE).isMatch();
    }

    public String getFileExtension() {
        return isHal() ? "json" : subType;
    }

    public MediaType toMediaType() {
        return new MediaType(getType(), getSubType(), getParams());
    }

    public MediaType toMediaTypeWithCharset() {
        if (isJson() || isXML()) {
            return toMediaType().withCharset(StandardCharsets.UTF_8.name());
        } else {
            return toMediaType();
        }
    }

    public boolean equalsIgnoreParams(final MimeType mimetype) {
        return mimetype != null && getType().equals(mimetype.getType()) && getSubType().equals(mimetype.getSubType());
    }

    private boolean mimeTypeEquals(final MimeType mimetype) {
        return mimetype != null && equalsIgnoreParams(mimetype) && getParams().equals(mimetype.getParams());
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof MimeType && mimeTypeEquals((MimeType) obj);
    }

    @Override
    public int hashCode() {
        return (getType().hashCode() + getSubType()).hashCode();
    }

    @Override
    public String toString() {
        final StringBuilder s = new StringBuilder(type);
        s.append(SEPARATOR)
                .append(subType)
                .append(params.isEmpty() ? "" : ";")
                .append(params.entrySet()
                        .stream()
                        .map(e -> e.getKey() + "=" + e.getValue())
                        .collect(Collectors.joining(";")));
        return s.toString();

    }

    /**
     * Representation of how a set of mime types matches.
     */
    public enum Match {

        NONE(0), WILDCARD(1), SUBTYPE_WILDCARD(2), EXACT(3);

        private final int priority;

        Match(final int priority) {
            this.priority = priority;
        }

        public int getPriority() {
            return priority;
        }

        public boolean isMatch() {
            return this == WILDCARD || this == SUBTYPE_WILDCARD || this == EXACT;
        }

    }
}
