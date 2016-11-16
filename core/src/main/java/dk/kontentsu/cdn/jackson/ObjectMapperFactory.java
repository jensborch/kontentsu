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
package dk.kontentsu.cdn.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import dk.kontentsu.cdn.model.MimeType;
import dk.kontentsu.cdn.model.SemanticUri;

/**
 *
 * @author Jens Borch Christiansen
 */
public final class ObjectMapperFactory {

    private ObjectMapperFactory() {
    }

    /**
     * Get standard JSON object mapper configuration, for e.g. manual usage.
     *
     * @return a configured object mapper.
     */
    public static ObjectMapper create() {
        final ObjectMapper result = new ObjectMapper();
        result.configure(SerializationFeature.INDENT_OUTPUT, true);
        result.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        result.registerModule(new Jdk8Module());

        SimpleModule mod = new SimpleModule("Mime type module");
        mod.addSerializer(new MimeTypeSerializer());
        mod.addDeserializer(MimeType.class, new MimeTypeDeserializer());
        result.registerModule(mod);

        mod = new SimpleModule("Semantic URI type module");
        mod.addSerializer(new SemanticUriSerializer());
        mod.addDeserializer(SemanticUri.class, new SemanticUriDeserializer());
        result.registerModule(mod);

        mod = new SimpleModule("Link module");
        mod.addSerializer(new LinkSerializer());
        result.registerModule(mod);

        result.setAnnotationIntrospector(new JacksonAnnotationIntrospector());

        return result;
    }

}
