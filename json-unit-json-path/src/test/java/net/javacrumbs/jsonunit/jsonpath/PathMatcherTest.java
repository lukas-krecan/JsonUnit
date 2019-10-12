package net.javacrumbs.jsonunit.jsonpath;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PathMatcherTest {
    private final PathMatcher matcher = new PathMatcher();

    @Test
    void shouldNotMatchPaths() {
        assertThat(matcher.matches("a.b.d", "a.b.c")).isFalse();
    }

    @Test
    void shouldMatchSamePaths() {
        assertThat(matcher.matches("a.b.c", "$.a.b.c")).isTrue();
    }

    @Test
    void shouldMatchDoubleDot() {
        assertThat(matcher.matches("a.b.c", "$..c")).isTrue();
    }

    @Test
    void shouldMatchSameArrays() {
        assertThat(matcher.matches("a[0].c", "$.a[0].c")).isTrue();
    }

    @Test
    void shouldNotMatchDifferentArrays() {
        assertThat(matcher.matches("a[0].c", "$.a[1].c")).isFalse();
    }

    @Test
    void shouldMatchArraysSlice() {
        assertThat(matcher.matches("a[2].c", "$.a[1:2].c")).isTrue();
    }

    @Test
    void shouldNotMatchArraysOutsideSlice() {
        assertThat(matcher.matches("a[0].c", "$.a[1:2].c")).isTrue();
    }
}