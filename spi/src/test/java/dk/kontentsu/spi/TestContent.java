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
package dk.kontentsu.spi;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

/**
 * Content implementation for test.
 */
public class TestContent implements ScopedContent {

    private final Integer id;
    private final UUID uuid;
    private final byte[] data;

    public TestContent(Integer id, String content) {
        this.id = id;
        this.uuid = UUID.randomUUID();
        this.data = content.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    @Override
    public int getSize() {
        return data.length;
    }

    @Override
    public InputStream getDataAsBinaryStream() {
        return new ByteArrayInputStream(data);
    }

    @Override
    public String getData() {
        return new String(data, StandardCharsets.UTF_8);
    }

    @Override
    public Optional<Charset> getEncoding() {
        return Optional.of(StandardCharsets.UTF_8);
    }

    @Override
    public String getHash() {
        throw new UnsupportedOperationException("Not supported");
    }

}
