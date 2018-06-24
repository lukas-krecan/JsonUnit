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

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.unmodifiableList;


/**
 * For internal use only!!! Abstract node representation.
 */
public interface Node {

    enum NodeType implements ValueExtractor {
        OBJECT("object") {
            public Object getValue(Node node) {
                // custom conversion to map. We want be consistent and native mapping may have different rules for
                // serializing numbers, dates etc.
                JsonMap result = new JsonMap();
                Iterator<KeyValue> fields = node.fields();
                while (fields.hasNext()) {
                    KeyValue keyValue = fields.next();
                    result.put(keyValue.getKey(), keyValue.getValue().getValue());
                }
                return result;
            }
        },
        ARRAY("array") {
            public Object getValue(Node node) {
                Iterator<Node> nodeIterator = node.arrayElements();
                LinkedList<Object> result = new LinkedList<>();
                while (nodeIterator.hasNext()) {
                    Node arrayNode = nodeIterator.next();
                    result.add(arrayNode.getValue());
                }
                return unmodifiableList(result);
            }
        },
        STRING("string") {
            public Object getValue(Node node) {
                return node.asText();
            }
        },
        NUMBER("number") {
            public Object getValue(Node node) {
                return node.decimalValue();
            }
        },
        BOOLEAN("boolean") {
            public Object getValue(Node node) {
                return node.asBoolean();
            }
        },
        NULL("null") {
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
    }

    Node MISSING_NODE = new Node() {
        public boolean isArray() {
            return false;
        }

        public Node element(int index) {
            return MISSING_NODE;
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

        public int size() {
            return 0;
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

        public Object getValue() {
            throw new UnsupportedOperationException();
        }

        public void ___do_not_implement_this_interface_seriously() {}
    };
    interface ValueExtractor {
        Object getValue(Node node);
    }

    class JsonMap extends LinkedHashMap<String, Object> {
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("{");
            Iterator<Map.Entry<String, Object>> entries = entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<String, Object> entry = entries.next();
                builder
                    .append('"').append(entry.getKey()).append('"')
                    .append(":")
                    .append(quoteString(entry.getValue()));
                if (entries.hasNext()) {
                    builder.append(", ");
                }
            }
            builder.append("}");
            return builder.toString();
        }

        private Object quoteString(Object value) {
            return value instanceof String ? "\"" + value + "\"" : value;
        }
    }
}
