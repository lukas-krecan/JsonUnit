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
package net.javacrumbs.jsonunit.test.base;

import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import net.javacrumbs.jsonunit.core.Option;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static java.math.BigDecimal.valueOf;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonPartEquals;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonPartMatches;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.ConfigurationWhen.path;
import static net.javacrumbs.jsonunit.core.ConfigurationWhen.rootPath;
import static net.javacrumbs.jsonunit.core.ConfigurationWhen.then;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_ARRAY_ITEMS;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static net.javacrumbs.jsonunit.core.Option.TREATING_NULL_AS_ABSENT;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.jsonSource;
import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class AbstractJsonFluentAssertTest {
    @Test
    void testAssertString() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":1}").isEqualTo("{\"test\":2}"))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"test\", expected: <2> but was: <1>.\n");
    }

    @Test
    void testAssertDifferentType() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":\"1\"}").node("test").isEqualTo("1"))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"test\", expected: <1> but was: <\"1\">.\n");
    }

    @Test
    void testAssertDifferentTypeInt() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":\"1\"}").node("test").isEqualTo(1))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"test\", expected: <1> but was: <\"1\">.\n");
    }

    @Test
    void testAssertTolerance() {
        assertThatJson("{\"test\":1.00001}").node("test").withTolerance(0.001).isEqualTo(1);
    }

    @Test
    void shouldAssertEmptyArray() {
        assertThatJson("{\"root\":[]}")
                .node("root")
                .isArray()
                .isEmpty();
    }

    @Test
    void shouldFailOnNonEmptyArray() {
        assertThatThrownBy(() -> assertThatJson("{\"root\":[1]}")
                .node("root")
                .isArray()
                .isEmpty()).hasMessage("Node \"root\" is not an empty array.");
    }

    @Test
    void shouldAssertNonEmptyArray() {
        assertThatJson("{\"root\":[1]}")
                .node("root")
                .isArray()
                .isNotEmpty();
    }

    @Test
    void shouldAssertNonEmptyArrayFailure() {
        assertThatThrownBy(() -> assertThatJson("{\"root\":[]}")
                .node("root")
                .isArray()
                .isNotEmpty()).hasMessage("Node \"root\" is an empty array.");
    }

    @Test
    public void shouldAllowUnquotedKeysAndCommentInExpectedValue() {
        assertThatJson("{\"test\":1}").isEqualTo("{//comment\ntest:1}");
    }

    @Test
    void testAssertToleranceDifferentOrder() {
        assertThatJson("{\"test\":1.00001}").withTolerance(0.001).node("test").isEqualTo(1);
    }

    @Test
    void testAssertToleranceDirect() {
        assertThatJson("{\"test\":1.00001}").withTolerance(0.001).isEqualTo("{\"test\":1}");
    }

    @Test
    void testAssertToleranceFailure() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":1.1}").node("test").withTolerance(0.001).isEqualTo(1))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"test\", expected: <1> but was: <1.1>, difference is 0.1, tolerance is 0.001\n");
    }

    @Test
    void testAssertNode() {
        assertThatThrownBy(() -> assertThatJson(readValue("{\"test\":1}")).isEqualTo(readValue("{\"test\":2}")))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"test\", expected: <2> but was: <1>.\n");
    }

    @Test
    void testAssertNodeInExpectOnly() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":1}").isEqualTo(readValue("{\"test\":2}")))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"test\", expected: <2> but was: <1>.\n");
    }

    @Test
    void testAssertReader() {
        assertThatThrownBy(() -> assertThatJson(new StringReader("{\"test\":1}")).isEqualTo(new StringReader("{\"test\":2}")))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"test\", expected: <2> but was: <1>.\n");
    }

    @Test
    void testOk() {
        assertThatJson("{\"test\":1}").isEqualTo("{\"test\":1}");
    }

    @Test
    void testArray() {
        assertThatJson("[1, 2]").node("[0]").isEqualTo(1);
    }

    @Test
    void testOkNumber() {
        assertThatJson("{\"test\":1}").node("test").isEqualTo(1);
    }

    @Test
    void testOkNumberInString() {
        assertThatJson("{\"test\":1}").node("test").isEqualTo("1");
    }

    @Test
    void testOkFloat() {
        assertThatJson("{\"test\":1.1}").node("test").isEqualTo(1.1);
    }

    @Test
    void testOkNull() {
        assertThatJson("{\"test\":null}").node("test").isEqualTo(null);
    }

    @Test
    void testNotEqualTo() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":1}").isNotEqualTo("{\"test\": 1}"))
            .hasMessage("JSON is equal.");
    }

    @Test
    void testSameStructureOk() {
        assertThatJson("{\"test\":1}").hasSameStructureAs("{\"test\":21}");
    }

    @Test
    void testDifferentStructure() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":1}").hasSameStructureAs("{\"test\":21, \"a\":true}"))
            .hasMessage("JSON documents are different:\n" +
                "Different keys found in node \"\", missing: \"a\", expected: <{\"a\":true,\"test\":21}> but was: <{\"test\":1}>\n");
    }

    @Test
    void testAssertPath() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":1}").node("test").isEqualTo("2"))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"test\", expected: <2> but was: <1>.\n");
    }

    @Test
    void testAssertPathWithDescription() {
        assertThatThrownBy(() -> assertThatJson(jsonSource("{\"test\":1}", "$")).node("test").isEqualTo("2"))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"$.test\", expected: <2> but was: <1>.\n");
    }

    @Test
    void testPresentWithDescription() {
        assertThatThrownBy(() -> assertThatJson(jsonSource("{\"test\":1}", "$")).node("test2").isPresent())
            .hasMessage("Different value found in node \"$.test2\", expected: <node to be present> but was: <missing>.");
    }

    @Test
    void testAssertPathArray() {
        assertThatThrownBy(() -> assertThatJson("{\"root\":{\"test\":[1,2,3]}}").node("root.test[0]").isEqualTo(2))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"root.test[0]\", expected: <2> but was: <1>.\n");
    }

    @Test
    void testAssertPathArrayOk() {
        assertThatJson("{\"root\":{\"test\":[1,2,3]}}").node("root.test[1]").isEqualTo(2);
    }


    @Test
    void testLongPaths() {
        assertThatThrownBy(() -> assertThatJson("{\"root\":{\"test\":1}}").node("root.test").isEqualTo("2"))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"root.test\", expected: <2> but was: <1>.\n");
    }

    @Test
    void testMoreNodes() {
        assertThatThrownBy(() -> assertThatJson("{\"test1\":2, \"test2\":1}").node("test1").isEqualTo(2).node("test2").isEqualTo(2))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"test2\", expected: <2> but was: <1>.\n");
    }

    @Test
    void testCompareArrays() {
        assertThatJson("[{\"b\": 10}]")
            .isEqualTo(json("[{\"b\": 10}]"));
    }

    @Test
    void testCompareArraysIgnoringChildOrder() {
        assertThatJson("[[1,2],[3,4],[5,6]]")
            .when(path("[*]"), then(IGNORING_ARRAY_ORDER))
            .isEqualTo(json("[[2,1],[4,3],[6,5]]"));
    }

    @Test
    void testCompareDifferentArraysIgnoringChildOrder() {
        assertThatJson("[[1,2],[3,4],[5,6]]")
            .when(path("[*]"), then(IGNORING_ARRAY_ORDER))
            .isNotEqualTo(json("[[4,3],[6,5],[2,1]]"));
    }

    @Test
    void testCompareArraysIgnoringChildOrderAndExtraElements() {
        assertThatJson("[[1,2,3],[3,4,5],[5,6,7]]")
            .when(path("[*]"), then(IGNORING_ARRAY_ORDER, IGNORING_EXTRA_ARRAY_ITEMS))
            .isEqualTo(json("[[2,1],[4,3],[6,5]]"));
    }

    @Test
    void testCompareArraysIgnoringChildOrderAndParentExtraElements() {
        assertThatJson("[[1,2],[3,4],[5,6],[7,8]]")
            .when(path("[*]"), then(IGNORING_ARRAY_ORDER))
            .when(rootPath(), then(IGNORING_EXTRA_ARRAY_ITEMS))
            .isEqualTo(json("[[2,1],[4,3],[6,5]]"));
    }

    @Test
    void testNodeAbsent() {
        assertThatThrownBy(() -> assertThatJson("{\"test1\":2, \"test2\":1}").node("test2").isAbsent())
            .hasMessage("Different value found in node \"test2\", expected: <node to be absent> but was: <1>.");
    }

    @Test
    void testNodeAbsentOk() {
        assertThatJson("{\"test1\":2, \"test2\":1}").node("test3").isAbsent();
    }

    @Test
    void shouldTreatNullAsAbsent() {
        assertThatJson("{\"a\":1, \"b\": null}").when(Option.TREATING_NULL_AS_ABSENT).node("b").isAbsent();
    }

    @Test
    void testNodePresent() {
        assertThatThrownBy(() -> assertThatJson("{\"test1\":2, \"test2\":1}").node("test3").isPresent())
            .hasMessage("Different value found in node \"test3\", expected: <node to be present> but was: <missing>.");
    }

    @Test
    void testNodePresentOk() {
        assertThatJson("{\"test1\":2, \"test2\":1}").node("test2").isPresent();
    }

    @Test
    void testNodePresentNull() {
        assertThatJson("{\"test1\":2, \"test2\":null}").node("test2").isPresent();
    }

    @Test
    void isPresentShouldTreatNullAsAbsentWhenSpecified() {
        assertThatThrownBy(() -> assertThatJson("{\"test1\":2, \"test2\":null}").when(TREATING_NULL_AS_ABSENT).node("test2").isPresent())
            .hasMessage("Different value found in node \"test2\", expected: <node to be present> but was: <missing>.");
    }

    @Test
    void shouldAllowWeirdCharsInArrayPattern() {
        assertThatJson("{\"root\": {\n" +
            "  \"@id\" : \"urn:uuid:50aa37c0-eef0-4d72-9f32-17ebbcf17c10\",\n" +
            "  \"@graph\" : [\n" +
            "    { \"foo\" : \"bar\" }\n" +
            "  ]\n" +
            "}}").node("root.@graph[0].foo").isEqualTo("bar");
    }

    @Test
    void testMessage() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":1}").as("Test is different").isEqualTo("{\"test\":2}"))
            .hasMessage("[Test is different] JSON documents are different:\nDifferent value found in node \"test\", expected: <2> but was: <1>.\n");
    }

    @Test
    void testIgnore() {
        assertThatJson("{\"test\":1}").isEqualTo("{\"test\":\"${json-unit.ignore}\"}");
    }


    @Test
    void testIgnoreHash() {
        assertThatJson("{\"test\":1}").isEqualTo("{\"test\":\"#{json-unit.ignore}\"}");
    }

    @Test
    void testIgnoreDifferent() {
        assertThatJson("{\"test\":1}").ignoring("##IGNORE##").isEqualTo("{\"test\":\"##IGNORE##\"}");
    }

    @Test
    void anyNumberShouldAcceptAnInt() {
        assertThatJson("{\"test\":1}").isEqualTo("{\"test\":\"${json-unit.any-number}\"}");
    }

    @Test
    void anyNumberShouldAcceptAFloat() {
        assertThatJson("{\"test\":1.1}").isEqualTo("{\"test\":\"${json-unit.any-number}\"}");
    }

    @Test
    void anyNumberShouldFailOnString() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":\"one\"}").isEqualTo("{\"test\":\"#{json-unit.any-number}\"}"))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"test\", expected: <a number> but was: <\"one\">.\n");
    }

    @Test
    void anyNumberShouldFailOnNull() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":null}").isEqualTo("{\"test\":\"${json-unit.any-number}\"}"))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"test\", expected: <a number> but was: <null>.\n");
    }

    @Test
    void anyNumberShouldFailOnObject() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":{\"a\":1}}").isEqualTo("{\"test\":\"${json-unit.any-number}\"}"))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"test\", expected: <a number> but was: <{\"a\":1}>.\n");
    }

    @Test
    void anyBooleanShouldAcceptTrue() {
        assertThatJson("{\"test\":true}").isEqualTo("{\"test\":\"${json-unit.any-boolean}\"}");
    }

    @Test
    void anyBooleanShouldFailOnString() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":\"true\"}").isEqualTo("{\"test\":\"#{json-unit.any-boolean}\"}"))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"test\", expected: <a boolean> but was: <\"true\">.\n");
    }

    @Test
    void anyBooleanShouldFailOnNull() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":null}").isEqualTo("{\"test\":\"${json-unit.any-boolean}\"}"))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"test\", expected: <a boolean> but was: <null>.\n");
    }

    @Test
    void anyBooleanShouldFailOnObject() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":{\"a\":1}}").isEqualTo("{\"test\":\"${json-unit.any-boolean}\"}"))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"test\", expected: <a boolean> but was: <{\"a\":1}>.\n");
    }

    @Test
    void anyStringShouldAcceptAString() {
        assertThatJson("{\"test\":\"value\"}").isEqualTo("{\"test\":\"${json-unit.any-string}\"}");
    }

    @Test
    void anyStringShouldFailOnBoolean() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":true}").isEqualTo("{\"test\":\"#{json-unit.any-string}\"}"))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"test\", expected: <a string> but was: <true>.\n");
    }

    @Test
    void anyStringShouldFailOnNull() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":null}").isEqualTo("{\"test\":\"${json-unit.any-string}\"}"))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"test\", expected: <a string> but was: <null>.\n");
    }

    @Test
    void anyStringShouldFailOnObject() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":{\"a\":1}}").isEqualTo("{\"test\":\"${json-unit.any-string}\"}"))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"test\", expected: <a string> but was: <{\"a\":1}>.\n");
    }

    @Test
    void emptinessCheck() {
        assertThatJson("{\"test\":{}}").node("test").isEqualTo("{}");
    }

    @Test
    void ifMatcherDoesNotMatchReportDifference() {
        RecordingDifferenceListener listener = new RecordingDifferenceListener();
        assertThatThrownBy(() -> assertThatJson("{\"test\":-1}").withMatcher("positive", greaterThan(valueOf(0))).withDifferenceListener(listener).isEqualTo("{\"test\": \"#{json-unit.matches:positive}\"}"))
            .hasMessage("JSON documents are different:\nMatcher \"positive\" does not match value -1 in node \"test\". <-1> was less than <0>\n");
        assertThat(listener.getDifferenceList()).hasSize(1);
        assertThat(listener.getDifferenceList().get(0).toString()).isEqualTo("DIFFERENT Expected #{json-unit.matches:positive} in test got -1 in test");
    }

    @Test
    void testLogging() {
        assertThatJson("[\"foo\", \"bar\"]")
        .isArray()
        .ofLength(2)
        .thatContains("foo")
        .thatContains("bar");
    }


    @Test
    void shoulEscapeDot() {
        assertThatJson("{\"name.with.dot\": \"value\"}").node("name\\.with\\.dot").isStringEqualTo("value");
    }

    @Test
    void shoulEscapeDotWithArray() {
        assertThatJson("{\"errors\":{\"days[0].date\":[\"validation.failed\"]}}").node("errors.days[0]\\.date").isArray();
    }

    @Test
    void comparisonShouldFailOnDifferentType() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":1}").isEqualTo("{\"test\":\"1\"}"))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"test\", expected: <\"1\"> but was: <1>.\n");
    }

    @Test
    void pathShouldBeIgnoredForExtraKey() {
        assertThatJson("{\"root\":{\"test\":1, \"ignored\": 1}}").whenIgnoringPaths("root.ignored").isEqualTo("{\"root\":{\"test\":1}}");
    }

    @Test
    void pathShouldBeIgnoredForDifferentValue() {
        assertThatJson("{\"root\":{\"test\":1, \"ignored\": 1}}").whenIgnoringPaths("root.ignored").isEqualTo("{\"root\":{\"test\":1, \"ignored\": 2}}");
    }

    @Test
    void testEqualsToArray() {
        assertThatJson("{\"test\":[1,2,3]}").node("test").isEqualTo(new int[]{1, 2, 3});
    }

    @Test
    void testEqualsToDoubleArray() {
        assertThatJson("{\"test\":[1.0,2.0,3.0]}").node("test").isEqualTo(new double[]{1, 2, 3});
    }

    @Test
    void isArrayShouldFailIfArrayDoesNotExist() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":1}").node("test2").isArray())
            .hasMessage("Different value found in node \"test2\", expected: <array> but was: <missing>.");
    }

    @Test
    void isArrayShouldFailIfItIsNotArray() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":\"1\"}").node("test").isArray())
            .hasMessage("Node \"test\" has invalid type, expected: <array> but was: <\"1\">.");
    }

    @Test
    void arrayOfLengthShouldFailOnIncorrectSize() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":[1,2,3]}").node("test").isArray().ofLength(2))
            .hasMessage("Node \"test\" has invalid length, expected: <2> but was: <3>.");
    }

    @Test
    void shouldReportExtraArrayItemsWhenNotIgnoringOrder() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":[1,2,3]}").node("test").isEqualTo("[1]"))
            .hasMessage("JSON documents are different:\n" +
                "Array \"test\" has different length, expected: <1> but was: <3>.\n" +
                "Array \"test\" has different content. Extra values: [2, 3], expected: <[1]> but was: <[1,2,3]>\n");
    }

    @Test
    void shouldReportExtraArrayItemsWhenIgnoringOrder() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":[1,2,3]}").node("test").when(IGNORING_ARRAY_ORDER).isEqualTo("[1]"))
            .hasMessage("JSON documents are different:\n" +
                "Array \"test\" has different length, expected: <1> but was: <3>.\n" +
                "Array \"test\" has different content. Missing values: [], extra values: [2, 3], expected: <[1]> but was: <[1,2,3]>\n");
    }

    @Test
    void shouldReportMissingArrayItemsWhenNotIgnoringOrder() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":[1]}").node("test").isEqualTo("[1, 2, 3]"))
            .hasMessage("JSON documents are different:\n" +
                "Array \"test\" has different length, expected: <3> but was: <1>.\n" +
                "Array \"test\" has different content. Missing values: [2, 3], expected: <[1,2,3]> but was: <[1]>\n");
    }

    @Test
    void shouldReportMissingArrayItemsWhenIgnoringOrder() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":[1]}").node("test").when(IGNORING_ARRAY_ORDER).isEqualTo("[1, 2, 3]"))
            .hasMessage("JSON documents are different:\n" +
                "Array \"test\" has different length, expected: <3> but was: <1>.\n" +
                "Array \"test\" has different content. Missing values: [2, 3], extra values: [], expected: <[1,2,3]> but was: <[1]>\n");
    }

    @Test
    void shouldReportExtraArrayItemsAndDifferencesWhenNotIgnoringOrder() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":[\"x\",\"b\",\"c\"]}").node("test").isEqualTo("[\"a\"]"))
            .hasMessage("JSON documents are different:\n" +
                "Array \"test\" has different length, expected: <1> but was: <3>.\n" +
                "Array \"test\" has different content. Extra values: [\"b\", \"c\"], expected: <[\"a\"]> but was: <[\"x\",\"b\",\"c\"]>\n" +
                "Different value found in node \"test[0]\", expected: <\"a\"> but was: <\"x\">.\n");
    }

    @Test
    void negativeArrayIndexShouldCountBackwards() {
        assertThatJson("{\"root\":{\"test\":[1,2,3]}}").node("root.test[-1]").isEqualTo(3);
    }

    @Test
    void negativeArrayIndexShouldCountBackwardsAndReportFailure() {
        try {
            assertThatJson("{\"root\":{\"test\":[1,2,3]}}").node("root.test[-3]").isEqualTo(3);
        } catch (AssertionError e) {
            assertEquals("JSON documents are different:\n" +
                "Different value found in node \"root.test[-3]\", expected: <3> but was: <1>.\n", e.getMessage());
        }
    }

    @Test
    void negativeArrayIndexOutOfBounds() {
        try {
            assertThatJson("{\"root\":{\"test\":[1,2,3]}}").node("root.test[-5]").isEqualTo(3);
        } catch (AssertionError e) {
            assertEquals("JSON documents are different:\n" +
                "Missing node in path \"root.test[-5]\".\n", e.getMessage());
        }
    }

    @Test
    void positiveArrayIndexOutOfBounds() {
        try {
            assertThatJson("{\"root\":{\"test\":[1,2,3]}}").node("root.test[5]").isEqualTo(3);
        } catch (AssertionError e) {
            assertEquals("JSON documents are different:\n" +
                "Missing node in path \"root.test[5]\".\n", e.getMessage());
        }
    }

    @Test
    void arrayThatContainsShouldFailOnMissingNode() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":[{\"id\":36},{\"id\":37},{\"id\":38}]}").node("test").isArray().thatContains("{\"id\":42}"))
            .hasMessage("Node \"test\" is '[{\"id\":36}, {\"id\":37}, {\"id\":38}]', expected to contain '{\"id\":42}'.");
    }

    @Test
    void testContains() {
        assertThatJson("[\"foo\", \"bar\"]").isArray().ofLength(2).thatContains("foo").thatContains("bar");
    }

    @Test
    void intValueShouldMatch() {
        assertThatJson("{\"test\":1}").node("test").matches(equalTo(valueOf(1)));
    }

    @Test
    void arrayContainsShouldMatch() {
        assertThatJson("[{\"a\": 7},8]").matches(hasItem(jsonEquals("{\"a\": 7}")));
    }

    @Test
    void testArrayShouldMatchRegardlessOfOrder() {

        final String actual = "{\"response\":[{\"attributes\":null,\"empolyees\":[{\"dob\":\"1987-03-21\",\"firstName\":\"Joe\",\"lastName\":\"Doe\"},{\"dob\":\"1986-02-12\",\"firstName\":\"Jason\",\"lastName\":\"Kowalski\"},{\"dob\":\"1985-01-11\",\"firstName\":\"Kate\",\"lastName\":\"Smith\"}],\"id\":123}]}";
        final String expected = "{\"response\":[{\"attributes\":null,\"empolyees\":[{\"dob\":\"1985-01-11\",\"firstName\":\"Kate\",\"lastName\":\"Smith\"},{\"dob\":\"1986-02-12\",\"firstName\":\"Jason\",\"lastName\":\"Kowalski\"},{\"dob\":\"1987-03-21\",\"firstName\":\"Joe\",\"lastName\":\"Doe\"}],\"id\":123}]}";

        assertThatJson(actual).when(Option.IGNORING_ARRAY_ORDER).isEqualTo(expected);
    }


    @Test
    void intValueShouldFailIfDoesNotMatch() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":1}").node("test").matches(equalTo(valueOf(2))))
            .hasMessage("Node \"test\" does not match.\nExpected: <2>\n     but: was <1>");
    }

    @Test
    void floatValueShouldMatch() {
        assertThatJson("{\"test\":1.10001}").node("test").matches(closeTo(valueOf(1.1), valueOf(0.001)));
    }


    @Test
    void floatValueShouldFailIfDoesNotMatch() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":1}").node("test").matches(equalTo(valueOf(2))))
            .hasMessage("Node \"test\" does not match.\nExpected: <2>\n     but: was <1>");
    }


    @Test
    void booleanValueShouldMatch() {
        assertThatJson("{\"test\":true}").node("test").matches(equalTo(true));
    }

    @Test
    void booleanValueShouldFailIfDoesNotMatch() {
        assertThatThrownBy(() -> assertThatJson("{\"test2\":true}").node("test2").matches(equalTo(false)))
            .hasMessage("Node \"test2\" does not match.\nExpected: <false>\n     but: was <true>");
    }

    @Test
    void missingValueShouldFail() {
        assertThatThrownBy(() -> assertThatJson("{\"test2\":true}").node("test").matches(equalTo(false)))
            .hasMessage("Different value found in node \"test\", expected: <node to be present> but was: <missing>.");
    }

    @Test
    void stringValueShouldMatch() {
        assertThatJson("{\"test\":\"one\"}").node("test").matches(equalTo("one"));
    }

    @Test
    void stringValueShouldFailIfDoesNotMatch() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":\"one\"}").node("test").matches(equalTo("two")))
            .hasMessage("Node \"test\" does not match.\nExpected: \"two\"\n     but: was \"one\"");
    }

    @Test
    void nullValueShouldMatch() {
        assertThatJson("{\"test\":null}").node("test").matches(nullValue());
    }

    @Test
    void nullValueShouldFailIfDoesNotMatch() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":\"one\"}").node("test").matches(equalTo(nullValue())))
            .hasMessage("Node \"test\" does not match.\nExpected: <null>\n     but: was \"one\"");
    }

    @Test
    void arrayShouldMatch() {
        assertThatJson("{\"test\":[1,2,3]}").node("test").matches(hasItem(valueOf(1)));
    }

    @Test
    void arraySizeShouldMatch() {
        assertThatJson("{\"test\":[1,2,3]}").node("test").matches(hasSize(3));
    }


    @Test
    void arrayMatcherShouldFailIfNotFound() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":[1,2,3]}").node("test").matches(hasItem(4)))
            .hasMessage("Node \"test\" does not match.\n" +
                "Expected: a collection containing <4>\n" +
                "     but: mismatches were: [was <1>, was <2>, was <3>]");
    }

    @Test
    void objectShouldMatch() {
        assertThatJson("{\"test\":[{\"value\":1},{\"value\":2},{\"value\":3}]}").node("test").matches(everyItem(jsonPartMatches("value", lessThanOrEqualTo(valueOf(4)))));
    }

    @Test
    void objectShouldMatchToMap() {
        assertThatJson("{\"test\":[{\"value\":1},{\"value\":2},{\"value\":3}]}").node("test").matches(hasItem(hasEntry("value", valueOf(1))));
    }

    @Test
    void objectMatcherShouldFailIfNotFound() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":[{\"value\":1},{\"value\":2},{\"value\":3}]}").node("test").matches(hasItem(jsonPartEquals("value", 4))))
            .hasMessage("Node \"test\" does not match.\n" +
                "Expected: a collection containing 4 in \"value\"\n" +
                "     but: mismatches were: [JSON documents are different:\n" +
                "Different value found in node \"value\", expected <4> but was <1>.\n" +
                ", JSON documents are different:\n" +
                "Different value found in node \"value\", expected <4> but was <2>.\n" +
                ", JSON documents are different:\n" +
                "Different value found in node \"value\", expected <4> but was <3>.\n" +
                "]");
    }

    @Test
    void isStringShouldFailIfItDoesNotExist() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":1}").node("test2").isString())
            .hasMessage("Different value found in node \"test2\", expected: <string> but was: <missing>.");
    }


    @Test
    void shouldChainStringEquals() {
        assertThatJson("{ \"key1\": \"[value1]\", \"key2\": \"[value2]\" }")
         .node("key1").isStringEqualTo("[value1]")
         .node("key2").isStringEqualTo("[value2]");
    }

    @Test
    void isStringShouldFailIfItIsNotAString() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":1}").node("test").isString())
            .hasMessage("Node \"test\" has invalid type, expected: <string> but was: <1>.");
    }

    @Test
    void isStringEqualToShouldFailIfItIsNotAString() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":1}").node("test").isStringEqualTo("1"))
            .hasMessage("Node \"test\" has invalid type, expected: <string> but was: <1>.");
    }

    @Test
    void isStringEqualToShouldFailIfItDiffers() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":\"2\"}").node("test").isStringEqualTo("1"))
            .hasMessage("Different value found in node \"test\", expected: <\"1\"> but was: <\"2\">.");
    }

    @Test
    void isStringEqualToShouldPass() {
        assertThatJson("{\"test\":\"1\"}").node("test").isStringEqualTo("1");
    }

    @Test
    void equalsShouldFailOnStringAndANumber() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":\"1\"}").node("test").isEqualTo("1"))
            .hasMessage("JSON documents are different:\n" +
                "Different value found in node \"test\", expected: <1> but was: <\"1\">.\n");
    }

    @Test
    void isStringShouldFailOnNull() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":null}").node("test").isStringEqualTo("1"))
            .hasMessage("Node \"test\" has invalid type, expected: <string> but was: <null>.");
    }

    @Test
    void isStringShouldPass() {
        assertThatJson("{\"test\":\"1\"}").node("test").isString();
    }

    @Test
    void arrayOfLengthShouldPass() {
        assertThatJson("{\"test\":[1,2,3]}").node("test").isArray().ofLength(3);
    }

    @Test
    void arrayThatContainsShouldPass() {
        assertThatJson("{\"test\":[{\"id\":36},{\"id\":37},{\"id\":38}]}").node("test").isArray().thatContains("{\"id\":37}");
    }

    @Test
    void isObjectShouldPassOnObject() {
        assertThatJson("{\"test\":{\"a\":true}}").node("test").isObject();
    }

    @Test
    void isObjectShouldFailOnBoolean() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":{\"a\":true}}").node("test.a").isObject())
            .hasMessage("Node \"test.a\" has invalid type, expected: <object> but was: <true>.");
    }

    @Test
    void isObjectShouldFailOnMissing() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":{\"a\":true}}").node("test.b").isObject())
            .hasMessage("Different value found in node \"test.b\", expected: <object> but was: <missing>.");
    }

    @Test
    void testNotEqualsToToArray() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":[1,2,3]}").node("test").isNotEqualTo(new int[]{1, 2, 3}))
            .hasMessage("JSON is equal.");
    }

    @Test
    void testEqualsToBoolean() {
        assertThatJson("{\"test\":true}").node("test").isEqualTo(true);
    }

    @Test
    void testEqualsToNull() {
        assertThatJson("{\"test\":null}").node("test").isEqualTo(null);
    }

    @Test
    void testEqualsToNullFail() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":1}").node("test").isEqualTo(null))
            .hasMessage("JSON documents are different:\n" +
                "Different value found in node \"test\", expected: <null> but was: <1>.\n");
    }

    @Test
    void testNotEqualsToNull() {
        assertThatJson("{\"test\":1}").node("test").isNotEqualTo(null);
    }

    @Test
    void testIssue3() {
        assertThatJson("{\"someKey\":\"111 text\"}").node("someKey").isEqualTo("\"111 text\"");
    }

    @Test
    void testIssue3NoSpace() {
        assertThatJson("{\"someKey\":\"111text\"}").node("someKey").isEqualTo("\"111text\"");
    }

    @Test
    void testIssue3SpaceStrings() {
        assertThatJson("{\"someKey\":\"a b\"}").node("someKey").isEqualTo("a b");
    }

    @Test
    void testIssue3Original() {
        assertThatJson("{\"someKey\":\"111 text\"}").node("someKey").isEqualTo("111 text");
    }

    @Test
    void testNullAndAbsent() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":{\"a\":1, \"b\": null}}").isEqualTo("{\"test\":{\"a\":1}}"))
            .hasMessage("JSON documents are different:\n" +
                "Different keys found in node \"test\", extra: \"test.b\", expected: <{\"a\":1}> but was: <{\"a\":1,\"b\":null}>\n");
    }

    @Test
    void testTreatNullAsAbsent() {
        assertThatJson("{\"test\":{\"a\":1, \"b\": null}}").when(TREATING_NULL_AS_ABSENT).isEqualTo("{\"test\":{\"a\":1}}");
    }

    @Test
    void shouldIgnoreExtraFields() {
        assertThatJson("{\"test\":{\"a\":1, \"b\":2, \"c\":3}}").when(IGNORING_EXTRA_FIELDS).isEqualTo("{\"test\":{\"b\":2}}");
    }

    @Test
    void shouldAcceptEscapedPath() {
        assertThatJson("{\"foo.bar\":\"baz\"}").node("foo\\.bar").isEqualTo("baz");
    }

    @Test
    void shouldAcceptEscapedPathWithTwoDots() {
        assertThatJson("{\"foo.bar.baz\":\"baz\"}").node("foo\\.bar\\.baz").isEqualTo("baz");
    }

    @Test
    void shouldAcceptEscapedPathAndShowCorrectErrorMessage() {
        assertThatThrownBy(() -> assertThatJson("{\"foo.bar\":\"boo\"}").node("foo\\.bar").isEqualTo("baz"))
            .hasMessage("JSON documents are different:\n" +
                "Different value found in node \"foo\\.bar\", expected: <\"baz\"> but was: <\"boo\">.\n");
    }

    protected abstract Object readValue(String value);
}
