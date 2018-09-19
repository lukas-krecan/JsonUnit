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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

class GenericNodeBuilder implements NodeBuilder {
    private static final NodeBuilder INSTANCE = new GenericNodeBuilder();

    static Node wrapDeserializedObject(Object object) {
        return INSTANCE.newNode(object);
    }

    public Node newNode(Object object) {
        if (object == null) {
            return new NullNode();
        } else if (object instanceof Map) {
            return new ObjectNode((Map<String, Object>) object, this);
        } else if (object instanceof Number) {
            return new NumberNode((Number) object);
        } else if (object instanceof String) {
            return new StringNode((String) object);
        } else if (object instanceof Boolean) {
            return new BooleanNode((Boolean) object);
        } else if (object instanceof Object[]) {
            return new ArrayNode(Arrays.asList((Object[]) object), this);
        } else if (object instanceof int[]) {
            int[] array = (int[]) object;
            List<Integer> list = new ArrayList<>(array.length);
            for (int i : array) {
                list.add(i);
            }
            return new ArrayNode(list, this);
        } else if (object instanceof double[]) {
            double[] array = (double[]) object;
            List<Double> list = new ArrayList<>(array.length);
            for (double i : array) {
                list.add(i);
            }
            return new ArrayNode(list, this);
        } else if (object instanceof List) {
            return new ArrayNode((List<?>) object, this);
        } else {
            throw new IllegalArgumentException("Unsupported type " + object.getClass());
        }
    }

    static abstract class NodeSkeleton extends AbstractNode {
        public Node element(int index) {
            throw new UnsupportedOperationException();
        }

        public Iterator<KeyValue> fields() {
            throw new UnsupportedOperationException();
        }

        public Node get(String key) {
            return MISSING_NODE;
        }

        public boolean isMissingNode() {
            return false;
        }

        public boolean isNull() {
            return false;
        }

        public Iterator<Node> arrayElements() {
            throw new UnsupportedOperationException();
        }

        public int size() {
            throw new UnsupportedOperationException();
        }

        public String asText() {
            throw new UnsupportedOperationException();
        }

        public BigDecimal decimalValue() {
            throw new UnsupportedOperationException();
        }

        public Boolean asBoolean() {
            throw new UnsupportedOperationException();
        }
    }

    static class NullNode extends NodeSkeleton {
        @Override
        public boolean isNull() {
            return true;
        }

        public NodeType getNodeType() {
            return NodeType.NULL;
        }

        @Override
        public String toString() {
            return "null";
        }
    }

    static class NumberNode extends GenericNodeBuilder.NodeSkeleton {
        private final Number value;

        NumberNode(Number value) {
            this.value = value;
        }

        public NodeType getNodeType() {
            return NodeType.NUMBER;
        }

        @Override
        public BigDecimal decimalValue() {
            return new BigDecimal(value.toString());
        }

        @Override
        public String toString() {
            return decimalValue().toString();
        }
    }


    static final class StringNode extends GenericNodeBuilder.NodeSkeleton {
        private final String value;

        StringNode(String value) {
            this.value = value;
        }

        public NodeType getNodeType() {
            return NodeType.STRING;
        }

        @Override
        public String asText() {
            return value;
        }

        @Override
        public String toString() {
            return '"' + value + '"';
        }
    }

    static final class BooleanNode extends NodeSkeleton {
        private final Boolean value;

        BooleanNode(Boolean value) {
            this.value = value;
        }

        public NodeType getNodeType() {
            return NodeType.BOOLEAN;
        }

        @Override
        public Boolean asBoolean() {
            return value;
        }

        @Override
        public String toString() {
            return value.toString();
        }
    }

    static final class ArrayNode extends NodeSkeleton {
        private final List<?> value;
        private final NodeBuilder nodeBuilder;

        ArrayNode(List<?> value, NodeBuilder nodeBuilder) {
            this.value = value;
            this.nodeBuilder = nodeBuilder;
        }

        public NodeType getNodeType() {
            return NodeType.ARRAY;
        }

        @Override
        public Node element(int index) {
            try {
                return newNode(value.get(index));
            } catch (IndexOutOfBoundsException e) {
                return MISSING_NODE;
            }
        }

        @Override
        public Iterator<Node> arrayElements() {
            final Iterator<?> iterator = value.iterator();
            return new Iterator<Node>() {
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                public Node next() {
                    return newNode(iterator.next());
                }

                public void remove() {
                    iterator.remove();
                }
            };
        }

        private Node newNode(Object object) {
            return nodeBuilder.newNode(object);
        }

        @Override
        public int size() {
            return value.size();
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append('[');
            Iterator<?> iterator = arrayElements();
            while (iterator.hasNext()) {
                Object value = iterator.next();
                builder.append(value.toString());
                if (iterator.hasNext()) {
                    builder.append(',');
                }
            }
            builder.append(']');
            return builder.toString();
        }
    }

    static final class ObjectNode extends NodeSkeleton implements Iterable<Node.KeyValue> {
        private final Map<String, Object> jsonObject;
        private final NodeBuilder nodeBuilder;

        ObjectNode(Map<String, Object> jsonObject, NodeBuilder nodeBuilder) {
            this.jsonObject = jsonObject;
            this.nodeBuilder = nodeBuilder;
        }

        public Node element(int index) {
            return nodeBuilder.newNode(null);
        }

        public Iterator<KeyValue> fields() {
            final Iterator<String> iterator = jsonObject.keySet().iterator();
            return new Iterator<KeyValue>() {
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                public void remove() {
                    iterator.remove();
                }

                public KeyValue next() {
                    String fieldName = iterator.next();
                    return new KeyValue(fieldName, newNode(fieldName));
                }
            };
        }

        public Iterator<KeyValue> iterator() {
            return fields();
        }

        public Node get(String key) {
            if (jsonObject.containsKey(key)) {
                return newNode(key);
            } else {
                return MISSING_NODE;
            }
        }

        private Node newNode(String fieldName) {
            return nodeBuilder.newNode(jsonObject.get(fieldName));
        }

        public NodeType getNodeType() {
            return NodeType.OBJECT;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("{");
            Iterator<Node.KeyValue> entries = this.iterator();
            while (entries.hasNext()) {
                Node.KeyValue entry = entries.next();
                builder
                    .append('"').append(entry.getKey()).append('"')
                    .append(":")
                    .append(entry.getValue());
                if (entries.hasNext()) {
                    builder.append(",");
                }
            }
            builder.append("}");
            return builder.toString();
        }
    }
}
