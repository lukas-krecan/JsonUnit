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
package net.javacrumbs.jsonunit.core.internal.matchers;

import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.ConfigurationWhen.ApplicableForPath;
import net.javacrumbs.jsonunit.core.ConfigurationWhen.PathsParam;
import net.javacrumbs.jsonunit.core.Option;
import net.javacrumbs.jsonunit.core.internal.Diff;
import net.javacrumbs.jsonunit.core.internal.JsonSource;
import net.javacrumbs.jsonunit.core.internal.Node;
import net.javacrumbs.jsonunit.core.internal.Path;
import net.javacrumbs.jsonunit.core.listener.DifferenceListener;
import org.hamcrest.Matcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.util.Collections.singletonList;
import static net.javacrumbs.jsonunit.core.internal.Diff.create;
import static net.javacrumbs.jsonunit.core.internal.Diff.quoteTextValue;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.getNode;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.nodeAbsent;
import static net.javacrumbs.jsonunit.core.internal.Node.NodeType.ARRAY;
import static net.javacrumbs.jsonunit.core.internal.Node.NodeType.NULL;
import static net.javacrumbs.jsonunit.core.internal.Node.NodeType.NUMBER;
import static net.javacrumbs.jsonunit.core.internal.Node.NodeType.OBJECT;
import static net.javacrumbs.jsonunit.core.internal.Node.NodeType.STRING;
import static org.hamcrest.MatcherAssert.assertThat;


/**
 * Internal class, please do not use outside the library
 */
public final class InternalMatcher {
    public static final String ACTUAL = "actual";

    private final Path path;
    private final Object actual;
    private final String description;
    private final Configuration configuration;
    private final String nodeDescription;

    public InternalMatcher(@Nullable Object actual, @NotNull Path path, @NotNull String description, @NotNull Configuration configuration, @NotNull String nodeDescription) {
        this.path = path;
        this.actual = actual;
        this.description = description;
        this.configuration = configuration;
        this.nodeDescription = nodeDescription;
    }

    public InternalMatcher(@Nullable Object actual, @NotNull Path path, @NotNull String description, @NotNull Configuration configuration) {
        this(actual, path, description, configuration, "Node \"" + path + "\"");
    }

    private InternalMatcher(@NotNull Object actual, @NotNull String pathPrefix) {
        this(actual, Path.create("", pathPrefix), "", Configuration.empty());
    }

    @NotNull
    public InternalMatcher whenIgnoringPaths(@NotNull String... pathsToBeIgnored) {
        return new InternalMatcher(actual, path, description, configuration.whenIgnoringPaths(pathsToBeIgnored));
    }

    /**
     * Sets the description of this object.
     */
    @NotNull
    public InternalMatcher describedAs(@NotNull String description) {
        return new InternalMatcher(actual, path, description, configuration);
    }

    /**
     * Sets the placeholder that can be used to ignore values.
     * The default value is ${json-unit.ignore}
     */
    @NotNull
    public InternalMatcher withIgnorePlaceholder(@NotNull String ignorePlaceholder) {
        return new InternalMatcher(actual, path, description, configuration.withIgnorePlaceholder(ignorePlaceholder));
    }

    /**
     * Sets the tolerance for floating number comparison. If set to null, requires exact match of the values.
     * For example, if set to 0.01, ignores all differences lower than 0.01, so 1 and 0.9999 are considered equal.
     */
    @NotNull
    public InternalMatcher withTolerance(double tolerance) {
        return withTolerance(BigDecimal.valueOf(tolerance));
    }

    /**
     * Sets the tolerance for floating number comparison. If set to null, requires exact match of the values.
     * For example, if set to 0.01, ignores all differences lower than 0.01, so 1 and 0.9999 are considered equal.
     */
    @NotNull
    public InternalMatcher withTolerance(@Nullable BigDecimal tolerance) {
        return new InternalMatcher(actual, path, description, configuration.withTolerance(tolerance));
    }


    /**
     * Adds a internalMatcher to be used in ${json-unit.matches:matcherName} macro.
     */
    @NotNull
    public InternalMatcher withMatcher(@NotNull String matcherName, @NotNull Matcher<?> matcher) {
        return new InternalMatcher(actual, path, description, configuration.withMatcher(matcherName, matcher));
    }

    @NotNull
    public InternalMatcher withDifferenceListener(@NotNull DifferenceListener differenceListener) {
        return new InternalMatcher(actual, path, description, configuration.withDifferenceListener(differenceListener));
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
    @NotNull
    public InternalMatcher withOptions(@NotNull Option firstOption, @NotNull Option... otherOptions) {
        return new InternalMatcher(actual, path, description, configuration.withOptions(firstOption, otherOptions));
    }

    /**
     * Sets advanced/local options. This method has to be called <b>before</b> assertion.
     * For more info see {@link Configuration#when(PathsParam, ApplicableForPath...)}
     *
     * @param object
     * @param actions
     * @see Configuration#when(PathsParam, ApplicableForPath...)
     */
    @NotNull
    public final InternalMatcher when(@NotNull PathsParam object, @NotNull ApplicableForPath... actions) {
        return new InternalMatcher(actual, path, description, configuration.when(object, actions));
    }


    public void isEqualTo(@Nullable Object expected) {
        Diff diff = createDiff(expected, configuration);
        diff.failIfDifferent(description);
    }


    /**
     * Fails if the selected JSON is not a String or is not present or the value
     * is not equal to expected value.
     */
    public void isStringEqualTo(@Nullable String expected) {
        isString();
        Node node = getActualNode();
        if (!node.asText().equals(expected)) {
            failOnDifference(quoteTextValue(expected), quoteTextValue(node.asText()));
        }
    }

    private void failOnDifference(@Nullable Object expected, @NotNull Object actual) {
        failOnDifference(expected, actual, singletonList(path.toString()));
    }

    private void failOnDifference(@Nullable Object expected, @NotNull Object actual, @NotNull List<String> paths) {
        String path;
        String node;
        if (paths.size() == 1) {
            path = paths.get(0);
            node = "node";
        } else {
            path = paths.toString();
            node = "nodes";
        }
        failWithMessage(String.format("Different value found in %s \"%s\", expected: <%s> but was: <%s>.", node, path, expected, actual));
    }

    /**
     * Fails if compared documents are equal. The expected object is converted to JSON
     * before comparison. Ignores order of sibling nodes and whitespaces.
     */
    public void isNotEqualTo(@Nullable Object expected) {
        Diff diff = createDiff(expected, configuration);
        if (diff.similar()) {
            failWithMessage("JSON is equal.");
        }
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
     */
    @NotNull
    public InternalMatcher node(@NotNull String newPath) {
        return new InternalMatcher(actual, path.copy(newPath), description, configuration);
    }

    @NotNull
    private Diff createDiff(@Nullable Object expected, @NotNull Configuration configuration) {
        return create(expected, actual, ACTUAL, path, configuration);
    }

    private void failWithMessage(@NotNull String message) {
        if (description != null && description.length() > 0) {
            throw new AssertionError("[" + description + "] " + message);
        } else {
            throw new AssertionError(message);
        }
    }


    /**
     * Fails if the node exists.
     */
    public void isAbsent() {
        if (!nodeAbsent(actual, path, configuration)) {
            List<String> matchingPaths = getMatchingPaths();
            failOnDifference("node to be absent", quoteTextValue(getActualNode()), matchingPaths);
        }
    }

    /**
     * Extracts data from JsonPath matches
     */
    private List<String> getMatchingPaths() {
        if (actual instanceof JsonSource) {
            JsonSource jsonSource = (JsonSource) actual;
            if (!jsonSource.getMatchingPaths().isEmpty()) {
                return jsonSource.getMatchingPaths();
            }
        }
        return singletonList(path.toString());
    }

    /**
     * Fails if the node is missing.
     */
    public void isPresent() {
        isPresent("node to be present");
    }

    public void isPresent(@Nullable String expectedValue) {
        if (nodeAbsent(actual, path, configuration)) {
            failOnDifference(expectedValue, "missing");
        }
    }

    /**
     * Fails if the selected JSON is not an Array or is not present.
     */
    @NotNull
    public ArrayMatcher isArray() {
        Node node = assertType(ARRAY);
        return new ArrayMatcher(node.arrayElements());
    }

    @NotNull
    public Node assertType(@NotNull Node.NodeType type) {
        isPresent(type.getDescription());
        Node node = getActualNode();
        if (node.getNodeType() != type) {
            failOnType(node, type);
        }
        return node;
    }

    @NotNull
    public Node assertIntegralNumber() {
        Node node = assertType(NUMBER);
        if (!node.isIntegralNumber()) {
            failOnType(node, "integer");
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

    public void isNull() {
        isPresent();
        Node node = getActualNode();
        if (node.getNodeType() != NULL) {
            failOnType(node, "a null");
        }
    }

    public void isNotNull() {
        isPresent("not null");
        Node node = getActualNode();
        if (node.getNodeType() == NULL) {
            failOnType(node, "not null");
        }
    }

    private Node getActualNode() {
        return getNode(actual, path);
    }

    private void failOnType(@NotNull Node node, @NotNull Node.NodeType expectedType) {
        failOnType(node, expectedType.getDescription());
    }

    public void failOnType(@NotNull Node node, @NotNull String expectedType) {
        failOnType(expectedType, quoteTextValue(node.getValue()));
    }

    private void failOnType(@NotNull String expectedType, @Nullable Object actualType) {
        failWithMessage(nodeDescription + " has invalid type, expected: <" + expectedType + "> but was: <" + actualType + ">.");
    }

    /**
     * Matches the node using Hamcrest matcher.
     *
     * <ul>
     * <li>Numbers are mapped to BigDecimal</li>
     * <li>Arrays are mapped to a Collection</li>
     * <li>Objects are mapped to a map so you can use json(Part)Equals or a Map matcher</li>
     * </ul>
     */
    public void matches(@NotNull Matcher<?> matcher) {
        isPresent();
        match(actual, path, matcher);
    }

    @SuppressWarnings("unchecked")
    private void match(@NotNull Object value, @NotNull Path path, @NotNull Matcher<?> matcher) {
        Node node = getNode(value, path);
        assertThat(nodeDescription + " does not match.", node.getValue(), (Matcher<? super Object>) matcher);
    }


    /**
     * Array assertions
     */
    public class ArrayMatcher {
        private final List<Node> array;

        ArrayMatcher(@NotNull Iterator<Node> array) {
            List<Node> list = new ArrayList<>();
            while (array.hasNext()) {
                list.add(array.next());
            }
            this.array = list;
        }

        /**
         * Fails if the array has different length.
         */
        public void ofLength(int expectedLength) {
            if (array.size() != expectedLength) {
                failWithMessage(nodeDescription + " has invalid length, expected: <" + expectedLength + "> but was: <" + array.size() + ">.");
            }
        }

        public void thatContains(@Nullable Object expected) {

            for (Node node : array) {
                Diff diff = create(expected, node, ACTUAL, "", configuration);
                if (diff.similar()) {
                    return;
                }
            }

            failWithMessage(nodeDescription + " is '" + array + "', expected to contain '" + expected + "'.");
        }

        public void isEmpty() {
            if (!array.isEmpty()) {
                failWithMessage(nodeDescription + " is not an empty array.");
            }
        }

        public void isNotEmpty() {
            if (array.isEmpty()) {
                failWithMessage(nodeDescription + " is an empty array.");
            }
        }

    }
}
