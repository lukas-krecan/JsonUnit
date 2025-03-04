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
import static org.assertj.core.description.Description.mostRelevantDescription;
import static org.assertj.core.util.Strings.isNullOrEmpty;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.ConfigurationWhen.ApplicableForPath;
import net.javacrumbs.jsonunit.core.ConfigurationWhen.PathsParam;
import net.javacrumbs.jsonunit.core.Option;
import net.javacrumbs.jsonunit.core.internal.Diff;
import net.javacrumbs.jsonunit.core.internal.JsonUtils;
import net.javacrumbs.jsonunit.core.internal.Node;
import net.javacrumbs.jsonunit.core.internal.Path;
import net.javacrumbs.jsonunit.core.internal.matchers.InternalMatcher;
import net.javacrumbs.jsonunit.core.listener.DifferenceListener;
import net.javacrumbs.jsonunit.jsonpath.JsonPathAdapter;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractStringAssert;
import org.assertj.core.api.BigDecimalAssert;
import org.assertj.core.api.BigIntegerAssert;
import org.assertj.core.api.BooleanAssert;
import org.assertj.core.api.InstanceOfAssertFactory;
import org.assertj.core.api.StringAssert;
import org.assertj.core.api.UriAssert;
import org.assertj.core.description.Description;
import org.assertj.core.error.MessageFormatter;
import org.hamcrest.Matcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JsonAssert extends AbstractAssert<JsonAssert, Object> {
    final Path path;
    final Configuration configuration;
    private final Object actualForMatcher;

    JsonAssert(Path path, Configuration configuration, Object actual, boolean alreadyParsed) {
        super(
                alreadyParsed ? JsonUtils.wrapDeserializedObject(actual) : JsonUtils.convertToJson(actual, "actual"),
                JsonAssert.class);
        this.path = path;
        this.configuration = configuration;
        this.actualForMatcher = alreadyParsed ? JsonUtils.wrapDeserializedObject(actual) : actual;
        usingComparator(new JsonComparator(configuration, path, false));
    }

    JsonAssert(Path path, Configuration configuration, Object actual) {
        this(path, configuration, actual, false);
    }

    JsonAssert(Object actual, Configuration configuration) {
        this(Path.create("", getPathPrefix(actual)), configuration, actual);
    }

    /**
     * Moves comparison to given node. Second call navigates from the last position in the JSON.
     */
    @NotNull
    public JsonAssert node(@NotNull String node) {
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
    @NotNull
    public JsonAssert and(@NotNull JsonAssertion... assertions) {
        Arrays.stream(assertions).forEach(a -> a.doAssert(this));
        return this;
    }

    /**
     * Compares JSONs. If <code>expected</code> is String it's first parsed as JSON. If it can't be
     * parsed as JSON, it is treated as a String. If you want to disambiguate, specify the type first
     * using <code>isString()</code>, <code>isNumber()</code> etc.
     * Examples:
     * <ul>
     *     <li><code>isEqualTo("{\"a\":1}")</code> is parsed as JSON</li>
     *     <li><code>isEqualTo("1")</code> is parsed as number 1</li>
     *     <li><code>isEqualTo("true")</code> is parsed as boolean</li>
     *     <li><code>isEqualTo("\"1\"")</code> is parsed as String "1"</li>
     *     <li><code>isEqualTo("a")</code> is parsed as String "a"</li>
     * </ul>
     */
    @Override
    @NotNull
    public JsonAssert isEqualTo(@Nullable Object expected) {
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
     * Assert that the value is string and checks for equality. A shortcut for <code>isString().isEqualTo(expected)</code>
     */
    public StringAssert isStringEqualTo(@NotNull String expected) {
        return isString().isEqualTo(expected);
    }

    /**
     * Asserts that given node is present and is of type object.
     *
     * @return MapAssert where the object is serialized as Map
     */
    @NotNull
    @SuppressWarnings("unchecked")
    public JsonMapAssert isObject() {
        Node node = assertType(OBJECT);
        return describe(new JsonMapAssert((Map<String, Object>) node.getValue(), path.asPrefix(), configuration));
    }

    /**
     * Asserts that given node is present and is of type number.
     */
    @NotNull
    public BigDecimalAssert isNumber() {
        Node node = assertType(NUMBER);
        return createBigDecimalAssert(node.decimalValue());
    }

    /**
     * Asserts that the value is an integer. 1 is an integer 1.0, 1.1, 1e3, 1e0, 1e-3 is not.
     */
    public BigIntegerAssert isIntegralNumber() {
        Node node = internalMatcher().assertIntegralNumber();
        return describe(new BigIntegerAssert(node.decimalValue().toBigIntegerExact()));
    }

    /**
     * Asserts that given node is present and is of type number or a string that can be parsed as a number.
     */
    @NotNull
    public BigDecimalAssert asNumber() {
        internalMatcher().isPresent(NUMBER.getDescription());
        Node node = getNode(actual, "");
        if (node.getNodeType() == NUMBER) {
            return createBigDecimalAssert(node.decimalValue());
        } else if (node.getNodeType() == STRING) {
            try {
                return createBigDecimalAssert(new BigDecimal(node.asText()));
            } catch (NumberFormatException e) {
                failWithMessage("Node \"" + path + "\" can not be converted to number expected: <a number> but was: <"
                        + quoteTextValue(node.getValue()) + ">.");
            }
        } else {
            internalMatcher().failOnType(node, "number or string");
        }
        //noinspection DataFlowIssue
        return null;
    }

    private BigDecimalAssert createBigDecimalAssert(BigDecimal value) {
        return describe(new BigDecimalAssert(value));
    }

    private InternalMatcher internalMatcher() {
        String description = mostRelevantDescription(info.description(), "Node \"" + path + "\"");
        return new InternalMatcher(actualForMatcher, path.asPrefix(), "", configuration, description);
    }

    /**
     * Asserts that given node is present and is of type array.
     */
    @NotNull
    public JsonListAssert isArray() {
        Node node = assertType(ARRAY);
        return createListAssert(node).as("Node \"%s\"", path);
    }

    private @NotNull JsonListAssert createListAssert(Node node) {
        return new JsonListAssert((List<?>) node.getValue(), path.asPrefix(), configuration);
    }

    /**
     * Asserts that given node is present and is of type boolean.
     */
    @NotNull
    public BooleanAssert isBoolean() {
        Node node = assertType(BOOLEAN);
        return createBooleanAssert(node);
    }

    private BooleanAssert createBooleanAssert(Node node) {
        return describe(new BooleanAssert((Boolean) node.getValue()));
    }

    /**
     * Asserts that given node is present and is of type string.
     */
    @NotNull
    public StringAssert isString() {
        Node node = assertType(STRING);
        return createStringAssert(node);
    }

    private StringAssert createStringAssert(Node node) {
        return describe(new StringAssert((String) node.getValue()));
    }

    private <T extends AbstractAssert<T, ?>> T describe(T ass) {
        return ass.as("Different value found in node \"%s\"", path);
    }

    @Override
    @NotNull
    public AbstractStringAssert<?> asString() {
        return isString();
    }

    /**
     * Asserts that given node is present and is null.
     */
    @Override
    public void isNull() {
        assertType(NULL);
    }

    /**
     * Asserts that given node is present and is URI.
     */
    @NotNull
    public UriAssert isUri() {
        Node node = assertType(STRING);
        return describe(new UriAssert(URI.create((String) node.getValue())));
    }

    /**
     * Asserts that given node is present.
     */
    @NotNull
    public JsonAssert isPresent() {
        internalMatcher().isPresent();
        return this;
    }

    /**
     * Asserts that given node is absent.
     */
    public void isAbsent() {
        internalMatcher().isAbsent();
    }

    /**
     * Asserts that given node is present and is not null.
     */
    @Override
    @NotNull
    public JsonAssert isNotNull() {
        internalMatcher().isNotNull();
        return this;
    }

    private Node assertType(Node.NodeType type) {
        return internalMatcher().assertType(type);
    }

    @Override
    public <ASSERT extends AbstractAssert<?, ?>> ASSERT asInstanceOf(
            InstanceOfAssertFactory<?, ASSERT> instanceOfAssertFactory) {
        Node node = internalMatcher().getActualNode();

        var ass =
                switch (node.getNodeType()) {
                    case OBJECT -> throw new UnsupportedOperationException(
                            "asInstanceOf is not supported for Object type");
                    case ARRAY -> createListAssert(node);
                    case STRING -> createStringAssert(node);
                    case NUMBER -> createBigDecimalAssert(node.decimalValue());
                    case BOOLEAN -> createBooleanAssert(node);
                    case NULL -> new StringAssert(null);
                };

        return ass.asInstanceOf(instanceOfAssertFactory);
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
        @NotNull
        public ConfigurableJsonAssert when(@NotNull Option first, @NotNull Option... other) {
            return withConfiguration(c -> c.when(first, other));
        }

        /**
         * Adds path specific options.
         *
         * @see Configuration#when(PathsParam, ApplicableForPath...)
         */
        @NotNull
        public final ConfigurableJsonAssert when(@NotNull PathsParam object, @NotNull ApplicableForPath... actions) {
            return withConfiguration(c -> c.when(object, actions));
        }

        /**
         * Adds comparison options.
         */
        @NotNull
        public ConfigurableJsonAssert withOptions(@NotNull Option first, @NotNull Option... next) {
            return withConfiguration(c -> c.withOptions(first, next));
        }

        /**
         * Adds comparison options.
         */
        @NotNull
        public ConfigurableJsonAssert withOptions(@NotNull Collection<Option> optionsToAdd) {
            return withConfiguration(c -> c.withOptions(optionsToAdd));
        }

        /**
         * Allows to configure like this
         * <code>
         *     assertThatJson(...)
         *             .withConfiguration(c -&gt; c.withMatcher("positive", greaterThan(valueOf(0)))
         *             ....
         * </code>
         */
        @NotNull
        public ConfigurableJsonAssert withConfiguration(
                @NotNull Function<Configuration, Configuration> configurationFunction) {
            Configuration newConfiguration = configurationFunction.apply(configuration);
            newConfiguration = resolveJsonPaths(originalActual, newConfiguration);
            return new ConfigurableJsonAssert(path, newConfiguration, actual);
        }

        /**
         * Sets numerical comparison tolerance.
         */
        @NotNull
        public ConfigurableJsonAssert withTolerance(@Nullable BigDecimal tolerance) {
            return withConfiguration(c -> c.withTolerance(tolerance));
        }

        /**
         * Sets numerical comparison tolerance.
         */
        @NotNull
        public ConfigurableJsonAssert withTolerance(double tolerance) {
            return withTolerance(BigDecimal.valueOf(tolerance));
        }

        /**
         * Makes JsonUnit ignore the specified paths in the actual value. If the path matches,
         * it's completely ignored. It may be missing, null or have any value
         */
        @NotNull
        public ConfigurableJsonAssert whenIgnoringPaths(@NotNull String... pathsToBeIgnored) {
            return withConfiguration(c -> c.whenIgnoringPaths(pathsToBeIgnored));
        }

        /**
         * Sets ignore placeholder.
         */
        @NotNull
        public ConfigurableJsonAssert withIgnorePlaceholder(@NotNull String ignorePlaceholder) {
            return withConfiguration(c -> c.withIgnorePlaceholder(ignorePlaceholder));
        }

        /**
         * Adds a matcher to be used in ${json-unit.matches:matcherName} macro.
         */
        @NotNull
        public ConfigurableJsonAssert withMatcher(@NotNull String matcherName, @NotNull Matcher<?> matcher) {
            return withConfiguration(c -> c.withMatcher(matcherName, matcher));
        }

        /**
         * Sets difference listener
         */
        @NotNull
        public ConfigurableJsonAssert withDifferenceListener(@NotNull DifferenceListener differenceListener) {
            return withConfiguration(c -> c.withDifferenceListener(differenceListener));
        }

        @NotNull
        public JsonAssert inPath(@NotNull String jsonPath) {
            return new JsonAssert(JsonPathAdapter.inPath(originalActual, jsonPath), configuration);
        }

        // Following methods are here just to return ConfigurableJsonAssert instead of JsonAssert
        @Override
        @NotNull
        public ConfigurableJsonAssert describedAs(@NotNull Description description) {
            return (ConfigurableJsonAssert) super.describedAs(description);
        }

        @Override
        @NotNull
        public ConfigurableJsonAssert describedAs(@NotNull String description, Object... args) {
            return (ConfigurableJsonAssert) super.describedAs(description, args);
        }

        @Override
        @NotNull
        public ConfigurableJsonAssert as(@NotNull Description description) {
            return (ConfigurableJsonAssert) super.as(description);
        }

        @Override
        @NotNull
        public ConfigurableJsonAssert as(@NotNull String description, Object... args) {
            return (ConfigurableJsonAssert) super.as(description, args);
        }
    }
}
