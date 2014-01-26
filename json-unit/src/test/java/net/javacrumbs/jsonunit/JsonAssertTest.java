/**
 * Copyright 2009-2012 the original author or authors.
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
package net.javacrumbs.jsonunit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;

import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static net.javacrumbs.jsonunit.JsonAssert.assertJsonPartEquals;
import static net.javacrumbs.jsonunit.JsonAssert.assertJsonStructureEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class JsonAssertTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void testEquals() {
        assertJsonEquals("{\"test\":1}", "{\n\"test\": 1\n}");
        assertJsonEquals("{\"foo\":\"bar\",\"test\": 1}", "{\n\"test\": 1,\n\"foo\":\"bar\"}");
        assertJsonEquals("{}", "{}");
    }

    @Test
    public void testArray() {
        assertJsonEquals("[{\"test\":1}, {\"test\":2}]", "[{\n\"test\": 1\n}, {\"test\": 2}]");
    }

    @Test
    public void testArrayDifferent() {
        try {
            assertJsonEquals("[{\"test\":1}, {\"test\":2}]", "[{\n\"test\": 1\n}, {\"test\": 4}]");
            fail("Exception expected");
        } catch (AssertionError e) {
            assertEquals("JSON documents have different values:\nDifferent value found in node \"[1].test\". Expected 2, got 4.\n", e.getMessage());
        }
    }

    @Test
    public void testSimple() {
        assertJsonEquals("1", "1");
    }

    @Test
    public void testSimpleDifferent() {
        try {
            assertJsonEquals("1", "\n2\n");
            fail("Exception expected");
        } catch (AssertionError e) {
            assertEquals("JSON documents have different values:\nDifferent value found in node \"\". Expected 1, got 2.\n", e.getMessage());
        }
    }

    @Test
    public void testSimpleIgnore() {
        assertJsonEquals("\"${json-unit.ignore}\"", "\n2\n");
    }

    @Test
    public void testEqualsNode() throws IOException {
        assertJsonEquals(MAPPER.readValue("{\"test\":1}", ObjectNode.class), MAPPER.readValue("{\"test\": 1}", ObjectNode.class));
    }

    @Test
    public void testEqualsNodeIgnore() throws IOException {
        assertJsonEquals(MAPPER.readValue("{\"test\":\"${json-unit.ignore}\"}", ObjectNode.class), MAPPER.readValue("{\"test\": 1}", ObjectNode.class));
    }

    @Test
    public void testEqualsNodeFail() throws IOException {
        try {
            assertJsonEquals(MAPPER.readValue("{\"test\":1}", ObjectNode.class), MAPPER.readValue("{\"test\": 2}", ObjectNode.class));
            fail("Exception expected");
        } catch (AssertionError e) {
            assertEquals("JSON documents have different values:\nDifferent value found in node \"test\". Expected 1, got 2.\n", e.getMessage());
        }
    }

    @Test
    public void testEqualsNodeStringFail() throws IOException {
        try {
            assertJsonEquals(MAPPER.readValue("{\"test\":\"a\"}", ObjectNode.class), MAPPER.readValue("{\"test\": \"b\"}", ObjectNode.class));
            fail("Exception expected");
        } catch (AssertionError e) {
            assertEquals("JSON documents have different values:\nDifferent value found in node \"test\". Expected \"a\", got \"b\".\n", e.getMessage());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidJsonActual() {
        assertJsonEquals("{\"test\":1}", "{\n\"foo\": 1\n");
    }

    @Test
    public void testObjectName() {
        try {
            assertJsonEquals("{\"test\":1}", "{\n\"foo\": 1\n}");
            fail("Exception expected");
        } catch (AssertionError e) {
            assertEquals("JSON documents have different structures:\nDifferent keys found in node \"\". Expected [test], got [foo]. Missing: \"test\" Extra: \"foo\"\n", e.getMessage());
        }
    }

    @Test
    public void testNullOk() {
        assertJsonEquals("{\"test\":null}", "{\n\"test\": null\n}");
    }

    @Test
    public void testNullFail() {
        try {
            assertJsonEquals("{\"test\":null}", "{\n\"test\": 1\n}");
            fail("Exception expected");
        } catch (AssertionError e) {
            assertEquals("JSON documents have different values:\nDifferent value found in node \"test\". Expected 'null', got '1'.\n", e.getMessage());
        }
    }

    @Test
    public void testExtraRootKey() {
        try {
            assertJsonEquals("{\"test\":1}", "{\n\"test\": 1\n, \"foo\": 2}");
            fail("Exception expected");
        } catch (AssertionError e) {
            assertEquals("JSON documents have different structures:\nDifferent keys found in node \"\". Expected [test], got [foo, test].  Extra: \"foo\"\n", e.getMessage());
        }
    }

    @Test
    public void testIgnoreOneElement() {
        try {
            assertJsonEquals("{\"test\":\"${json-unit.ignore}\"}", "{\n\"test\": 1\n, \"foo\": 2}");
            fail("Exception expected");
        } catch (AssertionError e) {
            assertEquals("JSON documents have different structures:\nDifferent keys found in node \"\". Expected [test], got [foo, test].  Extra: \"foo\"\n", e.getMessage());
        }
    }

    @Test
    public void testMissingRootKey() {
        try {
            assertJsonEquals("{\"test\":1, \"foo\": 2}", "{\n\"test\": 1\n}");
            fail("Exception expected");
        } catch (AssertionError e) {
            assertEquals("JSON documents have different structures:\nDifferent keys found in node \"\". Expected [foo, test], got [test]. Missing: \"foo\" \n", e.getMessage());
        }
    }

    @Test
    public void testDifferentNumericValue() {
        try {
            assertJsonEquals("{\"test\":1}", "{\n\"test\": 2\n}");
            fail("Exception expected");
        } catch (AssertionError e) {
            assertEquals("JSON documents have different values:\nDifferent value found in node \"test\". Expected 1, got 2.\n", e.getMessage());
        }
    }

    @Test
    public void testDifferentBooleanValue() {
        try {
            assertJsonEquals("{\"test\":true}", "{\n\"test\": false\n}");
            fail("Exception expected");
        } catch (AssertionError e) {
            assertEquals("JSON documents have different values:\nDifferent value found in node \"test\". Expected true, got false.\n", e.getMessage());
        }
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
        try {
            assertJsonEquals("{\"test\":[1, 2, 3]}", "{\n\"test\": [1, 2]\n}");
            fail("Exception expected");
        } catch (AssertionError e) {
            assertEquals("JSON documents have different structures:\nArray \"test\" has different length. Expected 3, got 2.\n", e.getMessage());
        }
    }

    @Test
    public void testDifferentArrayValue() {
        try {
            assertJsonEquals("{\"test\":[1, 2, 3]}", "{\n\"test\": [1, 2, 5]\n}");
            fail("Exception expected");
        } catch (AssertionError e) {
            assertEquals("JSON documents have different values:\nDifferent value found in node \"test[2]\". Expected 3, got 5.\n", e.getMessage());
        }
    }

    @Test
    public void testDifferentArrayType() {
        try {
            assertJsonEquals("{\"test\":[1, 2, 3]}", "{\n\"test\": [1, false, 3]\n}");
            fail("Exception expected");
        } catch (AssertionError e) {
            assertEquals("JSON documents have different values:\nDifferent value found in node \"test[1]\". Expected '2', got 'false'.\n", e.getMessage());
        }
    }

    @Test
    public void testComplexOk() {
        assertJsonEquals("{\"test\":[1, 2, {\"child\":{\"value1\":1, \"value2\":true, \"value3\": \"test\", \"value4\":{\"leaf\":5}}}], \"root2\": false}",
                "{\"test\":[1, 2, {\"child\":{\"value1\":1, \"value2\":true, \"value3\": \"test\", \"value4\":{\"leaf\":5}}}], \"root2\": false}");

    }

    @Test
    public void testComplexErrors() {
        try {
            assertJsonEquals("{\n" +
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
                            "}");
            fail("Exception expected");
        } catch (AssertionError e) {
            assertEquals(
                    "JSON documents have different structures:\n" +
                            "Different keys found in node \"\". Expected [root2, root3, test], got [root4, test]. Missing: \"root2\",\"root3\" Extra: \"root4\"\n" +
                            "Different keys found in node \"test[2]\". Expected [child], got [child, child2].  Extra: \"test[2].child2\"\n" +
                            "Different keys found in node \"test[2].child.value4\". Expected [leaf], got [leaf2]. Missing: \"test[2].child.value4.leaf\" Extra: \"test[2].child.value4.leaf2\"\n" +
                            "JSON documents have different values:\n" +
                            "Different value found in node \"test[0]\". Expected 1, got 5.\n" +
                            "Different value found in node \"test[1]\". Expected '2', got 'false'.\n" +
                            "Different value found in node \"test[2].child.value1\". Expected 1, got 5.\n" +
                            "Different value found in node \"test[2].child.value2\". Expected 'true', got '\"true\"'.\n"
                    , e.getMessage());
        }

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
        try {
            assertJsonEquals("{\"test\":{\"object\": \"${json-unit.ignore}\"}, \"sibling\":1}", "{\n\"test\": {\"object\" : {\"another\" : 1}}, \"sibling\" : 2}");
            fail("Exception expected");
        } catch (AssertionError e) {
            assertEquals("JSON documents have different values:\nDifferent value found in node \"sibling\". Expected 1, got 2.\n", e.getMessage());
        }
    }

    @Test
    public void testDifferentNumericTypes() {
        try {
            assertJsonEquals("{\"test\":1}", "{\n\"test\": 1.0\n}");
            fail("Exception expected");
        } catch (AssertionError e) {
            assertEquals("JSON documents have different values:\nDifferent value found in node \"test\". Expected 1, got 1.0.\n", e.getMessage());
        }
    }

    @Test
    public void testDifferentType() {
        try {
            assertJsonEquals("{\"test\":1}", "{\n\"test\": \"something\"\n}");
            fail("Exception expected");
        } catch (AssertionError e) {
            assertEquals("JSON documents have different values:\nDifferent value found in node \"test\". Expected '1', got '\"something\"'.\n", e.getMessage());
        }
    }

    @Test
    public void testEmpty() {
        try {
            assertJsonEquals("{\"test\":{}}", "{\n\"test\": \"something\"\n}");
            fail("Exception expected");
        } catch (AssertionError e) {
            assertEquals("JSON documents have different values:\nDifferent value found in node \"test\". Expected '{}', got '\"something\"'.\n", e.getMessage());
        }
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
    public void testAssertPartOkReaders() {
        assertJsonPartEquals(new StringReader("1"), new StringReader("{\"test\":{\"value\":1}}"), "test.value");
    }

    @Test
    public void testAssertPartOkString() {
        assertJsonPartEquals("a b", "{\"test\":{\"value\":\"a b\"}}", "test.value");
    }

    @Test
    public void testAssertPart() {
        try {
            assertJsonPartEquals("2", "{\"test\":{\"value\":1}}", "test.value");
            fail("Exception expected");
        } catch (AssertionError e) {
            assertEquals("JSON documents have different values:\nDifferent value found in node \"test.value\". Expected 2, got 1.\n", e.getMessage());
        }
    }

    @Test
    public void testAssertPartComplex() {
        try {
            assertJsonPartEquals("{\"value\":2}", "{\"test\":{\"value\":1}}", "test");
            fail("Exception expected");
        } catch (AssertionError e) {
            assertEquals("JSON documents have different values:\nDifferent value found in node \"test.value\". Expected 2, got 1.\n", e.getMessage());
        }
    }

    @Test
    public void testAssertPartArrayOk() {
        assertJsonPartEquals("2", "{\"test\":[{\"value\":1},{\"value\":2}]}", "test[1].value");
    }

    @Test
    public void testAssertPartArray() {
        try {
            assertJsonPartEquals("3", "{\"test\":[{\"value\":1},{\"value\":2}]}", "test[1].value");
            fail("Exception expected");
        } catch (AssertionError e) {
            assertEquals("JSON documents have different values:\nDifferent value found in node \"test[1].value\". Expected 3, got 2.\n", e.getMessage());
        }
    }

    @Test
    public void testAssertPartNonexisting() {
        try {
            assertJsonPartEquals("2", "{\"test\":{\"value\":1}}", "test.bogus");
            fail("Exception expected");
        } catch (AssertionError e) {
            assertEquals("JSON documents have different structures:\nMissing node in path \"test.bogus\".\n", e.getMessage());
        }
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
                        "}");
    }

    @Test
    public void testAssertStructureDiffers() {
        try {
            assertJsonStructureEquals("[{\"test\":1}, {\"test\":2}]", "[{\n\"test\": 1\n}, {\"TEST\": 4}]");
            fail("Exception expected");
        } catch (AssertionError e) {
            assertEquals("JSON documents have different structures:\nDifferent keys found in node \"[1]\". Expected [test], got [TEST]. Missing: \"[1].test\" Extra: \"[1].TEST\"\n", e.getMessage());
        }
    }

    @Test
    public void testAssertStructureArrayDiffers() {
        try {
            assertJsonStructureEquals("[1, 2]", "[1, 2, 3]");
            fail("Exception expected");
        } catch (AssertionError e) {
            assertEquals("JSON documents have different structures:\nArray \"\" has different length. Expected 2, got 3.\n", e.getMessage());
        }
    }
}
