/**
 * Copyright 2009-2013 the original author or authors.
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
package net.javacrumbs.jsonunit;

import net.javacrumbs.jsonunit.core.internal.Diff;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import static net.javacrumbs.jsonunit.core.internal.Diff.create;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.getNode;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.nodeExists;

/**
 * Contains Hamcrest matchers to be used with Hamcrest assertThat and other tools.
 * <p/>
 * All the methods accept Objects as parameters. The supported types are:
 * <ol>
 * <li>Jackson JsonNode</li>
 * <li>Numbers, booleans and any other type parseable by Jackson's ObjectMapper.convertValue</li>
 * <li>String is parsed as JSON. For expected values the string is quoted if it contains obviously invalid JSON.</li>
 * <li>{@link java.io.Reader} similarly to String</li>
 * <li>null as null Node</li>
 * </ol>
 */
public class JsonMatchers {
    /**
     * Are the JSONs equivalent?
     *
     * @param expected
     * @return
     */
    public static <T> Matcher<T> jsonEquals(Object expected) {
        return new JsonPartMatcher<T>("", expected);
    }

    /**
     * Is the part of the JSON equivalent?
     *
     * @param expected
     * @return
     */
    public static <T> Matcher<T> jsonPartEquals(String path, Object expected) {
        return new JsonPartMatcher<T>(path, expected);
    }

    /**
     * Are the JSONs equivalent?
     * <p/>
     * This method exist only for those cases, when you need to use it as Matcher&lt;String&gt; and Java refuses to
     * do the type inference correctly.
     *
     * @param expected
     * @return
     */
    public static Matcher<String> jsonStringEquals(Object expected) {
        return jsonEquals(expected);
    }

    /**
     * Is the part of the JSON equivalent?
     * <p/>
     * This method exist only for those cases, when you need to use it as Matcher&lt;String&gt; and Java refuses to
     * do the type inference correctly.
     *
     * @param expected
     * @return
     */
    public static Matcher<String> jsonStringPartEquals(String path, Object expected) {
        return jsonPartEquals(path, expected);
    }

    /**
     * Is the node in path absent?
     *
     * @param path
     * @return
     */
    public static <T> Matcher<T> jsonNodeAbsent(String path) {
        return new JsonNodeAbsenceMatcher<T>(path);
    }
    /**
     * Is the node in path present?
     *
     * @param path
     * @return
     */
    public static <T> Matcher<T> jsonNodePresent(String path) {
        return new JsonNodePresenceMatcher<T>(path);
    }


    private static final class JsonPartMatcher<T> extends BaseMatcher<T> {
        private final Object expected;
        private final String path;
        private String differences;

        JsonPartMatcher(String path, Object expected) {
            this.expected = expected;
            this.path = path;
        }

        public boolean matches(Object item) {
            Diff diff = create(expected, item, "fullJson", path, JsonAssert.getIgnorePlaceholder(), JsonAssert.getTolerance(), JsonAssert.getTreatNullAsAbsent());
            if (!diff.similar()) {
                differences = diff.differences();
            }
            return diff.similar();
        }

        public void describeTo(Description description) {
            if ("".equals(path)) {
                description.appendText(safeToString());
            } else {
                description.appendText(safeToString()).appendText(" in \"").appendText(path).appendText("\"");
            }
        }

        private String safeToString() {
            return expected != null ? expected.toString() : "null";
        }

        @Override
        public void describeMismatch(Object item, Description description) {
            description.appendText(differences);
        }
    }

    private static final class JsonNodeAbsenceMatcher<T> extends BaseMatcher<T> {
        private final String path;

        JsonNodeAbsenceMatcher(String path) {
            this.path = path;
        }

        public boolean matches(Object item) {
            return !nodeExists(item, path);
        }

        public void describeTo(Description description) {
            description.appendText("Node \"" + path + "\" is absent.");
        }

        @Override
        public void describeMismatch(Object item, Description description) {
            description.appendText("Node \"" + path + "\" is \"" + getNode(item, path) + "\".");
        }
    }

    private static final class JsonNodePresenceMatcher<T> extends BaseMatcher<T> {
        private final String path;

        JsonNodePresenceMatcher(String path) {
            this.path = path;
        }

        public boolean matches(Object item) {
            return nodeExists(item, path);
        }

        public void describeTo(Description description) {
            description.appendText("Node \"" + path + "\" is present.");
        }

        @Override
        public void describeMismatch(Object item, Description description) {
            description.appendText("Node \"" + path + "\" is missing.");
        }
    }
}
