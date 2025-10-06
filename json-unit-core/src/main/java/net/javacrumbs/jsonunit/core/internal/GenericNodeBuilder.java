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

import static net.javacrumbs.jsonunit.core.internal.ArrayUtils.toBoolList;
import static net.javacrumbs.jsonunit.core.internal.ArrayUtils.toDoubleList;
import static net.javacrumbs.jsonunit.core.internal.ArrayUtils.toIntList;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

class GenericNodeBuilder implements NodeBuilder {
    private static final GenericNodeBuilder INSTANCE = new GenericNodeBuilder();

    static Node wrapDeserializedObject(@Nullable Object object) {
        return INSTANCE.newNode(object);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Node newNode(@Nullable Object object) {
        if (object == null) {
            return new NullNode();
        } else if (object instanceof Map) {
            return new ObjectNode((Map<String, Object>) object, this);
        } else if (object instanceof Number number) {
            return new NumberNode(number);
        } else if (object instanceof String string) {
            return new StringNode(string);
        } else if (object instanceof Boolean bool) {
            return new BooleanNode(bool);
        } else if (object instanceof Object[] objects) {
            return new ArrayNode(Arrays.asList(objects), this);
        } else if (object instanceof int[] ints) {
            return new ArrayNode(toIntList(ints), this);
        } else if (object instanceof double[] doubles) {
            return new ArrayNode(toDoubleList(doubles), this);
        } else if (object instanceof boolean[] booleans) {
            return new ArrayNode(toBoolList(booleans), this);
        } else if (object instanceof List) {
            return new ArrayNode((List<?>) object, this);
        } else if (object instanceof Node node) {
            return node;
        } else {
            throw new IllegalArgumentException("Unsupported type " + object.getClass());
        }
    }

    abstract static class NodeSkeleton extends AbstractNode {
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
        public boolean isObject() {
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

    static final class ObjectNode extends NodeSkeleton {
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
                public KeyValue next() {
                    String fieldName = iterator.next();
                    return new KeyValue(fieldName, newNode(fieldName));
                }
            };
        }

        @Override
        public boolean isObject() {
            return true;
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
            Iterator<Node.KeyValue> entries = this.fields();
            while (entries.hasNext()) {
                Node.KeyValue entry = entries.next();
                builder.append('"')
                        .append(entry.getKey())
                        .append('"')
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
