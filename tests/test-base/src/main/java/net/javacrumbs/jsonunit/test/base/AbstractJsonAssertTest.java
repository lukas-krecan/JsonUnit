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
package net.javacrumbs.jsonunit.test.base;

import net.javacrumbs.jsonunit.JsonAssert;
import net.javacrumbs.jsonunit.core.ParametrizedMatcher;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;

import static java.math.BigDecimal.ZERO;
import static java.math.BigDecimal.valueOf;
import static java.util.Collections.singletonMap;
import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static net.javacrumbs.jsonunit.JsonAssert.assertJsonNodeAbsent;
import static net.javacrumbs.jsonunit.JsonAssert.assertJsonNodePresent;
import static net.javacrumbs.jsonunit.JsonAssert.assertJsonNotEquals;
import static net.javacrumbs.jsonunit.JsonAssert.assertJsonPartEquals;
import static net.javacrumbs.jsonunit.JsonAssert.assertJsonPartNotEquals;
import static net.javacrumbs.jsonunit.JsonAssert.assertJsonPartStructureEquals;
import static net.javacrumbs.jsonunit.JsonAssert.assertJsonStructureEquals;
import static net.javacrumbs.jsonunit.JsonAssert.setOptions;
import static net.javacrumbs.jsonunit.JsonAssert.setTolerance;
import static net.javacrumbs.jsonunit.JsonAssert.when;
import static net.javacrumbs.jsonunit.JsonAssert.whenIgnoringPaths;
import static net.javacrumbs.jsonunit.JsonAssert.withMatcher;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_ARRAY_ITEMS;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_VALUES;
import static net.javacrumbs.jsonunit.core.Option.TREATING_NULL_AS_ABSENT;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.jsonSource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.extractor.Extractors.toStringMethod;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;

public abstract class AbstractJsonAssertTest {

    @After
    public void reset() {
        JsonAssert.setTolerance(null);
        JsonAssert.resetOptions();
    }

    @Test
    public void testEquals() {
        assertJsonEquals("{\"test\":1}", "{\n\"test\": 1\n}");
        assertJsonEquals("{\"foo\":\"bar\",\"test\": 1}", "{\n\"test\": 1,\n\"foo\":\"bar\"}");
        assertJsonEquals("{}", "{}");
    }

    @Test
    public void shouldParseExpectedValueLeniently() {
        assertJsonEquals("{//Comment\ntest:'1'}", "{\n\"test\": \"1\"\n}");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailIfQuotationMarksMissingOnActualKeys() {
        assertJsonEquals("{\"test\":1}", "{test: 1}");
    }

    @Test
    public void testArray() {
        assertJsonEquals("[{\"test\":1}, {\"test\":2}]", "[{\n\"test\": 1\n}, {\"test\": 2}]");
    }

    @Test
    public void testArrayDifferent() {
        assertThatThrownBy(() -> assertJsonEquals("[{\"test\":1}, {\"test\":2}]", "[{\n\"test\": 1\n}, {\"test\": 4}]"))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"[1].test\", expected: <2> but was: <4>.\n");
    }

    @Test
    public void testSimple() {
        assertJsonEquals("1", "1");
    }

    @Test
    public void testNumberAndString() {
        setTolerance(0.001);
        assertThatThrownBy(() -> assertJsonEquals(1, "\"hi\""))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"\", expected: <1> but was: <\"hi\">.\n");
    }

    @Test
    public void testSimpleDifferent() {
        assertThatThrownBy(() -> assertJsonEquals("1", "\n2\n"))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"\", expected: <1> but was: <2>.\n");
    }

    @Test
    public void testSimpleIgnore() {
        assertJsonEquals("\"${json-unit.ignore}\"", "\n2\n");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidJsonActual() {
        assertJsonEquals("{\"test\":1}", "{\n\"foo\": 1\n");
    }

    @Test
    public void testObjectName() {
        assertThatThrownBy(() -> assertJsonEquals("{\"test\":1}", "{\n\"foo\": 1\n}"))
            .hasMessage("JSON documents are different:\nDifferent keys found in node \"\", expected: <[test]> but was: <[foo]>. Missing: \"test\" Extra: \"foo\"\n");
    }

    @Test
    public void testEmptyValues() {
        assertJsonEquals("", "");
    }

    @Test
    public void testNulls() {
        assertJsonEquals(null, null);
    }

    @Test
    public void testNotEqualWhenToleranceNotSet() {
        assertThatThrownBy(() -> assertJsonEquals("1", "\n1.0\n"))
            .hasMessage("JSON documents are different:\n" +
                "Different value found in node \"\", expected: <1> but was: <1.0>.\n");
    }

    @Test
    public void testComparisonWhenWithinTolerance() {
        JsonAssert.setTolerance(0.01);
        assertJsonEquals("{\"test\":1}", "{\"test\":1.009}");
    }

    @Test
    public void testComparisonWhenWithinToleranceInlineConfig() {
        assertJsonEquals("{\"test\":1}", "{\"test\":1.009}", JsonAssert.withTolerance(0.01));
    }

    @Test
    public void testComparisonWhenWithinToleranceNegative() {
        JsonAssert.setTolerance(0.01);
        assertJsonEquals("1", "\n0.9999\n");
    }

    @Test
    public void testComparisonWhenOverTolerance() {
        setTolerance(0.01);
        assertThatThrownBy(() -> assertJsonEquals("1", "\n1.1\n"))
            .hasMessage("JSON documents are different:\n" +
                "Different value found in node \"\", expected: <1> but was: <1.1>, difference is 0.1, tolerance is 0.01\n");
    }

    @Test
    public void testNullOk() {
        assertJsonEquals("{\"test\":null}", "{\n\"test\": null\n}");
    }

    @Test
    public void testNullFail() {
        assertThatThrownBy(() -> assertJsonEquals("{\"test\":null}", "{\n\"test\": 1\n}"))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"test\", expected: <null> but was: <1>.\n");
    }

    @Test
    public void testExtraRootKey() {
        assertThatThrownBy(() -> assertJsonEquals("{\"test\":1}", "{\n\"test\": 1\n, \"foo\": 2}"))
            .hasMessage("JSON documents are different:\nDifferent keys found in node \"\", expected: <[test]> but was: <[foo, test]>.  Extra: \"foo\"\n");
    }

    @Test
    public void testIgnoreOneElement() {
        assertThatThrownBy(() -> assertJsonEquals("{\"test\":\"${json-unit.ignore}\"}", "{\n\"test\": 1\n, \"foo\": 2}"))
            .hasMessage("JSON documents are different:\nDifferent keys found in node \"\", expected: <[test]> but was: <[foo, test]>.  Extra: \"foo\"\n");
    }

    @Test
    public void testMissingRootKey() {
        assertThatThrownBy(() -> assertJsonEquals("{\"test\":1, \"foo\": 2}", "{\n\"test\": 1\n}"))
            .hasMessage("JSON documents are different:\nDifferent keys found in node \"\", expected: <[foo, test]> but was: <[test]>. Missing: \"foo\" \n");
    }

    @Test
    public void testDifferentNumericValue() {
        assertThatThrownBy(() -> assertJsonEquals("{\"test\":1}", "{\n\"test\": 2\n}"))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"test\", expected: <1> but was: <2>.\n");
    }

    @Test
    public void testDifferentBooleanValue() {
        assertThatThrownBy(() -> assertJsonEquals("{\"test\":true}", "{\n\"test\": false\n}"))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"test\", expected: <true> but was: <false>.\n");
    }

    @Test
    public void testSameArrayValue() {
        assertJsonEquals("{\"test\":[1, 2, 3]}", "{\n\"test\": [1, 2, 3]\n}");
    }

    @Test
    public void testIgnoreArray() {
        assertJsonEquals("{\"test\":\"${json-unit.ignore}\"}", "{\n\"test\": [1, 2, 3]\n}");
    }

    @Test
    public void testDifferentArrayLength() {
        assertThatThrownBy(() -> assertJsonEquals("{\"test\":[1, 2, 3]}", "{\n\"test\": [1, 2]\n}"))
            .hasMessage("JSON documents are different:\nArray \"test\" has different length, expected: <3> but was: <2>.\n" +
                "Array \"test\" has different content, expected: <[1,2,3]> but was: <[1,2]>. Missing values [3]\n");
    }

    @Test
    public void testDifferentArrayLengthWhenIgnoringArrayOrder() {
        assertThatThrownBy(() -> assertJsonEquals("{\"test\":[\"a\", \"b\", \"c\"]}", "{\n\"test\": [\"a\", \"b\"]\n}", when(IGNORING_ARRAY_ORDER)))
            .hasMessage("JSON documents are different:\n" +
                "Array \"test\" has different length, expected: <3> but was: <2>.\n" +
                "Array \"test\" has different content, expected: <[\"a\",\"b\",\"c\"]> but was: <[\"a\",\"b\"]>. Missing values [\"c\"], extra values []\n");
    }

    @Test
    public void testDifferentArrayValue() {
        assertThatThrownBy(() -> assertJsonEquals("{\"test\":[1, 2, 3]}", "{\n\"test\": [1, 2, 5]\n}"))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"test[2]\", expected: <3> but was: <5>.\n");
    }

    @Test
    public void testDifferentArrayType() {
        assertThatThrownBy(() -> assertJsonEquals("{\"test\":[1, 2, 3]}", "{\n\"test\": [1, false, 3]\n}"))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"test[1]\", expected: <2> but was: <false>.\n");
    }

    @Test
    public void testComplexOk() {
        assertJsonEquals("{\"test\":[1, 2, {\"child\":{\"value1\":1, \"value2\":true, \"value3\": \"test\", \"value4\":{\"leaf\":5}}}], \"root2\": false}",
            "{\"test\":[1, 2, {\"child\":{\"value1\":1, \"value2\":true, \"value3\": \"test\", \"value4\":{\"leaf\":5}}}], \"root2\": false}");

    }

    @Test
    public void testComplexErrors() {
        assertThatThrownBy(() -> assertJsonEquals("{\n" +
                "   \"test\":[\n" +
                "      1,\n" +
                "      2,\n" +
                "      {\n" +
                "         \"child\":{\n" +
                "            \"value1\":1,\n" +
                "            \"value2\":true,\n" +
                "            \"value3\":\"test\",\n" +
                "            \"value4\":{\n" +
                "               \"leaf\":5\n" +
                "            }\n" +
                "         }\n" +
                "      }\n" +
                "   ],\n" +
                "   \"root2\":false,\n" +
                "   \"root3\":1\n" +
                "}",
            "{\n" +
                "   \"test\":[\n" +
                "      5,\n" +
                "      false,\n" +
                "      {\n" +
                "         \"child\":{\n" +
                "            \"value1\":5,\n" +
                "            \"value2\":\"true\",\n" +
                "            \"value3\":\"test\",\n" +
                "            \"value4\":{\n" +
                "               \"leaf2\":5\n" +
                "            }\n" +
                "         },\n" +
                "         \"child2\":{\n" +
                "\n" +
                "         }\n" +
                "      }\n" +
                "   ],\n" +
                "   \"root4\":\"bar\"\n" +
                "}"
        ))
            .hasMessage("JSON documents are different:\n" +
                "Different keys found in node \"\", expected: <[root2, root3, test]> but was: <[root4, test]>. Missing: \"root2\",\"root3\" Extra: \"root4\"\n" +
                "Different value found in node \"test[0]\", expected: <1> but was: <5>.\n" +
                "Different value found in node \"test[1]\", expected: <2> but was: <false>.\n" +
                "Different keys found in node \"test[2]\", expected: <[child]> but was: <[child, child2]>.  Extra: \"test[2].child2\"\n" +
                "Different value found in node \"test[2].child.value1\", expected: <1> but was: <5>.\n" +
                "Different value found in node \"test[2].child.value2\", expected: <true> but was: <\"true\">.\n" +
                "Different keys found in node \"test[2].child.value4\", expected: <[leaf]> but was: <[leaf2]>. Missing: \"test[2].child.value4.leaf\" Extra: \"test[2].child.value4.leaf2\"\n");

    }

    @Test
    public void testIgnoreObject() {
        assertJsonEquals("{\"test\":\"${json-unit.ignore}\"}", "{\n\"test\": {\"object\" : {\"another\" : 1}}}");
    }

    @Test
    public void testIgnoreObjectDifferentPlaceholder() {
        JsonAssert.setIgnorePlaceholder("@IGNORE");
        assertJsonEquals("{\"test\":\"@IGNORE\"}", "{\n\"test\": {\"object\" : {\"another\" : 1}}}");
        JsonAssert.setIgnorePlaceholder("${json-unit.ignore}");
    }

    @Test
    public void testIgnoreObjectErrorInSibling() {
        assertThatThrownBy(() -> assertJsonEquals("{\"test\":{\"object\": \"${json-unit.ignore}\"}, \"sibling\":1}", "{\n\"test\": {\"object\" : {\"another\" : 1}}, \"sibling\" : 2}"))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"sibling\", expected: <1> but was: <2>.\n");
    }

    @Test
    public void testDifferentNumericTypes() {
        assertThatThrownBy(() -> assertJsonEquals("{\"test\":1}", "{\n\"test\": 1.0\n}"))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"test\", expected: <1> but was: <1.0>.\n");
    }

    @Test
    public void testDifferentType() {
        assertThatThrownBy(() -> assertJsonEquals("{\"test\":1}", "{\n\"test\": \"something\"\n}"))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"test\", expected: <1> but was: <\"something\">.\n");
    }

    @Test
    public void testEmpty() {
        assertThatThrownBy(() -> assertJsonEquals("{\"test\":{}}", "{\n\"test\": \"something\"\n}"))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"test\", expected: <{}> but was: <\"something\">.\n");
    }

    @Test
    public void testEmptyOk() {
        assertJsonEquals("{\"test\":{}}", "{\n\"test\": {}\n}");
    }

    @Test
    public void testAssertPartOk() {
        assertJsonPartEquals("1", "{\"test\":{\"value\":1}}", "test.value");
    }

    @Test
    public void testAssertPartOkNumber() {
        assertJsonPartEquals(1, "{\"test\":{\"value\":1}}", "test.value");
    }

    @Test
    public void testAssertPartOkFloat() {
        assertJsonPartEquals(1.1, "{\"test\":{\"value\":1.1}}", "test.value");
    }

    @Test
    public void testAssertPartOkDouble() {
        assertJsonPartEquals(1.1d, "{\"test\":{\"value\":1.1}}", "test.value");
    }

    @Test
    public void testAssertPartOkReaders() {
        assertJsonPartEquals(new StringReader("1"), new StringReader("{\"test\":{\"value\":1}}"), "test.value");
    }

    @Test
    public void testAssertPartOkString() {
        assertJsonPartEquals("a b", "{\"test\":{\"value\":\"a b\"}}", "test.value");
    }

    @Test
    public void testAssertPart() {
        assertThatThrownBy(() -> assertJsonPartEquals("2", "{\"test\":{\"value\":1}}", "test.value"))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"test.value\", expected: <2> but was: <1>.\n");
    }

    @Test
    public void testAssertJsonNodeAbsent() {
        assertThatThrownBy(() -> assertJsonNodeAbsent("{\"test\":{\"value\":1}}", "test.value"))
            .hasMessage("Node \"test.value\" is present.");
    }

    @Test
    public void testAssertJsonNodeAbsentOk() {
        assertJsonNodeAbsent("{\"test\":{\"value\":1}}", "test.different");
    }

    @Test
    public void testAssertJsonNodePresent() {
        assertThatThrownBy(() -> assertJsonNodePresent("{\"test\":{\"value\":1}}", "test.different"))
            .hasMessage("Node \"test.different\" is missing.");
    }

    @Test
    public void testAssertJsonNodePresentInArray() {
        assertThatThrownBy(() -> assertJsonNodePresent("{\"array\":[1, 2]}", "array[3]"))
            .hasMessage("Node \"array[3]\" is missing.");
    }

    @Test
    public void testAssertJsonNodePresentOk() {
        assertJsonNodePresent("{\"test\":{\"value\":1}}", "test.value");
    }

    @Test
    public void testAssertPartComplex() {
        assertThatThrownBy(() -> assertJsonPartEquals("{\"value\":2}", "{\"test\":{\"value\":1}}", "test"))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"test.value\", expected: <2> but was: <1>.\n");
    }

    @Test
    public void testAssertPartArrayOk() {
        assertJsonPartEquals("2", "{\"test\":[{\"value\":1},{\"value\":2}]}", "test[1].value");
    }

    @Test
    public void testAssertPartArray() {
        assertThatThrownBy(() -> assertJsonPartEquals(3, "{\"test\":[{\"value\":1},{\"value\":2}]}", "test[1].value"))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"test[1].value\", expected: <3> but was: <2>.\n");
    }

    @Test
    public void testAssertPartStructureEquals() {
        assertJsonPartStructureEquals("{\"value\":5}", "{\"test\":[{\"value\":1},{\"value\":2}]}", "test[1]");
    }

    @Test
    public void testAssertPartNonexisting() {
        assertThatThrownBy(() -> assertJsonPartEquals("2", "{\"test\":{\"value\":1}}", "test.bogus"))
            .hasMessage("JSON documents are different:\nMissing node in path \"test.bogus\".\n");
    }

    @Test
    public void testAssertStructureEquals() {
        assertJsonStructureEquals("[{\"test\":1}, {\"test\":2}]", "[{\n\"test\": 1\n}, {\"test\": 4}]");
    }

    @Test
    public void testComplexStructureOk() {
        assertJsonStructureEquals("{\n" +
                "   \"test\":[\n" +
                "      1,\n" +
                "      2,\n" +
                "      {\n" +
                "         \"child\":{\n" +
                "            \"value1\":1,\n" +
                "            \"value2\":true,\n" +
                "            \"value3\":\"test\",\n" +
                "            \"value4\":{\n" +
                "               \"leaf\":5\n" +
                "            }\n" +
                "         }\n" +
                "      }\n" +
                "   ],\n" +
                "   \"root2\":false,\n" +
                "   \"root3\":1\n" +
                "}",
            "{\n" +
                "   \"test\":[\n" +
                "      4,\n" +
                "      5,\n" +
                "      {\n" +
                "         \"child\":{\n" +
                "            \"value1\":6,\n" +
                "            \"value2\":false,\n" +
                "            \"value3\":\"different\",\n" +
                "            \"value4\":{\n" +
                "               \"leaf\":6\n" +
                "            }\n" +
                "         }\n" +
                "      }\n" +
                "   ],\n" +
                "   \"root2\":true,\n" +
                "   \"root3\":2\n" +
                "}"
        );
    }

    @Test
    public void testAssertStructureDiffers() {
        assertThatThrownBy(() -> assertJsonStructureEquals("[{\"test\":1}, {\"test\":2}]", "[{\n\"test\": 1\n}, {\"TEST\": 4}]"))
            .hasMessage("JSON documents are different:\nDifferent keys found in node \"[1]\", expected: <[test]> but was: <[TEST]>. Missing: \"[1].test\" Extra: \"[1].TEST\"\n");
    }

    @Test
    public void testAssertStructureArrayDiffers() {
        assertThatThrownBy(() -> assertJsonStructureEquals("[1, 2]", "[1, 2, 3]"))
            .hasMessage("JSON documents are different:\nArray \"\" has different length, expected: <2> but was: <3>.\n");
    }

    @Test
    public void testNullAndAbsent() {
        assertThatThrownBy(() -> assertJsonEquals("{\"test\":{\"a\":1}}", "{\"test\":{\"a\":1, \"b\": null}}"))
            .hasMessage("JSON documents are different:\nDifferent keys found in node \"test\", expected: <[a]> but was: <[a, b]>.  Extra: \"test.b\"\n");
    }

    @Test
    public void testTreatNullAsAbsent() {
        JsonAssert.setOptions(TREATING_NULL_AS_ABSENT);
        assertJsonEquals("{\"test\":{\"a\":1}}", "{\"test\":{\"a\":1, \"b\": null}}");
    }

    @Test
    public void testTreatNullAsAbsentInNode() {
        JsonAssert.setOptions(TREATING_NULL_AS_ABSENT);
        assertJsonNodeAbsent("{\"test\": null}", "test");
    }

    @Test
    public void testTreatNullAsAbsentTwoValues() {
        JsonAssert.setOptions(TREATING_NULL_AS_ABSENT);
        assertJsonEquals("{\"test\":{\"a\":1}}", "{\"test\":{\"a\":1, \"b\": null, \"c\": null}}");
    }

    @Test
    public void testTreatNullAsNullInExpected() {
        setOptions(TREATING_NULL_AS_ABSENT);
        assertThatThrownBy(() -> assertJsonEquals("{\"test\":{\"a\":1, \"b\": null}}", "{\"test\":{\"a\":1}}"))
            .hasMessage("JSON documents are different:\nDifferent keys found in node \"test\", expected: <[a, b]> but was: <[a]>. Missing: \"test.b\" \n");
    }

    @Test
    public void testTreatNullAsNullInExpectedInline() {
        assertThatThrownBy(() -> assertJsonEquals("{\"test\":{\"a\":1, \"b\": null}}", "{\"test\":{\"a\":1}}", when(TREATING_NULL_AS_ABSENT)))
            .hasMessage("JSON documents are different:\nDifferent keys found in node \"test\", expected: <[a, b]> but was: <[a]>. Missing: \"test.b\" \n");
    }

    @Test
    public void shouldIgnoreArrayOrder() {
        JsonAssert.setOptions(IGNORING_ARRAY_ORDER);
        assertJsonEquals("{\"test\":[1,2,3]}", "{\"test\":[3,2,1]}");
    }

    @Test
    public void shouldIgnoreArrayOrderOnObjectArrays() {
        JsonAssert.setOptions(IGNORING_ARRAY_ORDER);
        assertJsonEquals("{\"test\":[{\"key\":1},{\"key\":2},{\"key\":3}]}", "{\"test\":[{\"key\":3},{\"key\":2},{\"key\":1}]}");
    }

    @Test
    public void shouldReportOnDifferentValuesWhenIgnoringOrder() {
        try {
            assertJsonEquals(
                "{\"test\":[{\"key\":1},{\"key\":2},{\"key\":3}]}",
                "{\"test\":[{\"key\":4},{\"key\":2},{\"key\":1}]}",
                when(IGNORING_ARRAY_ORDER)
            );
        } catch (AssertionError e) {
            assertEquals("JSON documents are different:\nDifferent value found when comparing expected array element test[2] to actual element test[0].\nDifferent value found in node \"test[0].key\", expected: <3> but was: <4>.\n", e.getMessage());
        }
    }

    @Test
    public void shouldReportOnDifferentValuesWhenIgnoringOrderActualsSame() {
        try {
            assertJsonEquals(
                "{\"test\":[{\"key\":1},{\"key\":1},{\"key\":2}]}",
                "{\"test\":[{\"key\":1},{\"key\":1},{\"key\":1}]}",
                when(IGNORING_ARRAY_ORDER)
            );
        } catch (AssertionError e) {
            assertEquals("JSON documents are different:\nDifferent value found when comparing expected array element test[2] to actual element test[2].\nDifferent value found in node \"test[2].key\", expected: <2> but was: <1>.\n", e.getMessage());
        }
    }

    @Test
    public void shouldFailIfArrayContentIsDifferent() {
        setOptions(IGNORING_ARRAY_ORDER);
        assertThatThrownBy(() -> assertJsonEquals("{\"test\":[1,2,3]}", "{\"test\":[3,2,4]}"))
            .hasMessage("JSON documents are different:\n" +
                "Different value found when comparing expected array element test[0] to actual element test[2].\n" +
                "Different value found in node \"test[2]\", expected: <1> but was: <4>.\n");
    }

    @Test
    public void arraysShouldMatchEvenWhenIgnoringExtraFields() {
        assertJsonEquals("[[2],[1]]", "[[1,2],[2]]", when(IGNORING_ARRAY_ORDER, IGNORING_EXTRA_ARRAY_ITEMS));
    }

    @Test
    public void arraysShouldMatchEvenWhenIgnoringExtraFieldsComplex() {
        assertJsonEquals("[[3],[2],[1]]", "[[1,2],[2,3],[3,1]]", when(IGNORING_ARRAY_ORDER, IGNORING_EXTRA_ARRAY_ITEMS));
    }

    @Test
    public void arraysShouldMatchEvenWhenIgnoringExtraFieldsInEmbeddedObjects() {
        assertJsonEquals("[{\"a\":[\"b\"]},{\"a\":[\"a\"]}]", "[{\"a\":[\"b\",\"a\"]},{\"a\":[\"b\",\"c\"]}]", when(IGNORING_ARRAY_ORDER, IGNORING_EXTRA_ARRAY_ITEMS));
    }

    @Test
    public void arraysMatchShouldReportErrorCorrectlyWhenIgnoringExtraFields() {
        assertThatThrownBy(() -> assertJsonEquals("[[2],[1]]", "[[1,2],[3]]", when(IGNORING_ARRAY_ORDER, IGNORING_EXTRA_ARRAY_ITEMS)))
            .hasMessage("JSON documents are different:\n" +
                "Different value found when comparing expected array element [1] to actual element [1].\n" +
                "Different value found when comparing expected array element [1][0] to actual element [1][0].\n" +
                "Different value found in node \"[1][0]\", expected: <1> but was: <3>.\n");
    }

    @Test
    public void arraysMatchShouldReportErrorCorrectlyWhenIgnoringExtraFieldsComplex() {
        assertThatThrownBy(() -> assertJsonEquals("[[3],[2],[1]]", "[[1,2],[2,3],[2,4]]", when(IGNORING_ARRAY_ORDER, IGNORING_EXTRA_ARRAY_ITEMS)))
            .hasMessage("JSON documents are different:\n" +
                "Different value found when comparing expected array element [2] to actual element [2].\n" +
                "Array \"[2]\" has different content, expected: <[1]> but was: <[2,4]>. Missing values [1]\n");
    }

    @Test
    public void arraysMatchShouldReportErrorCorrectlyWhenIgnoringExtraFieldsInEmbeddedObjects() {
        assertThatThrownBy(() -> assertJsonEquals("[{\"a\":[\"b\"]},{\"a\":[\"a\"]}]", "[{\"a\":[\"b\",\"a\"]},{\"a\":[\"d\",\"c\"]}]", when(IGNORING_ARRAY_ORDER, IGNORING_EXTRA_ARRAY_ITEMS)))
            .hasMessage("JSON documents are different:\n" +
                "Different value found when comparing expected array element [1] to actual element [1].\n" +
                "Array \"[1].a\" has different content, expected: <[\"a\"]> but was: <[\"d\",\"c\"]>. Missing values [\"a\"]\n");
    }

    @Test
    public void shouldFailIfArrayContentIsDifferentMoreThaOneDifference() {
        RecordingDifferenceListener listener = new RecordingDifferenceListener();
        assertThatThrownBy(() -> assertJsonEquals("{\"test\":[1,2,3]}", "{\"test\":[3,4,5]}", when(IGNORING_ARRAY_ORDER).withDifferenceListener(listener)))
            .hasMessage("JSON documents are different:\n" +
                "Array \"test\" has different content, expected: <[1,2,3]> but was: <[3,4,5]>. Missing values [1, 2], extra values [4, 5]\n");

        assertThat(listener.getDifferenceList()).hasSize(4);
        assertThat(listener.getDifferenceList()).extracting(toStringMethod()).containsExactly(
            "MISSING 1 in test[0]",
            "MISSING 2 in test[1]",
            "EXTRA 4 in test[1]",
            "EXTRA 5 in test[2]"
        );
    }

    @Test
    public void shouldFailIfArrayContentIsDifferentOnObjectArrays() {
        setOptions(IGNORING_ARRAY_ORDER);
        assertThatThrownBy(() -> assertJsonEquals("{\"test\":[{\"key\":1},{\"key\":2},{\"key\":3}]}", "{\"test\":[{\"key\":3},{\"key\":2},{\"key\":4}]}"))
            .hasMessage("JSON documents are different:\n" +
                "Different value found when comparing expected array element test[0] to actual element test[2].\n" +
                "Different value found in node \"test[2].key\", expected: <1> but was: <4>.\n");
    }

    @Test
    public void shouldIgnoreExtraFieldsIfRequested() {
        JsonAssert.setOptions(IGNORING_EXTRA_FIELDS);
        assertJsonEquals("{\"test\":{\"b\":2}}", "{\"test\":{\"a\":1, \"b\":2, \"c\":3}}");
    }

    @Test
    public void shouldIgnoreExtraFieldsInArray() {
        JsonAssert.setOptions(IGNORING_ARRAY_ORDER, IGNORING_EXTRA_FIELDS);
        assertJsonEquals("{\"test\":[{\"key\":1},{\"key\":2},{\"key\":3}]}", "{\"test\":[{\"key\":3},{\"key\":2, \"extraField\":2},{\"key\":1}]}");
    }

    @Test
    public void shouldIgnoreValues() {
        JsonAssert.setOptions(IGNORING_VALUES);
        assertJsonEquals("{\"test\":{\"a\":1,\"b\":2,\"c\":3}}", "{\"test\":{\"a\":3,\"b\":2,\"c\":1}}");
    }

    @Test
    public void shouldIgnoreValuesInArray() {
        JsonAssert.setOptions(IGNORING_VALUES);
        assertJsonEquals("{\"test\":[{\"a\":1},{\"b\":2},{\"c\":3}]}", "{\"test\":[{\"a\":3},{\"b\":2},{\"c\":1}]}");
    }

    @Test
    public void shouldFailIfIgnoringValuesButTypesAreDifferent() {
        setOptions(IGNORING_VALUES);
        assertThatThrownBy(() -> assertJsonEquals("{\"test\":{\"a\":1,\"b\":2,\"c\":3}}", "{\"test\":{\"a\":3,\"b\":\"2\",\"c\":1}}"))
            .hasMessage("JSON documents are different:\n" +
                "Different value found in node \"test.b\", expected: <2> but was: <\"2\">.\n");
    }

    @Test
    public void shouldFailIfIgnoringValuesButTypesAreDifferentInArray() {
        setOptions(IGNORING_VALUES);
        assertThatThrownBy(() -> assertJsonEquals("{\"test\":[{\"a\":1},{\"b\":2},{\"c\":3}]}", "{\"test\":[{\"a\":1},{\"b\":\"2\"},{\"c\":3}]}"))
            .hasMessage("JSON documents are different:\n" +
                "Different value found in node \"test[1].b\", expected: <2> but was: <\"2\">.\n");
    }

    @Test
    public void shouldIgnoreValuesWhenToleranceSet() {
        JsonAssert.setTolerance(0.01);
        JsonAssert.setOptions(IGNORING_VALUES);
        assertJsonEquals("5", "\n0.9999\n");
    }

    @Test
    public void assertPartNotEqualsShouldPass() {
        assertJsonPartNotEquals("2", "{\"test\":{\"value\":1}}", "test.value");
    }

    @Test
    public void assertPartNotEqualsShouldFailWithCorrectMessage() {
        assertThatThrownBy(() -> assertJsonPartNotEquals("1", "{\"test\":{\"value\":1}}", "test.value"))
            .hasMessage("Expected different values in node \"test.value\" but the values were equal.");
    }

    @Test
    public void assertNotEqualsShouldPass() {
        assertJsonNotEquals("{\"test\":{\"value\":2}}", "{\"test\":{\"value\":1}}");
    }

    @Test
    public void assertNotEqualsShouldFailWithCorrectMessage() {
        assertThatThrownBy(() -> assertJsonNotEquals("{\"test\":{\"value\":1}}", "{\"test\":{\"value\":1}}"))
            .hasMessage("Expected different values but the values were equal.");
    }

    @Test
    public void structureEqualsShouldPassOnNull() {
        assertJsonStructureEquals("{\"test\": 3}", null);
    }

    @Test
    public void strictStructureEqualsShouldFailOnNull() {
        assertThatThrownBy(() -> assertJsonEquals("{\"test\": 3}", null, when(IGNORING_VALUES)))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"\", expected: <{\"test\":3}> but was: <null>.\n");
    }

    @Test
    public void structureEqualsShouldPassOnDifferentType() {
        assertJsonStructureEquals("{\"test\": 3}", "{\"test\": \"3\"}");
    }

    @Test
    public void strictStructureEqualsShouldFailOnDifferentType() {
        assertThatThrownBy(() -> assertJsonEquals("{\"test\": 3}", "{\"test\": \"3\"}", when(IGNORING_VALUES)))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"test\", expected: <3> but was: <\"3\">.\n");
    }

    @Test
    public void strictStructureEqualsShouldPassOnDifferentValue() {
        assertJsonEquals("{\"test\": \"4\"}", "{\"test\": \"3\"}", when(IGNORING_VALUES));
    }

    @Test
    public void shouldNotAssertArraySizeWithEmptyArrays() {
        assertJsonEquals("{\"test\":[]}", "{\"test\":[]}", when(IGNORING_EXTRA_ARRAY_ITEMS));
    }

    @Test
    public void shouldNotAssertArraySizeWithExpectedArrayEmpty() {
        assertJsonEquals("{\"test\":[]}", "{\"test\":[\"a\"]}", when(IGNORING_EXTRA_ARRAY_ITEMS));
    }

    @Test
    public void shouldNotAssertArraySize() {
        assertJsonEquals("{\"test\":[\"a\"]}", "{\"test\":[\"a\",\"b\",\"c\"]}", when(IGNORING_EXTRA_ARRAY_ITEMS));
    }

    @Test
    public void shouldCompareOnlyFirstElements() {
        assertJsonEquals("{\"test\":[{\"a\":1},{\"b\":2}]}", "{\"test\":[{\"a\":1},{\"b\":2},{\"c\":3}]}", when(IGNORING_EXTRA_ARRAY_ITEMS));
    }

    @Test
    public void shouldFailIfFirstElementsDoNotMatch() {
        assertThatThrownBy(() -> assertJsonEquals("{\"test\":[1,2,3]}", "{\"test\":[1,2,5,4]}", when(IGNORING_EXTRA_ARRAY_ITEMS)))
            .hasMessage("JSON documents are different:\n" +
                "Different value found in node \"test[2]\", expected: <3> but was: <5>.\n");
    }

    @Test
    public void shouldIgnoreExtraItemInTheMiddleIfOrderIsIgnored() {
        assertJsonEquals("{\"test\":[{\"a\":1},{\"b\":2}]}", "{\"test\":[{\"a\":1},{\"c\":3},{\"b\":2}]}", when(IGNORING_EXTRA_ARRAY_ITEMS, IGNORING_ARRAY_ORDER));
    }

    @Test
    public void shouldAcceptLongerShuffledArray() {
        assertJsonEquals("{\"test\":[1,2,3]}", "{\"test\":[5,5,4,4,3,3,2,2,1,1]}", when(IGNORING_EXTRA_ARRAY_ITEMS, IGNORING_ARRAY_ORDER));
    }

    @Test
    public void shouldFailIfOneValueIsMissing() {
        assertThatThrownBy(() -> assertJsonEquals("{\"test\":[1,2,3,9]}", "{\"test\":[5,5,4,4,3,3,2,2,1,1]}", when(IGNORING_EXTRA_ARRAY_ITEMS, IGNORING_ARRAY_ORDER)))
            .hasMessage("JSON documents are different:\n" +
                "Array \"test\" has different content, expected: <[1,2,3,9]> but was: <[5,5,4,4,3,3,2,2,1,1]>. Missing values [9]\n");
    }

    @Test
    public void shouldFailIfIfExpectedIsLongerAndOrderIsIgnored() {
        assertThatThrownBy(() -> assertJsonEquals("{\"test\":[1,2,3,9]}", "{\"test\":[3,2,1]}", when(IGNORING_EXTRA_ARRAY_ITEMS, IGNORING_ARRAY_ORDER)))
            .hasMessage("JSON documents are different:\n" +
                "Array \"test\" has invalid length, expected: <at least 4> but was: <3>.\n" +
                "Array \"test\" has different content, expected: <[1,2,3,9]> but was: <[3,2,1]>. Missing values [9]\n");
    }

    @Test
    public void shouldFailIfIfExpectedIsLongerAndOrderIsIgnoredAndTheItemsDoNotMatch() {
        assertThatThrownBy(() -> assertJsonEquals("{\"test\":[5,6,7,8]}", "{\"test\":[3,2,1]}", when(IGNORING_EXTRA_ARRAY_ITEMS, IGNORING_ARRAY_ORDER)))
            .hasMessage("JSON documents are different:\n" +
                "Array \"test\" has invalid length, expected: <at least 4> but was: <3>.\n" +
                "Array \"test\" has different content, expected: <[5,6,7,8]> but was: <[3,2,1]>. Missing values [5, 6, 7, 8]\n");
    }

    @Test
    public void shouldNotIgnoreExtraItemInTheMiddle() {
        assertThatThrownBy(() -> assertJsonEquals("{\"test\":[{\"a\":1},{\"b\":2}]}", "{\"test\":[{\"a\":1},{\"c\":3},{\"b\":2}]}", when(IGNORING_EXTRA_ARRAY_ITEMS)))
            .hasMessage("JSON documents are different:\n" +
                "Different keys found in node \"test[1]\", expected: <[b]> but was: <[c]>. Missing: \"test[1].b\" Extra: \"test[1].c\"\n");
    }

    @Test
    public void shouldFailIfFirstElementsAreDifferent() {
        assertThatThrownBy(() -> assertJsonEquals("{\"test\":[{\"a\":1},{\"b\":2}]}", "{\"test\":[{\"a\":1},{\"b\":1}]}", when(IGNORING_EXTRA_ARRAY_ITEMS)))
            .hasMessage("JSON documents are different:\n" +
                "Different value found in node \"test[1].b\", expected: <2> but was: <1>.\n");
    }

    @Test
    public void shouldFailIfExpectsMoreArrayItems() {
        assertThatThrownBy(() -> assertJsonEquals("{\"test\":[{\"a\":1},{\"b\":1},{\"c\":3}]}", "{\"test\":[{\"a\":1},{\"b\":1}]}", when(IGNORING_EXTRA_ARRAY_ITEMS)))
            .hasMessage("JSON documents are different:\n" +
                "Array \"test\" has invalid length, expected: <at least 3> but was: <2>.\n" +
                "Array \"test\" has different content, expected: <[{\"a\":1},{\"b\":1},{\"c\":3}]> but was: <[{\"a\":1},{\"b\":1}]>. Missing values [{\"c\":3}]\n");
    }

    @Test
    public void shouldFailIfExpectsMoreArrayItemsAndDifferentValues() {
        assertThatThrownBy(() -> assertJsonEquals("{\"test\":[{\"a\":1},{\"b\":2},{\"c\":3}]}", "{\"test\":[{\"a\":1},{\"b\":1}]}", when(IGNORING_EXTRA_ARRAY_ITEMS)))
            .hasMessage("JSON documents are different:\n" +
                "Array \"test\" has invalid length, expected: <at least 3> but was: <2>.\n" +
                "Array \"test\" has different content, expected: <[{\"a\":1},{\"b\":2},{\"c\":3}]> but was: <[{\"a\":1},{\"b\":1}]>. Missing values [{\"c\":3}]\n" +
                "Different value found in node \"test[1].b\", expected: <2> but was: <1>.\n");
    }

    @Test
    public void shouldAddPathPrefixToPath() {
        assertThatThrownBy(() -> assertJsonPartEquals("2", jsonSource("{\"test\":1}", "$"), "test"))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"$.test\", expected: <2> but was: <1>.\n");
    }

    @Test
    public void testEqualsNode() throws IOException {
        assertJsonEquals(readValue("{\"test\":1}"), readValue("{\"test\": 1}"));
    }

    @Test
    public void testEqualsNodeIgnore() throws IOException {
        assertJsonEquals(readValue("{\"test\":\"${json-unit.ignore}\"}"), readValue("{\"test\": 1}"));
    }

    @Test
    public void testRegex() {
        assertJsonEquals("{\"test\": \"${json-unit.regex}[A-Z]+\"}", "{\"test\": \"ABCD\"}");
    }

    @Test
    public void regexShouldFail() {
        assertThatThrownBy(() -> assertJsonEquals("{\"test\": \"${json-unit.regex}[A-Z]+\"}", "{\"test\": \"123\"}"))
            .hasMessage("JSON documents are different:\n" +
                "Different value found in node \"test\". Pattern \"[A-Z]+\" did not match \"123\".\n");
    }

    @Test
    public void regexShouldFailOnNullGracefully() {
        assertThatThrownBy(() -> assertJsonEquals("{\"test\": \"${json-unit.regex}[A-Z]+\"}", "{\"test\": null}"))
            .hasMessage("JSON documents are different:\n" +
                "Different value found in node \"test\", expected: <\"${json-unit.regex}[A-Z]+\"> but was: <null>.\n");
    }

    @Test
    public void regexShouldFailOnNumberGracefully() {
        assertThatThrownBy(() -> assertJsonEquals("{\"test\": \"${json-unit.regex}[A-Z]+\"}", "{\"test\": 123}"))
            .hasMessage("JSON documents are different:\n" +
                "Different value found in node \"test\", expected: <\"${json-unit.regex}[A-Z]+\"> but was: <123>.\n");
    }

    @Test
    public void regexShouldFailOnNonexistingGracefully() {
        assertThatThrownBy(() -> assertJsonEquals("{\"test\": \"${json-unit.regex}[A-Z]+\"}", "{\"test2\": 123}"))
            .hasMessage("JSON documents are different:\n" +
                "Different keys found in node \"\", expected: <[test]> but was: <[test2]>. Missing: \"test\" Extra: \"test2\"\n");
    }


    @Test
    public void matcherShouldMatch() {
        assertJsonEquals("{\"test\": \"${json-unit.matches:positive}\"}", "{\"test\":1}", JsonAssert.withMatcher("positive", greaterThan(valueOf(0))));
    }

    @Test
    public void matcherParameterShouldBeIgnored() {
        assertJsonEquals("{\"test\": \"${json-unit.matches:positive}param\"}", "{\"test\":1}", JsonAssert.withMatcher("positive", greaterThan(valueOf(0))));
    }

    @Test
    public void parametrizedMatcherShouldFail() {
        Matcher<?> divisionMatcher = new DivisionMatcher();
        try {
            assertJsonEquals("{test: '${json-unit.matches:isDivisibleBy}3'}", "{\"test\":5}", JsonAssert.withMatcher("isDivisibleBy", divisionMatcher));
        } catch (AssertionError e) {
            assertEquals("JSON documents are different:\nMatcher \"isDivisibleBy\" does not match value 5 in node \"test\". It is not divisible by <3>\n", e.getMessage());
        }
    }

    @Test
    public void parametrizedMatcherShouldMatch() {
        Matcher<?> divisionMatcher = new DivisionMatcher();
        assertJsonEquals("{test: '${json-unit.matches:isDivisibleBy}3'}", "{\"test\":6}", JsonAssert.withMatcher("isDivisibleBy", divisionMatcher));
    }

    @Test(expected = NumberFormatException.class)
    public void missingParameterShouldResultInEmptyString() {
        Matcher<?> divisionMatcher = new DivisionMatcher();
        assertJsonEquals("{test: '${json-unit.matches:isDivisibleBy}'}", "{\"test\":6}", JsonAssert.withMatcher("isDivisibleBy", divisionMatcher));
    }

    @Test
    public void shouldUseMultipleMatchers() {
        Matcher<?> divisionMatcher = new DivisionMatcher();
        Matcher<?> emptyMatcher = empty();

        assertJsonEquals(
            "{test: '${json-unit.matches:isDivisibleBy}3', x: '${json-unit.matches:isEmpty}'}",
            "{\"test\":6, \"x\": []}",
            JsonAssert
                .withMatcher("isDivisibleBy", divisionMatcher)
                .withMatcher("isEmpty", emptyMatcher)
        );

    }

    @Test
    public void matcherNameShouldBeparsedUntilFirstCurlyBrace() {
        String expected =
            "{ \"o\" : \"${json-unit.matches:embedded}{\\\"x\\\" : \\\"y\\\"}\" }";
        String actual =
            "{ \"o\" : \"{\\\"x\\\" : \\\"y\\\"}\" }";

        JsonAssert.assertJsonEquals(expected, actual,
            JsonAssert.withMatcher("embedded", new TrueMatcher()));

    }

    @Test
    public void parameterMatchingShouldWork() {
        assertThatThrownBy(() -> assertJsonEquals("{\"test\": [\"${json-unit.matches:eq}1\", \"${json-unit.matches:eq}2\"]}", "{\"test\":[2, 2]}", withMatcher("eq", new NumEqualsMatcher()).when(IGNORING_ARRAY_ORDER)))
            .hasMessage("JSON documents are different:\n" +
                "Different value found when comparing expected array element test[0] to actual element test[1].\n" +
                "Matcher \"eq\" does not match value 2 in node \"test[1]\". \n");
    }


    private static class DivisionMatcher extends BaseMatcher<Object> implements ParametrizedMatcher {
        private BigDecimal param;

        public boolean matches(Object item) {
            return ((BigDecimal)item).remainder(param).compareTo(ZERO) == 0;
        }

        public void describeTo(Description description) {
            description.appendValue(param);
        }

        @Override
        public void describeMismatch(Object item, Description description) {
            description.appendText("It is not divisible by ").appendValue(param);
        }

        public void setParameter(String parameter) {
            this.param = new BigDecimal(parameter);
        }
    }

    private static class TrueMatcher extends BaseMatcher<Object> implements ParametrizedMatcher {
        private String param;

        public boolean matches(Object item) {
            return true;
        }

        public void describeTo(Description description) {
            description.appendValue(param);
        }

        @Override
        public void describeMismatch(Object item, Description description) {

        }

        public void setParameter(String parameter) {
            this.param = parameter;
        }
    }

    private static class NumEqualsMatcher extends BaseMatcher<Object> implements ParametrizedMatcher {

        private BigDecimal param;

        @Override
        public boolean matches(Object item) {
            return param.compareTo((BigDecimal) item) == 0;
        }

        public void describeTo(Description description) {
            description.appendValue(param);
        }

        @Override
        public void describeMismatch(Object item, Description description) {

        }

        public void setParameter(String parameter) {
            this.param = new BigDecimal(parameter);
        }


    }


    @Test
    public void pathShouldBeIgnoredForDifferentValue() {
        assertJsonEquals("{\"root\":{\"test\":1, \"ignored\": 2}}", "{\"root\":{\"test\":1, \"ignored\": 1}}", JsonAssert.whenIgnoringPaths("root.ignored"));
    }

    @Test
    public void arrayWildcardForPathIgnoring() {
        assertJsonEquals("[{\"a\":1, \"b\":0},{\"a\":2, \"b\":0}]", "[{\"a\":1, \"b\":2},{\"a\":2, \"b\":3}]", JsonAssert.whenIgnoringPaths("[*].b"));
    }

    @Test
    public void arrayWildcardForPathIgnoringArrayOrder() {
        assertJsonEquals("[{\"a\":1, \"b\":0},{\"a\":2, \"b\":0}]", "[{\"a\":2, \"b\":2},{\"a\":1, \"b\":3}]", JsonAssert.whenIgnoringPaths("[*].b").when(IGNORING_ARRAY_ORDER));
    }

    @Test
    public void arrayWildcardShouldWorkWhenIgnoringArrayOrder() {
        String expected = "{\n" +
            "  \"calendars\": [\n" +
            "    {\n" +
            "      \"dates\": [\n" +
            "        {\n" +
            "          \"id\": \"date-8c9c9ffa58fee47b47c2c1eb511c4e6c\",\n" +
            "          \"type\": \"NOTIFICATION\",\n" +
            "          \"from\": 1\n" +
            "        }\n" +
            "      ]\n" +
            "    }\n" +
            "  ]\n" +
            "}";

        String actual = "{\n" +
            "  \"calendars\": [\n" +
            "    {\n" +
            "      \"dates\": [\n" +
            "        {\n" +
            "          \"id\": \"date-8c9c9ffa58fee47b47c2c1eb511c4e6c\",\n" +
            "          \"type\": \"NOTIFICATION\",\n" +
            "          \"from\": 2\n" +
            "        }\n" +
            "      ]\n" +
            "    }\n" +
            "  ]\n" +
            "}";

        assertJsonEquals(expected, actual,
            JsonAssert.whenIgnoringPaths(
                "calendars[*].dates[*].from"
            ).when(IGNORING_ARRAY_ORDER)
        );
    }

    @Test
    public void arrayWildcardForPathIgnoringMultidimensional() {
        assertJsonEquals("[[1, 2], [3, 4], [5, 6]]", "[[2, 2], [2, 4], [2, 6]]", JsonAssert.whenIgnoringPaths("[*][0]"));
    }

    @Test
    public void multidimensionalArrayCompareFailure() {
        try {
            assertJsonEquals("[[1, 2], [3, 4], [5, 6]]", "[[2, 2], [2, 4], [2, 6]]");
        } catch (AssertionError e) {
            assertEquals("JSON documents are different:\n" +
                "Different value found in node \"[0][0]\", expected: <1> but was: <2>.\n" +
                "Different value found in node \"[1][0]\", expected: <3> but was: <2>.\n" +
                "Different value found in node \"[2][0]\", expected: <5> but was: <2>.\n", e.getMessage());
        }
    }

    @Test
    public void arrayWildcardForPathIgnoringFail() {
        assertThatThrownBy(() -> assertJsonEquals("[{\"a\":1, \"b\":5},{\"a\":1, \"b\":5}]", "[{\"a\":1, \"b\":2},{\"a\":1, \"b\":3}]", whenIgnoringPaths("[*].a")))
            .hasMessage("JSON documents are different:\n" +
                "Different value found in node \"[0].b\", expected: <5> but was: <2>.\n" +
                "Different value found in node \"[1].b\", expected: <5> but was: <3>.\n");
    }

    @Test
    public void ifMatcherDoesNotMatchReportDifference() {
        assertThatThrownBy(() -> assertJsonEquals("{\"test\": \"${json-unit.matches:positive}\"}", "{\"test\":-1}", withMatcher("positive", greaterThan(valueOf(0)))))
            .hasMessage("JSON documents are different:\nMatcher \"positive\" does not match value -1 in node \"test\". <-1> was less than <0>\n");
    }

    @Test
    public void failIfMatcherNotFound() {
        assertThatThrownBy(() -> assertJsonEquals("{\"test\": \"${json-unit.matches:unknown}\"}", "{\"test\":-1}"))
            .hasMessage("JSON documents are different:\nMatcher \"unknown\" not found.\n");
    }


    @Test
    public void testEqualsNodeFail() {
        assertThatThrownBy(() -> assertJsonEquals(readValue("{\"test\":1}"), "{\"test\": 2}"))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"test\", expected: <1> but was: <2>.\n");
    }

    @Test
    public void testEqualsNodeStringFail() throws IOException {
        assertThatThrownBy(() -> assertJsonEquals("{\"test\":\"a\"}", readValue("{\"test\": \"b\"}")))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"test\", expected: <\"a\"> but was: <\"b\">.\n");
    }

    @Test
    public void testBinary() {
        assertJsonEquals("{\"binary\":\"aGk=\"}", singletonMap("binary", "hi".getBytes()));
    }

    protected abstract Object readValue(String value);


}
