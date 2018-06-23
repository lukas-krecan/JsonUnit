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
package net.javacrumbs.jsonunit.core.internal;

import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.Option;
import net.javacrumbs.jsonunit.core.ParametrizedMatcher;
import net.javacrumbs.jsonunit.core.internal.ArrayComparison.NodeWithIndex;
import net.javacrumbs.jsonunit.core.listener.Difference;
import org.hamcrest.Description;
import org.hamcrest.StringDescription;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Collections.emptySet;
import static net.javacrumbs.jsonunit.core.Option.COMPARING_ONLY_STRUCTURE;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_ARRAY_ITEMS;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_VALUES;
import static net.javacrumbs.jsonunit.core.internal.ClassUtils.isClassPresent;
import static net.javacrumbs.jsonunit.core.internal.DifferenceContextImpl.differenceContext;
import static net.javacrumbs.jsonunit.core.internal.JsonUnitLogger.NULL_LOGGER;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.convertToJson;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.quoteIfNeeded;
import static net.javacrumbs.jsonunit.core.internal.Node.KeyValue;
import static net.javacrumbs.jsonunit.core.internal.Node.NodeType;


/**
 * Compares JSON structures. Mainly for internal use, the API might be more volatile than the rest.
 *
 * @author Lukas Krecan
 */
public class Diff {

    private static final String ANY_NUMBER_PLACEHOLDER = "${json-unit.any-number}";
    private static final String ANY_BOOLEAN_PLACEHOLDER = "${json-unit.any-boolean}";
    private static final String ANY_STRING_PLACEHOLDER = "${json-unit.any-string}";

    private static final String REGEX_PLACEHOLDER = "${json-unit.regex}";
    private static final Pattern MATCHER_PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{json-unit.matches:(.+?)}(.*)");

    private static final JsonUnitLogger DEFAULT_DIFF_LOGGER = createLogger("net.javacrumbs.jsonunit.difference.diff");
    private static final JsonUnitLogger DEFAULT_VALUE_LOGGER = createLogger("net.javacrumbs.jsonunit.difference.values");

    private final Node expectedRoot;
    private final Node actualRoot;
    private final Differences differences = new Differences();
    private final Path startPath;
    private boolean compared = false;
    private final Configuration configuration;
    private final PathMatcher pathsToBeIgnored;

    private final JsonUnitLogger diffLogger;
    private final JsonUnitLogger valuesLogger;

    Diff(Node expected, Node actual, Path startPath, Configuration configuration, JsonUnitLogger diffLogger, JsonUnitLogger valuesLogger) {
        this.expectedRoot = expected;
        this.actualRoot = actual;
        this.startPath = startPath;
        this.configuration = configuration;
        this.diffLogger = diffLogger;
        this.valuesLogger = valuesLogger;
        this.pathsToBeIgnored = PathMatcher.create(configuration.getPathsToBeIgnored());
    }

    public static Diff create(Object expected, Object actual, String actualName, String path, Configuration configuration) {
        if (actual instanceof JsonSource) {
            return create(expected, actual, actualName, Path.create(path, ((JsonSource) actual).getPathPrefix()), configuration);
        } else {
            return create(expected, actual, actualName, Path.create(path, ""), configuration);
        }
    }

    public static Diff create(Object expected, Object actual, String actualName, Path path, Configuration configuration) {
        return new Diff(convertToJson(quoteIfNeeded(expected), "expected", true), convertToJson(actual, actualName, false), path, configuration, DEFAULT_DIFF_LOGGER, DEFAULT_VALUE_LOGGER);
    }

    private void compare() {
        if (!compared) {
            Node part = startPath.getNode(actualRoot);
            if (part.isMissingNode()) {
                structureDifferenceFound("Missing node in path \"%s\".", startPath);
            } else {
                compareNodes(new Context(expectedRoot, part, startPath, startPath, configuration));
            }
            compared = true;
        }
    }

    /**
     * Compares object nodes.
     */
    private void compareObjectNodes(Context context) {
        Node expected = context.getExpectedNode();
        Node actual = context.getActualNode();

        Path path = context.getActualPath();

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


            removePathsToBeIgnored(path, extraKeys);

            if (!missingKeys.isEmpty() || !extraKeys.isEmpty()) {
                for (String key : missingKeys) {
                    reportDifference(DifferenceImpl.missing(context.inField(key)));
                }
                for (String key : extraKeys) {
                    reportDifference(DifferenceImpl.extra(context.inField(key)));
                }
                String missingKeysMessage = getMissingKeysMessage(missingKeys, path);
                String extraKeysMessage = getExtraKeysMessage(extraKeys, path);
                structureDifferenceFound("Different keys found in node \"%s\", expected: <%s> but was: <%s>. %s %s", path, sort(expectedFields.keySet()), sort(actualFields.keySet()), missingKeysMessage, extraKeysMessage);
            }
        }

        for (String fieldName : commonFields(expectedFields, actualFields)) {
            compareNodes(context.inField(fieldName));
        }
    }

    private void reportDifference(Difference difference) {
        configuration.getDifferenceListener().diff(difference,
                differenceContext(configuration, actualRoot, expectedRoot));
    }

    private void removePathsToBeIgnored(Path path, Set<String> extraKeys) {
        if (!configuration.getPathsToBeIgnored().isEmpty()) {
            Iterator<String> iterator = extraKeys.iterator();
            while (iterator.hasNext()) {
                Path keyWithPath = path.toField(iterator.next());
                if (shouldIgnorePath(keyWithPath)) {
                    iterator.remove();
                }
            }
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
        Set<String> notNullExtraKeys = new TreeSet<>();
        for (String extraKey : extraKeys) {
            if (!actual.get(extraKey).isNull()) {
                notNullExtraKeys.add(extraKey);
            }
        }
        return notNullExtraKeys;
    }


    private static String getMissingKeysMessage(Set<String> missingKeys, Path path) {
        if (!missingKeys.isEmpty()) {
            return "Missing: " + appendKeysToPrefix(missingKeys, path);
        } else {
            return "";
        }
    }

    private static Set<String> getMissingKeys(Set<String> expectedKeys, Collection<String> actualKeys) {
        Set<String> missingKeys = new TreeSet<>(expectedKeys);
        missingKeys.removeAll(actualKeys);
        return missingKeys;
    }

    private static String getExtraKeysMessage(Set<String> extraKeys, Path path) {
        if (!extraKeys.isEmpty()) {
            return "Extra: " + appendKeysToPrefix(extraKeys, path);
        } else {
            return "";
        }
    }

    private Set<String> getExtraKeys(Set<String> expectedKeys, Collection<String> actualKeys) {
        if (!hasOption(IGNORING_EXTRA_FIELDS)) {
            Set<String> extraKeys = new TreeSet<>(actualKeys);
            extraKeys.removeAll(expectedKeys);
            return extraKeys;
        } else {
            return emptySet();
        }
    }

    private boolean hasOption(Option option) {
        return configuration.getOptions().contains(option);
    }

    private static String appendKeysToPrefix(Iterable<String> keys, Path prefix) {
        Iterator<String> iterator = keys.iterator();
        StringBuilder buffer = new StringBuilder();
        while (iterator.hasNext()) {
            String key = iterator.next();
            buffer.append("\"").append(prefix.toField(key)).append("\"");
            if (iterator.hasNext()) {
                buffer.append(",");
            }
        }
        return buffer.toString();
    }

    /**
     * Compares two nodes.
     */
    private void compareNodes(Context context) {
        if (shouldIgnorePath(context.getActualPath())) {
            return;
        }

        Node expectedNode = context.getExpectedNode();
        Node actualNode = context.getActualNode();

        NodeType expectedNodeType = expectedNode.getNodeType();
        NodeType actualNodeType = actualNode.getNodeType();

        Path fieldPath = context.getActualPath();

        //ignoring value
        if (expectedNodeType == NodeType.STRING && configuration.getIgnorePlaceholder().equals(expectedNode.asText())) {
            return;
        }

        // Any number
        if (checkAny(NodeType.NUMBER, ANY_NUMBER_PLACEHOLDER, "a number", context)) {
            return;
        }

        // Any boolean
        if (checkAny(NodeType.BOOLEAN, ANY_BOOLEAN_PLACEHOLDER, "a boolean", context)) {
            return;
        }

        // Any string
        if (checkAny(NodeType.STRING, ANY_STRING_PLACEHOLDER, "a string", context)) {
            return;
        }

        if (checkMatcher(context)) {
            return;
        }

        if (!expectedNodeType.equals(actualNodeType)) {
            valueDifferenceFound(context, "Different value found in node \"%s\", expected: <%s> but was: <%s>.", fieldPath, quoteTextValue(expectedNode), quoteTextValue(actualNode));
        } else {
            switch (expectedNodeType) {
                case OBJECT:
                    compareObjectNodes(context);
                    break;
                case ARRAY:
                    compareArrayNodes(context);
                    break;
                case STRING:
                    compareStringValues(context);
                    break;
                case NUMBER:
                    BigDecimal actualValue = actualNode.decimalValue();
                    BigDecimal expectedValue = expectedNode.decimalValue();
                    if (configuration.getTolerance() != null && !hasOption(IGNORING_VALUES)) {
                        BigDecimal diff = expectedValue.subtract(actualValue).abs();
                        if (diff.compareTo(configuration.getTolerance()) > 0) {
                            valueDifferenceFound(context, "Different value found in node \"%s\", expected: <%s> but was: <%s>, difference is %s, tolerance is %s",
                                    fieldPath, quoteTextValue(expectedValue), quoteTextValue(actualValue), diff.toString(), configuration.getTolerance());
                        }
                    } else {
                        compareValues(context, expectedValue, actualValue);
                    }
                    break;
                case BOOLEAN:
                    compareValues(context, expectedNode.asBoolean(), actualNode.asBoolean());
                    break;
                case NULL:
                    //nothing
                    break;
                default:
                    throw new IllegalStateException("Unexpected node type " + expectedNodeType);
            }
        }
    }

    private boolean shouldIgnorePath(Path fieldPath) {
        return pathsToBeIgnored.matches(fieldPath.getFullPath());
    }

    private boolean checkMatcher(Context context) {
        Node expectedNode = context.getExpectedNode();
        Node actualNode = context.getActualNode();

        if (expectedNode.getNodeType() == NodeType.STRING) {
            Matcher patternMatcher = MATCHER_PLACEHOLDER_PATTERN.matcher(expectedNode.asText());
            if (patternMatcher.matches()) {
                String matcherName = patternMatcher.group(1);
                org.hamcrest.Matcher<?> matcher = configuration.getMatcher(matcherName);
                if (matcher != null) {
                    if (matcher instanceof ParametrizedMatcher) {
                        ((ParametrizedMatcher) matcher).setParameter(patternMatcher.group(2));
                    }
                    Object value = actualNode.getValue();
                    if (!matcher.matches(value)) {
                        Description description = new StringDescription();
                        matcher.describeMismatch(value, description);
                        valueDifferenceFound(context, "Matcher \"%s\" does not match value %s in node \"%s\". %s", matcherName, quoteTextValue(actualNode), context.getActualPath(), description);
                    }
                } else {
                    structureDifferenceFound("Matcher \"%s\" not found.", matcherName);
                }
                return true;
            }
        }
        return false;
    }

    private boolean checkAny(NodeType type, String placeholder, String name, Context context) {
        Node expectedNode = context.getExpectedNode();
        Node actualNode = context.getActualNode();

        if (expectedNode.getNodeType() == NodeType.STRING && placeholder.equals(expectedNode.asText())) {
            if (actualNode.getNodeType() == type) {
                return true;
            } else {
                valueDifferenceFound(context, "Different value found in node \"%s\", expected: <%s> but was: <%s>.", context.getActualPath(), name, quoteTextValue(actualNode));
                return true;
            }
        }
        return false;
    }

    private void compareStringValues(Context context) {
        String expectedValue = context.getExpectedNode().asText();
        String actualValue = context.getActualNode().asText();
        Path path = context.getActualPath();

        if (hasOption(IGNORING_VALUES)) {
            return;
        }
        if (isRegexExpected(expectedValue)) {
            String pattern = getRegexPattern(expectedValue);
            if (!actualValue.matches(pattern)) {
                valueDifferenceFound(context, "Different value found in node \"%s\". Pattern %s did not match %s.", path, quoteTextValue(pattern), quoteTextValue(actualValue));
            }
        } else {
            compareValues(context, expectedValue, actualValue);
        }
    }

    private String getRegexPattern(String expectedValue) {
        return expectedValue.substring(REGEX_PLACEHOLDER.length());
    }

    private boolean isRegexExpected(String expectedValue) {
        return expectedValue.startsWith(REGEX_PLACEHOLDER);
    }

    private void compareValues(Context context, Object expectedValue, Object actualValue) {
        if (!hasOption(IGNORING_VALUES)) {
            if (!expectedValue.equals(actualValue)) {
                valueDifferenceFound(context, "Different value found in node \"%s\", expected: <%s> but was: <%s>.", context.getActualPath(), quoteTextValue(expectedValue), quoteTextValue(actualValue));
            }
        }
    }

    /**
     * If the value is String than it's quoted in ".
     *
     * @param value
     * @return
     */
    public static Object quoteTextValue(Object value) {
        if (value instanceof String) {
            return "\"" + value + "\"";
        } else {
            return value;
        }
    }


    private void compareArrayNodes(Context context) {
        Node expectedNode = context.getExpectedNode();
        Node actualNode = context.getActualNode();
        Path path = context.getActualPath();

        List<Node> expectedElements = asList(expectedNode.arrayElements());
        List<Node> actualElements = asList(actualNode.arrayElements());


        if (failOnExtraArrayItems()) {
            if (expectedElements.size() != actualElements.size()) {
                structureDifferenceFound("Array \"%s\" has different length, expected: <%d> but was: <%d>.", path, expectedElements.size(), actualElements.size());
            }
        } else {
            // if we expect more elements in the array than we get, it's error even when IGNORING_EXTRA_ARRAY_ITEMS
            if (expectedElements.size() > actualElements.size()) {
                structureDifferenceFound("Array \"%s\" has invalid length, expected: <at least %d> but was: <%d>.", path, expectedElements.size(), actualElements.size());
            }
        }

        if (hasOption(IGNORING_ARRAY_ORDER)) {
            ArrayComparison arrayComparison = compareArraysIgnoringOrder(expectedElements, actualElements, path);
            List<NodeWithIndex> missingValues = arrayComparison.getMissingValues();
            List<NodeWithIndex> extraValues = arrayComparison.getExtraValues();
            if (expectedElements.size() == actualElements.size() && missingValues.size() == 1 && extraValues.size() == 1) {
                // handling special case where only one difference is found.
                NodeWithIndex missing = missingValues.get(0);
                NodeWithIndex extra = extraValues.get(0);

                Path expectedPath = context.getExpectedPath().toElement(missing.getIndex());
                Path actualPath = context.getActualPath().toElement(extra.getIndex());
                valueDifferenceFound("Different value found when comparing expected array element %s to actual element %s.", expectedPath, actualPath);
                compareNodes(new Context(missing.getNode(), extra.getNode(), expectedPath, actualPath, configuration));
            } else if (failOnExtraArrayItems() && (!missingValues.isEmpty() || !extraValues.isEmpty())) {
                reportMissingValues(context, missingValues);
                reportExtraValues(context, extraValues);

                valueDifferenceFound("Array \"%s\" has different content, expected: <%s> but was: <%s>. Missing values %s, extra values %s", path, expectedNode, actualNode, missingValues, extraValues);
            } else if (!missingValues.isEmpty()) {
                reportMissingValues(context, missingValues);
                valueDifferenceFound("Array \"%s\" has different content, expected: <%s> but was: <%s>. Missing values %s", path, expectedNode, actualNode, missingValues);
            }
        } else {
            if (expectedElements.size() > actualElements.size()) {
                for (int i = actualElements.size(); i < expectedElements.size(); i++) {
                    reportDifference(DifferenceImpl.missing(context.missingElement(i)));
                }
                valueDifferenceFound("Array \"%s\" has different content, expected: <%s> but was: <%s>. Missing values %s", path, expectedNode, actualNode, expectedElements.subList(actualElements.size(), expectedElements.size()));
            } else if (failOnExtraArrayItems() && expectedElements.size() < actualElements.size()) {
                for (int i = expectedElements.size(); i < actualElements.size(); i++) {
                    reportDifference(DifferenceImpl.extra(context.extraElement(i)));
                }
                valueDifferenceFound("Array \"%s\" has different content, expected: <%s> but was: <%s>. Extra values %s", path, expectedNode, actualNode, actualElements.subList(expectedElements.size(), actualElements.size()));
            }
            for (int i = 0; i < Math.min(expectedElements.size(), actualElements.size()); i++) {
                compareNodes(context.toElement(i));
            }
        }
    }

    private void reportMissingValues(Context context, List<NodeWithIndex> missingValues) {
        for (NodeWithIndex missingValue : missingValues) {
            reportDifference(DifferenceImpl.missing(context.missingElement(missingValue.getIndex())));
        }
    }

    private void reportExtraValues(Context context, List<NodeWithIndex> extraValues) {
        for (NodeWithIndex extraValue : extraValues) {
            reportDifference(DifferenceImpl.extra(context.extraElement(extraValue.getIndex())));
        }
    }

    private ArrayComparison compareArraysIgnoringOrder(List<Node> expectedElements, List<Node> actualElements, Path path) {
        return new ArrayComparison(expectedElements, actualElements, path, configuration).compareArraysIgnoringOrder();
    }


    private boolean failOnExtraArrayItems() {
        return !hasOption(IGNORING_EXTRA_ARRAY_ITEMS);
    }


    private List<Node> asList(Iterator<Node> elements) {
        List<Node> result = new ArrayList<>();
        while (elements.hasNext()) {
            Node Node = elements.next();
            result.add(Node);
        }
        return Collections.unmodifiableList(result);
    }


    private void structureDifferenceFound(String message, Object... arguments) {
        differences.add(message, arguments);
    }

    private void valueDifferenceFound(String message, Object... arguments) {
        if (!hasOption(COMPARING_ONLY_STRUCTURE)) {
            differences.add(message, arguments);
        }
    }

    private void valueDifferenceFound(Context context, String message, Object... arguments) {
        reportDifference(DifferenceImpl.different(context));
        valueDifferenceFound(message, arguments);
    }


    private Set<String> commonFields(Map<String, Node> expectedFields, Map<String, Node> actualFields) {
        Set<String> result = new TreeSet<>(expectedFields.keySet());
        result.retainAll(actualFields.keySet());
        return Collections.unmodifiableSet(result);
    }


    private SortedSet<String> sort(Set<String> set) {
        return new TreeSet<>(set);
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
                valuesLogger.log("Comparing expected:\n{}\n------------\nwith actual:\n{}\n", expectedRoot, startPath.getNode(actualRoot));
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
        Map<String, Node> result = new HashMap<>();
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
            return NULL_LOGGER;
        }
    }
}
