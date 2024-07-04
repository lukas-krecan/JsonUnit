package net.javacrumbs.jsonunit.core.internal;

import static java.util.Comparator.comparing;

import java.util.Iterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.javacrumbs.jsonunit.core.internal.Node.KeyValue;

class Normalizer {

    private static int depth = 2;

    static String toNormalizedString(Node node) {
        StringBuilder sb = new StringBuilder();
        normalize(node, sb, 0);
        return sb.toString();
    }

    private static void normalize(Node node, StringBuilder sb, int indent) {
        switch (node.getNodeType()) {
            case OBJECT -> normalizeObject(node, sb, indent);
            case ARRAY -> normalizeArray(node, sb, indent);
            case STRING -> sb.append('\"').append(node).append('"');
            default -> sb.append(node);
        }
    }

    private static void normalizeArray(Node node, StringBuilder sb, int indent) {
        sb.append("[\n");
        Iterator<Node> elements = node.arrayElements();
        while (elements.hasNext()) {
            var element = elements.next();
            addIndent(sb, indent + depth);
            normalize(element, sb, indent + depth);
            if (elements.hasNext()) sb.append(",");
            sb.append('\n');
        }
        addIndent(sb, indent);
        sb.append("]");
    }

    private static void normalizeObject(Node node, StringBuilder sb, int indent) {
        sb.append("{\n");
        Iterator<KeyValue> sortedValues =
                stream(node.fields()).sorted(comparing(KeyValue::getKey)).iterator();
        while (sortedValues.hasNext()) {
            var keyValue = sortedValues.next();
            addIndent(sb, indent + depth);
            sb.append('"').append(keyValue.getKey()).append("\": ");
            normalize(keyValue.getValue(), sb, indent + depth);
            if (sortedValues.hasNext()) sb.append(",");
            sb.append('\n');
        }
        addIndent(sb, indent);
        sb.append("}");
    }

    private static void addIndent(StringBuilder sb, int indent) {
        for (int i = 0; i < indent; i++) {
            sb.append(' ');
        }
    }

    private static <T> Stream<T> stream(Iterator<T> iterator) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 0), false);
    }
}
