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

import com.fasterxml.jackson.databind.JsonNode;
import net.javacrumbs.jsonunit.core.internal.Diff;

import static net.javacrumbs.jsonunit.core.internal.JsonUtils.convertToJson;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.convertToJsonQuoteIfNeeded;


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
 */
public class JsonFluentAssert {
    private static final String EXPECTED = "expected";
    private static final String ACTUAL = "actual";
    private static final String DEFAULT_IGNORE_PLACEHOLDER = "${json-unit.ignore}";

    private final String path;
    private final JsonNode actual;
    private final String description;
    private final String ignorePlaceholder;

    protected JsonFluentAssert(JsonNode actual, String path, String description, String ignorePlaceholder) {
        if (actual == null) {
            throw new IllegalArgumentException("Can not make assertions about null JSON.");
        }
        this.path = path;
        this.actual = actual;
        this.description = description;
        this.ignorePlaceholder = ignorePlaceholder;
    }

    protected JsonFluentAssert(JsonNode actual) {
        this(actual, "", "", DEFAULT_IGNORE_PLACEHOLDER);
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
        Diff diff = createDiff(expected);
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
        Diff diff = createDiff(expected);
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
        Diff diff = createDiff(expected);
        if (!diff.similarStructure()) {
            failWithMessage(diff.structureDifferences());
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
        return new JsonFluentAssert(actual, path, description, ignorePlaceholder);
    }


    private Diff createDiff(Object expected) {
        return new Diff(convertExpectedToJson(expected), actual, path, ignorePlaceholder);
    }

    private JsonNode convertExpectedToJson(Object expected) {
        return convertToJsonQuoteIfNeeded(expected, EXPECTED);
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
        return new JsonFluentAssert(actual, path, description, ignorePlaceholder);
    }

    /**
     * Sets the placeholder that can be used to ignore values.
     * The default value is ${json-unit.ignore}
     *
     * @param ignorePlaceholder
     * @return
     */
    public JsonFluentAssert ignoring(String ignorePlaceholder) {
        return new JsonFluentAssert(actual, path, description, ignorePlaceholder);
    }
}
