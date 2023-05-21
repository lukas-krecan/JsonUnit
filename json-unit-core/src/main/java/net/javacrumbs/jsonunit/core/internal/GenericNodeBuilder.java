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

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static net.javacrumbs.jsonunit.core.internal.ArrayUtils.toBoolList;
import static net.javacrumbs.jsonunit.core.internal.ArrayUtils.toDoubleList;
import static net.javacrumbs.jsonunit.core.internal.ArrayUtils.toIntList;

class GenericNodeBuilder implements NodeBuilder {
    private static final GenericNodeBuilder INSTANCE = new GenericNodeBuilder();

    static Node wrapDeserializedObject(Object object) {
        return INSTANCE.newNode(object);
    }

    @Override
    @SuppressWarnings("unchecked")
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
            return new ArrayNode(toIntList((int[]) object), this);
        } else if (object instanceof double[]) {
            return new ArrayNode(toDoubleList((double[]) object), this);
        } else if (object instanceof boolean[]) {
            return new ArrayNode(toBoolList((boolean[]) object), this);
        } else if (object instanceof List) {
            return new ArrayNode((List<?>) object, this);
        } else if (object instanceof Node) {
            return (Node) object;
        } else {
            throw new IllegalArgumentException("Unsupported type " + object.getClass());
        }
    }

    static abstract class NodeSkeleton extends AbstractNode {
        @Override
        public Node element(int index) {
            return MISSING_NODE;
        }

        @Override
        public Iterator<KeyValue> fields() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Node get(String key) {
            return MISSING_NODE;
        }

        @Override
        public boolean isMissingNode() {
            return false;
        }

        @Override
        public boolean isNull() {
            return false;
        }

        @Override
        public Iterator<Node> arrayElements() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int size() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String asText() {
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
    }

    static class NullNode extends NodeSkeleton {
        @Override
        public boolean isNull() {
            return true;
        }

        @Override
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

        @Override
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

        @Override
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

        @Override
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

        @Override
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
            return new Iterator<>() {
                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                @Override
                public Node next() {
                    return newNode(iterator.next());
                }

                @Override
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

        @Override
        public Node element(int index) {
            return nodeBuilder.newNode(null);
        }

        @Override
        public Iterator<KeyValue> fields() {
            final Iterator<String> iterator = jsonObject.keySet().iterator();
            return new Iterator<>() {
                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                @Override
                public void remove() {
                    iterator.remove();
                }

                @Override
                public KeyValue next() {
                    String fieldName = iterator.next();
                    return new KeyValue(fieldName, newNode(fieldName));
                }
            };
        }

        @Override
        public @NotNull Iterator<KeyValue> iterator() {
            return fields();
        }

        @Override
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

        @Override
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
