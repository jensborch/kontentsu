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

import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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


/**
 * Class representing the content Mime Type of a item in the CDN, but also serves the purpose of parsing accept headers.
 *
 * @see <a href="http://tools.ietf.org/html/rfc6838">RFC Media Type Specification</a>
 *
 * @author Jens Borch Christiansen
 */
@Embeddable
public class MimeType implements Serializable {

    public static final MimeType APPLICATION_XML_TYPE = new MimeType("application", "xml");
    public static final String APPLICATION_HAL_JSON = "application/hal+json";
    public static final MimeType APPLICATION_HAL_JSON_TYPE = new MimeType("application", "hal+json");
    public static final MimeType APPLICATION_JSON_TYPE = new MimeType("application", "json");
    public static final MimeType IMAGE_ANY_TYPE = new MimeType("image");
    public static final MimeType VIDEO_ANY_TYPE = new MimeType("video");

    private static final String PARAM_CHARSET = "charset";

    private static final String SEPERATOR = "/";

    // See http://www.ietf.org/rfc/rfc2045.txt for valid mime-type characters.
    private static final String VALID_MIMETYPE_CHARS = "[^\\c\\(\\)<>@,;:\\\\\"/\\[\\]\\?=\\s]";
    private static final String MIME_PARAMS_REGEX_STR = ";\\s*(" + VALID_MIMETYPE_CHARS + "+)\\s*=\\s*(" + VALID_MIMETYPE_CHARS + "+)\\s*";
    private static final String MIME_REGEX_STR = "^\\s*((?<type>" + VALID_MIMETYPE_CHARS + "+|\\*)/(?<subtype>" + VALID_MIMETYPE_CHARS + "+|\\*)|(?<wildcard>\\*))"
            + "\\s*(" + MIME_PARAMS_REGEX_STR + ")*$";

    private static final Pattern MIME_REGEX = Pattern.compile(MIME_REGEX_STR);
    private static final Pattern MIME_PARAMS_REGEX = Pattern.compile(MIME_PARAMS_REGEX_STR);
    private static final long serialVersionUID = -6748129010939249437L;

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
        this.type = type;
        this.subType = "*";
    }

    public MimeType(final String type, final String subtype) {
        this.type = type;
        this.subType = subtype;
    }

    public MimeType(final String type, final String subtype, final Map<String, String> params) {
        this(type, subtype);
        this.params.putAll(params);
    }

    public String getType() {
        return type;
    }

    public String getSubType() {
        return subType;
    }

    /**
     * Parses a mime type including media range and returns a MediaType object. For example, the media range 'application/*;q=0.5' would get parsed into: ('application', '*', {'q',
     * '0.5'}).
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
                //Do nothing...
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

    public boolean matches(final String type, final String typeFromRange) {
        return type.equals(typeFromRange) || "*".equals(typeFromRange) || "*".equals(type);
    }

    public boolean matches(final MimeType range) {
        return range != null && matches(getType(), range.getType()) && matches(getSubType(), range.getSubType());
    }

    public boolean matchesHeader(final String header) {
        if (header == null || header.trim().isEmpty()) {
            return true;
        } else {
            return Pattern.compile(",").splitAsStream(header).filter(h -> matches(MimeType.parse(h))).findAny().isPresent();
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
        return matches(IMAGE_ANY_TYPE);
    }

    public boolean isVideo() {
        return matches(VIDEO_ANY_TYPE);
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
        s.append(SEPERATOR)
                .append(subType)
                .append(params.isEmpty() ? "" : ";")
                .append(params.entrySet()
                        .stream()
                        .map(e -> e.getKey() + "=" + e.getValue())
                        .collect(Collectors.joining(";")));
        return s.toString();
    }
}
