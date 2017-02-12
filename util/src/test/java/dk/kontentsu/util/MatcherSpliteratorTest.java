package dk.kontentsu.util;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.Test;

/**
 * Test for {@link MatcherSpliterator}.
 */
public class MatcherSpliteratorTest {

    @Test
    public void testSpliterator() {
        Pattern p = Pattern.compile("((.*?)/)+?");
        Matcher m = p.matcher("aaaa/bb/c/ddd/");

        List<String[]> result = MatcherSpliterator.stream(m)
                .collect(Collectors.toList());

        assertEquals("aaaa", result.get(0)[2]);
        assertEquals("bb", result.get(1)[2]);
        assertEquals("c", result.get(2)[2]);
        assertEquals("ddd", result.get(3)[2]);
    }

}
