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

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * Deserializes node using Gson
 */
class GsonNodeFactory extends AbstractNodeFactory {
    private final Gson gson = new Gson();

    @Override
    protected Node doConvertValue(Object source) {
        if (source instanceof JsonElement jsonElement) {
            return newNode(jsonElement);
        } else {
            return newNode(gson.toJsonTree(source));
        }
    }

    @Override
    protected Node nullNode() {
        return newNode(JsonNull.INSTANCE);
    }

    @Override
    protected Node readValue(Reader value, String label, boolean lenient) {
        // GSON is always lenient :-(
        try {
            return newNode(JsonParser.parseReader(value));
        } catch (JsonIOException | JsonSyntaxException e) {
            throw newParseException(label, value, e);
        } finally {
            closeQuietly(value);
        }
    }

    private static Node newNode(@Nullable JsonElement jsonNode) {
        if (jsonNode != null) {
            return new GsonNode(jsonNode);
        } else {
            return Node.MISSING_NODE;
        }
    }

    @Override
    public boolean isPreferredFor(@Nullable Object source) {
        return source instanceof JsonElement;
    }

    static final class GsonNode extends AbstractNode {
        private final JsonElement jsonNode;

        GsonNode(JsonElement jsonNode) {
            this.jsonNode = jsonNode;
        }

        @Override
        public Node element(int index) {
            if (jsonNode instanceof JsonArray jsonArray) {
                try {
                    return newNode(jsonArray.get(index));
                } catch (IndexOutOfBoundsException e) {
                    return MISSING_NODE;
                }
            } else {
                return MISSING_NODE;
            }
        }

        @Override
        public Iterator<KeyValue> fields() {
            if (isObject()) {
                final Iterator<Map.Entry<String, JsonElement>> iterator =
                        ((JsonObject) jsonNode).entrySet().iterator();
                return new Iterator<>() {
                    @Override
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }

                    @Override
                    public KeyValue next() {
                        Map.Entry<String, JsonElement> entry = iterator.next();
                        return new KeyValue(entry.getKey(), newNode(entry.getValue()));
                    }
                };
            }
            throw new IllegalStateException("Can call fields() only on an JsonObject");
        }

        @Override
        public Node get(String key) {
            if (jsonNode instanceof JsonObject jsonObject) {
                return newNode(jsonObject.get(key));
            } else {
                return Node.MISSING_NODE;
            }
        }

        @Override
        public boolean isMissingNode() {
            return false;
        }

        @Override
        public boolean isNull() {
            return jsonNode.isJsonNull();
        }

        @Override
        public boolean isObject() {
            return jsonNode instanceof JsonObject;
        }

        @Override
        public Iterator<Node> arrayElements() {
            if (jsonNode instanceof JsonArray jsonArray) {
                final Iterator<JsonElement> iterator = jsonArray.iterator();
                return new Iterator<>() {
                    @Override
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }

                    @Override
                    public Node next() {
                        JsonElement entry = iterator.next();
                        return newNode(entry);
                    }
                };
            }
            throw new IllegalStateException("Can call arrayElements() only on an JsonArray");
        }

        @Override
        public int size() {
            if (jsonNode instanceof JsonArray jsonArray) {
                return jsonArray.size();
            }
            throw new IllegalStateException("Can call arrayElements() only on an JsonArray");
        }

        @Override
        public String asText() {
            return jsonNode.getAsString();
        }

        @Override
        public NodeType getNodeType() {
            if (jsonNode.isJsonObject()) {
                return NodeType.OBJECT;
            } else if (jsonNode.isJsonArray()) {
                return NodeType.ARRAY;
            } else if (jsonNode instanceof JsonPrimitive jsonPrimitive && jsonPrimitive.isString()) {
                return NodeType.STRING;
            } else if (jsonNode instanceof JsonPrimitive jsonPrimitive && jsonPrimitive.isNumber()) {
                return NodeType.NUMBER;
            } else if (jsonNode instanceof JsonPrimitive jsonPrimitive && jsonPrimitive.isBoolean()) {
                return NodeType.BOOLEAN;
            } else if (jsonNode.isJsonNull()) {
                return NodeType.NULL;
            } else {
                throw new IllegalStateException("Unexpected node type " + jsonNode);
            }
        }

        @Override
        public BigDecimal decimalValue() {
            return jsonNode.getAsBigDecimal();
        }

        @Override
        public boolean isIntegralNumber() {
            String string = jsonNode.getAsString();
            return jsonNode.getAsBigDecimal().scale() == 0 && !string.contains("e") && !string.contains("E");
        }

        @Override
        public Boolean asBoolean() {
            return jsonNode.getAsBoolean();
        }

        @Override
        public String toString() {
            return jsonNode.toString();
        }
    }
}
