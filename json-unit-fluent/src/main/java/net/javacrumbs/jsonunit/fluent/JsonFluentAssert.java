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
package net.javacrumbs.jsonunit.fluent;

import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.ConfigurationWhen;
import net.javacrumbs.jsonunit.core.ConfigurationWhen.ApplicableForPath;
import net.javacrumbs.jsonunit.core.ConfigurationWhen.PathsParam;
import net.javacrumbs.jsonunit.core.Option;
import net.javacrumbs.jsonunit.core.internal.Path;
import net.javacrumbs.jsonunit.core.internal.matchers.InternalMatcher;
import net.javacrumbs.jsonunit.core.listener.DifferenceListener;
import org.hamcrest.Matcher;

import java.math.BigDecimal;

import static net.javacrumbs.jsonunit.core.internal.JsonUtils.convertToJson;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.getNode;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.getPathPrefix;
import static net.javacrumbs.jsonunit.core.internal.matchers.InternalMatcher.ACTUAL;
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
    final InternalMatcher internalMatcher;

    private JsonFluentAssert(InternalMatcher internalMatcher) {
        this.internalMatcher = internalMatcher;
    }

    private JsonFluentAssert(Object actual, Path path, String description, Configuration configuration) {
        this.internalMatcher = new InternalMatcher(actual, path, description, configuration);
    }

    private JsonFluentAssert(Object actual, String pathPrefix) {
        this(actual, Path.create("", pathPrefix), "", Configuration.empty());
    }

    /**
     * Creates a new instance of <code>{@link JsonFluentAssert}</code>.
     * It is not called assertThat to not clash with StringAssert.
     * The json parameter is converted to JSON using ObjectMapper.
     *
     * @param json
     * @return new JsonFluentAssert object.
     */
    public static ConfigurableJsonFluentAssert assertThatJson(Object json) {
        return new ConfigurableJsonFluentAssert(convertToJson(json, ACTUAL), getPathPrefix(json));
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
    public JsonFluentAssert isEqualTo(Object expected) {
        internalMatcher.isEqualTo(expected);
        return this;
    }


    /**
     * Fails if the selected JSON is not a String or is not present or the value
     * is not equal to expected value.
     */
    public JsonFluentAssert isStringEqualTo(String expected) {
        internalMatcher.isStringEqualTo(expected);
        return this;
    }

    /**
     * Fails if compared documents are equal. The expected object is converted to JSON
     * before comparison. Ignores order of sibling nodes and whitespaces.
     *
     * @param expected
     * @return {@code this} object.
     */
    public JsonFluentAssert isNotEqualTo(Object expected) {
        internalMatcher.isNotEqualTo(expected);
        return this;
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
    public JsonFluentAssert hasSameStructureAs(Object expected) {
        internalMatcher.hasSameStructureAs(expected);
        return this;
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
        return new JsonFluentAssert(internalMatcher.node(newPath));
    }

    /**
     * Fails if the node exists.
     *
     * @return
     */
    public JsonFluentAssert isAbsent() {
        internalMatcher.isAbsent();
        return this;
    }

    /**
     * Fails if the node is missing.
     */
    public JsonFluentAssert isPresent() {
        internalMatcher.isPresent();
        return this;
    }


    /**
     * Fails if the selected JSON is not an Array or is not present.
     *
     * @return
     */
    public ArrayAssert isArray() {

        return new ArrayAssert(internalMatcher.isArray());
    }


    /**
     * Fails if the selected JSON is not an Object or is not present.
     */
    public void isObject() {
        internalMatcher.isObject();
    }

    /**
     * Fails if the selected JSON is not a String or is not present.
     */
    public void isString() {
        internalMatcher.isString();
    }

    /**
     * Matches the node using Hamcrest internalMatcher.
     *
     * <ul>
     *     <li>Numbers are mapped to BigDecimal</li>
     *     <li>Arrays are mapped to a Collection</li>
     *     <li>Objects are mapped to a map so you can use json(Part)Equals or a Map internalMatcher</li>
     * </ul>
     *
     * @param matcher
     * @return
     */
    public JsonFluentAssert matches(Matcher<?> matcher) {
        internalMatcher.matches(matcher);
        return this;
    }


    /**
     * Array assertions
     */
    public static class ArrayAssert {
        private final InternalMatcher.ArrayMatcher arrayMatcher;

        ArrayAssert(InternalMatcher.ArrayMatcher arrayMatcher) {
            this.arrayMatcher = arrayMatcher;
        }

        /**
         * Fails if the array has different length.
         * @param expectedLength
         * @return
         */
        public ArrayAssert ofLength(int expectedLength) {
            arrayMatcher.ofLength(expectedLength);
            return this;
        }

        public ArrayAssert thatContains(Object expected) {
            arrayMatcher.thatContains(expected);
            return this;
        }

        public ArrayAssert isEmpty() {
            arrayMatcher.isEmpty();
            return this;
        }

        public ArrayAssert isNotEmpty() {
            arrayMatcher.isNotEmpty();
            return this;
        }

    }


    /**
     * JsonFluentAssert that can be configured. To make sure that configuration is done before comparison and not after.
     */
    public static class ConfigurableJsonFluentAssert extends JsonFluentAssert {
        private ConfigurableJsonFluentAssert(InternalMatcher internalMatcher) {
            super(internalMatcher);
        }

        private ConfigurableJsonFluentAssert(Object actual, String pathPrefix) {
            super(actual, pathPrefix);
        }

        /**
         * Creates an assert object that only compares given node.
         * The path is denoted by JSON path, for example. Second call navigates from the root.
         *
         * <code>
         * assertThatJson("{\"root\":{\"test\":[1,2,3]}}").node("root.test[0]").isEqualTo("1");
         * </code>
         *
         * @param newPath
         * @return object comparing only node given by path.
         */
        public ConfigurableJsonFluentAssert node(String newPath) {
            return new ConfigurableJsonFluentAssert(internalMatcher.node(newPath));
        }

        /**
         * Makes JsonUnit ignore the specified paths in the actual value. If the path matches,
         * it's completely ignored. It may be missing, null or have any value
         */
        public ConfigurableJsonFluentAssert whenIgnoringPaths(String... pathsToBeIgnored) {
            return new ConfigurableJsonFluentAssert(internalMatcher.whenIgnoringPaths(pathsToBeIgnored));
        }

        /**
         * Sets the description of this object.
         *
         * @param description
         * @return
         */
        public ConfigurableJsonFluentAssert as(String description) {
            return describedAs(description);
        }

        /**
         * Sets the description of this object.
         *
         * @param description
         * @return
         */
        public ConfigurableJsonFluentAssert describedAs(String description) {
            return new ConfigurableJsonFluentAssert(internalMatcher.describedAs(description));
        }

        /**
         * Sets the placeholder that can be used to ignore values.
         * The default value is ${json-unit.ignore}
         *
         * @param ignorePlaceholder
         * @return
         */
        public ConfigurableJsonFluentAssert ignoring(String ignorePlaceholder) {
            return new ConfigurableJsonFluentAssert(internalMatcher.withIgnorePlaceholder(ignorePlaceholder));
        }

        /**
         * Sets the tolerance for floating number comparison. If set to null, requires exact match of the values.
         * For example, if set to 0.01, ignores all differences lower than 0.01, so 1 and 0.9999 are considered equal.
         *
         * @param tolerance
         */
        public ConfigurableJsonFluentAssert withTolerance(double tolerance) {
            return new ConfigurableJsonFluentAssert(internalMatcher.withTolerance(tolerance));
        }

        /**
         * Sets the tolerance for floating number comparison. If set to null, requires exact match of the values.
         * For example, if set to 0.01, ignores all differences lower than 0.01, so 1 and 0.9999 are considered equal.
         *
         * @param tolerance
         */
        public ConfigurableJsonFluentAssert withTolerance(BigDecimal tolerance) {
            return new ConfigurableJsonFluentAssert(internalMatcher.withTolerance(tolerance));
        }


        /**
         * Adds a internalMatcher to be used in ${json-unit.matches:matcherName} macro.
         */
        public ConfigurableJsonFluentAssert withMatcher(String matcherName, Matcher<?> matcher) {
            return new ConfigurableJsonFluentAssert(internalMatcher.withMatcher(matcherName, matcher));
        }

        public ConfigurableJsonFluentAssert withDifferenceListener(DifferenceListener differenceListener) {
            return new ConfigurableJsonFluentAssert(internalMatcher.withDifferenceListener(differenceListener));
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
        public ConfigurableJsonFluentAssert when(Option firstOption, Option... otherOptions) {
            return new ConfigurableJsonFluentAssert(internalMatcher.withOptions(firstOption, otherOptions));
        }

        /**
         * Sets advanced/local options. This method has to be called <b>before</b> assertion.
         * For more information see {@link Configuration#when(PathsParam, ApplicableForPath...)}
         *
         * @see Configuration#when(ConfigurationWhen.PathsParam, ApplicableForPath...)
         */
        public final ConfigurableJsonFluentAssert when(PathsParam object, ApplicableForPath... actions) {
            return new ConfigurableJsonFluentAssert(internalMatcher.when(object, actions));
        }
    }
}
