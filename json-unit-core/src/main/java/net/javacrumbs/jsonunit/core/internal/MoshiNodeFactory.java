/**
 * Copyright 2009-2017 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.javacrumbs.jsonunit.core.internal;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static net.javacrumbs.jsonunit.core.internal.Utils.closeQuietly;

/**
 * Deserializes node using Moshi
 */
class MoshiNodeFactory extends AbstractNodeFactory {
    private static final Moshi moshi = new Moshi.Builder().build();

    @Override
    protected Node convertValue(Object source) {
        return newNode(source);
    }

    @Override
    protected Node nullNode() {
        return newNode(null);
    }

    @Override
    protected Node readValue(String source, String label, boolean lenient) {
        try {
            JsonAdapter<Object> adapter = moshi.adapter(Object.class);
            if (lenient) {
                adapter = adapter.lenient();
            }
            return newNode(adapter.fromJson(source));
        } catch (IOException e) {
            throw new IllegalArgumentException("Can not parse " + label + " value.", e);
        }
    }

    protected Node readValue(Reader value, String label, boolean lenient) {
        try {
            return readValue(Utils.readAsString(value), label, lenient);
        } catch (IOException e) {
            throw new IllegalArgumentException("Can not parse " + label + " value.", e);
        } finally {
            closeQuietly(value);
        }
    }


    private static Node newNode(Object object) {
        if (object == null) {
            return new NullNode();
        } else if (object instanceof Map) {
            return new ObjectNode((Map<String, Object>) object);
        } else if (object instanceof Number) {
            return new NumberNode((Number) object);
        } else if (object instanceof String) {
            return new StringNode((String) object);
        } else if (object instanceof Boolean) {
            return new BooleanNode((Boolean) object);
        } else if (object instanceof Object[]) {
            return new ArrayNode(Arrays.asList((Object[]) object));
        } else if (object instanceof int[]) {
            int[] array = (int[]) object;
            List<Integer> list = new ArrayList<Integer>(array.length);
            for (int i : array) {
                list.add(i);
            }
            return new ArrayNode(list);
        } else if (object instanceof double[]) {
            double[] array = (double[]) object;
            List<Double> list = new ArrayList<Double>(array.length);
            for (double i : array) {
                list.add(i);
            }
            return new ArrayNode(list);
        } else if (object instanceof Collection) {
            return new ArrayNode((List<?>) object);
        } else {
            throw new IllegalArgumentException("Unsupported type " + object.getClass());
        }
    }

    public boolean isPreferredFor(Object source) {
        return false;
    }


    private static final class NullNode extends NodeSkeleton {
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


    private static final class NumberNode extends NodeSkeleton {
        private final Number value;

        private NumberNode(Number value) {
            this.value = value;
        }

        public NodeType getNodeType() {
            return NodeType.NUMBER;
        }

        @Override
        public BigDecimal decimalValue() {
            // Workaround for Moshi bug https://github.com/square/moshi/issues/192
            return new BigDecimal(value.toString()).stripTrailingZeros();
        }

        @Override
        public String toString() {
            return decimalValue().toString();
        }
    }

    private static final class StringNode extends NodeSkeleton {
        private final String value;

        private StringNode(String value) {
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

    private static final class BooleanNode extends NodeSkeleton {
        private final Boolean value;

        private BooleanNode(Boolean value) {
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

    private static final class ArrayNode extends NodeSkeleton {
        private final List<?> value;

        private ArrayNode(List<?> value) {
            this.value = value;
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

        @Override
        public int size() {
            return value.size();
        }

        @Override
        public String toString() {
            return moshi.adapter(Object.class).toJson(value);
        }
    }

    private static final class ObjectNode extends NodeSkeleton implements Iterable<Node.KeyValue> {
        private final Map<String, Object> jsonObject;

        private ObjectNode(Map<String, Object> jsonObject) {
            this.jsonObject = jsonObject;
        }

        public Node element(int index) {
            return newNode(null);
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
                    return new KeyValue(fieldName, newNode(jsonObject.get(fieldName)));
                }
            };
        }

        public Iterator<KeyValue> iterator() {
            return fields();
        }

        public Node get(String key) {
            if (jsonObject.containsKey(key)) {
                return newNode(jsonObject.get(key));
            } else {
                return MISSING_NODE;
            }
        }

        public NodeType getNodeType() {
            return NodeType.OBJECT;
        }

        @Override
        public String toString() {
            // custom serialization to be able to serialize ints as ints https://github.com/square/moshi/issues/192
            StringBuilder builder = new StringBuilder();
            builder.append('{');
            for (KeyValue kv: this) {
                builder.append('"').append(kv.getKey()).append("\":").append(kv.getValue());
            }
            builder.append('}');
            return builder.toString();
        }
    }

    private static abstract class NodeSkeleton extends AbstractNode {
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
}
