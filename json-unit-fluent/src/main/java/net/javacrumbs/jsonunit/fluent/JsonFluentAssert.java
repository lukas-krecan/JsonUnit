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
package net.javacrumbs.jsonunit.fluent;

import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.Option;
import net.javacrumbs.jsonunit.core.internal.Diff;

import java.math.BigDecimal;

import static net.javacrumbs.jsonunit.core.Option.COMPARING_ONLY_STRUCTURE;
import static net.javacrumbs.jsonunit.core.internal.Diff.create;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.convertToJson;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.nodeExists;


/**
 * Contains JSON related fluent assertions inspired by FEST or AssertJ. Typical usage is:
 * <p/>
 * <code>
 * assertThatJson("{\"test\":1}").isEqualTo("{\"test\":2}");
 * assertThatJson("{\"test\":1}").hasSameStructureAs("{\"test\":21}");
 * assertThatJson("{\"root\":{\"test\":1}}").node("root.test").isEqualTo("2");
 * </code>
 * <p/>
 * Please note that the method name is assertThatJson and not assertThat. The reason is that we need to accept String parameter
 * and do not want to override standard FEST or AssertJ assertThat(String) method.
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
public class JsonFluentAssert {
    private static final String ACTUAL = "actual";

    private final String path;
    private final Object actual;
    private final String description;
    private final Configuration configuration;

    protected JsonFluentAssert(Object actual, String path, String description, Configuration configuration) {
        if (actual == null) {
            throw new IllegalArgumentException("Can not make assertions about null JSON.");
        }
        this.path = path;
        this.actual = actual;
        this.description = description;
        this.configuration = configuration;
    }

    protected JsonFluentAssert(Object actual) {
        this(actual, "", "", Configuration.empty());
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
        return new JsonFluentAssert(convertToJson(json, ACTUAL));
    }

    /**
     * Compares JSON for equality. The expected object is converted to JSON
     * before comparison. Ignores order of sibling nodes and whitespaces.
     *
     * @param expected
     * @return {@code this} object.
     */
    public JsonFluentAssert isEqualTo(Object expected) {
        Diff diff = createDiff(expected, configuration);
        if (!diff.similar()) {
            failWithMessage(diff.differences());
        }
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
        Diff diff = createDiff(expected, configuration);
        if (diff.similar()) {
            failWithMessage("JSON is equal.");
        }
        return this;
    }

    /**
     * Compares JSON structure. Ignores values, only compares shape of the document and key names.
     *
     * @param expected
     * @return {@code this} object.
     */
    public JsonFluentAssert hasSameStructureAs(Object expected) {
        Diff diff = createDiff(expected, configuration.withOptions(COMPARING_ONLY_STRUCTURE));
        if (!diff.similar()) {
            failWithMessage(diff.differences());
        }
        return this;
    }

    /**
     * Creates an assert object that only compares given node.
     * The path is denoted by JSON path, for example.
     * <p/>
     * <code>
     * assertThatJson("{\"root\":{\"test\":[1,2,3]}}").node("root.test[0]").isEqualTo("1");
     * </code>
     *
     * @param path
     * @return object comparing only node given by path.
     */
    public JsonFluentAssert node(String path) {
        return new JsonFluentAssert(actual, path, description, configuration);
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
        return new JsonFluentAssert(actual, path, description, configuration);
    }

    /**
     * Sets the placeholder that can be used to ignore values.
     * The default value is ${json-unit.ignore}
     *
     * @param ignorePlaceholder
     * @return
     */
    public JsonFluentAssert ignoring(String ignorePlaceholder) {
        return new JsonFluentAssert(actual, path, description, configuration.withIgnorePlaceholder(ignorePlaceholder));
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
        return new JsonFluentAssert(actual, path, description, configuration.withTolerance(tolerance));
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
     * Sets options changing comparison behavior. For more
     * details see {@link net.javacrumbs.jsonunit.core.Option}
     * @param firstOption
     * @param rest
     * @see net.javacrumbs.jsonunit.core.Option
     */
    public JsonFluentAssert when(Option firstOption, Option ...otherOptions) {
        return new JsonFluentAssert(actual, path, description, configuration.withOptions(firstOption, otherOptions));
    }

    /**
     * Fails if the node exists.
     *
     * @return
     */
    public JsonFluentAssert isAbsent() {
        if (nodeExists(actual, path)) {
            failWithMessage("Node \"" + path + "\" is present.");
        }
        return this;
    }

    /**
     * Fails if the node is missing.
     */
    public JsonFluentAssert isPresent() {
        if (!nodeExists(actual, path)) {
            failWithMessage("Node \"" + path + "\" is missing.");
        }
        return this;
    }
}
