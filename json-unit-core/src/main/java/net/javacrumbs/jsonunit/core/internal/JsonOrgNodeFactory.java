/**
 * Copyright 2009-2017 the original author or authors.
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.Reader;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import static net.javacrumbs.jsonunit.core.internal.Utils.closeQuietly;

/**
 * Deserializes node using org.json.JSONObject
 */
class JsonOrgNodeFactory extends AbstractNodeFactory {

    @Override
    protected Node convertValue(Object source) {
        return newNode(source);
    }

    @Override
    protected Node nullNode() {
        return newNode(null);
    }

    protected Node readValue(Reader value, String label, boolean lenient) {
        try {
            return newNode(new JSONTokener(value).nextValue());
        } catch (JSONException e) {
            throw new IllegalArgumentException("Can not parse " + label + " value.", e);
        } finally {
            closeQuietly(value);
        }
    }

    private static Node newNode(Object object) {
        if (object instanceof JSONObject) {
            return new JSONObjectNode((JSONObject) object);
        } else if (object instanceof Number) {
            return new NumberNode((Number) object);
        } else if (object instanceof String) {
            return new StringNode((String) object);
        } else if (object instanceof Boolean) {
            return new BooleanNode((Boolean) object);
        } else if (object instanceof JSONArray) {
            return new JSONArrayNode((JSONArray) object);
        } else if (JSONObject.NULL.equals(object)) {
            return new NullNode();
        } else if (object instanceof Map) {
            return new JSONObjectNode(new JSONObject((Map<?, ?>) object));
        } else if (object instanceof Collection || object.getClass().isArray()) {
            return new JSONArrayNode((JSONArray) JSONObject.wrap(object));
        } else {
            throw new IllegalArgumentException("Unsupported type " + object.getClass());
        }
    }

    public boolean isPreferredFor(Object source) {
        return source instanceof JSONObject || source instanceof JSONArray;
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
            return new BigDecimal(value.toString());
        }

        @Override
        public String toString() {
            return value.toString();
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

    private static final class JSONArrayNode extends NodeSkeleton {
        private final JSONArray value;

        private JSONArrayNode(JSONArray value) {
            this.value = value;
        }

        public NodeType getNodeType() {
            return NodeType.ARRAY;
        }

        @Override
        public Node element(int index) {
            try {
                return newNode(value.get(index));
            } catch (JSONException e) {
                return MISSING_NODE;
            }
        }

        @Override
        public Iterator<Node> arrayElements() {
            final Iterator<Object> iterator = value.iterator();
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
            return value.length();
        }
    }

    private static final class JSONObjectNode extends NodeSkeleton {
        private final JSONObject jsonObject;

        private JSONObjectNode(JSONObject jsonObject) {
            this.jsonObject = jsonObject;
        }

        public Node element(int index) {
            return newNode(null);
        }

        public Iterator<KeyValue> fields() {
            final Iterator<String> iterator = jsonObject.keys();
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

        public Node get(String key) {
            Object value = jsonObject.opt(key);
            if (value != null) {
                return newNode(value);
            } else {
                return MISSING_NODE;
            }
        }

        public NodeType getNodeType() {
            return NodeType.OBJECT;
        }

        @Override
        public String toString() {
            return jsonObject.toString();
        }
    }

    private static abstract class NodeSkeleton extends AbstractNode {
        public Node element(int index) {
            throw new UnsupportedOperationException();
        }

        public Iterator<Node.KeyValue> fields() {
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
