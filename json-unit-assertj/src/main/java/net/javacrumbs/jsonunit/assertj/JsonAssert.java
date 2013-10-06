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
package net.javacrumbs.jsonunit.assertj;

import net.javacrumbs.jsonunit.core.internal.Diff;
import org.assertj.core.api.AbstractAssert;
import org.codehaus.jackson.JsonNode;

import java.io.Reader;

import static net.javacrumbs.jsonunit.core.internal.JsonUtils.convertToJson;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.readValue;


/**
 * Contains JSON related assertions. Typical usage is:
 * <p/>
 * <code>
 * assertThatJson("{\"test\":1}").isEqualTo("{\"test\":2}");
 * assertThatJson("{\"test\":1}").hasSameStructureAs("{\"test\":21}");
 * assertThatJson("{\"root\":{\"test\":1}}").node("root.test").isEqualTo("2");
 * </code>
 * <p/>
 * Please note that the method name is assertThatJson and not assertThat. The reason is that we need to accept String parameter
 * and do not want to override standard FEST assertThat(String) method.
 */
public class JsonAssert extends AbstractAssert<JsonAssert, JsonNode> {
    private static final String EXPECTED = "expected";
    private static final String ACTUAL = "actual";

    private static String ignorePlaceholder = "${json-unit.ignore}";

    private final String path;

    protected JsonAssert(JsonNode actual, String path) {
        super(actual, JsonAssert.class);
        this.path = path;
    }


    /**
     * Creates a new instance of <code>{@link JsonAssert}</code>.
     * It is not called assertThat to not clash with StringAssert.
     *
     * @param json
     * @return new JsonAssert object.
     */
    public static JsonAssert assertThatJson(JsonNode json) {
        return new JsonAssert(json, "");
    }

    /**
     * Creates a new instance of <code>{@link JsonAssert}</code>.
     * It is not called assertThat to not clash with StringAssert.
     * The json parameter is converted to JSON using ObjectMapper.
     *
     * @param json
     * @return new JsonAssert object.
     */
    public static JsonAssert assertThatJson(Object json) {
        return assertThatJson(convertToJson(json, ACTUAL));
    }

    /**
     * Compares JSON for equality. Ignores order of sibling nodes and whitespaces.
     *
     * @param expected
     * @return {@code this} object.
     */
    public JsonAssert isEqualTo(JsonNode expected) {
        isNotNull();

        Diff diff = new Diff(expected, actual, path, ignorePlaceholder);
        if (!diff.similar()) {
            failWithMessage(diff.differences());
        }
        return this;
    }

    /**
     * Compares JSON for equality. The expected object is converted to JSON
     * before comparison. Ignores order of sibling nodes and whitespaces.
     *
     * @param expected
     * @return {@code this} object.
     */
    @Override
    public JsonAssert isEqualTo(Object expected) {
        return isEqualTo(convertToJson(expected, EXPECTED));
    }

    /**
     * Fails if compared documents are equal.
     *
     * @param expected
     * @return {@code this} object.
     */
    public JsonAssert isNotEqualTo(JsonNode expected) {
        isNotNull();

        Diff diff = new Diff(expected, actual, path, ignorePlaceholder);
        if (diff.similar()) {
            failWithMessage("JSON is equal.");
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
    @Override
    public JsonAssert isNotEqualTo(Object expected) {
        return isNotEqualTo(convertToJson(expected, EXPECTED));
    }

    /**
     * Compares JSON structure. Ignores values, only compares shape of the document and key names.
     *
     * @param expected
     * @return {@code this} object.
     */
    public JsonAssert hasSameStructureAs(JsonNode expected) {
        isNotNull();

        Diff diff = new Diff(expected, actual, path, ignorePlaceholder);
        if (!diff.similarStructure()) {
            failWithMessage(diff.structureDifferences());
        }
        return this;
    }


    /**
     * Compares JSON structure. Ignores values, only compares shape of the document and key names.
     *
     * @param expected
     * @return {@code this} object.
     */
    public JsonAssert hasSameStructureAs(Object expected) {
        return hasSameStructureAs(convertToJson(expected, EXPECTED));
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
    public JsonAssert node(String path) {
        return new JsonAssert(actual, path);
    }

    @Override
    public JsonAssert isIn(Object... values) {
        throw new UnsupportedOperationException("isIn assertion is not yet supported.");
    }

    @Override
    public JsonAssert isIn(Iterable<?> values) {
        throw new UnsupportedOperationException("isIn assertion is not yet supported.");
    }

    @Override
    public JsonAssert isNotIn(Object... values) {
        throw new UnsupportedOperationException("isNotIn assertion is not yet supported.");
    }

    @Override
    public JsonAssert isNotIn(Iterable<?> values) {
        throw new UnsupportedOperationException("isNotIn assertion is not yet supported.");
    }

    /**
     * Set's string that will be ignored in comparison. Default value is "${json-unit.ignore}"
     *
     * @param ignorePlaceholder
     */
    public static void setIgnorePlaceholder(String ignorePlaceholder) {
        JsonAssert.ignorePlaceholder = ignorePlaceholder;
    }

    public static String getIgnorePlaceholder() {
        return ignorePlaceholder;
    }
}
