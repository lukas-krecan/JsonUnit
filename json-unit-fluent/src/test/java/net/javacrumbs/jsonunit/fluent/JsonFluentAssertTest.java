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

import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;

import static net.javacrumbs.jsonunit.core.internal.JsonUtils.readValue;
import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class JsonFluentAssertTest {
    @Test
    public void testAssertString() {
        try {
            assertThatJson("{\"test\":1}").isEqualTo("{\"test\":2}");
            fail("Exception expected");
        } catch (AssertionError e) {
            assertEquals("JSON documents have different values:\nDifferent value found in node \"test\". Expected 2, got 1.\n", e.getMessage());
        }
    }

    @Test
    public void testAssertDifferentType() {
        try {
            assertThatJson("{\"test\":\"1\"}").node("test").isEqualTo("1");
            fail("Exception expected");
        } catch (AssertionError e) {
            assertEquals("JSON documents have different values:\nDifferent value found in node \"test\". Expected '1', got '\"1\"'.\n", e.getMessage());
        }
    }

    @Test
    public void testAssertDifferentTypeInt() {
        try {
            assertThatJson("{\"test\":\"1\"}").node("test").isEqualTo(1);
            fail("Exception expected");
        } catch (AssertionError e) {
            assertEquals("JSON documents have different values:\nDifferent value found in node \"test\". Expected '1', got '\"1\"'.\n", e.getMessage());
        }
    }

    @Test
    public void testAssertTolerance() {
        assertThatJson("{\"test\":1.00001}").node("test").withTolerance(0.001).isEqualTo(1);
    }

    @Test
    public void testAssertToleranceDifferentOrder() {
        assertThatJson("{\"test\":1.00001}").withTolerance(0.001).node("test").isEqualTo(1);
    }

    @Test
    public void testAssertToleranceDirect() {
        assertThatJson("{\"test\":1.00001}").withTolerance(0.001).isEqualTo("{\"test\":1.00001}");
    }

    @Test
    public void testAssertToleranceFailure() {
        try {
            assertThatJson("{\"test\":1.1}").node("test").withTolerance(0.001).isEqualTo(1);
        } catch (AssertionError e) {
            assertEquals("JSON documents have different values:\nDifferent value found in node \"test\". Expected 1, got 1.1, difference is 0.1, tolerance is 0.001\n", e.getMessage());
        }
    }

    @Test
    public void testAssertNode() throws IOException {
        try {
            assertThatJson(readValue("{\"test\":1}","")).isEqualTo(readValue("{\"test\":2}", ""));
            fail("Exception expected");
        } catch (AssertionError e) {
            assertEquals("JSON documents have different values:\nDifferent value found in node \"test\". Expected 2, got 1.\n", e.getMessage());
        }
    }

    @Test
    public void testAssertNodeInExpectOnly() throws IOException {
        try {
            assertThatJson("{\"test\":1}").isEqualTo(readValue("{\"test\":2}", ""));
            fail("Exception expected");
        } catch (AssertionError e) {
            assertEquals("JSON documents have different values:\nDifferent value found in node \"test\". Expected 2, got 1.\n", e.getMessage());
        }
    }

    @Test
    public void testAssertReader() throws IOException {
        try {
            assertThatJson(new StringReader("{\"test\":1}")).isEqualTo(new StringReader("{\"test\":2}"));
            fail("Exception expected");
        } catch (AssertionError e) {
            assertEquals("JSON documents have different values:\nDifferent value found in node \"test\". Expected 2, got 1.\n", e.getMessage());
        }
    }

    @Test
    public void testOk() throws IOException {
        assertThatJson("{\"test\":1}").isEqualTo("{\"test\":1}");
    }

    @Test
    public void testOkNumber() throws IOException {
        assertThatJson("{\"test\":1}").node("test").isEqualTo(1);
    }

    @Test
    public void testOkFloat() throws IOException {
        assertThatJson("{\"test\":1.1}").node("test").isEqualTo(1.1);
    }

    @Test
    public void testOkNull() throws IOException {
        assertThatJson("{\"test\":null}").node("test").isEqualTo(null);
    }

    @Test
    public void testNotEqualTo() throws IOException {
        try {
            assertThatJson("{\"test\":1}").isNotEqualTo("{\"test\": 1}");
            fail("Exception expected");
        } catch (AssertionError e) {
            assertEquals("JSON is equal.", e.getMessage());
        }
    }

    @Test
    public void testSameStructureOk() throws IOException {
        assertThatJson("{\"test\":1}").hasSameStructureAs("{\"test\":21}");
    }

    @Test
    public void testDifferentStructure() throws IOException {
        try {
            assertThatJson("{\"test\":1}").hasSameStructureAs("{\"test\":21, \"a\":true}");
            fail("Exception expected");
        } catch (AssertionError e) {
            assertEquals("JSON documents have different structures:\nDifferent keys found in node \"\". Expected [a, test], got [test]. Missing: \"a\" \n", e.getMessage());
        }
    }

    @Test
    public void testAssertPath() {
        try {
            assertThatJson("{\"test\":1}").node("test").isEqualTo("2");
            fail("Exception expected");
        } catch (AssertionError e) {
            assertEquals("JSON documents have different values:\nDifferent value found in node \"test\". Expected 2, got 1.\n", e.getMessage());
        }
    }

    @Test
    public void testAssertPathArray() {
        try {
            assertThatJson("{\"root\":{\"test\":[1,2,3]}}").node("root.test[0]").isEqualTo(2);
            fail("Exception expected");
        } catch (AssertionError e) {
            assertEquals("JSON documents have different values:\nDifferent value found in node \"root.test[0]\". Expected 2, got 1.\n", e.getMessage());
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
            fail("Exception expected");
        } catch (AssertionError e) {
            assertEquals("JSON documents have different values:\nDifferent value found in node \"root.test\". Expected 2, got 1.\n", e.getMessage());
        }
    }

    @Test
    public void testMoreNodes() {
        try {
            assertThatJson("{\"test1\":2, \"test2\":1}").node("test1").isEqualTo(2).node("test2").isEqualTo(2);
            fail("Exception expected");
        } catch (AssertionError e) {
            assertEquals("JSON documents have different values:\nDifferent value found in node \"test2\". Expected 2, got 1.\n", e.getMessage());
        }
    }

    @Test
    public void testMessage() {
        try {
            assertThatJson("{\"test\":1}").as("Test is different").isEqualTo("{\"test\":2}");
            fail("Exception expected");
        } catch (AssertionError e) {
            assertEquals("[Test is different] JSON documents have different values:\nDifferent value found in node \"test\". Expected 2, got 1.\n", e.getMessage());
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
    public void testEqualsToArray() throws IOException {
        assertThatJson("{\"test\":[1,2,3]}").node("test").isEqualTo(new int[]{1, 2, 3});
    }

    @Test(expected = AssertionError.class)
    public void testNotEqualsToToArray() throws IOException {
        assertThatJson("{\"test\":[1,2,3]}").node("test").isNotEqualTo(new int[]{1, 2, 3});
    }

    @Test
    public void testEqualsToBoolean() throws IOException {
        assertThatJson("{\"test\":true}").node("test").isEqualTo(true);
    }

    @Test
    public void testEqualsToNull() throws IOException {
        assertThatJson("{\"test\":null}").node("test").isEqualTo(null);
    }

    @Test(expected = AssertionError.class)
    public void testEqualsToNullFail() throws IOException {
        assertThatJson("{\"test\":1}").node("test").isEqualTo(null);
    }

    @Test
    public void testNotEqualsToNull() throws IOException {
        assertThatJson("{\"test\":1}").node("test").isNotEqualTo(null);
    }

    @Test
    public void testIssue3() throws IOException {
        assertThatJson("{\"someKey\":\"111 text\"}").node("someKey").isEqualTo("\"111 text\"");
    }

    @Test
    public void testIssue3NoSpace() throws IOException {
        assertThatJson("{\"someKey\":\"111text\"}").node("someKey").isEqualTo("\"111text\"");
    }

    @Test
    public void testIssue3SpaceStrings() throws IOException {
        assertThatJson("{\"someKey\":\"a b\"}").node("someKey").isEqualTo("a b");
    }

    @Test

    public void testIssue3Original() throws IOException {
        assertThatJson("{\"someKey\":\"111 text\"}").node("someKey").isEqualTo("111 text");
    }
}
