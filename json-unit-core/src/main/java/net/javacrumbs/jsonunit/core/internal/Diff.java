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

import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.Option;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static java.util.Collections.emptySet;
import static net.javacrumbs.jsonunit.core.Option.COMPARING_ONLY_STRUCTURE;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_ARRAY_ITEMS;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_VALUES;
import static net.javacrumbs.jsonunit.core.internal.ClassUtils.isClassPresent;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.convertToJson;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.getNode;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.quoteIfNeeded;
import static net.javacrumbs.jsonunit.core.internal.Node.KeyValue;
import static net.javacrumbs.jsonunit.core.internal.Node.NodeType;


/**
 * Compares JSON structures. Mainly for internal use, the API might be more volatile than the rest.
 *
 * @author Lukas Krecan
 */
public class Diff {
    private static final String REGEX_PLACEHOLDER = "${json-unit.regex}";
    private final Node expectedRoot;
    private final Node actualRoot;
    private final Differences differences = new Differences();
    private final String startPath;
    private boolean compared = false;
    private final Configuration configuration;


    private static final JsonUnitLogger diffLogger = createLogger("net.javacrumbs.jsonunit.difference.diff");
    private static final JsonUnitLogger valuesLogger = createLogger("net.javacrumbs.jsonunit.difference.values");

    private Diff(Node expected, Node actual, String startPath, Configuration configuration) {
        this.expectedRoot = expected;
        this.actualRoot = actual;
        this.startPath = startPath;
        this.configuration = configuration;
    }

    public static Diff create(Object expected, Object actual, String actualName, String startPath, Configuration configuration) {
        return new Diff(convertToJson(quoteIfNeeded(expected), "expected", true), convertToJson(actual, actualName, false), startPath, configuration);
    }

    private void compare() {
        if (!compared) {
            Node part = getNode(actualRoot, startPath);
            if (part.isMissingNode()) {
                structureDifferenceFound("Missing node in path \"%s\".", startPath);
            } else {
                compareNodes(expectedRoot, part, startPath);
            }
            compared = true;
        }
    }

    /**
     * Compares object nodes.
     *
     * @param expected
     * @param actual
     * @param path
     */
    private void compareObjectNodes(Node expected, Node actual, String path) {
        Map<String, Node> expectedFields = getFields(expected);
        Map<String, Node> actualFields = getFields(actual);

        Set<String> expectedKeys = expectedFields.keySet();
        Set<String> actualKeys = actualFields.keySet();

        if (!expectedKeys.equals(actualKeys)) {
            Set<String> missingKeys = getMissingKeys(expectedKeys, actualKeys);
            Set<String> extraKeys = getExtraKeys(expectedKeys, actualKeys);
            if (hasOption(Option.TREATING_NULL_AS_ABSENT)) {
                extraKeys = getNotNullExtraKeys(actual, extraKeys);
            }

            if (!missingKeys.isEmpty() || !extraKeys.isEmpty()) {
                String missingKeysMessage = getMissingKeysMessage(missingKeys, path);
                String extraKeysMessage = getExtraKeysMessage(extraKeys, path);
                structureDifferenceFound("Different keys found in node \"%s\". Expected %s, got %s. %s %s", path, sort(expectedFields.keySet()), sort(actualFields.keySet()), missingKeysMessage, extraKeysMessage);
            }
        }

        for (String fieldName : commonFields(expectedFields, actualFields)) {
            Node expectedNode = expectedFields.get(fieldName);
            Node actualNode = actualFields.get(fieldName);
            String fieldPath = getPath(path, fieldName);
            compareNodes(expectedNode, actualNode, fieldPath);
        }
    }

    /**
     * Returns extra keys that are not null.
     *
     * @param actual
     * @param extraKeys
     * @return
     */
    private Set<String> getNotNullExtraKeys(Node actual, Set<String> extraKeys) {
        Set<String> notNullExtraKeys = new TreeSet<String>();
        for (String extraKey : extraKeys) {
            if (!actual.get(extraKey).isNull()) {
                notNullExtraKeys.add(extraKey);
            }
        }
        return notNullExtraKeys;
    }


    private static String getMissingKeysMessage(Set<String> missingKeys, String path) {
        if (!missingKeys.isEmpty()) {
            return "Missing: " + appendKeysToPrefix(missingKeys, path);
        } else {
            return "";
        }
    }

    private static Set<String> getMissingKeys(Set<String> expectedKeys, Collection<String> actualKeys) {
        Set<String> missingKeys = new TreeSet<String>(expectedKeys);
        missingKeys.removeAll(actualKeys);
        return missingKeys;
    }

    private static String getExtraKeysMessage(Set<String> extraKeys, String path) {
        if (!extraKeys.isEmpty()) {
            return "Extra: " + appendKeysToPrefix(extraKeys, path);
        } else {
            return "";
        }
    }

    private Set<String> getExtraKeys(Set<String> expectedKeys, Collection<String> actualKeys) {
        if (!hasOption(IGNORING_EXTRA_FIELDS)) {
            Set<String> extraKeys = new TreeSet<String>(actualKeys);
            extraKeys.removeAll(expectedKeys);
            return extraKeys;
        } else {
            return emptySet();
        }
    }

    private boolean hasOption(Option option) {
        return configuration.getOptions().contains(option);
    }

    private static String appendKeysToPrefix(Iterable<String> keys, String prefix) {
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
    private void compareNodes(Node expectedNode, Node actualNode, String fieldPath) {
        NodeType expectedNodeType = expectedNode.getNodeType();
        NodeType actualNodeType = actualNode.getNodeType();

        //ignoring value
        if (expectedNodeType == NodeType.STRING && configuration.getIgnorePlaceholder().equals(expectedNode.asText())) {
            return;
        }


        // Any number
        if (checkAny(NodeType.NUMBER, "${json-unit.any-number}", "a number", expectedNode, actualNode, fieldPath)) {
            return;
        }
        // Any boolean
        if (checkAny(NodeType.BOOLEAN, "${json-unit.any-boolean}", "a boolean", expectedNode, actualNode, fieldPath)) {
            return;
        }
        // Any string
        if (checkAny(NodeType.STRING, "${json-unit.any-string}", "a string", expectedNode, actualNode, fieldPath)) {
            return;
        }

        if (!expectedNodeType.equals(actualNodeType)) {
            valueDifferenceFound("Different value found in node \"%s\". Expected '%s', got '%s'.", fieldPath, quoteTextValue(expectedNode), quoteTextValue(actualNode));
        } else {
            switch (expectedNodeType) {
                case OBJECT:
                    compareObjectNodes(expectedNode, actualNode, fieldPath);
                    break;
                case ARRAY:
                    compareArrayNodes(expectedNode, actualNode, fieldPath);
                    break;
                case STRING:
                    compareStringValues(expectedNode.asText(), actualNode.asText(), fieldPath);
                    break;
                case NUMBER:
                    BigDecimal actualValue = actualNode.decimalValue();
                    BigDecimal expectedValue = expectedNode.decimalValue();
                    if (configuration.getTolerance() != null && !hasOption(IGNORING_VALUES)) {
                        BigDecimal diff = expectedValue.subtract(actualValue).abs();
                        if (diff.compareTo(configuration.getTolerance()) > 0) {
                            valueDifferenceFound("Different value found in node \"%s\". Expected %s, got %s, difference is %s, tolerance is %s",
                                fieldPath, quoteTextValue(expectedValue), quoteTextValue(actualValue), diff.toString(), configuration.getTolerance());
                        }
                    } else {
                        compareValues(expectedValue, actualValue, fieldPath);
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

    private boolean checkAny(NodeType type, String placeholder, String name, Node expectedNode, Node actualNode, String fieldPath) {
        if (expectedNode.getNodeType() == NodeType.STRING && placeholder.equals(expectedNode.asText())) {
            if (actualNode.getNodeType() == type) {
                return true;
            } else {
                valueDifferenceFound("Different value found in node \"%s\". Expected %s, got '%s'.", fieldPath, name, quoteTextValue(actualNode));
                return true;
            }
        }
        return false;
    }

    private void compareStringValues(String expectedValue, String actualValue, String path) {
        if (hasOption(IGNORING_VALUES)) {
            return;
        }
        if (isRegexExpected(expectedValue)) {
            String pattern = getRegexPattern(expectedValue);
            if (!actualValue.matches(pattern)) {
                valueDifferenceFound("Different value found in node \"%s\". Pattern %s did not match %s.", path, quoteTextValue(pattern), quoteTextValue(actualValue));
            }
        } else {
            compareValues(expectedValue, actualValue, path);
        }
    }

    private String getRegexPattern(String expectedValue) {
        return expectedValue.substring(REGEX_PLACEHOLDER.length());
    }

    private boolean isRegexExpected(String expectedValue) {
        return expectedValue.startsWith(REGEX_PLACEHOLDER);
    }

    private void compareValues(Object expectedValue, Object actualValue, String path) {
        if (!hasOption(IGNORING_VALUES)) {
            if (!expectedValue.equals(actualValue)) {
                valueDifferenceFound("Different value found in node \"%s\". Expected %s, got %s.", path, quoteTextValue(expectedValue), quoteTextValue(actualValue));
            }
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


    private void compareArrayNodes(Node expectedNode, Node actualNode, String path) {
        List<Node> expectedElements = asList(expectedNode.arrayElements());
        List<Node> actualElements = asList(actualNode.arrayElements());


        if (failOnExtraArrayItems()) {
            if (expectedElements.size() != actualElements.size()) {
                structureDifferenceFound("Array \"%s\" has different length. Expected %d, got %d.", path, expectedElements.size(), actualElements.size());
            }
        } else {
            // if we expect more elements in the array then we get, it's error even when IGNORING_EXTRA_ARRAY_ITEMS
            if (expectedElements.size() > actualElements.size()) {
                structureDifferenceFound("Array \"%s\" has invalid length. Expected at least %d, got %d.", path, expectedElements.size(), actualElements.size());
            }
        }
        List<Node> extraValues = new ArrayList<Node>();
        List<Node> missingValues = new ArrayList<Node>(expectedElements);
        if (hasOption(IGNORING_ARRAY_ORDER)) {
            for (Node actual : actualElements) {
                int index = indexOf(missingValues, actual);
                if (index != -1) {
                    missingValues.remove(index);
                } else {
                    extraValues.add(actual);
                }
            }
            if (expectedElements.size() == actualElements.size() && missingValues.size() == 1 && extraValues.size() == 1) {
                // handling special case where only one difference is found.
                Node missing = missingValues.get(0);
                Node extra = extraValues.get(0);
                int missingIndex = indexOf(expectedElements, missing);
                int extraIndex = indexOf(actualElements, extra);

                valueDifferenceFound("Different value found when comparing expected array element %s to actual element %s.", getArrayPath(path, missingIndex), getArrayPath(path, extraIndex));
                compareNodes(missing, extra, getArrayPath(path, extraIndex));
            } else if (failOnExtraArrayItems()) {
                if (!missingValues.isEmpty() || !extraValues.isEmpty()) {
                    valueDifferenceFound("Array \"%s\" has different content. Missing values %s, extra values %s", path, missingValues, extraValues);
                }
            } else {
                if (!missingValues.isEmpty()) {
                    valueDifferenceFound("Array \"%s\" has different content. Missing values %s", path, missingValues);
                }
            }
        } else {
            for (int i = 0; i < Math.min(expectedElements.size(), actualElements.size()); i++) {
                compareNodes(expectedElements.get(i), actualElements.get(i), getArrayPath(path, i));
            }
        }
    }

    private boolean failOnExtraArrayItems() {
        return !hasOption(IGNORING_EXTRA_ARRAY_ITEMS);
    }

    /**
     * Finds element in the expected elements. Can not use Jackson comparison since we need to take Options into account
     *
     * @param expectedElements
     * @param actual
     * @return
     */
    private int indexOf(List<Node> expectedElements, Node actual) {
        int i = 0;
        for (Node expected : expectedElements) {
            Diff diff = new Diff(expected, actual, "", configuration);
            if (diff.similar()) {
                return i;
            }
            i++;
        }
        return -1;
    }


    private List<Node> asList(Iterator<Node> elements) {
        List<Node> result = new ArrayList<Node>();
        while (elements.hasNext()) {
            Node Node = elements.next();
            result.add(Node);
        }
        return Collections.unmodifiableList(result);
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
        differences.add(message, arguments);
    }

    private void valueDifferenceFound(String message, Object... arguments) {
        if (!hasOption(COMPARING_ONLY_STRUCTURE)) {
            differences.add(message, arguments);
        }
    }


    private Set<String> commonFields(Map<String, Node> expectedFields, Map<String, Node> actualFields) {
        Set<String> result = new TreeSet<String>(expectedFields.keySet());
        result.retainAll(actualFields.keySet());
        return Collections.unmodifiableSet(result);
    }


    private SortedSet<String> sort(Set<String> set) {
        return new TreeSet<String>(set);
    }

    public boolean similar() {
        compare();
        boolean isSimilar = differences.isEmpty();
        logDifferences(isSimilar);
        return isSimilar;
    }

    private void logDifferences(boolean isSimilar) {
        if (!isSimilar) {
            if (diffLogger.isEnabled()) {
                diffLogger.log(getDifferences().trim());
            }
            if (valuesLogger.isEnabled()) {
                valuesLogger.log("Comparing expected:\n{}\n------------\nwith actual:\n{}\n", expectedRoot, getNode(actualRoot, startPath));
            }
        }
    }

    /**
     * Returns children of an ObjectNode.
     *
     * @param node
     * @return
     */
    private static Map<String, Node> getFields(Node node) {
        Map<String, Node> result = new HashMap<String, Node>();
        Iterator<KeyValue> fields = node.fields();
        while (fields.hasNext()) {
            KeyValue field = fields.next();
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
        differences.appendDifferences(message);
        return message.toString();
    }

    private static JsonUnitLogger createLogger(String name) {
        if (isClassPresent("org.slf4j.Logger")) {
            return new JsonUnitLogger.SLF4JLogger(name);
        } else {
            return new JsonUnitLogger.NullLogger();
        }
    }
}
