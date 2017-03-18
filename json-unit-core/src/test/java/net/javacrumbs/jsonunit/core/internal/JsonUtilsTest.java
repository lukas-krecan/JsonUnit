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
package net.javacrumbs.jsonunit.core.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

import static net.javacrumbs.jsonunit.core.internal.JsonUtils.convertToJson;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.getNode;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.nodeAbsent;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.nodeExists;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.quoteIfNeeded;
import static net.javacrumbs.jsonunit.core.internal.Node.NodeType.ARRAY;
import static net.javacrumbs.jsonunit.core.internal.Node.NodeType.BOOLEAN;
import static net.javacrumbs.jsonunit.core.internal.Node.NodeType.NUMBER;
import static net.javacrumbs.jsonunit.core.internal.Node.NodeType.OBJECT;
import static net.javacrumbs.jsonunit.core.internal.Node.NodeType.STRING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JsonUtilsTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testConvertToJson() {
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
        Node startNode = getNode(mapper.readTree("{\"test\":{\"value\":1}}"), "");
        assertEquals("{\"test\":{\"value\":1}}", startNode.toString());
    }

    @Test
    public void testGetStartNodeSimple() throws IOException {
        Node startNode = getNode(mapper.readTree("{\"test\":{\"value\":1}}"), "test");
        assertEquals("{\"value\":1}", startNode.toString());
    }

    @Test
    public void testGetStartNodeTwoSteps() throws IOException {
        Node startNode = getNode(mapper.readTree("{\"test\":{\"value\":1}}"), "test.value");
        assertEquals(1, startNode.decimalValue().intValue());
    }

    @Test
    public void testGetStartNodeArrays() throws IOException {
        Node startNode = getNode(mapper.readTree("{\"test\":{\"values\":[1,2]}}"), "test.values[1]");
        assertEquals(2, startNode.decimalValue().intValue());
    }

    @Test
    public void testGetStartNodeArraysNegated() throws IOException {
        Node startNode = getNode(mapper.readTree("{\"test\":{\"values\":[1,2,3,4,5]}}"), "test.values[-2]");
        assertEquals(4, startNode.decimalValue().intValue());
    }

    @Test
    public void testGetStartNodeArrays2() throws IOException {
        Node startNode = getNode(mapper.readTree("{\"test\":[{\"values\":[1,2]}, {\"values\":[3,4]}]}"), "test[1].values[1]");
        assertEquals(4, startNode.decimalValue().intValue());
    }

    @Test
    public void testGetStartNodeArraysRootComplex() throws IOException {
        Node startNode = getNode(mapper.readTree("[{\"values\":[1,2]}, {\"values\":[3,4]}]"), "[1].values[1]");
        assertEquals(4, startNode.decimalValue().intValue());
    }

    @Test
    public void testGetStartNodeArraysConvoluted() throws IOException {
        Node startNode = getNode(mapper.readTree("{\"test\":[{\"values\":[1,2]}, {\"values\":[3,4]}]}"), "test.[1].values.[1]");
        assertEquals(4, startNode.decimalValue().intValue());
    }

    @Test
    public void testGetStartNodeArraysRoot() throws IOException {
        Node startNode = getNode(mapper.readTree("[1,2]"), "[0]");
        assertEquals(1, startNode.decimalValue().intValue());
    }

    @Test
    public void testGetStartNodeNonexisting() throws IOException {
        Node startNode = getNode(mapper.readTree("{\"test\":{\"value\":1}}"), "test.bogus");
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

    @Test
    public void testNodeAbsent() throws IOException {
        String json = "{\"test\":{\"value\":1, \"value2\": null}}";
        assertFalse(nodeAbsent(json, "test", false));
        assertFalse(nodeAbsent(json, "test.value", false));
        assertTrue(nodeAbsent(json, "test.nonsense", false));
        assertTrue(nodeAbsent(json, "root", false));
        assertTrue(nodeAbsent(json, "test.value2", true));
        assertFalse(nodeAbsent(json, "test.value2", false));
    }

    @Test
    public void shouldIgnoreEscapedDot() throws IOException {
        assertFalse(nodeAbsent("{\"test.1\":{\"value\":1}}", "test\\.1", false));
    }
}
