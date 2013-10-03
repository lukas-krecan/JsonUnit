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
package net.javacrumbs.jsonunit.fest;

import net.javacrumbs.jsonunit.core.internal.Diff;
import org.codehaus.jackson.JsonNode;
import org.fest.assertions.api.AbstractAssert;
import org.fest.assertions.description.Description;

import java.io.Reader;
import java.io.StringReader;

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
     *
     * @param json
     * @return new JsonAssert object.
     */
    public static JsonAssert assertThatJson(String json) {
        return assertThatJson(readValue(json, ACTUAL));
    }

    /**
     * Creates a new instance of <code>{@link JsonAssert}</code>.
     * It is not called assertThat to not clash with StringAssert.
     *
     * @param json
     * @return new JsonAssert object.
     */
    public static JsonAssert assertThatJson(StringReader json) {
        return assertThatJson(readValue(json, ACTUAL));
    }

    /**
     * Compares JSON for equality. Ignores order of sibling nodes and whitespaces.
     *
     * @param expected
     * @return {@code this} object.
     */
    @Override
    public JsonAssert isEqualTo(JsonNode expected) {
        isNotNull();

        Diff diff = new Diff(expected, actual, path, ignorePlaceholder);
        if (!diff.similar()) {
            doFail(diff.differences());
        }
        return this;
    }

    /**
     * Compares JSON for equality. Ignores order of sibling nodes and whitespaces.
     *
     * @param expected
     * @return {@code this} object.
     */
    public JsonAssert isEqualTo(String expected) {
        return isEqualTo(readValue(expected, EXPECTED));
    }

    /**
     * Compares JSON for equality. Ignores order of sibling nodes and whitespaces.
     *
     * @param expected
     * @return {@code this} object.
     */
    public JsonAssert isEqualTo(Reader expected) {
        return isEqualTo(readValue(expected, EXPECTED));
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
            doFail(diff.structureDifferences());
        }
        return this;
    }


    /**
     * Compares JSON structure. Ignores values, only compares shape of the document and key names.
     *
     * @param expected
     * @return {@code this} object.
     */
    public JsonAssert hasSameStructureAs(String expected) {
        return hasSameStructureAs(readValue(expected, EXPECTED));
    }

    /**
     * Compares JSON structure. Ignores values, only compares shape of the document and key names.
     *
     * @param expected
     * @return {@code this} object.
     */
    public JsonAssert hasSameStructureAs(Reader expected) {
        return hasSameStructureAs(readValue(expected, EXPECTED));
    }

    /**
     * Creates an assert object that only compares given node.
     *
     * @param path
     * @return object comparing only node given by path.
     */
    public JsonAssert node(String path) {
        return new JsonAssert(actual, path);
    }

    /**
     * Fails a test with the given message.
     */
    private void doFail(String diffMessage) {
        Description description = getWritableAssertionInfo().description();
        throw new AssertionError((description != null ? description.value() + "\n" : "") + diffMessage);
    }

    /**
     * Set's string that will be ignored in comparison. Default value is "${json-unit.ignore}"
     * @param ignorePlaceholder
     */
    public static void setIgnorePlaceholder(String ignorePlaceholder) {
        JsonAssert.ignorePlaceholder = ignorePlaceholder;
    }

    public static String getIgnorePlaceholder() {
        return ignorePlaceholder;
    }
}
