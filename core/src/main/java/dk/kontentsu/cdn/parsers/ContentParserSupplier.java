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
package dk.kontentsu.cdn.parsers;

/**
 * Supplies a list of content parser valid for the give content.
 *
 * @author Jens Borch Christiansen
 */
//@Singleton
public class ContentParserSupplier {

    /*private static final Logger LOGGER = LoggerFactory.getLogger(ContentParserSupplier.class);

    @Inject
    private Instance<ContentParser.Factory> factories;

    @PostConstruct
    private void init() {
        factories.forEach(f -> LOGGER.info("Found content parser factory: {}", f.toString()));
    }

    public Set<ContentParser> get(final Content content) {
        Set<ContentParser> results = new HashSet<>();
        factories.forEach(f -> f.create(content).ifPresent(p -> results.add(p)));
        return results;
    }*/

}
