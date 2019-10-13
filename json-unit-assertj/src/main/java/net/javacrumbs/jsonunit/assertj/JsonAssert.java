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
package net.javacrumbs.jsonunit.assertj;

import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.ConfigurationWhen.ApplicableForPath;
import net.javacrumbs.jsonunit.core.ConfigurationWhen.PathsParam;
import net.javacrumbs.jsonunit.core.Option;
import net.javacrumbs.jsonunit.core.internal.Diff;
import net.javacrumbs.jsonunit.core.internal.JsonUtils;
import net.javacrumbs.jsonunit.core.internal.Node;
import net.javacrumbs.jsonunit.core.internal.Options;
import net.javacrumbs.jsonunit.core.internal.Path;
import net.javacrumbs.jsonunit.core.internal.matchers.InternalMatcher;
import net.javacrumbs.jsonunit.core.listener.DifferenceListener;
import net.javacrumbs.jsonunit.jsonpath.JsonPathAdapter;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractStringAssert;
import org.assertj.core.api.BigDecimalAssert;
import org.assertj.core.api.BooleanAssert;
import org.assertj.core.api.ListAssert;
import org.assertj.core.api.MapAssert;
import org.assertj.core.api.StringAssert;
import org.assertj.core.description.Description;
import org.assertj.core.error.MessageFormatter;
import org.assertj.core.internal.Failures;
import org.hamcrest.Matcher;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static net.javacrumbs.jsonunit.core.internal.Diff.quoteTextValue;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.getNode;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.getPathPrefix;
import static net.javacrumbs.jsonunit.core.internal.Node.NodeType.ARRAY;
import static net.javacrumbs.jsonunit.core.internal.Node.NodeType.BOOLEAN;
import static net.javacrumbs.jsonunit.core.internal.Node.NodeType.NULL;
import static net.javacrumbs.jsonunit.core.internal.Node.NodeType.NUMBER;
import static net.javacrumbs.jsonunit.core.internal.Node.NodeType.OBJECT;
import static net.javacrumbs.jsonunit.core.internal.Node.NodeType.STRING;
import static net.javacrumbs.jsonunit.jsonpath.InternalJsonPathUtils.resolveJsonPaths;
import static org.assertj.core.util.Strings.isNullOrEmpty;

public class JsonAssert extends AbstractAssert<JsonAssert, Object> {
    final Path path;
    final Configuration configuration;
    private final InternalMatcher internalMatcher;

    JsonAssert(Path path, Configuration configuration, Object actual) {
        super(JsonUtils.convertToJson(actual, "actual"), JsonAssert.class);
        this.path = path;
        this.configuration = configuration;
        this.internalMatcher = new InternalMatcher(actual, path.asPrefix(), "", configuration);
        usingComparator(new JsonComparator(configuration, path, false));
    }

    JsonAssert(Object actual, Configuration configuration) {
        this(Path.create("", getPathPrefix(actual)), configuration, actual);
    }

    /**
     * Moves comparison to given node. Second call navigates from the last position in the JSON.
     */
    public JsonAssert node(String node) {
        return new JsonAssert(path.to(node), configuration, getNode(actual, node));
    }


    /**
     * Allows to do multiple comparisons on a document like
     *
     * <code>
     *     assertThatJson("{\"test\":{\"a\":1, \"b\":2, \"c\":3}}").and(
     *         a -&gt; a.node("test").isObject(),
     *         a -&gt; a.node("test.b").isEqualTo(3)
     *     );
     * </code>
     */
    public JsonAssert and(JsonAssertion... assertions) {
        Arrays.stream(assertions).forEach(a -> a.doAssert(this));
        return this;
    }

    /**
     * Compares JSONs.
     */
    @Override
    public JsonAssert isEqualTo(Object expected) {
        Diff diff = Diff.create(expected, actual, "fullJson", path.asPrefix(), configuration);

        String overridingErrorMessage = info.overridingErrorMessage();
        if (!isNullOrEmpty(overridingErrorMessage) && !diff.similar()) {
            failWithMessage(overridingErrorMessage);
        } else {
            diff.failIfDifferent(MessageFormatter.instance().format(info.description(), info.representation(), ""));
        }
        return this;
    }

    /**
     * Copy from AssertJ to prevent errors with percents in error message
     */
    private void failWithMessage(String errorMessage) {
      AssertionError assertionError = Failures.instance().failureIfErrorMessageIsOverridden(info);
      if (assertionError == null) {
        // error message was not overridden, build it.
        String description = MessageFormatter.instance().format(info.description(), info.representation(), "");
        assertionError = new AssertionError(description + errorMessage);
      }
      Failures.instance().removeAssertJRelatedElementsFromStackTraceIfNeeded(assertionError);
      throw assertionError;
    }

    /**
     * Asserts that given node is present and is of type object.
     *
     * @return MapAssert where the object is serialized as Map
     */
    public MapAssert<String, Object> isObject() {
        Node node = assertType(OBJECT);
        return new JsonMapAssert((Map<String, Object>) node.getValue(), path.asPrefix(), configuration)
            .as("Different value found in node \"%s\"", path);
    }

    /**
     * Asserts that given node is present and is of type number.
     *
     * @return
     */
    public BigDecimalAssert isNumber() {
        Node node = assertType(NUMBER);
        return createBigDecimalAssert(node.decimalValue());
    }

    /**
     * Asserts that given node is present and is of type number or a string that can be parsed as a number.
     */
    public BigDecimalAssert asNumber() {
        internalMatcher.isPresent(NUMBER.getDescription());
        Node node = getNode(actual, "");
        if (node.getNodeType() == NUMBER) {
            return createBigDecimalAssert(node.decimalValue());
        } else if (node.getNodeType() == STRING) {
            try {
                return createBigDecimalAssert(new BigDecimal(node.asText()));
            } catch (NumberFormatException e) {
                failWithMessage("Node \"" + path + "\" can not be converted to number expected: <a number> but was: <" + quoteTextValue(node.getValue()) + ">.");
            }
        } else {
            internalMatcher.failOnType(node, "number or string");
        }
        return null;
    }

    private BigDecimalAssert createBigDecimalAssert(BigDecimal value) {
        return new BigDecimalAssert(value).as("Different value found in node \"%s\"", path);
    }

    /**
     * Asserts that given node is present and is of type array.
     *
     * @return
     */
    public ListAssert<Object> isArray() {
        Node node = assertType(ARRAY);
        return new JsonListAssert((List<?>)node.getValue(), path.asPrefix(), configuration)
            .as("Different value found in node \"%s\"", path);
    }

    /**
     * Asserts that given node is present and is of type boolean.
     *
     * @return
     */
    public BooleanAssert isBoolean() {
        Node node = assertType(BOOLEAN);
        return new BooleanAssert((Boolean) node.getValue()).as("Different value found in node \"%s\"", path);
    }

    /**
     * Asserts that given node is present and is of type string.
     *
     * @return
     */
    public StringAssert isString() {
        Node node = assertType(STRING);
        return new StringAssert((String) node.getValue()).as("Different value found in node \"%s\"", path);
    }

    @Override
    public AbstractStringAssert<?> asString() {
        return isString();
    }

    /**
     * Asserts that given node is present and is null.
     * @return
     */
    @Override
    public void isNull() {
        assertType(NULL);
    }

    /**
     * Asserts that given node is present.
     *
     * @return
     */
    public JsonAssert isPresent() {
        internalMatcher.isPresent();
        return this;
    }

    /**
     * Asserts that given node is absent.
     *
     * @return
     */
    public void isAbsent() {
        internalMatcher.isAbsent();
    }

    /**
     * Asserts that given node is present and is not null.
     * @return
     */
    @Override
    public JsonAssert isNotNull() {
        internalMatcher.isNotNull();
        return this;
    }

    private Node assertType(Node.NodeType type) {
        return internalMatcher.assertType(type);
    }

    /**
     * JsonAssert that can be configured to prevent mistakes like
     *
     * <code>
     * assertThatJson(...).isEqualsTo(...).when(...);
     * </code>
     */
    public static class ConfigurableJsonAssert extends JsonAssert {
        // Want to pass to inPath to not parse twice.
        private final Object originalActual;

        ConfigurableJsonAssert(Path path, Configuration configuration, Object actual) {
            super(path, configuration, actual);
            this.originalActual = actual;
        }

        ConfigurableJsonAssert(Object actual, Configuration configuration) {
            this(Path.create("", getPathPrefix(actual)), configuration, actual);
        }

        /**
         * Adds comparison options.
         */
        public ConfigurableJsonAssert when(Option first, Option... other) {
            return withConfiguration(c -> c.when(first, other));
        }

        /**
         * Adds path specific options.
         *
         * @see Configuration#when(PathsParam, ApplicableForPath...)
         */
        public final ConfigurableJsonAssert when(PathsParam object, ApplicableForPath... actions) {
            return withConfiguration(c -> c.when(object, actions));
        }

        /**
         * Sets comparison options.
         *
         * @param options
         * @return
         */
        public ConfigurableJsonAssert withOptions(Options options) {
            return withConfiguration(c -> c.withOptions(options));
        }

        /**
         * Allows to configure like this
         *
         * <code>
         *     assertThatJson(...)
         *             .withConfiguration(c -&gt; c.withMatcher("positive", greaterThan(valueOf(0)))
         *             ....
         * </code>
         */
        public ConfigurableJsonAssert withConfiguration(Function<Configuration, Configuration> configurationFunction) {
            Configuration newConfiguration = configurationFunction.apply(configuration);
            newConfiguration = resolveJsonPaths(originalActual, newConfiguration);
            return new ConfigurableJsonAssert(path, newConfiguration, actual);
        }

        /**
         * Sets numerical comparison tolerance.
         *
         * @param tolerance
         * @return
         */
        public ConfigurableJsonAssert withTolerance(BigDecimal tolerance) {
            return withConfiguration(c -> c.withTolerance(tolerance));
        }

        /**
         * Sets numerical comparison tolerance.
         *
         * @param tolerance
         * @return
         */
        public ConfigurableJsonAssert withTolerance(double tolerance) {
            return withTolerance(BigDecimal.valueOf(tolerance));
        }

        /**
         * Makes JsonUnit ignore the specified paths in the actual value. If the path matches,
         * it's completely ignored. It may be missing, null or have any value
         *
         * @param pathsToBeIgnored
         * @return
         */
        public ConfigurableJsonAssert whenIgnoringPaths(String... pathsToBeIgnored) {
            return withConfiguration(c -> c.whenIgnoringPaths(pathsToBeIgnored));
        }

        /**
         * Sets ignore placeholder.
         *
         * @param ignorePlaceholder
         * @return
         */
        public ConfigurableJsonAssert withIgnorePlaceholder(String ignorePlaceholder) {
            return withConfiguration(c -> c.withIgnorePlaceholder(ignorePlaceholder));
        }

        /**
         * Adds a matcher to be used in ${json-unit.matches:matcherName} macro.
         *
         * @param matcherName
         * @param matcher
         * @return
         */
        public ConfigurableJsonAssert withMatcher(String matcherName, Matcher<?> matcher) {
            return withConfiguration(c -> c.withMatcher(matcherName, matcher));
        }


        /**
         * Sets difference listener
         */
        public ConfigurableJsonAssert withDifferenceListener(DifferenceListener differenceListener) {
            return withConfiguration(c -> c.withDifferenceListener(differenceListener));
        }

        public JsonAssert inPath(String jsonPath) {
            return new JsonAssert(JsonPathAdapter.inPath(originalActual, jsonPath), configuration);
        }

        // Following methods are here just to return ConfigurableJsonAssert instead of JsonAssert
        @Override
        public ConfigurableJsonAssert describedAs(Description description) {
            return (ConfigurableJsonAssert) super.describedAs(description);
        }

        @Override
        public ConfigurableJsonAssert describedAs(String description, Object... args) {
            return (ConfigurableJsonAssert) super.describedAs(description, args);
        }

        @Override
        public ConfigurableJsonAssert as(Description description) {
            return (ConfigurableJsonAssert) super.as(description);
        }

        @Override
        public ConfigurableJsonAssert as(String description, Object... args) {
            return (ConfigurableJsonAssert) super.as(description, args);
        }
    }
}
