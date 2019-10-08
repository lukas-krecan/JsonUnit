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
import net.javacrumbs.jsonunit.core.ConfigurationWhen;
import net.javacrumbs.jsonunit.core.ConfigurationWhen.ApplicableForPath;
import net.javacrumbs.jsonunit.core.ConfigurationWhen.PathsParam;
import net.javacrumbs.jsonunit.core.Option;
import net.javacrumbs.jsonunit.core.internal.Diff;
import net.javacrumbs.jsonunit.core.internal.Node;
import net.javacrumbs.jsonunit.core.internal.Path;
import net.javacrumbs.jsonunit.core.listener.DifferenceListener;
import org.hamcrest.Matcher;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static net.javacrumbs.jsonunit.core.Option.COMPARING_ONLY_STRUCTURE;
import static net.javacrumbs.jsonunit.core.internal.Diff.create;
import static net.javacrumbs.jsonunit.core.internal.Diff.quoteTextValue;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.getNode;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.nodeAbsent;
import static net.javacrumbs.jsonunit.core.internal.Node.NodeType.ARRAY;
import static net.javacrumbs.jsonunit.core.internal.Node.NodeType.NULL;
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


    public InternalMatcher(Object actual, Path path, String description, Configuration configuration) {
        if (actual == null) {
            throw new IllegalArgumentException("Can not make assertions about null JSON.");
        }
        this.path = path;
        this.actual = actual;
        this.description = description;
        this.configuration = configuration;
    }

    private InternalMatcher(Object actual, String pathPrefix) {
        this(actual, Path.create("", pathPrefix), "", Configuration.empty());
    }

    public InternalMatcher whenIgnoringPaths(String... pathsToBeIgnored) {
        return new InternalMatcher(actual, path, description, configuration.whenIgnoringPaths(pathsToBeIgnored));
    }

    /**
     * Sets the description of this object.
     */
    public InternalMatcher describedAs(String description) {
        return new InternalMatcher(actual, path, description, configuration);
    }

    /**
     * Sets the placeholder that can be used to ignore values.
     * The default value is ${json-unit.ignore}
     */
    public InternalMatcher withIgnorePlaceholder(String ignorePlaceholder) {
        return new InternalMatcher(actual, path, description, configuration.withIgnorePlaceholder(ignorePlaceholder));
    }

    /**
     * Sets the tolerance for floating number comparison. If set to null, requires exact match of the values.
     * For example, if set to 0.01, ignores all differences lower than 0.01, so 1 and 0.9999 are considered equal.
     */
    public InternalMatcher withTolerance(double tolerance) {
        return withTolerance(BigDecimal.valueOf(tolerance));
    }

    /**
     * Sets the tolerance for floating number comparison. If set to null, requires exact match of the values.
     * For example, if set to 0.01, ignores all differences lower than 0.01, so 1 and 0.9999 are considered equal.
     */
    public InternalMatcher withTolerance(BigDecimal tolerance) {
        return new InternalMatcher(actual, path, description, configuration.withTolerance(tolerance));
    }


    /**
     * Adds a internalMatcher to be used in ${json-unit.matches:matcherName} macro.
     */
    public InternalMatcher withMatcher(String matcherName, Matcher<?> matcher) {
        return new InternalMatcher(actual, path, description, configuration.withMatcher(matcherName, matcher));
    }

    public InternalMatcher withDifferenceListener(DifferenceListener differenceListener) {
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
    public InternalMatcher withOptions(Option firstOption, Option... otherOptions) {
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
    public final <T> InternalMatcher when(PathsParam object, ApplicableForPath... actions) {
        return new InternalMatcher(actual, path, description, configuration.when(object, actions));
    }


    public void isEqualTo(Object expected) {
        Diff diff = createDiff(expected, configuration);
        diff.failIfDifferent(description);
    }


    /**
     * Fails if the selected JSON is not a String or is not present or the value
     * is not equal to expected value.
     */
    public void isStringEqualTo(String expected) {
        isString();
        Node node = getNode(actual, path);
        if (!node.asText().equals(expected)) {
            failOnDifference(quoteTextValue(expected), quoteTextValue(node.asText()));
        }
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
    public void isNotEqualTo(Object expected) {
        Diff diff = createDiff(expected, configuration);
        if (diff.similar()) {
            failWithMessage("JSON is equal.");
        }
    }

    /**
     * Compares JSON structure. Ignores values, only compares shape of the document and key names.
     * Is too lenient, ignores types, prefer IGNORING_VALUES option instead.
     *
     * @param expected
     * @return {@code this} object.
     */
    public void hasSameStructureAs(Object expected) {
        Diff diff = createDiff(expected, configuration.withOptions(COMPARING_ONLY_STRUCTURE));
        diff.failIfDifferent();
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
    public InternalMatcher node(String newPath) {
        return new InternalMatcher(actual, path.copy(newPath), description, configuration);
    }


    private Diff createDiff(Object expected, Configuration configuration) {
        return create(expected, actual, ACTUAL, path, configuration);
    }

    private void failWithMessage(String message) {
        if (description != null && description.length() > 0) {
            throw new AssertionError("[" + description + "] " + message);
        } else {
            throw new AssertionError(message);
        }
    }


    /**
     * Fails if the node exists.
     *
     * @return
     */
    public void isAbsent() {
        if (!nodeAbsent(actual, path, configuration)) {
            failOnDifference("node to be absent", quoteTextValue(getNode(actual, path)));
        }
    }

    /**
     * Fails if the node is missing.
     */
    public void isPresent() {
        isPresent("node to be present");
    }

    public void isPresent(String expectedValue) {
        if (nodeAbsent(actual, path, configuration)) {
            failOnDifference(expectedValue, "missing");
        }
    }

    /**
     * Fails if the selected JSON is not an Array or is not present.
     *
     * @return
     */
    public ArrayMatcher isArray() {
        Node node = assertType(ARRAY);
        return new ArrayMatcher(node.arrayElements());
    }

    public Node assertType(Node.NodeType type) {
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

    public void isNull() {
        isPresent();
        Node node = getNode(actual, path);
        if (node.getNodeType() != NULL) {
            failOnType(node, "a null");
        }
    }

    public void isNotNull() {
        isPresent("not null");
        Node node = getNode(actual, path);
        if (node.getNodeType() == NULL) {
            failOnType(node, "not null");
        }
    }

    private void failOnType(Node node, Node.NodeType expectedType) {
        failOnType(node, expectedType.getDescription());
    }

    public void failOnType(Node node, String expectedType) {
        failWithMessage("Node \"" + path + "\" has invalid type, expected: <" + expectedType + "> but was: <" + quoteTextValue(node.getValue()) + ">.");
    }


    /**
     * Matches the node using Hamcrest matcher.
     *
     * <ul>
     * <li>Numbers are mapped to BigDecimal</li>
     * <li>Arrays are mapped to a Collection</li>
     * <li>Objects are mapped to a map so you can use json(Part)Equals or a Map matcher</li>
     * </ul>
     *
     * @param matcher
     * @return
     */
    public void matches(Matcher<?> matcher) {
        isPresent();
        match(actual, path, matcher);
    }

    private static void match(Object value, Path path, Matcher<?> matcher) {
        Node node = getNode(value, path);
        assertThat("Node \"" + path + "\" does not match.", node.getValue(), (Matcher<? super Object>) matcher);
    }


    /**
     * Array assertions
     */
    public class ArrayMatcher {
        private final List<Node> array;

        ArrayMatcher(Iterator<Node> array) {
            List<Node> list = new ArrayList<>();
            while (array.hasNext()) {
                list.add(array.next());
            }
            this.array = list;
        }

        /**
         * Fails if the array has different length.
         *
         * @param expectedLength
         * @return
         */
        public void ofLength(int expectedLength) {
            if (array.size() != expectedLength) {
                failWithMessage("Node \"" + path + "\" has invalid length, expected: <" + expectedLength + "> but was: <" + array.size() + ">.");
            }
        }

        public void thatContains(Object expected) {

            for (Node node : array) {
                Diff diff = create(expected, node, ACTUAL, "", configuration);
                if (diff.similar()) {
                    return;
                }
            }

            failWithMessage("Node \"" + path + "\" is '" + array.toString() + "', expected to contain '" + expected + "'.");
        }

        public void isEmpty() {
            if (!array.isEmpty()) {
                failWithMessage("Node \"" + path + "\" is not an empty array.");
            }
        }

        public void isNotEmpty() {
            if (array.isEmpty()) {
                failWithMessage("Node \"" + path + "\" is an empty array.");
            }
        }

    }
}
