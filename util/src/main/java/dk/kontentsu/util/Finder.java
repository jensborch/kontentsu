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
package dk.kontentsu.util;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Recursively search for something matching a predicate.'
 *
 * @author Jens Borch Christiansen
 */
public class Finder<T> {

    private final Predicate<T> predicate;
    private Function<T, T> finder;

    public Finder(final Predicate<T> predicate, final Function<T, T> finder) {
        this.predicate = predicate;
        this.finder = finder;
    }

    public Optional<T> find(final T t) {
        T next = finder.apply(t);
        return t != null && predicate.test(t) ?
                Optional.of(t) :
                t == null || next == null ? Optional.empty() : find(next);
    }
}
