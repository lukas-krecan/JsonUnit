package net.javacrumbs.jsonunit.core.internal;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PathMatcherTest {

    @Test
    public void shouldMatchExactly() {
        assertTrue(PathMatcher.create("path.other").matches("path.other"));
        assertFalse(PathMatcher.create("path.other").matches("path.other2"));
    }

    @Test
    public void shouldMatchExactlyArray() {
        assertTrue(PathMatcher.create("path.other[1].next").matches("path.other[1].next"));
        assertFalse(PathMatcher.create("path.other[1].next").matches("path.other[2].next"));
    }

    @Test
    public void shouldMatchExactlyWithFalseWildcards() {
        assertTrue(PathMatcher.create("*.\\d").matches("*.\\d"));
        assertFalse(PathMatcher.create("*.\\d").matches("*.1"));
    }

    @Test
    public void shouldMatchWithArrayWildcard() {
        assertTrue(PathMatcher.create("root.*.array[*].next").matches("root.*.array[2].next"));
        assertFalse(PathMatcher.create("root.*.array[*].next").matches("root.x.array[2].next"));
        assertFalse(PathMatcher.create("root.*.array[*].next").matches("root.*.array[2].next2"));
    }

    @Test
    public void shouldMatchWithArrayWildcardWice() {
        assertTrue(PathMatcher.create("root.*.array[*].next[*]").matches("root.*.array[2].next[1]"));
        assertFalse(PathMatcher.create("root.*.array[*].next[*]").matches("root.x.array[2].next[1]"));
    }
}