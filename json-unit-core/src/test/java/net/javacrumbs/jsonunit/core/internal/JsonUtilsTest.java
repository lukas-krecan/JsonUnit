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
package net.javacrumbs.jsonunit.core.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;

import static java.util.Collections.singletonMap;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.convertToJson;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.getNode;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.nodeAbsent;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.quoteIfNeeded;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.valueToNode;
import static net.javacrumbs.jsonunit.core.internal.Node.NodeType.ARRAY;
import static net.javacrumbs.jsonunit.core.internal.Node.NodeType.BOOLEAN;
import static net.javacrumbs.jsonunit.core.internal.Node.NodeType.NULL;
import static net.javacrumbs.jsonunit.core.internal.Node.NodeType.NUMBER;
import static net.javacrumbs.jsonunit.core.internal.Node.NodeType.OBJECT;
import static net.javacrumbs.jsonunit.core.internal.Node.NodeType.STRING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonUtilsTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    void testConvertToJson() {
        assertEquals(STRING, convertToJson("\"a\"", "x").getNodeType());
        assertEquals(NUMBER, convertToJson(1, "x").getNodeType());
        assertEquals(NUMBER, convertToJson("1", "x").getNodeType());
        assertEquals(NUMBER, convertToJson(1.0, "x").getNodeType());
        assertEquals(NUMBER, convertToJson("1.0", "x").getNodeType());
        assertEquals(OBJECT, convertToJson("{\"a\":1}", "x").getNodeType());
        assertEquals(ARRAY, convertToJson("[1 ,2, 3]", "x").getNodeType());
        assertEquals(BOOLEAN, convertToJson("true", "x").getNodeType());
        assertEquals(BOOLEAN, convertToJson(true, "x").getNodeType());
        assertEquals(BOOLEAN, convertToJson("false", "x").getNodeType());
        assertEquals(BOOLEAN, convertToJson(false, "x").getNodeType());
        assertTrue(convertToJson("null", "x").isNull());
        assertTrue(convertToJson(null, "x").isNull());
    }

    @Test
    void testValueToJson() {
        assertEquals(STRING, valueToNode("a").getNodeType());
        assertEquals(NUMBER, valueToNode(BigDecimal.valueOf(1)).getNodeType());
        assertEquals(STRING, valueToNode("1").getNodeType());
        assertEquals(NUMBER, valueToNode(1.0).getNodeType());
        assertEquals(STRING, valueToNode("1.0").getNodeType());
        assertEquals(OBJECT, valueToNode(singletonMap("a", 1)).getNodeType());
        assertEquals(ARRAY, valueToNode(new int[]{1 ,2, 3}).getNodeType());
        assertEquals(STRING, valueToNode("true").getNodeType());
        assertEquals(BOOLEAN, valueToNode(true).getNodeType());
        assertEquals(STRING, valueToNode("false").getNodeType());
        assertEquals(BOOLEAN, valueToNode(false).getNodeType());
        assertEquals(STRING, valueToNode("null").getNodeType());
        assertEquals(NULL, valueToNode(null).getNodeType());
    }

    @Test
    void testQuoteIfNeeded() {
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
    void testGetStartNodeRoot() throws IOException {
        Node startNode = getNode(mapper.readTree("{\"test\":{\"value\":1}}"), "");
        assertEquals("{\"test\":{\"value\":1}}", startNode.toString());
    }

    @Test
    void testGetStartNodeSimple() throws IOException {
        Node startNode = getNode(mapper.readTree("{\"test\":{\"value\":1}}"), "test");
        assertEquals("{\"value\":1}", startNode.toString());
    }

    @Test
    void testGetStartNodeTwoSteps() throws IOException {
        Node startNode = getNode(mapper.readTree("{\"test\":{\"value\":1}}"), "test.value");
        assertEquals(1, startNode.decimalValue().intValue());
    }

    @Test
    void testGetStartNodeArrays() throws IOException {
        Node startNode = getNode(mapper.readTree("{\"test\":{\"values\":[1,2]}}"), "test.values[1]");
        assertEquals(2, startNode.decimalValue().intValue());
    }

    @Test
    void testGetStartNodeArraysNegated() throws IOException {
        Node startNode = getNode(mapper.readTree("{\"test\":{\"values\":[1,2,3,4,5]}}"), "test.values[-2]");
        assertEquals(4, startNode.decimalValue().intValue());
    }

    @Test
    void testGetStartNodeArrays2() throws IOException {
        Node startNode = getNode(mapper.readTree("{\"test\":[{\"values\":[1,2]}, {\"values\":[3,4]}]}"), "test[1].values[1]");
        assertEquals(4, startNode.decimalValue().intValue());
    }

    @Test
    void testGetStartNodeArraysRootComplex() throws IOException {
        Node startNode = getNode(mapper.readTree("[{\"values\":[1,2]}, {\"values\":[3,4]}]"), "[1].values[1]");
        assertEquals(4, startNode.decimalValue().intValue());
    }

    @Test
    void testGetStartNodeArraysConvoluted() throws IOException {
        Node startNode = getNode(mapper.readTree("{\"test\":[{\"values\":[1,2]}, {\"values\":[3,4]}]}"), "test.[1].values.[1]");
        assertEquals(4, startNode.decimalValue().intValue());
    }

    @Test
    void testGetStartNodeArraysRoot() throws IOException {
        Node startNode = getNode(mapper.readTree("[1,2]"), "[0]");
        assertEquals(1, startNode.decimalValue().intValue());
    }

    @Test
    void testGetStartNodeNonexisting() throws IOException {
        Node startNode = getNode(mapper.readTree("{\"test\":{\"value\":1}}"), "test.bogus");
        assertTrue(startNode.isMissingNode());
    }

    @Test
    void testNodeAbsent() {
        String json = "{\"test\":{\"value\":1, \"value2\": null}}";
        assertFalse(nodeAbsent(json, Path.create("test"), false));
        assertFalse(nodeAbsent(json, Path.create("test.value"), false));
        assertTrue(nodeAbsent(json, Path.create("test.nonsense"), false));
        assertTrue(nodeAbsent(json, Path.create("root"), false));
        assertTrue(nodeAbsent(json, Path.create("test.value2"), true));
        assertFalse(nodeAbsent(json, Path.create("test.value2"), false));
    }

    @Test
    void shouldIgnoreEscapedDot() {
        assertFalse(nodeAbsent("{\"test.1\":{\"value\":1}}", Path.create("test\\.1"), false));
    }

    @Test
    void shouldNotProcessBackslash() {
        // JSON string has to be double escaped (once for Java, once for JSON)
        assertFalse(nodeAbsent("{\"test\\\\backslash\":{\"value\":1}}", Path.create("test\\backslash"), false));
    }
}
