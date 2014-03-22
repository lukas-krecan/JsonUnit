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
package net.javacrumbs.jsonunit.core.internal;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

import static net.javacrumbs.jsonunit.core.internal.JsonUtils.convertToJson;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.getNode;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.nodeExists;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.quoteIfNeeded;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JsonUtilsTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testConvertToJson() {
        assertTrue(convertToJson("\"a\"", "x").isTextual());
        assertTrue(convertToJson(1, "x").isNumber());
        assertTrue(convertToJson("1", "x").isNumber());
        assertTrue(convertToJson(1.0, "x").isNumber());
        assertTrue(convertToJson("1.0", "x").isNumber());
        assertTrue(convertToJson("{\"a\":1}", "x").isObject());
        assertTrue(convertToJson("[1 ,2, 3]", "x").isArray());
        assertTrue(convertToJson("true", "x").isBoolean());
        assertTrue(convertToJson(true, "x").isBoolean());
        assertTrue(convertToJson("false", "x").isBoolean());
        assertTrue(convertToJson(false, "x").isBoolean());
        assertTrue(convertToJson("null", "x").isNull());
        assertTrue(convertToJson(null, "x").isNull());
    }

    @Test
    public void testQuoteIfNeeded() {
        assertEquals("1", quoteIfNeeded("1"));
        assertEquals("1.1", quoteIfNeeded("1.1"));
        assertEquals("{\"a\":1}", quoteIfNeeded("{\"a\":1}"));
        assertEquals("[1 ,2, 3]", quoteIfNeeded("[1 ,2, 3]"));
        assertEquals("true", quoteIfNeeded("true"));
        assertEquals("false", quoteIfNeeded("false"));
        assertEquals("null", quoteIfNeeded("null"));


        assertEquals("\"a\"", quoteIfNeeded("a"));
        assertEquals("\"a b\"", quoteIfNeeded("a b"));
        assertEquals("\"123 b\"", quoteIfNeeded("123 b"));
    }

    @Test
    public void testGetStartNodeRoot() throws IOException {
        JsonNode startNode = getNode(mapper.readTree("{\"test\":{\"value\":1}}"), "");
        assertEquals("{\"test\":{\"value\":1}}", mapper.writeValueAsString(startNode));
    }

    @Test
    public void testGetStartNodeSimple() throws IOException {
        JsonNode startNode = getNode(mapper.readTree("{\"test\":{\"value\":1}}"), "test");
        assertEquals("{\"value\":1}", mapper.writeValueAsString(startNode));
    }

    @Test
    public void testGetStartNodeTwoSteps() throws IOException {
        JsonNode startNode = getNode(mapper.readTree("{\"test\":{\"value\":1}}"), "test.value");
        assertEquals("1", mapper.writeValueAsString(startNode));
    }

    @Test
    public void testGetStartNodeArrays() throws IOException {
        JsonNode startNode = getNode(mapper.readTree("{\"test\":{\"values\":[1,2]}}"), "test.values[1]");
        assertEquals("2", mapper.writeValueAsString(startNode));
    }

    @Test
    public void testGetStartNodeArrays2() throws IOException {
        JsonNode startNode = getNode(mapper.readTree("{\"test\":[{\"values\":[1,2]}, {\"values\":[3,4]}]}"), "test[1].values[1]");
        assertEquals("4", mapper.writeValueAsString(startNode));
    }

    @Test
    public void testGetStartNodeArraysRootComplex() throws IOException {
        JsonNode startNode = getNode(mapper.readTree("[{\"values\":[1,2]}, {\"values\":[3,4]}]"), "[1].values[1]");
        assertEquals("4", mapper.writeValueAsString(startNode));
    }

    @Test
    public void testGetStartNodeArraysConvoluted() throws IOException {
        JsonNode startNode = getNode(mapper.readTree("{\"test\":[{\"values\":[1,2]}, {\"values\":[3,4]}]}"), "test.[1].values.[1]");
        assertEquals("4", mapper.writeValueAsString(startNode));
    }

    @Test
    public void testGetStartNodeArraysRoot() throws IOException {
        JsonNode startNode = getNode(mapper.readTree("[1,2]"), "[0]");
        assertEquals("1", mapper.writeValueAsString(startNode));
    }

    @Test
    public void testGetStartNodeNonexisting() throws IOException {
        JsonNode startNode = getNode(mapper.readTree("{\"test\":{\"value\":1}}"), "test.bogus");
        assertEquals(true, startNode.isMissingNode());
    }
    @Test
    public void testNodeExists() throws IOException {
        String json = "{\"test\":{\"value\":1}}";
        assertTrue(nodeExists(json, "test"));
        assertTrue(nodeExists(json, "test.value"));
        assertFalse(nodeExists(json, "test.nonsense"));
        assertFalse(nodeExists(json, "root"));
    }

}
