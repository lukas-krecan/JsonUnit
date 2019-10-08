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
package net.javacrumbs.jsonunit;

import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.ConfigurationWhen;
import net.javacrumbs.jsonunit.core.ConfigurationWhen.ApplicableForPath;
import net.javacrumbs.jsonunit.core.ConfigurationWhen.PathsParam;
import net.javacrumbs.jsonunit.core.Option;
import net.javacrumbs.jsonunit.core.internal.Diff;
import net.javacrumbs.jsonunit.core.internal.Node;
import net.javacrumbs.jsonunit.core.internal.Options;
import net.javacrumbs.jsonunit.core.internal.Path;
import net.javacrumbs.jsonunit.core.listener.DifferenceListener;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.math.BigDecimal;
import java.util.IdentityHashMap;
import java.util.Map;

import static net.javacrumbs.jsonunit.core.internal.Diff.createInternal;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.getNode;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.getPathPrefix;
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
        return new JsonPartMatcher<>(EMPTY_PATH, expected);
    }

    /**
     * Is the part of the JSON equivalent?
     */
    public static <T> ConfigurableJsonMatcher<T> jsonPartEquals(String path, Object expected) {
        return new JsonPartMatcher<>(path, expected);
    }

    /**
     * Applies matcher to the part of the JSON.
     */
    public static <T> Matcher<T> jsonPartMatches(String path, Matcher<?> matcher) {
        return new MatcherApplyingMatcher<>(path, matcher);
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
        return new JsonNodeAbsenceMatcher<>(path);
    }

    /**
     * Is the node in path present?
     */
    public static <T> ConfigurableJsonMatcher<T> jsonNodePresent(String path) {
        return new JsonNodePresenceMatcher<>(path);
    }

    private static abstract class AbstractMatcher<T> extends BaseMatcher<T> {
        final String path;
        Object actual;

        AbstractMatcher(String path) {
            this.path = path;
        }

        @Override
        public final boolean matches(Object item) {
            actual = item;
            return doMatch(item);
        }

        abstract boolean doMatch(Object item);

        Path getPath() {
            return Path.create(path, getPathPrefix(actual));
        }
    }

    private static abstract class AbstractJsonMatcher<T> extends AbstractMatcher<T> implements ConfigurableJsonMatcher<T> {

        Configuration configuration = JsonAssert.getConfiguration();


        AbstractJsonMatcher(String path) {
            super(path);
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

        @Override
        public final ConfigurableJsonMatcher<T> when(PathsParam object, ApplicableForPath... actions) {
            configuration = configuration.when(object, actions);
            return this;
        }

        /**
         * Adds a matcher to be used in ${json-unit.matches:matcherName} macro.
         */
        public ConfigurableJsonMatcher<T> withMatcher(String matcherName, Matcher<?> matcher) {
            configuration = configuration.withMatcher(matcherName, matcher);
            return this;
        }

        /**
         * Makes JsonUnit ignore the specified paths in the actual value. If the path matches,
         * it's completely ignored. It may be missing, null or have any value
         */
        public ConfigurableJsonMatcher<T> whenIgnoringPaths(String... paths) {
            configuration = configuration.whenIgnoringPaths(paths);
            return this;
        }

        public ConfigurableJsonMatcher<T> withDifferenceListener(DifferenceListener differenceListener) {
            configuration = configuration.withDifferenceListener(differenceListener);
            return this;
        }
    }

    private static final class JsonPartMatcher<T> extends AbstractJsonMatcher<T> {
        // IntelliJ integration is broken by default difference string. Hamcrest generates 'Expected:' and IntelliJ searches for last 'but was:' and everything between is taken as expected value
        private static final String HAMCREST_DIFFERENCE_STRING = "expected <%s> but was <%s>";
        private final Object expected;

        // One matcher can be used to match multiple array items. We need to persist diff description between doMatch() and
        // describeMismatch() method calls. While Hamcrest 1 called doMatch() and describeMismatch() one after each other
        // Hamcrest 2 calls doMatch() multiple times followed by multiple calls of describeMismatch()
        private final Map<Object, String> differences = new IdentityHashMap<>();

        JsonPartMatcher(String path, Object expected) {
            super(path);
            this.expected = expected;
        }

        boolean doMatch(Object item) {
            Diff diff = createInternal(expected, item, FULL_JSON,  Path.create(path, ""), configuration, HAMCREST_DIFFERENCE_STRING);
            if (!diff.similar()) {
                differences.put(item, diff.differences());
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
            description.appendText(differences.get(item));
        }
    }


    private static final class JsonNodeAbsenceMatcher<T> extends AbstractJsonMatcher<T> {
        JsonNodeAbsenceMatcher(String path) {
            super(path);
        }

        boolean doMatch(Object item) {
            return nodeAbsent(item, path, configuration);
        }

        public void describeTo(Description description) {
            description.appendText("Node \"" + getPath() + "\" is absent.");
        }

        @Override
        public void describeMismatch(Object item, Description description) {
            description.appendText("Node \"" + getPath() + "\" is \"" + getNode(item, path) + "\".");
        }
    }

    private static final class JsonNodePresenceMatcher<T> extends AbstractJsonMatcher<T> {
        JsonNodePresenceMatcher(String path) {
            super(path);
        }

        boolean doMatch(Object item) {
            return !nodeAbsent(item, getPath(), configuration);
        }

        public void describeTo(Description description) {
            description.appendText("Node \"" + getPath() + "\" is present.");
        }

        @Override
        public void describeMismatch(Object item, Description description) {
            description.appendText("Node \"" + getPath() + "\" is missing.");
        }
    }

    private static final class MatcherApplyingMatcher<T> extends AbstractMatcher<T> {
        private final Matcher<?> matcher;

        MatcherApplyingMatcher(String path, Matcher<?> matcher) {
            super(path);
            this.matcher = matcher;
        }

        boolean doMatch(Object item) {
            Node node = getNode(item, path);
            if (!node.isMissingNode()) {
                return matcher.matches(node.getValue());
            } else {
                // missing node does not match
                return false;
            }
        }

        public void describeTo(Description description) {
            description.appendText("node \"" + getPath() + "\" ");
            matcher.describeTo(description);
        }

        @Override
        public void describeMismatch(Object item, Description description) {
            Node node = getNode(item, path);
            if (!node.isMissingNode()) {
                super.describeMismatch(node.getValue(), description);
            } else {
                description.appendText("Node \"" + getPath() + "\" is missing.");
            }
        }
    }
}
