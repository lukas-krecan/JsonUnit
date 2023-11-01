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

import static net.javacrumbs.jsonunit.core.internal.JsonUtils.prettyPrint;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;

/**
 * For internal use only!!! Abstract node representation.
 */
public interface Node {

    enum NodeType implements ValueExtractor {
        OBJECT("object") {
            @Override
            public Object getValue(Node node) {
                // custom conversion to map. We want be consistent and native mapping may have different rules for
                // serializing numbers, dates etc.
                return new JsonMap(node);
            }
        },
        ARRAY("array") {
            @Override
            public Object getValue(Node node) {
                return new JsonList(node);
            }
        },
        STRING("string") {
            @Override
            public Object getValue(Node node) {
                return node.asText();
            }
        },
        NUMBER("number") {
            @Override
            public Object getValue(Node node) {
                return node.decimalValue();
            }
        },
        BOOLEAN("boolean") {
            @Override
            public Object getValue(Node node) {
                return node.asBoolean();
            }
        },
        NULL("null") {
            @Override
            public Object getValue(Node node) {
                return null;
            }
        };

        private final String description;

        NodeType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    Node element(int index);

    Iterator<KeyValue> fields();

    Node get(String key);

    boolean isMissingNode();

    boolean isNull();

    Iterator<Node> arrayElements();

    /**
     * Array length
     */
    int size();

    String asText();

    NodeType getNodeType();

    BigDecimal decimalValue();

    /**
     * Returns true if the value is an integer. 1 is an integer 1.0, 1.1, 1e3, 1e0, 1e-3 is not.
     */
    default boolean isIntegralNumber() {
        BigDecimal decimalValue = decimalValue();
        String text = decimalValue.toString();
        return decimalValue.scale() == 0 && !text.contains("e") && !text.contains("E");
    }

    Boolean asBoolean();

    Object getValue();

    void ___do_not_implement_this_interface_seriously();

    class KeyValue {
        private final String key;
        private final Node value;

        KeyValue(String key, Node value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public Node getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "{\"" + key + "\": " + value + "}";
        }
    }

    Node MISSING_NODE = new Node() {
        @Override
        public Node element(int index) {
            return MISSING_NODE;
        }

        @Override
        public Iterator<KeyValue> fields() {
            return Collections.emptyIterator();
        }

        @Override
        public Node get(String key) {
            return this;
        }

        @Override
        public boolean isMissingNode() {
            return true;
        }

        @Override
        public boolean isNull() {
            return false;
        }

        @Override
        public Iterator<Node> arrayElements() {
            return Collections.emptyIterator();
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public String asText() {
            throw new UnsupportedOperationException();
        }

        @Override
        public NodeType getNodeType() {
            throw new UnsupportedOperationException();
        }

        @Override
        public BigDecimal decimalValue() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Boolean asBoolean() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object getValue() {
            return null;
        }

        @Override
        public void ___do_not_implement_this_interface_seriously() {}

        @Override
        public String toString() {
            return "<missing>";
        }
    };

    interface ValueExtractor {
        Object getValue(Node node);
    }

    class JsonMap extends LinkedHashMap<String, Object> implements NodeWrapper {
        private final Node wrappedNode;

        JsonMap(Node node) {
            wrappedNode = node;
            Iterator<KeyValue> fields = node.fields();
            while (fields.hasNext()) {
                KeyValue keyValue = fields.next();
                put(keyValue.getKey(), keyValue.getValue().getValue());
            }
        }

        @Override
        public String toString() {
            return prettyPrint(this);
        }

        @Override
        public Node getWrappedNode() {
            return wrappedNode;
        }
    }

    class JsonList extends LinkedList<Object> implements NodeWrapper {
        private final Node wrappedNode;

        JsonList(Node node) {
            Iterator<Node> nodeIterator = node.arrayElements();
            while (nodeIterator.hasNext()) {
                Node arrayNode = nodeIterator.next();
                add(arrayNode.getValue());
            }
            wrappedNode = node;
        }

        @Override
        public Node getWrappedNode() {
            return wrappedNode;
        }
    }
}
