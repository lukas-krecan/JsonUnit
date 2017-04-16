/**
 * Copyright 2009-2017 the original author or authors.
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
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static net.javacrumbs.jsonunit.core.Option.TREATING_NULL_AS_ABSENT;
import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;
import static net.javacrumbs.jsonunit.test.base.JsonTestUtils.failIfNoException;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertEquals;

public abstract class AbstractJsonFluentAssertTest {
    @Test
    public void testAssertString() {
        try {
            assertThatJson("{\"test\":1}").isEqualTo("{\"test\":2}");
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("JSON documents are different:\nDifferent value found in node \"test\". Expected 2, got 1.\n", e.getMessage());
        }
    }

    @Test
    public void testAssertDifferentType() {
        try {
            assertThatJson("{\"test\":\"1\"}").node("test").isEqualTo("1");
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("JSON documents are different:\nDifferent value found in node \"test\". Expected '1', got '\"1\"'.\n", e.getMessage());
        }
    }

    @Test
    public void testAssertDifferentTypeInt() {
        try {
            assertThatJson("{\"test\":\"1\"}").node("test").isEqualTo(1);
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("JSON documents are different:\nDifferent value found in node \"test\". Expected '1', got '\"1\"'.\n", e.getMessage());
        }
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
        try {
            assertThatJson("{\"test\":1.1}").node("test").withTolerance(0.001).isEqualTo(1);
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("JSON documents are different:\nDifferent value found in node \"test\". Expected 1, got 1.1, difference is 0.1, tolerance is 0.001\n", e.getMessage());
        }
    }

    @Test
    public void testAssertNode() {
        try {
            assertThatJson(readValue("{\"test\":1}")).isEqualTo(readValue("{\"test\":2}"));
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("JSON documents are different:\nDifferent value found in node \"test\". Expected 2, got 1.\n", e.getMessage());
        }
    }

    @Test
    public void testAssertNodeInExpectOnly() {
        try {
            assertThatJson("{\"test\":1}").isEqualTo(readValue("{\"test\":2}"));
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("JSON documents are different:\nDifferent value found in node \"test\". Expected 2, got 1.\n", e.getMessage());
        }
    }

    @Test
    public void testAssertReader() {
        try {
            assertThatJson(new StringReader("{\"test\":1}")).isEqualTo(new StringReader("{\"test\":2}"));
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("JSON documents are different:\nDifferent value found in node \"test\". Expected 2, got 1.\n", e.getMessage());
        }
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
        try {
            assertThatJson("{\"test\":1}").isNotEqualTo("{\"test\": 1}");
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("JSON is equal.", e.getMessage());
        }
    }

    @Test
    public void testSameStructureOk() {
        assertThatJson("{\"test\":1}").hasSameStructureAs("{\"test\":21}");
    }

    @Test
    public void testDifferentStructure() {
        try {
            assertThatJson("{\"test\":1}").hasSameStructureAs("{\"test\":21, \"a\":true}");
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("JSON documents are different:\nDifferent keys found in node \"\". Expected [a, test], got [test]. Missing: \"a\" \n", e.getMessage());
        }
    }

    @Test
    public void testAssertPath() {
        try {
            assertThatJson("{\"test\":1}").node("test").isEqualTo("2");
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("JSON documents are different:\nDifferent value found in node \"test\". Expected 2, got 1.\n", e.getMessage());
        }
    }

    @Test
    public void testAssertPathArray() {
        try {
            assertThatJson("{\"root\":{\"test\":[1,2,3]}}").node("root.test[0]").isEqualTo(2);
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("JSON documents are different:\nDifferent value found in node \"root.test[0]\". Expected 2, got 1.\n", e.getMessage());
        }
    }

    @Test
    public void testAssertPathArrayOk() {
        assertThatJson("{\"root\":{\"test\":[1,2,3]}}").node("root.test[1]").isEqualTo(2);
    }


    @Test
    public void testLongPaths() {
        try {
            assertThatJson("{\"root\":{\"test\":1}}").node("root.test").isEqualTo("2");
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("JSON documents are different:\nDifferent value found in node \"root.test\". Expected 2, got 1.\n", e.getMessage());
        }
    }

    @Test
    public void testMoreNodes() {
        try {
            assertThatJson("{\"test1\":2, \"test2\":1}").node("test1").isEqualTo(2).node("test2").isEqualTo(2);
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("JSON documents are different:\nDifferent value found in node \"test2\". Expected 2, got 1.\n", e.getMessage());
        }
    }

    @Test
    public void testDeprecation() {
        assertThatJson("{\"test1\":2, \"test2\":1}").node("test1").isEqualTo(2).when(TREATING_NULL_AS_ABSENT);
    }

    @Test
    public void testNodeAbsent() {
        try {
            assertThatJson("{\"test1\":2, \"test2\":1}").node("test2").isAbsent();
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("Node \"test2\" is present.", e.getMessage());
        }
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
        try {
            assertThatJson("{\"test1\":2, \"test2\":1}").node("test3").isPresent();
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("Node \"test3\" is missing.", e.getMessage());
        }
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
        try {
            assertThatJson("{\"test1\":2, \"test2\":null}").when(Option.TREATING_NULL_AS_ABSENT).node("test2").isPresent();
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("Node \"test2\" is missing.", e.getMessage());
        }
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
        try {
            assertThatJson("{\"test\":1}").as("Test is different").isEqualTo("{\"test\":2}");
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("[Test is different] JSON documents are different:\nDifferent value found in node \"test\". Expected 2, got 1.\n", e.getMessage());
        }
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
        try {
            assertThatJson("{\"test\":\"one\"}").isEqualTo("{\"test\":\"${json-unit.any-number}\"}");
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("JSON documents are different:\nDifferent value found in node \"test\". Expected a number, got '\"one\"'.\n", e.getMessage());
        }
    }

    @Test
    public void anyNumberShouldFailOnNull() {
        try {
            assertThatJson("{\"test\":null}").isEqualTo("{\"test\":\"${json-unit.any-number}\"}");
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("JSON documents are different:\nDifferent value found in node \"test\". Expected a number, got 'null'.\n", e.getMessage());
        }
    }

    @Test
    public void anyNumberShouldFailOnObject() {
        try {
            assertThatJson("{\"test\":{\"a\":1}}").isEqualTo("{\"test\":\"${json-unit.any-number}\"}");
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("JSON documents are different:\nDifferent value found in node \"test\". Expected a number, got '{\"a\":1}'.\n", e.getMessage());
        }
    }

    @Test
    public void anyBooleanShouldAcceptTrue() {
        assertThatJson("{\"test\":true}").isEqualTo("{\"test\":\"${json-unit.any-boolean}\"}");
    }

    @Test
    public void anyBooleanShouldFailOnString() {
        try {
            assertThatJson("{\"test\":\"true\"}").isEqualTo("{\"test\":\"${json-unit.any-boolean}\"}");
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("JSON documents are different:\nDifferent value found in node \"test\". Expected a boolean, got '\"true\"'.\n", e.getMessage());
        }
    }

    @Test
    public void anyBooleanShouldFailOnNull() {
        try {
            assertThatJson("{\"test\":null}").isEqualTo("{\"test\":\"${json-unit.any-boolean}\"}");
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("JSON documents are different:\nDifferent value found in node \"test\". Expected a boolean, got 'null'.\n", e.getMessage());
        }
    }

    @Test
    public void anyBooleanShouldFailOnObject() {
        try {
            assertThatJson("{\"test\":{\"a\":1}}").isEqualTo("{\"test\":\"${json-unit.any-boolean}\"}");
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("JSON documents are different:\nDifferent value found in node \"test\". Expected a boolean, got '{\"a\":1}'.\n", e.getMessage());
        }
    }

    @Test
    public void anyStringShouldAcceptAString() {
        assertThatJson("{\"test\":\"value\"}").isEqualTo("{test:'${json-unit.any-string}'}");
    }

    @Test
    public void anyStringShouldFailOnBoolean() {
        try {
            assertThatJson("{\"test\":true}").isEqualTo("{\"test\":\"${json-unit.any-string}\"}");
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("JSON documents are different:\nDifferent value found in node \"test\". Expected a string, got 'true'.\n", e.getMessage());
        }
    }

    @Test
    public void anyStringShouldFailOnNull() {
        try {
            assertThatJson("{\"test\":null}").isEqualTo("{\"test\":\"${json-unit.any-string}\"}");
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("JSON documents are different:\nDifferent value found in node \"test\". Expected a string, got 'null'.\n", e.getMessage());
        }
    }

    @Test
    public void anyStringShouldFailOnObject() {
        try {
            assertThatJson("{\"test\":{\"a\":1}}").isEqualTo("{\"test\":\"${json-unit.any-string}\"}");
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("JSON documents are different:\nDifferent value found in node \"test\". Expected a string, got '{\"a\":1}'.\n", e.getMessage());
        }
    }

    @Test
    public void comparisonShouldFailOnDifferentType() {
        try {
            assertThatJson("{\"test\":1}").isEqualTo("{\"test\":\"1\"}");
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("JSON documents are different:\nDifferent value found in node \"test\". Expected '\"1\"', got '1'.\n", e.getMessage());
        }
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
        try {
            assertThatJson("{\"test\":1}").node("test2").isArray();
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("Node \"test2\" is missing.", e.getMessage());
        }
    }

    @Test
    public void isArrayShouldFailIfItIsNotArray() {
        try {
            assertThatJson("{\"test\":\"1\"}").node("test").isArray();
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("Node \"test\" is not an array. The actual value is '\"1\"'.", e.getMessage());
        }
    }

    @Test
    public void arrayOfLengthShouldFailOnIncorrectSize() {
        try {
            assertThatJson("{\"test\":[1,2,3]}").node("test").isArray().ofLength(2);
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("Node \"test\" length is 3, expected length is 2.", e.getMessage());
        }
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
                "Different value found in node \"root.test[-3]\". Expected 3, got 1.\n", e.getMessage());
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
        try {
            assertThatJson("{\"test\":[{\"id\":36},{\"id\":37},{\"id\":38}]}").node("test").isArray().thatContains("{\"id\":42}");
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("Node \"test\" is '[{\"id\":36}, {\"id\":37}, {\"id\":38}]', expected to contain '{\"id\":42}'.", e.getMessage());
        }
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
        try {
            assertThatJson("{\"test\":1}").node("test").matches(equalTo(valueOf(2)));
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("Node \"test\" does not match.\nExpected: <2>\n     but: was <1>", e.getMessage());
        }
    }

    @Test
    public void floatValueShouldMatch() {
        assertThatJson("{\"test\":1.10001}").node("test").matches(closeTo(valueOf(1.1), valueOf(0.001)));
    }


    @Test
    public void floatValueShouldFailIfDoesNotMatch() {
        try {
            assertThatJson("{\"test\":1}").node("test").matches(equalTo(valueOf(2)));
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("Node \"test\" does not match.\nExpected: <2>\n     but: was <1>", e.getMessage());
        }
    }


    @Test
    public void booleanValueShouldMatch() {
        assertThatJson("{\"test\":true}").node("test").matches(equalTo(true));
    }

    @Test
    public void booleanValueShouldFailIfDoesNotMatch() {
        try {
            assertThatJson("{\"test2\":true}").node("test2").matches(equalTo(false));
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("Node \"test2\" does not match.\nExpected: <false>\n     but: was <true>", e.getMessage());
        }
    }

    @Test
    public void missingValueShouldFail() {
        try {
            assertThatJson("{\"test2\":true}").node("test").matches(equalTo(false));
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("Node \"test\" is missing.", e.getMessage());
        }
    }

    @Test
    public void stringValueShouldMatch() {
        assertThatJson("{\"test\":\"one\"}").node("test").matches(equalTo("one"));
    }

    @Test
    public void stringValueShouldFailIfDoesNotMatch() {
        try {
            assertThatJson("{\"test\":\"one\"}").node("test").matches(equalTo("two"));
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("Node \"test\" does not match.\nExpected: \"two\"\n     but: was \"one\"", e.getMessage());
        }
    }

    @Test
    public void nullValueShouldMatch() {
        assertThatJson("{\"test\":null}").node("test").matches(nullValue());
    }

    @Test
    public void nullValueShouldFailIfDoesNotMatch() {
        try {
            assertThatJson("{\"test\":\"one\"}").node("test").matches(equalTo(nullValue()));
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("Node \"test\" does not match.\nExpected: <null>\n     but: was \"one\"", e.getMessage());
        }
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
        try {
            assertThatJson("{\"test\":[1,2,3]}").node("test").matches(hasItem(4));
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("Node \"test\" does not match.\nExpected: a collection containing <4>\n" +
                "     but: was <1>, was <2>, was <3>", e.getMessage());
        }
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
        try {
            assertThatJson("{\"test\":[{\"value\":1},{\"value\":2},{\"value\":3}]}").node("test").matches(hasItem(jsonPartEquals("value", 4)));
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("Node \"test\" does not match.\n" +
                "Expected: a collection containing 4 in \"value\"\n" +
                "     but: JSON documents are different:\n" +
                "Different value found in node \"value\". Expected 4, got 1.\n" +
                ", JSON documents are different:\n" +
                "Different value found in node \"value\". Expected 4, got 2.\n" +
                ", JSON documents are different:\n" +
                "Different value found in node \"value\". Expected 4, got 3.\n", e.getMessage());
        }
    }

    @Test
    public void isStringShouldFailIfItDoesNotExist() {
        try {
            assertThatJson("{\"test\":1}").node("test2").isString();
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("Node \"test2\" is missing.", e.getMessage());
        }
    }

    @Test
    public void isStringShouldFailIfItIsNotAString() {
        try {
            assertThatJson("{\"test\":1}").node("test").isString();
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("Node \"test\" is not a string. The actual value is '1'.", e.getMessage());
        }
    }

    @Test
    public void isStringEqualToShouldFailIfItIsNotAString() {
        try {
            assertThatJson("{\"test\":1}").node("test").isStringEqualTo("1");
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("Node \"test\" is not a string. The actual value is '1'.", e.getMessage());
        }
    }

    @Test
    public void isStringEqualToShouldFailIfItDiffers() {
        try {
            assertThatJson("{\"test\":\"2\"}").node("test").isStringEqualTo("1");
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("Node \"test\" is not equal to \"1\".", e.getMessage());
        }
    }

    @Test
    public void isStringEqualToShouldPass() {
        assertThatJson("{\"test\":\"1\"}").node("test").isStringEqualTo("1");
    }

    @Test
    public void equalsShouldFailOnStringAndANumber() {
        try {
            assertThatJson("{\"test\":\"1\"}").node("test").isEqualTo("1");
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("JSON documents are different:\n" +
                "Different value found in node \"test\". Expected '1', got '\"1\"'.\n", e.getMessage());
        }
    }

    @Test
    public void isStringShouldFailOnNull() {
        try {
            assertThatJson("{\"test\":null}").node("test").isStringEqualTo("1");
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("Node \"test\" is not a string. The actual value is 'null'.", e.getMessage());
        }
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
        try {
            assertThatJson("{\"test\":{\"a\":true}}").node("test.a").isObject();
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("Node \"test.a\" is not an object. The actual value is 'true'.", e.getMessage());
        }
    }

    @Test
    public void isObjectShouldFailOnMissing() {
        try {
            assertThatJson("{\"test\":{\"a\":true}}").node("test.b").isObject();
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("Node \"test.b\" is missing.", e.getMessage());
        }
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
        try {
            assertThatJson("{\"test\":{\"a\":1, \"b\": null}}").isEqualTo("{\"test\":{\"a\":1}}");
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("JSON documents are different:\n" +
                "Different keys found in node \"test\". Expected [a], got [a, b].  Extra: \"test.b\"\n", e.getMessage());
        }
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
        try {
            assertThatJson("{\"foo.bar\":\"boo\"}").node("foo\\.bar").isEqualTo("baz");
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("JSON documents are different:\n" +
                "Different value found in node \"foo\\.bar\". Expected \"baz\", got \"boo\".\n", e.getMessage());
        }
    }

    protected abstract Object readValue(String value);
}
