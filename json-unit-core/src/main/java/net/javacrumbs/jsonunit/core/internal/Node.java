package net.javacrumbs.jsonunit.core.internal;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;


/**
 * Abstract node representation
 */
interface Node {


    enum NodeType {OBJECT, ARRAY, STRING, NUMBER, BOOLEAN, NULL}

    Node element(int index);

    Iterator<KeyValue> fields();

    Node get(String key);

    boolean isMissingNode();

    boolean isNull();

    Iterator<Node> arrayElements();

    String asText();

    NodeType getNodeType();

    BigDecimal decimalValue();

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

    static final Node MISSING_NODE = new Node() {

        public Node element(int index) {
            return null;
        }

        public Iterator<KeyValue> fields() {
            Set<KeyValue> emptySet = Collections.emptySet();
            return emptySet.iterator();
        }

        public Node get(String key) {
            return this;
        }

        public boolean isMissingNode() {
            return true;
        }

        public boolean isNull() {
            return false;
        }

        public Iterator<Node> arrayElements() {
            Set<Node> emptySet = Collections.emptySet();
            return emptySet.iterator();
        }

        public String asText() {
            throw new UnsupportedOperationException();
        }

        public NodeType getNodeType() {
            throw new UnsupportedOperationException();
        }

        public BigDecimal decimalValue() {
            throw new UnsupportedOperationException();
        }

        public Boolean asBoolean() {
            throw new UnsupportedOperationException();
        }
    };
}
