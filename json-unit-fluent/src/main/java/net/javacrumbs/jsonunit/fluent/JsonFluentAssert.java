/**
 * Copyright 2009-2018 the original author or authors.
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
package net.javacrumbs.jsonunit.fluent;

import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.Option;
import net.javacrumbs.jsonunit.core.internal.Diff;
import net.javacrumbs.jsonunit.core.internal.Filter;
import net.javacrumbs.jsonunit.core.internal.Node;
import net.javacrumbs.jsonunit.core.internal.Node.NodeType;
import net.javacrumbs.jsonunit.core.internal.Path;
import org.hamcrest.Matcher;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static net.javacrumbs.jsonunit.core.Option.COMPARING_ONLY_STRUCTURE;
import static net.javacrumbs.jsonunit.core.internal.Diff.create;
import static net.javacrumbs.jsonunit.core.internal.Diff.quoteTextValue;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.convertToJson;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.getNode;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.getPathPrefix;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.nodeAbsent;
import static net.javacrumbs.jsonunit.core.internal.Node.NodeType.ARRAY;
import static net.javacrumbs.jsonunit.core.internal.Node.NodeType.OBJECT;
import static net.javacrumbs.jsonunit.core.internal.Node.NodeType.STRING;
import static org.hamcrest.MatcherAssert.assertThat;


/**
 * Contains JSON related fluent assertions inspired by FEST or AssertJ. Typical usage is:
 *
 * <code>
 * assertThatJson("{\"test\":1}").isEqualTo("{\"test\":2}");
 * assertThatJson("{\"test\":1}").hasSameStructureAs("{\"test\":21}");
 * assertThatJson("{\"root\":{\"test\":1}}").node("root.test").isEqualTo("2");
 * </code>
 *
 * Please note that the method name is assertThatJson and not assertThat. The reason is that we need to accept String parameter
 * and do not want to override standard FEST or AssertJ assertThat(String) method.
 *
 * All the methods accept Objects as parameters. The supported types are:
 * <ol>
 * <li>Jackson JsonNode</li>
 * <li>Gson JsonElement</li>
 * <li>Numbers, booleans and any other type parsable by underlying JSON library</li>
 * <li>String is parsed as JSON. For expected values the string is quoted if it contains obviously invalid JSON.</li>
 * <li>{@link java.io.Reader} similarly to String</li>
 * <li>null as null Node</li>
 * </ol>
 */
public class JsonFluentAssert {
    private static final String ACTUAL = "actual";

    private final Path path;
    private final Object actual;
    private final String description;
    private final Configuration configuration;
    private static List<Filter> filters = new ArrayList<Filter>();

    private JsonFluentAssert(Object actual, Path path, String description, Configuration configuration, List<Filter> filters) {
        if (actual == null) {
            throw new IllegalArgumentException("Can not make assertions about null JSON.");
        }
        this.path = path;
        this.actual = actual;
        this.description = description;
        this.configuration = configuration;
        this.filters = filters;
    }

    private JsonFluentAssert(Object actual, String pathPrefix) {
        this(actual, Path.create("", pathPrefix), "", Configuration.empty(), filters);
    }

    /**
     * Creates a new instance of <code>{@link JsonFluentAssert}</code>.
     * It is not called assertThat to not clash with StringAssert.
     * The json parameter is converted to JSON using ObjectMapper.
     *
     * @param json
     * @return new JsonFluentAssert object.
     */
    public static JsonFluentAssert assertThatJson(Object json) {
        return new JsonFluentAssert(convertToJson(json, ACTUAL), getPathPrefix(json));
    }

    /**
     * Compares JSON for equality. The expected object is converted to JSON
     * before comparison. Ignores order of sibling nodes and whitespaces.
     *
     * Please note that if you pass a String, it's parsed as JSON which can lead to an
     * unexpected behavior. If you pass in "1" it is parsed as a JSON containing
     * integer 1. If you compare it with a string it fails due to a different type.
     * If you want to pass in real string you have to quote it "\"1\"" or use
     * {@link #isStringEqualTo(String)}.
     *
     * If the string parameter is not a valid JSON, it is quoted automatically.
     *
     * @param expected
     * @return {@code this} object.
     * @see #isStringEqualTo(String)
     */
    public JsonFluentAssertAfterAssertion isEqualTo(Object expected) {
        Diff diff = createDiff(expected, configuration);
        if (!diff.similar()) {
            failWithMessage(diff.differences());
        }
        return JsonFluentAssertAfterAssertion.wrap(this);
    }


    /**
     * Fails if the selected JSON is not a String or is not present or the value
     * is not equal to expected value.
     */
    public JsonFluentAssertAfterAssertion isStringEqualTo(String expected) {
        isString();
        Node node = getNode(actual, path);
        if (!node.asText().equals(expected)) {
            failOnDifference(quoteTextValue(expected), quoteTextValue(node.asText()));
        }
        return JsonFluentAssertAfterAssertion.wrap(this);
    }

    private void failOnDifference(Object expected, Object actual) {
        failWithMessage(String.format("Different value found in node \"%s\", expected: <%s> but was: <%s>.", path, expected, actual));
    }

    /**
     * Fails if compared documents are equal. The expected object is converted to JSON
     * before comparison. Ignores order of sibling nodes and whitespaces.
     *
     * @param expected
     * @return {@code this} object.
     */
    public JsonFluentAssertAfterAssertion isNotEqualTo(Object expected) {
        Diff diff = createDiff(expected, configuration);
        if (diff.similar()) {
            failWithMessage("JSON is equal.");
        }
        return JsonFluentAssertAfterAssertion.wrap(this);
    }

    /**
     * Compares JSON structure. Ignores values, only compares shape of the document and key names.
     * Is too lenient, ignores types, prefer IGNORING_VALUES option instead.
     *
     * @param expected
     * @return {@code this} object.
     *
     * @deprecated Use IGNORING_VALUES option instead
     */
    @Deprecated
    public JsonFluentAssertAfterAssertion hasSameStructureAs(Object expected) {
        Diff diff = createDiff(expected, configuration.withOptions(COMPARING_ONLY_STRUCTURE));
        if (!diff.similar()) {
            failWithMessage(diff.differences());
        }
        return JsonFluentAssertAfterAssertion.wrap(this);
    }

    /**
     * Creates an assert object that only compares given node.
     * The path is denoted by JSON path, for example.
     *
     * <code>
     * assertThatJson("{\"root\":{\"test\":[1,2,3]}}").node("root.test[0]").isEqualTo("1");
     * </code>
     *
     * @param newPath
     * @return object comparing only node given by path.
     */
    public JsonFluentAssert node(String newPath) {
        return new JsonFluentAssert(actual, path.copy(newPath), description, configuration, filters);
    }

    /**
     * Adds paths to be ignored
     */
    public JsonFluentAssert whenIgnoringPaths(String... pathsToBeIgnored) {
        return new JsonFluentAssert(actual, path, description, configuration.whenIgnoringPaths(pathsToBeIgnored), filters);
    }

    private Diff createDiff(Object expected, Configuration configuration) {
        return create(expected, actual, ACTUAL, path, configuration, filters);
    }

    private void failWithMessage(String message) {
        if (description != null && description.length() > 0) {
            throw new AssertionError("[" + description + "] " + message);
        } else {
            throw new AssertionError(message);
        }
    }

    /**
     * Sets the description of this object.
     *
     * @param description
     * @return
     */
    public JsonFluentAssert as(String description) {
        return describedAs(description);
    }

    /**
     * Sets the description of this object.
     *
     * @param description
     * @return
     */
    public JsonFluentAssert describedAs(String description) {
        return new JsonFluentAssert(actual, path, description, configuration, filters);
    }

    /**
     * Sets the placeholder that can be used to ignore values.
     * The default value is ${json-unit.ignore}
     *
     * @param ignorePlaceholder
     * @return
     */
    public JsonFluentAssert ignoring(String ignorePlaceholder) {
        return new JsonFluentAssert(actual, path, description, configuration.withIgnorePlaceholder(ignorePlaceholder), filters);
    }

    /**
     * Sets the tolerance for floating number comparison. If set to null, requires exact match of the values.
     * For example, if set to 0.01, ignores all differences lower than 0.01, so 1 and 0.9999 are considered equal.
     *
     * @param tolerance
     */
    public JsonFluentAssert withTolerance(double tolerance) {
        return withTolerance(BigDecimal.valueOf(tolerance));
    }

    /**
     * Sets the tolerance for floating number comparison. If set to null, requires exact match of the values.
     * For example, if set to 0.01, ignores all differences lower than 0.01, so 1 and 0.9999 are considered equal.
     *
     * @param tolerance
     */
    public JsonFluentAssert withTolerance(BigDecimal tolerance) {
        return new JsonFluentAssert(actual, path, description, configuration.withTolerance(tolerance), filters);
    }


    /**
     * Adds a matcher to be used in ${json-unit.matches:matcherName} macro.
     */
    public JsonFluentAssert withMatcher(String matcherName, Matcher<?> matcher) {
        return new JsonFluentAssert(actual, path, description, configuration.withMatcher(matcherName, matcher), filters);
    }

    public JsonFluentAssert withFilters(Filter... filters) {
        return new JsonFluentAssert(actual, path, description, configuration, Arrays.asList(filters));
    }

    /**
     * When set to true, treats null nodes in actual value as absent. In other words
     * if you expect {"test":{"a":1}} this {"test":{"a":1, "b": null}} will pass the test.
     *
     * @return
     * @deprecated Use when(Option.TREATING_NULL_AS_ABSENT)
     */
    @Deprecated
    public JsonFluentAssert treatingNullAsAbsent() {
        return when(Option.TREATING_NULL_AS_ABSENT);
    }

    /**
     * Sets options changing comparison behavior. This method has to be called
     * <b>before</b> assertion.
     * For more info see {@link net.javacrumbs.jsonunit.core.Option}
     *
     * @param firstOption
     * @param otherOptions
     * @see net.javacrumbs.jsonunit.core.Option
     */
    public JsonFluentAssert when(Option firstOption, Option... otherOptions) {
        return new JsonFluentAssert(actual, path, description, configuration.withOptions(firstOption, otherOptions), filters);
    }

    /**
     * Fails if the node exists.
     *
     * @return
     */
    public JsonFluentAssertAfterAssertion isAbsent() {
        if (!nodeAbsent(actual, path, configuration)) {
            failOnDifference("node to be absent", quoteTextValue(getNode(actual, path)));

        }
        return JsonFluentAssertAfterAssertion.wrap(this);
    }

    /**
     * Fails if the node is missing.
     */
    public JsonFluentAssertAfterAssertion isPresent() {
        return isPresent("node to be present");
    }

    private JsonFluentAssertAfterAssertion isPresent(String expectedValue) {
        if (nodeAbsent(actual, path, configuration)) {
            failOnDifference(expectedValue, "missing");
        }
        return JsonFluentAssertAfterAssertion.wrap(this);
    }

    /**
     * Fails if the selected JSON is not an Array or is not present.
     *
     * @return
     */
    public ArrayAssert isArray() {
        Node node = assertType(ARRAY);
        return new ArrayAssert(node.arrayElements());
    }

    private Node assertType(NodeType type) {
        isPresent(type.getDescription());
        Node node = getNode(actual, path);
        if (node.getNodeType() != type) {
            failOnType(node, type);
        }
        return node;
    }

    /**
     * Fails if the selected JSON is not an Object or is not present.
     */
    public void isObject() {
        assertType(OBJECT);
    }

    /**
     * Fails if the selected JSON is not a String or is not present.
     */
    public void isString() {
        assertType(STRING);
    }

    private void failOnType(Node node, final NodeType expectedType) {
        failWithMessage("Node \"" + path + "\" has invalid type, expected: <" + expectedType.getDescription() + "> but was: <" + quoteTextValue(node.getValue()) + ">.");
    }


    /**
     * Matches the node using Hamcrest matcher.
     *
     * <ul>
     *     <li>Numbers are mapped to BigDecimal</li>
     *     <li>Arrays are mapped to a Collection</li>
     *     <li>Objects are mapped to a map so you can use json(Part)Equals or a Map matcher</li>
     * </ul>
     *
     * @param matcher
     * @return
     */
    public JsonFluentAssert matches(Matcher<?> matcher) {
        isPresent();
        match(actual, path, matcher);
        return this;
    }

    private static void match(Object value, Path path, Matcher<?> matcher) {
        Node node = getNode(value, path);
        assertThat("Node \"" + path + "\" does not match.", node.getValue(), (Matcher<? super Object>) matcher);
    }


    /**
     * Array assertions
     */
    public class ArrayAssert {
        private final List<Node> array;

        public ArrayAssert(Iterator<Node> array) {
            List<Node> list = new ArrayList<Node>();
            while (array.hasNext()) {
                list.add(array.next());
            }
            this.array = list;
        }

        /**
         * Fails if the array has different length.
         * @param expectedLength
         * @return
         */
        public ArrayAssert ofLength(int expectedLength) {
            if (array.size() != expectedLength) {
                failWithMessage("Node \"" + path + "\" has invalid length, expected: <" + expectedLength + "> but was: <" + array.size() + ">.");
            }
            return this;
        }

        public ArrayAssert thatContains(Object expected) {

            for (Node node : array) {
                Diff diff = create(expected, node, ACTUAL, "", configuration, filters);
                if (diff.similar()) {
                    return this;
                }
            }

            failWithMessage("Node \"" + path + "\" is '" + array.toString() + "', expected to contain '" + expected +  "'.");
            // unfortunately I can't think of a better solution to make this compile
            return this;
        }
    }

    /**
     * JsonFluentAssert after assertion. It does not make much sense to call some of the methods
     * after assertion so this class deprecates them. It's hard to fix bad API design, isEqual method should have
     * returned void.
     */
    public static class JsonFluentAssertAfterAssertion extends JsonFluentAssert {
        private static JsonFluentAssertAfterAssertion wrap(JsonFluentAssert assertion) {
            if (assertion instanceof JsonFluentAssertAfterAssertion) {
                return (JsonFluentAssertAfterAssertion)assertion;
            } else {
                return new JsonFluentAssertAfterAssertion(assertion.actual, assertion.path, assertion.description, assertion.configuration);
            }
        }

        private JsonFluentAssertAfterAssertion(Object actual, Path path, String description, Configuration configuration) {
            super(actual, path, description, configuration, filters);
        }

        /**
         * This method should be called before assertion.
         */
        @Override
        @Deprecated
        public JsonFluentAssert when(Option firstOption, Option... otherOptions) {
            return super.when(firstOption, otherOptions);
        }


        /**
         * This method should be called before assertion.
         */
        @Override
        @Deprecated
        public JsonFluentAssert withTolerance(double tolerance) {
            return super.withTolerance(tolerance);
        }

        /**
         * This method should be called before assertion.
         */
        @Override
        @Deprecated
        public JsonFluentAssert withTolerance(BigDecimal tolerance) {
            return super.withTolerance(tolerance);
        }

        /**
         * This method should be called before assertion.
         * @param ignorePlaceholder
         * @return
         */
        @Override
        @Deprecated
        public JsonFluentAssert ignoring(String ignorePlaceholder) {
            return super.ignoring(ignorePlaceholder);
        }

        /**
         * This method should be called before assertion.
         * @param pathsToBeIgnored
         * @return
         */
        @Override
        @Deprecated
        public JsonFluentAssert whenIgnoringPaths(String... pathsToBeIgnored) {
            return super.whenIgnoringPaths(pathsToBeIgnored);
        }
    }
}
