/**
 * Copyright 2009-2019 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.javacrumbs.jsonunit.assertj;

import static java.util.Objects.requireNonNull;
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
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class JsonAssert extends AbstractAssert<JsonAssert, Object> {
    final Path path;
    final Configuration configuration;

    @Nullable
    private final Object actualForMatcher;

    @SuppressWarnings("CheckReturnValue")
    JsonAssert(Path path, Configuration configuration, @Nullable Object actual, boolean alreadyParsed) {
        super(
                alreadyParsed ? JsonUtils.wrapDeserializedObject(actual) : JsonUtils.convertToJson(actual, "actual"),
                JsonAssert.class);
        this.path = path;
        this.configuration = configuration;
        this.actualForMatcher = alreadyParsed ? JsonUtils.wrapDeserializedObject(actual) : actual;
        //noinspection ResultOfMethodCallIgnored
        usingComparator(new JsonComparator(configuration, path, false));
    }

    JsonAssert(Path path, Configuration configuration, @Nullable Object actual) {
        this(path, configuration, actual, false);
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
    public StringAssert isStringEqualTo(String expected) {
        return isString().isEqualTo(expected);
    }

    /**
     * Asserts that given node is present and is of type object.
     *
     * @return MapAssert where the object is serialized as Map
     */
    @SuppressWarnings("unchecked")
    public JsonMapAssert isObject() {
        Node node = assertType(OBJECT);
        return describe(new JsonMapAssert(
                (Map<String, Object>) requireNonNull(node.getValue()), path.asPrefix(), configuration));
    }

    /**
     * Asserts that given node is present and is of type number.
     */
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
        throw new IllegalStateException("Unreachable");
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
    public JsonListAssert isArray() {
        Node node = assertType(ARRAY);
        return createListAssert(node).as("Node \"%s\"", path);
    }

    private JsonListAssert createListAssert(Node node) {
        return new JsonListAssert((List<?>) requireNonNull(node.getValue()), path.asPrefix(), configuration);
    }

    /**
     * Asserts that given node is present and is of type boolean.
     */
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
    public UriAssert isUri() {
        Node node = assertType(STRING);
        //noinspection DataFlowIssue
        return describe(new UriAssert(URI.create((String) node.getValue())));
    }

    /**
     * Asserts that given node is present.
     */
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
                    case OBJECT ->
                        throw new UnsupportedOperationException("asInstanceOf is not supported for Object type");
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
     * <code>
     * assertThatJson(...).isEqualsTo(...).when(...);
     * </code>
     */
    @NullMarked
    public static class ConfigurableJsonAssert extends JsonAssert {
        // Want to pass to inPath to not parse twice.
        @Nullable
        private final Object originalActual;

        ConfigurableJsonAssert(Path path, Configuration configuration, @Nullable Object actual) {
            super(path, configuration, actual);
            this.originalActual = actual;
        }

        ConfigurableJsonAssert(@Nullable Object actual, Configuration configuration) {
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
         * Adds comparison options.
         */
        public ConfigurableJsonAssert withOptions(Option first, Option... next) {
            return withConfiguration(c -> c.withOptions(first, next));
        }

        /**
         * Adds comparison options.
         */
        public ConfigurableJsonAssert withOptions(Collection<Option> optionsToAdd) {
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
        public ConfigurableJsonAssert withConfiguration(Function<Configuration, Configuration> configurationFunction) {
            Configuration newConfiguration = configurationFunction.apply(configuration);
            newConfiguration = resolveJsonPaths(originalActual, newConfiguration);
            return new ConfigurableJsonAssert(path, newConfiguration, actual);
        }

        /**
         * Sets numerical comparison tolerance.
         */
        public ConfigurableJsonAssert withTolerance(@Nullable BigDecimal tolerance) {
            return withConfiguration(c -> c.withTolerance(tolerance));
        }

        /**
         * Sets numerical comparison tolerance.
         */
        public ConfigurableJsonAssert withTolerance(double tolerance) {
            return withTolerance(BigDecimal.valueOf(tolerance));
        }

        /**
         * Makes JsonUnit ignore the specified paths in the actual value. If the path matches,
         * it's completely ignored. It may be missing, null or have any value
         */
        public ConfigurableJsonAssert whenIgnoringPaths(String... pathsToBeIgnored) {
            return withConfiguration(c -> c.whenIgnoringPaths(pathsToBeIgnored));
        }

        /**
         * Sets ignore placeholder.
         */
        public ConfigurableJsonAssert withIgnorePlaceholder(String ignorePlaceholder) {
            return withConfiguration(c -> c.withIgnorePlaceholder(ignorePlaceholder));
        }

        /**
         * Adds a matcher to be used in ${json-unit.matches:matcherName} macro.
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
