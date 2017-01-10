/**
 * Copyright 2009-2017 the original author or authors.
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

import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.Option;
import net.javacrumbs.jsonunit.core.internal.Diff;
import net.javacrumbs.jsonunit.core.internal.Node;
import net.javacrumbs.jsonunit.core.internal.Options;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.math.BigDecimal;

import static net.javacrumbs.jsonunit.core.internal.Diff.create;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.getNode;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.nodeAbsent;

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

    private static final String EMPTY_PATH = "";
    private static final String FULL_JSON = "fullJson";

    /**
     * Are the JSONs equivalent?
     */
    public static <T> ConfigurableJsonMatcher<T> jsonEquals(Object expected) {
        return new JsonPartMatcher<T>(EMPTY_PATH, expected);
    }

    /**
     * Is the part of the JSON equivalent?
     */
    public static <T> ConfigurableJsonMatcher<T> jsonPartEquals(String path, Object expected) {
        return new JsonPartMatcher<T>(path, expected);
    }

    /**
     * Applies matcher to the part of the JSON.
     */
    public static <T> Matcher<T> jsonPartMatches(String path, Matcher<?> matcher) {
        return new MatcherApplyingMatcher<T>(path, matcher);
    }

    /**
     * Are the JSONs equivalent?
     * <p/>
     * This method exist only for those cases, when you need to use it as Matcher&lt;String&gt; and Java refuses to
     * do the type inference correctly.
     */
    public static ConfigurableJsonMatcher<String> jsonStringEquals(Object expected) {
        return jsonEquals(expected);
    }

    /**
     * Is the part of the JSON equivalent?
     * <p/>
     * This method exist only for those cases, when you need to use it as Matcher&lt;String&gt; and Java refuses to
     * do the type inference correctly.
     */
    public static ConfigurableJsonMatcher<String> jsonStringPartEquals(String path, Object expected) {
        return jsonPartEquals(path, expected);
    }

    /**
     * Is the node in path absent?
     */
    public static <T> ConfigurableJsonMatcher<T> jsonNodeAbsent(String path) {
        return new JsonNodeAbsenceMatcher<T>(path);
    }

    /**
     * Is the node in path present?
     */
    public static <T> ConfigurableJsonMatcher<T> jsonNodePresent(String path) {
        return new JsonNodePresenceMatcher<T>(path);
    }

    private static abstract class AbstractJsonMatcher<T> extends BaseMatcher<T> implements ConfigurableJsonMatcher<T> {
        protected final String path;
        protected Configuration configuration = JsonAssert.getConfiguration();

        public AbstractJsonMatcher(String path) {
            this.path = path;
        }

        public ConfigurableJsonMatcher<T> withTolerance(BigDecimal tolerance) {
            configuration = configuration.withTolerance(tolerance);
            return this;
        }

        public ConfigurableJsonMatcher<T> withTolerance(double tolerance) {
            configuration = configuration.withTolerance(tolerance);
            return this;
        }

        public ConfigurableJsonMatcher<T> when(Option first, Option... next) {
            configuration = configuration.withOptions(first, next);
            return this;
        }

        public ConfigurableJsonMatcher<T> withOptions(Options options) {
            configuration = configuration.withOptions(options);
            return this;
        }
    }

    private static final class JsonPartMatcher<T> extends AbstractJsonMatcher<T> {
        private final Object expected;
        private String differences;

        JsonPartMatcher(String path, Object expected) {
            super(path);
            this.expected = expected;
        }

        public boolean matches(Object item) {
            Diff diff = create(expected, item, FULL_JSON, path, configuration);
            if (!diff.similar()) {
                differences = diff.differences();
            }
            return diff.similar();
        }

        public void describeTo(Description description) {
            if (EMPTY_PATH.equals(path)) {
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


    private static final class JsonNodeAbsenceMatcher<T> extends AbstractJsonMatcher<T> {
        JsonNodeAbsenceMatcher(String path) {
            super(path);
        }

        public boolean matches(Object item) {
            return nodeAbsent(item, path, configuration);
        }

        public void describeTo(Description description) {
            description.appendText("Node \"" + path + "\" is absent.");
        }

        @Override
        public void describeMismatch(Object item, Description description) {
            description.appendText("Node \"" + path + "\" is \"" + getNode(item, path) + "\".");
        }
    }

    private static final class JsonNodePresenceMatcher<T> extends AbstractJsonMatcher<T> {
        JsonNodePresenceMatcher(String path) {
            super(path);
        }

        public boolean matches(Object item) {
            return !nodeAbsent(item, path, configuration);
        }

        public void describeTo(Description description) {
            description.appendText("Node \"" + path + "\" is present.");
        }

        @Override
        public void describeMismatch(Object item, Description description) {
            description.appendText("Node \"" + path + "\" is missing.");
        }
    }

    private static final class MatcherApplyingMatcher<T> extends BaseMatcher<T> {
        private final String path;
        private final Matcher<?> matcher;

        public MatcherApplyingMatcher(String path, Matcher<?> matcher) {
            this.path = path;
            this.matcher = matcher;
        }

        public boolean matches(Object item) {
            Node node = getNode(item, path);
            if (!node.isMissingNode()) {
                return matcher.matches(node.getValue());
            } else {
                // missing node does not match
                return false;
            }
        }

        public void describeTo(Description description) {
            description.appendText("node \"" + path + "\" ");
            matcher.describeTo(description);
        }

        @Override
        public void describeMismatch(Object item, Description description) {
            Node node = getNode(item, path);
            if (!node.isMissingNode()) {
                super.describeMismatch(node.getValue(), description);
            } else {
                description.appendText("Node \"" + path + "\" is missing.");
            }
        }
    }
}
