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

import static net.javacrumbs.jsonunit.core.internal.Utils.closeQuietly;

import java.io.Reader;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import net.javacrumbs.jsonunit.core.internal.GenericNodeBuilder.NodeSkeleton;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.jspecify.annotations.Nullable;

/**
 * Deserializes node using org.json.JSONObject
 */
class JsonOrgNodeFactory extends AbstractNodeFactory {

    @Override
    protected Node doConvertValue(Object source) {
        return newNode(source);
    }

    @Override
    protected Node nullNode() {
        return newNode(null);
    }

    @Override
    protected Node readValue(Reader value, String label, boolean lenient) {
        try {
            return newNode(new JSONTokener(value).nextValue());
        } catch (JSONException e) {
            throw newParseException(label, value, e);
        } finally {
            closeQuietly(value);
        }
    }

    private static Node newNode(@Nullable Object object) {
        if (object instanceof JSONObject jSONObject) {
            return new JSONObjectNode(jSONObject);
        } else if (object instanceof Number number) {
            return new GenericNodeBuilder.NumberNode(number);
        } else if (object instanceof String string) {
            return new GenericNodeBuilder.StringNode(string);
        } else if (object instanceof Boolean b) {
            return new GenericNodeBuilder.BooleanNode(b);
        } else if (object instanceof JSONArray jSONArray) {
            return new JSONArrayNode(jSONArray);
        } else if (object == null || JSONObject.NULL.equals(object)) {
            return new GenericNodeBuilder.NullNode();
        } else if (object instanceof Map) {
            return new JSONObjectNode(new JSONObject((Map<?, ?>) object));
        } else if (object instanceof Collection || object.getClass().isArray()) {
            return new JSONArrayNode((JSONArray) JSONObject.wrap(object));
        } else {
            throw new IllegalArgumentException("Unsupported type " + object.getClass());
        }
    }

    @Override
    public boolean isPreferredFor(@Nullable Object source) {
        return source instanceof JSONObject || source instanceof JSONArray;
    }

    private static final class JSONArrayNode extends NodeSkeleton {
        private final JSONArray value;

        private JSONArrayNode(JSONArray value) {
            this.value = value;
        }

        @Override
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

        @Override
        public int size() {
            return value.length();
        }

        @Override
        public String toString() {
            return value.toString();
        }
    }

    private static final class JSONObjectNode extends NodeSkeleton {
        private final JSONObject jsonObject;

        private JSONObjectNode(JSONObject jsonObject) {
            this.jsonObject = jsonObject;
        }

        @Override
        public Node element(int index) {
            return newNode(null);
        }

        @Override
        public Iterator<KeyValue> fields() {
            final Iterator<String> iterator = jsonObject.keys();
            return new Iterator<>() {
                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                @Override
                public KeyValue next() {
                    String fieldName = iterator.next();
                    return new KeyValue(fieldName, newNode(jsonObject.get(fieldName)));
                }
            };
        }

        @Override
        public Node get(String key) {
            Object value = jsonObject.opt(key);
            if (value != null) {
                return newNode(value);
            } else {
                return MISSING_NODE;
            }
        }

        @Override
        public NodeType getNodeType() {
            return NodeType.OBJECT;
        }

        @Override
        public String toString() {
            return jsonObject.toString();
        }
    }
}
