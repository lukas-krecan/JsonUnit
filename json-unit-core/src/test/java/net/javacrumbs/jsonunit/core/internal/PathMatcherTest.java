/**
 * Copyright 2009-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.javacrumbs.jsonunit.core.internal;

import org.junit.jupiter.api.Test;

import static net.javacrumbs.jsonunit.core.internal.Node.MISSING_NODE;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


class PathMatcherTest {

    @Test
    void shouldMatchExactly() {
        assertTrue(create("path.other").matches("path.other"));
        assertFalse(create("path.other").matches("path.other2"));
    }

    @Test
    void shouldMatchExactlyArray() {
        assertTrue(create("path.other[1].next").matches("path.other[1].next"));
        assertFalse(create("path.other[1].next").matches("path.other[2].next"));
    }

    @Test
    void shouldMatchExactlyWithFalseWildcards() {
        assertTrue(create("*.\\d").matches("*.\\d"));
        assertFalse(create("*.\\d").matches("*.1"));
    }

    @Test
    void shouldMatchWithArrayWildcard() {
        assertTrue(create("root.*.array[*].next").matches("root.*.array[2].next"));
        assertFalse(create("root.*.array[*].next").matches("root.x.array[2].next"));
        assertFalse(create("root.*.array[*].next").matches("root.*.array[2].next2"));
    }

    @Test
    void shouldMatchWithArrayWildcardWice() {
        assertTrue(create("root.*.array[*].next[*]").matches("root.*.array[2].next[1]"));
        assertFalse(create("root.*.array[*].next[*]").matches("root.x.array[2].next[1]"));
    }

    private PathMatcher create(String s) {
        return PathMatcher.create(s);
    }
}