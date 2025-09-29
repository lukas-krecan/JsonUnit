package net.javacrumbs.jsonunit.core.internal;

import static java.util.Collections.emptyIterator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.javacrumbs.jsonunit.core.internal.Node.KeyValue;

class Normalizer {

    private static final int depth = 2;

    /**
     *
     * @param node to be printed
     * @param expected to use for ordering in objects
     */
    static String toNormalizedString(Node node, Node expected) {
        StringBuilder sb = new StringBuilder();
        normalize(node, expected, sb, 0);
        return sb.toString();
    }

    private static void normalize(Node node, Node expected, StringBuilder sb, int indent) {
        switch (node.getNodeType()) {
            case OBJECT -> normalizeObject(node, expected, sb, indent);
            case ARRAY -> normalizeArray(node, expected, sb, indent);
            default -> sb.append(node);
        }
    }

    private static void normalizeArray(Node node, Node expected, StringBuilder sb, int indent) {
        sb.append("[\n");
        Iterator<Node> elements = node.arrayElements();
        int i = 0;
        while (elements.hasNext()) {
            var element = elements.next();
            addIndent(sb, indent + depth);
            normalize(element, expected.element(i), sb, indent + depth);
            if (elements.hasNext()) sb.append(",");
            sb.append('\n');
        }
        addIndent(sb, indent);
        sb.append("]");
    }

    private static void normalizeObject(Node node, Node expected, StringBuilder sb, int indent) {
        sb.append("{\n");
        Iterator<KeyValue> expectedValues = expected.isObject() ? expected.fields() : emptyIterator();
        List<KeyValue> toBePrinted = new ArrayList<>();
        Set<String> processedKeys = new HashSet<>();
        // Print actual values in order of expected values
        while (expectedValues.hasNext()) {
            var key = expectedValues.next().getKey();
            var value = node.get(key);
            if (!value.isMissingNode()) {
                toBePrinted.add(new KeyValue(key, value));
                processedKeys.add(key);
            }
        }

        // Print values that were not in expected
        Iterator<KeyValue> nodeFields = node.fields();
        while (nodeFields.hasNext()) {
            var keyValue = nodeFields.next();
            if (!processedKeys.contains(keyValue.getKey())) {
                toBePrinted.add(keyValue);
            }
        }

        var toBePrintedIterator = toBePrinted.iterator();
        while (toBePrintedIterator.hasNext()) {
            var keyValue = toBePrintedIterator.next();
            addIndent(sb, indent + depth);
            sb.append('"').append(keyValue.getKey()).append("\": ");
            normalize(keyValue.getValue(), expected.get(keyValue.getKey()), sb, indent + depth);
            if (toBePrintedIterator.hasNext()) sb.append(",");
            sb.append('\n');
        }
        addIndent(sb, indent);
        sb.append("}");
    }

    private static void addIndent(StringBuilder sb, int indent) {
        sb.append(" ".repeat(Math.max(0, indent)));
    }
}
