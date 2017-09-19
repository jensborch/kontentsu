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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Spliterator for streaming the results of a regular expression group match.
 *
 * @author Jens Borch Christiansen
 */
public class MatcherSpliterator extends AbstractSpliterator<String[]> {

    private final Matcher matcher;
    private final List<Integer> groups;
    //private final boolean matches;

    public MatcherSpliterator(final Matcher matcher) {
        super(Long.MAX_VALUE, NONNULL | ORDERED | IMMUTABLE);
        this.matcher = matcher;
        this.groups = null;
        //this.matches = matcher.matches();
    }

    public MatcherSpliterator(final Matcher matcher, Integer... groups) {
        super(Long.MAX_VALUE, NONNULL | ORDERED | IMMUTABLE);
        this.matcher = matcher;
        this.groups = Arrays.asList(groups);
        //this.matches = matcher.matches();
    }

    @Override
    public boolean tryAdvance(final Consumer<? super String[]> action) {
        boolean found = matcher.find();
        if (found) {
            final String[] gs = new String[matcher.groupCount() + 1];
            for (int i = 0; i <= matcher.groupCount(); i++) {
                if (this.groups == null || this.groups.contains(i)) {
                    gs[i] = matcher.group(i);
                }
            }
            action.accept(gs);
        }
        return found;
    }

    public static Stream<String[]> stream(final Matcher matcher) {
        return StreamSupport.stream(new MatcherSpliterator(matcher), false);
    }

    public static Stream<String[]> stream(final Matcher matcher, Integer... groups) {
        return StreamSupport.stream(new MatcherSpliterator(matcher, groups), false);
    }
}
