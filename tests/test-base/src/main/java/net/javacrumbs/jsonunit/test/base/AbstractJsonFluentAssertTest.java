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

import net.javacrumbs.jsonunit.core.Option;
import org.junit.Test;

import java.io.StringReader;

import static java.math.BigDecimal.valueOf;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonPartEquals;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonPartMatches;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
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
import static org.junit.Assert.assertEquals;

public abstract class AbstractJsonFluentAssertTest {
    @Test
    public void testAssertString() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":1}").isEqualTo("{\"test\":2}"))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"test\", expected: <2> but was: <1>.\n");
    }

    @Test
    public void testAssertDifferentType() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":\"1\"}").node("test").isEqualTo("1"))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"test\", expected: <1> but was: <\"1\">.\n");
    }

    @Test
    public void testAssertDifferentTypeInt() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":\"1\"}").node("test").isEqualTo(1))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"test\", expected: <1> but was: <\"1\">.\n");
    }

    @Test
    public void testAssertTolerance() {
        assertThatJson("{\"test\":1.00001}").node("test").withTolerance(0.001).isEqualTo(1);
    }

    @Test
    public void shouldAllowUnquotedKeysAndCommentInExpectedValue() {
        assertThatJson("{\"test\":1}").isEqualTo("{//comment\ntest:1}");
    }

    @Test
    public void testAssertToleranceDifferentOrder() {
        assertThatJson("{\"test\":1.00001}").withTolerance(0.001).node("test").isEqualTo(1);
    }

    @Test
    public void testAssertToleranceDirect() {
        assertThatJson("{\"test\":1.00001}").withTolerance(0.001).isEqualTo("{\"test\":1}");
    }

    @Test
    public void testAssertToleranceFailure() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":1.1}").node("test").withTolerance(0.001).isEqualTo(1))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"test\", expected: <1> but was: <1.1>, difference is 0.1, tolerance is 0.001\n");
    }

    @Test
    public void testAssertNode() {
        assertThatThrownBy(() -> assertThatJson(readValue("{\"test\":1}")).isEqualTo(readValue("{\"test\":2}")))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"test\", expected: <2> but was: <1>.\n");
    }

    @Test
    public void testAssertNodeInExpectOnly() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":1}").isEqualTo(readValue("{\"test\":2}")))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"test\", expected: <2> but was: <1>.\n");
    }

    @Test
    public void testAssertReader() {
        assertThatThrownBy(() -> assertThatJson(new StringReader("{\"test\":1}")).isEqualTo(new StringReader("{\"test\":2}")))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"test\", expected: <2> but was: <1>.\n");
    }

    @Test
    public void testOk() {
        assertThatJson("{\"test\":1}").isEqualTo("{\"test\":1}");
    }

    @Test
    public void testArray() {
        assertThatJson("[1, 2]").node("[0]").isEqualTo(1);
    }

    @Test
    public void testOkNumber() {
        assertThatJson("{\"test\":1}").node("test").isEqualTo(1);
    }

    @Test
    public void testOkNumberInString() {
        assertThatJson("{\"test\":1}").node("test").isEqualTo("1");
    }

    @Test
    public void testOkFloat() {
        assertThatJson("{\"test\":1.1}").node("test").isEqualTo(1.1);
    }

    @Test
    public void testOkNull() {
        assertThatJson("{\"test\":null}").node("test").isEqualTo(null);
    }

    @Test
    public void testNotEqualTo() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":1}").isNotEqualTo("{\"test\": 1}"))
            .hasMessage("JSON is equal.");
    }

    @Test
    public void testSameStructureOk() {
        assertThatJson("{\"test\":1}").hasSameStructureAs("{\"test\":21}");
    }

    @Test
    public void testDifferentStructure() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":1}").hasSameStructureAs("{\"test\":21, \"a\":true}"))
            .hasMessage("JSON documents are different:\nDifferent keys found in node \"\", expected: <[a, test]> but was: <[test]>. Missing: \"a\" \n");
    }

    @Test
    public void testAssertPath() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":1}").node("test").isEqualTo("2"))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"test\", expected: <2> but was: <1>.\n");
    }

    @Test
    public void testAssertPathWithDescription() {
        assertThatThrownBy(() -> assertThatJson(jsonSource("{\"test\":1}", "$")).node("test").isEqualTo("2"))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"$.test\", expected: <2> but was: <1>.\n");
    }

    @Test
    public void testPresentWithDescription() {
        assertThatThrownBy(() -> assertThatJson(jsonSource("{\"test\":1}", "$")).node("test2").isPresent())
            .hasMessage("Different value found in node \"$.test2\", expected: <node to be present> but was: <missing>.");
    }

    @Test
    public void testAssertPathArray() {
        assertThatThrownBy(() -> assertThatJson("{\"root\":{\"test\":[1,2,3]}}").node("root.test[0]").isEqualTo(2))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"root.test[0]\", expected: <2> but was: <1>.\n");
    }

    @Test
    public void testAssertPathArrayOk() {
        assertThatJson("{\"root\":{\"test\":[1,2,3]}}").node("root.test[1]").isEqualTo(2);
    }


    @Test
    public void testLongPaths() {
        assertThatThrownBy(() -> assertThatJson("{\"root\":{\"test\":1}}").node("root.test").isEqualTo("2"))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"root.test\", expected: <2> but was: <1>.\n");
    }

    @Test
    public void testMoreNodes() {
        assertThatThrownBy(() -> assertThatJson("{\"test1\":2, \"test2\":1}").node("test1").isEqualTo(2).node("test2").isEqualTo(2))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"test2\", expected: <2> but was: <1>.\n");
    }

    @Test
    public void testCompareArrays() {
        assertThatJson("[{\"b\": 10}]")
            .isEqualTo(json("[{\"b\": 10}]"));
    }

    @Test
    public void testNodeAbsent() {
        assertThatThrownBy(() -> assertThatJson("{\"test1\":2, \"test2\":1}").node("test2").isAbsent())
            .hasMessage("Different value found in node \"test2\", expected: <node to be absent> but was: <1>.");
    }

    @Test
    public void testNodeAbsentOk() {
        assertThatJson("{\"test1\":2, \"test2\":1}").node("test3").isAbsent();
    }

    @Test
    public void shouldTreatNullAsAbsent() {
        assertThatJson("{\"a\":1, \"b\": null}").when(Option.TREATING_NULL_AS_ABSENT).node("b").isAbsent();
    }

    @Test
    public void testNodePresent() {
        assertThatThrownBy(() -> assertThatJson("{\"test1\":2, \"test2\":1}").node("test3").isPresent())
            .hasMessage("Different value found in node \"test3\", expected: <node to be present> but was: <missing>.");
    }

    @Test
    public void testNodePresentOk() {
        assertThatJson("{\"test1\":2, \"test2\":1}").node("test2").isPresent();
    }

    @Test
    public void testNodePresentNull() {
        assertThatJson("{\"test1\":2, \"test2\":null}").node("test2").isPresent();
    }

    @Test
    public void isPresentShouldTreatNullAsAbsentWhenSpecified() {
        assertThatThrownBy(() -> assertThatJson("{\"test1\":2, \"test2\":null}").when(TREATING_NULL_AS_ABSENT).node("test2").isPresent())
            .hasMessage("Different value found in node \"test2\", expected: <node to be present> but was: <missing>.");
    }

    @Test
    public void shouldAllowWeirdCharsInArrayPattern() {
        assertThatJson("{\"root\": {\n" +
            "  \"@id\" : \"urn:uuid:50aa37c0-eef0-4d72-9f32-17ebbcf17c10\",\n" +
            "  \"@graph\" : [\n" +
            "    { \"foo\" : \"bar\" }\n" +
            "  ]\n" +
            "}}").node("root.@graph[0].foo").isEqualTo("bar");
    }

    @Test
    public void testMessage() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":1}").as("Test is different").isEqualTo("{\"test\":2}"))
            .hasMessage("[Test is different] JSON documents are different:\nDifferent value found in node \"test\", expected: <2> but was: <1>.\n");
    }

    @Test
    public void testIgnore() {
        assertThatJson("{\"test\":1}").isEqualTo("{\"test\":\"${json-unit.ignore}\"}");
    }

    @Test
    public void testIgnoreDifferent() {
        assertThatJson("{\"test\":1}").ignoring("##IGNORE##").isEqualTo("{\"test\":\"##IGNORE##\"}");
    }

    @Test
    public void anyNumberShouldAcceptAnInt() {
        assertThatJson("{\"test\":1}").isEqualTo("{\"test\":\"${json-unit.any-number}\"}");
    }

    @Test
    public void anyNumberShouldAcceptAFloat() {
        assertThatJson("{\"test\":1.1}").isEqualTo("{\"test\":\"${json-unit.any-number}\"}");
    }

    @Test
    public void anyNumberShouldFailOnString() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":\"one\"}").isEqualTo("{\"test\":\"${json-unit.any-number}\"}"))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"test\", expected: <a number> but was: <\"one\">.\n");
    }

    @Test
    public void anyNumberShouldFailOnNull() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":null}").isEqualTo("{\"test\":\"${json-unit.any-number}\"}"))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"test\", expected: <a number> but was: <null>.\n");
    }

    @Test
    public void anyNumberShouldFailOnObject() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":{\"a\":1}}").isEqualTo("{\"test\":\"${json-unit.any-number}\"}"))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"test\", expected: <a number> but was: <{\"a\":1}>.\n");
    }

    @Test
    public void anyBooleanShouldAcceptTrue() {
        assertThatJson("{\"test\":true}").isEqualTo("{\"test\":\"${json-unit.any-boolean}\"}");
    }

    @Test
    public void anyBooleanShouldFailOnString() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":\"true\"}").isEqualTo("{\"test\":\"${json-unit.any-boolean}\"}"))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"test\", expected: <a boolean> but was: <\"true\">.\n");
    }

    @Test
    public void anyBooleanShouldFailOnNull() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":null}").isEqualTo("{\"test\":\"${json-unit.any-boolean}\"}"))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"test\", expected: <a boolean> but was: <null>.\n");
    }

    @Test
    public void anyBooleanShouldFailOnObject() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":{\"a\":1}}").isEqualTo("{\"test\":\"${json-unit.any-boolean}\"}"))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"test\", expected: <a boolean> but was: <{\"a\":1}>.\n");
    }

    @Test
    public void anyStringShouldAcceptAString() {
        assertThatJson("{\"test\":\"value\"}").isEqualTo("{test:'${json-unit.any-string}'}");
    }

    @Test
    public void anyStringShouldFailOnBoolean() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":true}").isEqualTo("{\"test\":\"${json-unit.any-string}\"}"))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"test\", expected: <a string> but was: <true>.\n");
    }

    @Test
    public void anyStringShouldFailOnNull() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":null}").isEqualTo("{\"test\":\"${json-unit.any-string}\"}"))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"test\", expected: <a string> but was: <null>.\n");
    }

    @Test
    public void anyStringShouldFailOnObject() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":{\"a\":1}}").isEqualTo("{\"test\":\"${json-unit.any-string}\"}"))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"test\", expected: <a string> but was: <{\"a\":1}>.\n");
    }

    @Test
    public void emptinessCheck() {
        assertThatJson("{\"test\":{}}").node("test").isEqualTo("{}");
    }

    @Test
    public void ifMatcherDoesNotMatchReportDifference() {
        RecordingDifferenceListener listener = new RecordingDifferenceListener();
        assertThatThrownBy(() -> assertThatJson("{\"test\":-1}").withMatcher("positive", greaterThan(valueOf(0))).withDifferenceListener(listener).isEqualTo("{\"test\": \"${json-unit.matches:positive}\"}"))
            .hasMessage("JSON documents are different:\nMatcher \"positive\" does not match value -1 in node \"test\". <-1> was less than <0>\n");
        assertThat(listener.getDifferenceList()).hasSize(1);
        assertThat(listener.getDifferenceList().get(0).toString()).isEqualTo("DIFFERENT Expected ${json-unit.matches:positive} in test got -1 in test");
    }

    @Test
    public void testLogging() {
        assertThatJson("[\"foo\", \"bar\"]")
        .isArray()
        .ofLength(2)
        .thatContains("foo")
        .thatContains("bar");
    }


    @Test
    public void shoulEscapeDot() {
        assertThatJson("{\"name.with.dot\": \"value\"}").node("name\\.with\\.dot").isStringEqualTo("value");
    }

    @Test
    public void shoulEscapeDotWithArray() {
        assertThatJson("{\"errors\":{\"days[0].date\":[\"validation.failed\"]}}").node("errors.days[0]\\.date").isArray();
    }

    @Test
    public void comparisonShouldFailOnDifferentType() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":1}").isEqualTo("{\"test\":\"1\"}"))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"test\", expected: <\"1\"> but was: <1>.\n");
    }

    @Test
    public void pathShouldBeIgnoredForExtraKey() {
        assertThatJson("{\"root\":{\"test\":1, \"ignored\": 1}}").whenIgnoringPaths("root.ignored").isEqualTo("{\"root\":{\"test\":1}}");
    }

    @Test
    public void pathShouldBeIgnoredForDifferentValue() {
        assertThatJson("{\"root\":{\"test\":1, \"ignored\": 1}}").whenIgnoringPaths("root.ignored").isEqualTo("{\"root\":{\"test\":1, \"ignored\": 2}}");
    }

    @Test
    public void testEqualsToArray() {
        assertThatJson("{\"test\":[1,2,3]}").node("test").isEqualTo(new int[]{1, 2, 3});
    }

    @Test
    public void testEqualsToDoubleArray() {
        assertThatJson("{\"test\":[1.0,2.0,3.0]}").node("test").isEqualTo(new double[]{1, 2, 3});
    }

    @Test
    public void isArrayShouldFailIfArrayDoesNotExist() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":1}").node("test2").isArray())
            .hasMessage("Different value found in node \"test2\", expected: <array> but was: <missing>.");
    }

    @Test
    public void isArrayShouldFailIfItIsNotArray() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":\"1\"}").node("test").isArray())
            .hasMessage("Node \"test\" has invalid type, expected: <array> but was: <\"1\">.");
    }

    @Test
    public void arrayOfLengthShouldFailOnIncorrectSize() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":[1,2,3]}").node("test").isArray().ofLength(2))
            .hasMessage("Node \"test\" has invalid length, expected: <2> but was: <3>.");
    }

    @Test
    public void shouldReportExtraArrayItemsWhenNotIgnoringOrder() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":[1,2,3]}").node("test").isEqualTo("[1]"))
            .hasMessage("JSON documents are different:\n" +
                "Array \"test\" has different length, expected: <1> but was: <3>.\n" +
                "Array \"test\" has different content, expected: <[1]> but was: <[1,2,3]>. Extra values [2, 3]\n");
    }

    @Test
    public void shouldReportExtraArrayItemsWhenIgnoringOrder() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":[1,2,3]}").node("test").when(IGNORING_ARRAY_ORDER).isEqualTo("[1]"))
            .hasMessage("JSON documents are different:\n" +
                "Array \"test\" has different length, expected: <1> but was: <3>.\n" +
                "Array \"test\" has different content, expected: <[1]> but was: <[1,2,3]>. Missing values [], extra values [2, 3]\n");
    }

    @Test
    public void shouldReportMissingArrayItemsWhenNotIgnoringOrder() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":[1]}").node("test").isEqualTo("[1, 2, 3]"))
            .hasMessage("JSON documents are different:\n" +
                "Array \"test\" has different length, expected: <3> but was: <1>.\n" +
                "Array \"test\" has different content, expected: <[1,2,3]> but was: <[1]>. Missing values [2, 3]\n");
    }

    @Test
    public void shouldReportMissingArrayItemsWhenIgnoringOrder() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":[1]}").node("test").when(IGNORING_ARRAY_ORDER).isEqualTo("[1, 2, 3]"))
            .hasMessage("JSON documents are different:\n" +
                "Array \"test\" has different length, expected: <3> but was: <1>.\n" +
                "Array \"test\" has different content, expected: <[1,2,3]> but was: <[1]>. Missing values [2, 3], extra values []\n");
    }

    @Test
    public void shouldReportExtraArrayItemsAndDifferencesWhenNotIgnoringOrder() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":[\"x\",\"b\",\"c\"]}").node("test").isEqualTo("[\"a\"]"))
            .hasMessage("JSON documents are different:\n" +
                "Array \"test\" has different length, expected: <1> but was: <3>.\n" +
                "Array \"test\" has different content, expected: <[\"a\"]> but was: <[\"x\",\"b\",\"c\"]>. Extra values [\"b\", \"c\"]\n" +
                "Different value found in node \"test[0]\", expected: <\"a\"> but was: <\"x\">.\n");
    }

    @Test
    public void negativeArrayIndexShouldCountBackwards() {
        assertThatJson("{\"root\":{\"test\":[1,2,3]}}").node("root.test[-1]").isEqualTo(3);
    }

    @Test
    public void negativeArrayIndexShouldCountBackwardsAndReportFailure() {
        try {
            assertThatJson("{\"root\":{\"test\":[1,2,3]}}").node("root.test[-3]").isEqualTo(3);
        } catch (AssertionError e) {
            assertEquals("JSON documents are different:\n" +
                "Different value found in node \"root.test[-3]\", expected: <3> but was: <1>.\n", e.getMessage());
        }
    }

    @Test
    public void negativeArrayIndexOutOfBounds() {
        try {
            assertThatJson("{\"root\":{\"test\":[1,2,3]}}").node("root.test[-5]").isEqualTo(3);
        } catch (AssertionError e) {
            assertEquals("JSON documents are different:\n" +
                "Missing node in path \"root.test[-5]\".\n", e.getMessage());
        }
    }

    @Test
    public void positiveArrayIndexOutOfBounds() {
        try {
            assertThatJson("{\"root\":{\"test\":[1,2,3]}}").node("root.test[5]").isEqualTo(3);
        } catch (AssertionError e) {
            assertEquals("JSON documents are different:\n" +
                "Missing node in path \"root.test[5]\".\n", e.getMessage());
        }
    }

    @Test
    public void arrayThatContainsShouldFailOnMissingNode() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":[{\"id\":36},{\"id\":37},{\"id\":38}]}").node("test").isArray().thatContains("{\"id\":42}"))
            .hasMessage("Node \"test\" is '[{\"id\":36}, {\"id\":37}, {\"id\":38}]', expected to contain '{\"id\":42}'.");
    }

    @Test
    public void testContains() {
        assertThatJson("[\"foo\", \"bar\"]").isArray().ofLength(2).thatContains("foo").thatContains("bar");
    }

    @Test
    public void intValueShouldMatch() {
        assertThatJson("{\"test\":1}").node("test").matches(equalTo(valueOf(1)));
    }

    @Test
    public void arrayContainsShouldMatch() {
        assertThatJson("[{\"a\": 7},8]").matches(hasItem(jsonEquals("{\"a\": 7}")));
    }

    @Test
    public void testArrayShouldMatchRegardlessOfOrder() {

        final String actual = "{\"response\":[{\"attributes\":null,\"empolyees\":[{\"dob\":\"1987-03-21\",\"firstName\":\"Joe\",\"lastName\":\"Doe\"},{\"dob\":\"1986-02-12\",\"firstName\":\"Jason\",\"lastName\":\"Kowalski\"},{\"dob\":\"1985-01-11\",\"firstName\":\"Kate\",\"lastName\":\"Smith\"}],\"id\":123}]}";
        final String expected = "{\"response\":[{\"attributes\":null,\"empolyees\":[{\"dob\":\"1985-01-11\",\"firstName\":\"Kate\",\"lastName\":\"Smith\"},{\"dob\":\"1986-02-12\",\"firstName\":\"Jason\",\"lastName\":\"Kowalski\"},{\"dob\":\"1987-03-21\",\"firstName\":\"Joe\",\"lastName\":\"Doe\"}],\"id\":123}]}";

        assertThatJson(actual).when(Option.IGNORING_ARRAY_ORDER).isEqualTo(expected);
    }


    @Test
    public void intValueShouldFailIfDoesNotMatch() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":1}").node("test").matches(equalTo(valueOf(2))))
            .hasMessage("Node \"test\" does not match.\nExpected: <2>\n     but: was <1>");
    }

    @Test
    public void floatValueShouldMatch() {
        assertThatJson("{\"test\":1.10001}").node("test").matches(closeTo(valueOf(1.1), valueOf(0.001)));
    }


    @Test
    public void floatValueShouldFailIfDoesNotMatch() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":1}").node("test").matches(equalTo(valueOf(2))))
            .hasMessage("Node \"test\" does not match.\nExpected: <2>\n     but: was <1>");
    }


    @Test
    public void booleanValueShouldMatch() {
        assertThatJson("{\"test\":true}").node("test").matches(equalTo(true));
    }

    @Test
    public void booleanValueShouldFailIfDoesNotMatch() {
        assertThatThrownBy(() -> assertThatJson("{\"test2\":true}").node("test2").matches(equalTo(false)))
            .hasMessage("Node \"test2\" does not match.\nExpected: <false>\n     but: was <true>");
    }

    @Test
    public void missingValueShouldFail() {
        assertThatThrownBy(() -> assertThatJson("{\"test2\":true}").node("test").matches(equalTo(false)))
            .hasMessage("Different value found in node \"test\", expected: <node to be present> but was: <missing>.");
    }

    @Test
    public void stringValueShouldMatch() {
        assertThatJson("{\"test\":\"one\"}").node("test").matches(equalTo("one"));
    }

    @Test
    public void stringValueShouldFailIfDoesNotMatch() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":\"one\"}").node("test").matches(equalTo("two")))
            .hasMessage("Node \"test\" does not match.\nExpected: \"two\"\n     but: was \"one\"");
    }

    @Test
    public void nullValueShouldMatch() {
        assertThatJson("{\"test\":null}").node("test").matches(nullValue());
    }

    @Test
    public void nullValueShouldFailIfDoesNotMatch() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":\"one\"}").node("test").matches(equalTo(nullValue())))
            .hasMessage("Node \"test\" does not match.\nExpected: <null>\n     but: was \"one\"");
    }

    @Test
    public void arrayShouldMatch() {
        assertThatJson("{\"test\":[1,2,3]}").node("test").matches(hasItem(valueOf(1)));
    }

    @Test
    public void arraySizeShouldMatch() {
        assertThatJson("{\"test\":[1,2,3]}").node("test").matches(hasSize(3));
    }


    @Test
    public void arrayMatcherShouldFailIfNotFound() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":[1,2,3]}").node("test").matches(hasItem(4)))
            .hasMessage("Node \"test\" does not match.\nExpected: a collection containing <4>\n" +
                "     but: was <1>, was <2>, was <3>");
    }

    @Test
    public void objectShouldMatch() {
        assertThatJson("{\"test\":[{\"value\":1},{\"value\":2},{\"value\":3}]}").node("test").matches(everyItem(jsonPartMatches("value", lessThanOrEqualTo(valueOf(4)))));
    }

    @Test
    public void objectShouldMatchToMap() {
        assertThatJson("{\"test\":[{\"value\":1},{\"value\":2},{\"value\":3}]}").node("test").matches(hasItem(hasEntry("value", valueOf(1))));
    }

    @Test
    public void objectMatcherShouldFailIfNotFound() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":[{\"value\":1},{\"value\":2},{\"value\":3}]}").node("test").matches(hasItem(jsonPartEquals("value", 4))))
            .hasMessage("Node \"test\" does not match.\n" +
                "Expected: a collection containing 4 in \"value\"\n" +
                "     but: JSON documents are different:\n" +
                "Different value found in node \"value\", expected: <4> but was: <1>.\n" +
                ", JSON documents are different:\n" +
                "Different value found in node \"value\", expected: <4> but was: <2>.\n" +
                ", JSON documents are different:\n" +
                "Different value found in node \"value\", expected: <4> but was: <3>.\n");
    }

    @Test
    public void isStringShouldFailIfItDoesNotExist() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":1}").node("test2").isString())
            .hasMessage("Different value found in node \"test2\", expected: <string> but was: <missing>.");
    }


    @Test
    public void shouldChainStringEquals() {
        assertThatJson("{ \"key1\": \"[value1]\", \"key2\": \"[value2]\" }")
         .node("key1").isStringEqualTo("[value1]")
         .node("key2").isStringEqualTo("[value2]");
    }

    @Test
    public void isStringShouldFailIfItIsNotAString() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":1}").node("test").isString())
            .hasMessage("Node \"test\" has invalid type, expected: <string> but was: <1>.");
    }

    @Test
    public void isStringEqualToShouldFailIfItIsNotAString() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":1}").node("test").isStringEqualTo("1"))
            .hasMessage("Node \"test\" has invalid type, expected: <string> but was: <1>.");
    }

    @Test
    public void isStringEqualToShouldFailIfItDiffers() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":\"2\"}").node("test").isStringEqualTo("1"))
            .hasMessage("Different value found in node \"test\", expected: <\"1\"> but was: <\"2\">.");
    }

    @Test
    public void isStringEqualToShouldPass() {
        assertThatJson("{\"test\":\"1\"}").node("test").isStringEqualTo("1");
    }

    @Test
    public void equalsShouldFailOnStringAndANumber() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":\"1\"}").node("test").isEqualTo("1"))
            .hasMessage("JSON documents are different:\n" +
                "Different value found in node \"test\", expected: <1> but was: <\"1\">.\n");
    }

    @Test
    public void isStringShouldFailOnNull() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":null}").node("test").isStringEqualTo("1"))
            .hasMessage("Node \"test\" has invalid type, expected: <string> but was: <null>.");
    }

    @Test
    public void isStringShouldPass() {
        assertThatJson("{\"test\":\"1\"}").node("test").isString();
    }

    @Test
    public void arrayOfLengthShouldPass() {
        assertThatJson("{\"test\":[1,2,3]}").node("test").isArray().ofLength(3);
    }

    @Test
    public void arrayThatContainsShouldPass() {
        assertThatJson("{\"test\":[{\"id\":36},{\"id\":37},{\"id\":38}]}").node("test").isArray().thatContains("{\"id\":37}");
    }

    @Test
    public void isObjectShouldPassOnObject() {
        assertThatJson("{\"test\":{\"a\":true}}").node("test").isObject();
    }

    @Test
    public void isObjectShouldFailOnBoolean() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":{\"a\":true}}").node("test.a").isObject())
            .hasMessage("Node \"test.a\" has invalid type, expected: <object> but was: <true>.");
    }

    @Test
    public void isObjectShouldFailOnMissing() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":{\"a\":true}}").node("test.b").isObject())
            .hasMessage("Different value found in node \"test.b\", expected: <object> but was: <missing>.");
    }

    @Test(expected = AssertionError.class)
    public void testNotEqualsToToArray() {
        assertThatJson("{\"test\":[1,2,3]}").node("test").isNotEqualTo(new int[]{1, 2, 3});
    }

    @Test
    public void testEqualsToBoolean() {
        assertThatJson("{\"test\":true}").node("test").isEqualTo(true);
    }

    @Test
    public void testEqualsToNull() {
        assertThatJson("{\"test\":null}").node("test").isEqualTo(null);
    }

    @Test(expected = AssertionError.class)
    public void testEqualsToNullFail() {
        assertThatJson("{\"test\":1}").node("test").isEqualTo(null);
    }

    @Test
    public void testNotEqualsToNull() {
        assertThatJson("{\"test\":1}").node("test").isNotEqualTo(null);
    }

    @Test
    public void testIssue3() {
        assertThatJson("{\"someKey\":\"111 text\"}").node("someKey").isEqualTo("\"111 text\"");
    }

    @Test
    public void testIssue3NoSpace() {
        assertThatJson("{\"someKey\":\"111text\"}").node("someKey").isEqualTo("\"111text\"");
    }

    @Test
    public void testIssue3SpaceStrings() {
        assertThatJson("{\"someKey\":\"a b\"}").node("someKey").isEqualTo("a b");
    }

    @Test
    public void testIssue3Original() {
        assertThatJson("{\"someKey\":\"111 text\"}").node("someKey").isEqualTo("111 text");
    }

    @Test
    public void testNullAndAbsent() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":{\"a\":1, \"b\": null}}").isEqualTo("{\"test\":{\"a\":1}}"))
            .hasMessage("JSON documents are different:\n" +
                "Different keys found in node \"test\", expected: <[a]> but was: <[a, b]>.  Extra: \"test.b\"\n");
    }

    @Test
    public void testTreatNullAsAbsent() {
        assertThatJson("{\"test\":{\"a\":1, \"b\": null}}").when(TREATING_NULL_AS_ABSENT).isEqualTo("{\"test\":{\"a\":1}}");
    }

    @Test
    public void shouldIgnoreExtraFields() {
        assertThatJson("{\"test\":{\"a\":1, \"b\":2, \"c\":3}}").when(IGNORING_EXTRA_FIELDS).isEqualTo("{\"test\":{\"b\":2}}");
    }

    @Test
    public void shouldAcceptEscapedPath() {
        assertThatJson("{\"foo.bar\":\"baz\"}").node("foo\\.bar").isEqualTo("baz");
    }

    @Test
    public void shouldAcceptEscapedPathWithTwoDots() {
        assertThatJson("{\"foo.bar.baz\":\"baz\"}").node("foo\\.bar\\.baz").isEqualTo("baz");
    }

    @Test
    public void shouldAcceptEscapedPathAndShowCorrectErrorMessage() {
        assertThatThrownBy(() -> assertThatJson("{\"foo.bar\":\"boo\"}").node("foo\\.bar").isEqualTo("baz"))
            .hasMessage("JSON documents are different:\n" +
                "Different value found in node \"foo\\.bar\", expected: <\"baz\"> but was: <\"boo\">.\n");
    }

    protected abstract Object readValue(String value);
}
