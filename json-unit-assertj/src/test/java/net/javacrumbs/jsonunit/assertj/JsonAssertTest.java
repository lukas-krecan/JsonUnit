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

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;

import static net.javacrumbs.jsonunit.assertj.JsonAssert.assertThatJson;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class JsonAssertTest {
    private final ObjectMapper mapper = new ObjectMapper();


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
    public void testAssertNode() throws IOException {
        try {
            assertThatJson(mapper.readTree("{\"test\":1}")).isEqualTo(mapper.readTree("{\"test\":2}"));
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
            assertThatJson("{\"root\":{\"test\":[1,2,3]}}").node("root.test[0]").isEqualTo("2");
            fail("Exception expected");
        } catch (AssertionError e) {
            assertEquals("JSON documents have different values:\nDifferent value found in node \"root.test[0]\". Expected 2, got 1.\n", e.getMessage());
        }
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
            assertThatJson("{\"test1\":2, \"test2\":1}").node("test1").isEqualTo("2").node("test2").isEqualTo("2");
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
        JsonAssert.setIgnorePlaceholder("##IGNORE##");
        assertThatJson("{\"test\":1}").isEqualTo("{\"test\":\"##IGNORE##\"}");
        JsonAssert.setIgnorePlaceholder("${json-unit.ignore}");
    }

    @Test
    public void testEqualsToArray() throws IOException {
        assertThatJson("{\"test\":[1,2,3]}").node("test").isEqualTo(new int[]{1,2,3});
    }

    @Test(expected = AssertionError.class)
    public void testNotEqualsToToArray() throws IOException {
        assertThatJson("{\"test\":[1,2,3]}").node("test").isNotEqualTo(new int[]{1,2,3});
    }

    @Test
    public void testEqualsToBoolean() throws IOException {
        assertThatJson("{\"test\":true}").node("test").isEqualTo(true);
    }
}
