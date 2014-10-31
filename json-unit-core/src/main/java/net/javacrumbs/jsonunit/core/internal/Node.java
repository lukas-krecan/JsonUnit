package net.javacrumbs.jsonunit.core.internal;

import java.math.BigDecimal;
import java.util.Iterator;


/**
 * Abstract node representation
 */
interface Node {
    enum NodeType {OBJECT, ARRAY, STRING, NUMBER, BOOLEAN, NULL}

    Node path(int index);

    Node path(String path);

    boolean isMissingNode();

    Iterator<KeyValue> fields();

    Node get(String key);

    boolean isNull();

    Iterator<Node> elements();

    String asText();

    NodeType getNodeType();

    BigDecimal decimalValue();

    Number numberValue();

    Boolean asBoolean();

    static class KeyValue {
        private final String key;
        private final Node value;

        public KeyValue(String key, Node value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public Node getValue() {
            return value;
        }
    }
}
