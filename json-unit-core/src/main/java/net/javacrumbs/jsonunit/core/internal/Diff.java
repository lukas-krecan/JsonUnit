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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.javacrumbs.jsonunit.core.internal.JsonUtils.convertToJson;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.quoteIfNeeded;


/**
 * Compares JSON structures. Mainly for internal use, the API might be mre volatile than the rest.
 *
 * @author Lukas Krecan
 */
public class Diff {
    private static final Pattern ARRAY_PATTERN = Pattern.compile("(\\w+)\\[(\\d+)\\]");
    private final JsonNode expectedRoot;
    private final JsonNode actualRoot;
    private final Differences structureDifferences = new Differences("structures");
    private final Differences valueDifferences = new Differences("values");
    private final String startPath;
    private final BigDecimal numericComparisonTolerance;
    private boolean compared = false;
    private final String ignorePlaceholder;

    private static final Logger diffLogger = LoggerFactory.getLogger("net.javacrumbs.jsonunit.difference.diff");
    private static final Logger valuesLogger = LoggerFactory.getLogger("net.javacrumbs.jsonunit.difference.values");

    private enum NodeType {OBJECT, ARRAY, STRING, NUMBER, BOOLEAN, NULL}

    public Diff(JsonNode expected, JsonNode actual, String startPath, String ignorePlaceholder, BigDecimal numericComparisonTolerance) {
        super();
        this.expectedRoot = expected;
        this.actualRoot = actual;
        this.startPath = startPath;
        this.ignorePlaceholder = ignorePlaceholder;
        this.numericComparisonTolerance = numericComparisonTolerance;
    }

    @Deprecated
    public Diff(JsonNode expected, JsonNode actual, String startPath, String ignorePlaceholder) {
        super();
        this.expectedRoot = expected;
        this.actualRoot = actual;
        this.startPath = startPath;
        this.ignorePlaceholder = ignorePlaceholder;
        this.numericComparisonTolerance = null;
    }

    public static Diff create(Object expected, Object actual, String actualName, String startPath, String ignorePlaceholder, BigDecimal numericComparisonTolerance) {
        return new Diff(convertToJson(quoteIfNeeded(expected), "expected"), convertToJson(actual, actualName), startPath, ignorePlaceholder, numericComparisonTolerance);
    }

    @Deprecated
    public static Diff create(Object expected, Object actual, String actualName, String startPath, String ignorePlaceholder) {
        return create(expected, actual, actualName, startPath, ignorePlaceholder, null);
    }


    private void compare() {
        if (!compared) {
            JsonNode part = getStartNode(actualRoot, startPath);
            if (part.isMissingNode()) {
                structureDifferenceFound("Missing node in path \"%s\".", startPath);
            } else {
                compareNodes(expectedRoot, part, startPath);
            }
            compared = true;
        }
    }

    static JsonNode getStartNode(JsonNode actualRoot, String startPath) {
        if (startPath.length() == 0) {
            return actualRoot;
        }

        JsonNode startNode = actualRoot;
        StringTokenizer stringTokenizer = new StringTokenizer(startPath, ".");
        while (stringTokenizer.hasMoreElements()) {
            String step = stringTokenizer.nextToken();
            Matcher matcher = ARRAY_PATTERN.matcher(step);
            if (!matcher.matches()) {
                startNode = startNode.path(step);
            } else {
                startNode = startNode.path(matcher.group(1));
                startNode = startNode.path(Integer.valueOf(matcher.group(2)));
            }
        }
        return startNode;
    }


    /**
     * Compares object nodes.
     *
     * @param expected
     * @param actual
     * @param path
     */
    private void compareObjectNodes(ObjectNode expected, ObjectNode actual, String path) {
        Map<String, JsonNode> expectedFields = getFields(expected);
        Map<String, JsonNode> actualFields = getFields(actual);

        Set<String> expectedKeys = expectedFields.keySet();
        Set<String> actualKeys = actualFields.keySet();
        if (!expectedKeys.equals(actualKeys)) {
            String missingKeys = getMissingKeys(expectedKeys, actualKeys, path);
            String extraKeys = getExtraKeys(expectedKeys, actualKeys, path);
            structureDifferenceFound("Different keys found in node \"%s\". Expected %s, got %s. %s %s", path, sort(expectedFields.keySet()), sort(actualFields.keySet()), missingKeys, extraKeys);
        }

        for (String fieldName : commonFields(expectedFields, actualFields)) {
            JsonNode expectedNode = expectedFields.get(fieldName);
            JsonNode actualNode = actualFields.get(fieldName);
            String fieldPath = getPath(path, fieldName);
            compareNodes(expectedNode, actualNode, fieldPath);
        }
    }


    static String getMissingKeys(Set<String> expectedKeys, Collection<String> actualKeys, String path) {
        Set<String> missingKeys = new TreeSet<String>(expectedKeys);
        missingKeys.removeAll(actualKeys);
        if (!missingKeys.isEmpty()) {
            return "Missing: " + appendKeysToPrefix(missingKeys, path);
        } else {
            return "";
        }
    }

    static String getExtraKeys(Set<String> expectedKeys, Collection<String> actualKeys, String path) {
        Set<String> extraKeys = new TreeSet<String>(actualKeys);
        extraKeys.removeAll(expectedKeys);
        if (!extraKeys.isEmpty()) {
            return "Extra: " + appendKeysToPrefix(extraKeys, path);
        } else {
            return "";
        }
    }

    static String appendKeysToPrefix(Iterable<String> keys, String prefix) {
        Iterator<String> iterator = keys.iterator();
        StringBuilder buffer = new StringBuilder();
        while (iterator.hasNext()) {
            String key = iterator.next();
            buffer.append("\"").append(getPath(prefix, key)).append("\"");
            if (iterator.hasNext()) {
                buffer.append(",");
            }
        }
        return buffer.toString();
    }


    /**
     * Compares two nodes.
     *
     * @param expectedNode
     * @param actualNode
     * @param fieldPath
     */
    private void compareNodes(JsonNode expectedNode, JsonNode actualNode, String fieldPath) {
        NodeType expectedNodeType = getNodeType(expectedNode);
        NodeType actualNodeType = getNodeType(actualNode);

        //ignoring value
        if (expectedNodeType == NodeType.STRING && ignorePlaceholder.equals(expectedNode.asText())) {
            return;
        }

        if (!expectedNodeType.equals(actualNodeType)) {
            valueDifferenceFound("Different value found in node \"%s\". Expected '%s', got '%s'.", fieldPath, expectedNode, actualNode);
        } else {
            switch (expectedNodeType) {
                case OBJECT:
                    compareObjectNodes((ObjectNode) expectedNode, (ObjectNode) actualNode, fieldPath);
                    break;
                case ARRAY:
                    compareArrayNodes((ArrayNode) expectedNode, (ArrayNode) actualNode, fieldPath);
                    break;
                case STRING:
                    compareValues(expectedNode.asText(), actualNode.asText(), fieldPath);
                    break;
                case NUMBER:
                    if (numericComparisonTolerance != null) {
                        BigDecimal diff = expectedNode.decimalValue().subtract(actualNode.decimalValue()).abs();
                        if (diff.compareTo(numericComparisonTolerance) > 0) {
                            valueDifferenceFound("Different value found in node \"%s\". Expected %s, got %s, difference is %s, tolerance is %s",
                                    fieldPath, quoteTextValue(expectedNode.numberValue()), quoteTextValue(actualNode.numberValue()), diff.toString(), numericComparisonTolerance);
                        }
                    } else {
                        compareValues(expectedNode.numberValue(), actualNode.numberValue(), fieldPath);
                    }
                    break;
                case BOOLEAN:
                    compareValues(expectedNode.asBoolean(), actualNode.asBoolean(), fieldPath);
                    break;
                case NULL:
                    //nothing
                    break;
                default:
                    throw new IllegalStateException("Unexpected node type " + expectedNodeType);
            }
        }
    }


    private void compareValues(Object expectedValue, Object actualValue, String path) {
        if (!expectedValue.equals(actualValue)) {
            valueDifferenceFound("Different value found in node \"%s\". Expected %s, got %s.", path, quoteTextValue(expectedValue), quoteTextValue(actualValue));
        }
    }

    /**
     * If the value is String than it's quoted in ".
     *
     * @param value
     * @return
     */
    private Object quoteTextValue(Object value) {
        if (value instanceof String) {
            return "\"" + value + "\"";
        } else {
            return value;
        }
    }


    private void compareArrayNodes(ArrayNode expectedNode, ArrayNode actualNode, String path) {
        List<JsonNode> expectedElements = asList(expectedNode.elements());
        List<JsonNode> actualElements = asList(actualNode.elements());
        if (expectedElements.size() != actualElements.size()) {
            structureDifferenceFound("Array \"%s\" has different length. Expected %d, got %d.", path, expectedElements.size(), actualElements.size());
        }
        for (int i = 0; i < Math.min(expectedElements.size(), actualElements.size()); i++) {
            compareNodes(expectedElements.get(i), actualElements.get(i), getArrayPath(path, i));
        }
    }


    private List<JsonNode> asList(Iterator<JsonNode> elements) {
        List<JsonNode> result = new ArrayList<JsonNode>();
        while (elements.hasNext()) {
            JsonNode jsonNode = elements.next();
            result.add(jsonNode);
        }
        return Collections.unmodifiableList(result);
    }


    /**
     * Returns NodeType of the node.
     *
     * @param node
     * @return
     */
    private NodeType getNodeType(JsonNode node) {
        if (node.isObject()) {
            return NodeType.OBJECT;
        } else if (node.isArray()) {
            return NodeType.ARRAY;
        } else if (node.isTextual()) {
            return NodeType.STRING;
        } else if (node.isNumber()) {
            return NodeType.NUMBER;
        } else if (node.isBoolean()) {
            return NodeType.BOOLEAN;
        } else if (node.isNull()) {
            return NodeType.NULL;
        } else {
            throw new IllegalStateException("Unexpected node type " + node);
        }
    }


    /**
     * Construct path to an element.
     *
     * @param parent
     * @param name
     * @return
     */
    private static String getPath(String parent, String name) {
        if (parent.length() == 0) {
            return name;
        } else {
            return parent + "." + name;
        }
    }

    /**
     * Constructs path to an array element.
     *
     * @param parent
     * @param i
     * @return
     */
    private String getArrayPath(String parent, int i) {
        if (parent.length() == 0) {
            return "[" + i + "]";
        } else {
            return parent + "[" + i + "]";
        }
    }

    private void structureDifferenceFound(String message, Object... arguments) {
        structureDifferences.add(message, arguments);
    }

    private void valueDifferenceFound(String message, Object... arguments) {
        valueDifferences.add(message, arguments);
    }


    private Set<String> commonFields(Map<String, JsonNode> expectedFields, Map<String, JsonNode> actualFields) {
        Set<String> result = new TreeSet<String>(expectedFields.keySet());
        result.retainAll(actualFields.keySet());
        return Collections.unmodifiableSet(result);
    }


    private SortedSet<String> sort(Set<String> set) {
        return new TreeSet<String>(set);
    }

    private boolean hasSimilarStructure() {
        compare();
        return structureDifferences.isEmpty();
    }

    public boolean similarStructure() {
        boolean result = hasSimilarStructure();
        logDifferences(result);
        return result;
    }

    public boolean similar() {
        boolean result = hasSimilarStructure() && valueDifferences.isEmpty();
        logDifferences(result);
        return result;
    }

    private void logDifferences(boolean result) {
        if (!result) {
            if (diffLogger.isDebugEnabled()) {
                diffLogger.debug(getDifferences().trim());
            }
            if (valuesLogger.isDebugEnabled()) {
                valuesLogger.debug("Comparing expected:\n{}\n------------\nwith actual:\n{}\n", expectedRoot, getStartNode(actualRoot, startPath));
            }
        }
    }

    /**
     * Returns children of an ObjectNode.
     *
     * @param node
     * @return
     */
    private static Map<String, JsonNode> getFields(ObjectNode node) {
        Map<String, JsonNode> result = new HashMap<String, JsonNode>();
        Iterator<Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            result.put(field.getKey(), field.getValue());
        }
        return Collections.unmodifiableMap(result);
    }

    @Override
    public String toString() {
        return differences();
    }

    public String differences() {
        if (similar()) {
            return "JSON documents have the same value.";
        }
        return getDifferences();
    }

    private String getDifferences() {
        StringBuilder message = new StringBuilder();
        structureDifferences.appendDifferences(message);
        valueDifferences.appendDifferences(message);
        return message.toString();
    }

    public String structureDifferences() {
        if (similarStructure()) {
            return "JSON documents have the same structure.";
        }
        StringBuilder message = new StringBuilder();
        structureDifferences.appendDifferences(message);
        return message.toString();
    }

}
