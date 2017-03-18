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

import static net.javacrumbs.jsonunit.core.internal.Utils.closeQuietly;

/**
 * Deserializes node using Gson
 */
class GsonNodeFactory extends AbstractNodeFactory {
    private final Gson gson = new Gson();

    @Override
    protected Node convertValue(Object source) {
        if (source instanceof JsonElement) {
            return newNode((JsonElement) source);
        } else {
            return newNode(gson.toJsonTree(source));
        }
    }

    @Override
    protected Node nullNode() {
        return newNode(JsonNull.INSTANCE);
    }

    protected Node readValue(Reader value, String label, boolean lenient) {
        // GSON is always lenient :-(
        try {
            return newNode(new JsonParser().parse(value));
        } catch (JsonIOException e) {
            throw new IllegalArgumentException(e);
        } catch (JsonSyntaxException e) {
            throw new IllegalArgumentException(e);
        } finally {
            closeQuietly(value);
        }
    }

    private static Node newNode(JsonElement jsonNode) {
        if (jsonNode != null) {
            return new GsonNode(jsonNode);
        } else {
            return Node.MISSING_NODE;
        }
    }

    public boolean isPreferredFor(Object source) {
        return source instanceof JsonElement;
    }

    static final class GsonNode extends AbstractNode {
        private final JsonElement jsonNode;

        public GsonNode(JsonElement jsonNode) {
            this.jsonNode = jsonNode;
        }

        public Node element(int index) {
            if (jsonNode instanceof JsonArray) {
                try {
                    return newNode(((JsonArray) jsonNode).get(index));
                } catch (IndexOutOfBoundsException e) {
                    return MISSING_NODE;
                }
            }
            throw new IllegalStateException("Can call element() only on an JsonArray");
        }


        public Iterator<KeyValue> fields() {
            if (jsonNode instanceof JsonObject) {
                final Iterator<Map.Entry<String, JsonElement>> iterator = ((JsonObject) jsonNode).entrySet().iterator();
                return new Iterator<KeyValue>() {
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }

                    public void remove() {
                        iterator.remove();
                    }

                    public KeyValue next() {
                        Map.Entry<String, JsonElement> entry = iterator.next();
                        return new KeyValue(entry.getKey(), newNode(entry.getValue()));
                    }
                };
            }
            throw new IllegalStateException("Can call fields() only on an JsonObject");
        }

        public Node get(String key) {
            if (jsonNode instanceof JsonObject) {
                return newNode(((JsonObject) jsonNode).get(key));
            } else {
                return Node.MISSING_NODE;
            }
        }

        public boolean isMissingNode() {
            return false;
        }

        public boolean isNull() {
            return jsonNode.isJsonNull();
        }

        public Iterator<Node> arrayElements() {
            if (jsonNode instanceof JsonArray) {
                final Iterator<JsonElement> iterator = ((JsonArray) jsonNode).iterator();
                return new Iterator<Node>() {
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }

                    public void remove() {
                        iterator.remove();
                    }

                    public Node next() {
                        JsonElement entry = iterator.next();
                        return newNode(entry);
                    }
                };
            }
            throw new IllegalStateException("Can call arrayElements() only on an JsonArray");
        }

        public int size() {
            if (jsonNode instanceof JsonArray) {
               return ((JsonArray) jsonNode).size();
            }
            throw new IllegalStateException("Can call arrayElements() only on an JsonArray");
        }

        public String asText() {
            return jsonNode.getAsString();
        }

        public NodeType getNodeType() {
            if (jsonNode.isJsonObject()) {
                return NodeType.OBJECT;
            } else if (jsonNode.isJsonArray()) {
                return NodeType.ARRAY;
            } else if (jsonNode instanceof JsonPrimitive && ((JsonPrimitive) jsonNode).isString()) {
                return NodeType.STRING;
            } else if (jsonNode instanceof JsonPrimitive && ((JsonPrimitive) jsonNode).isNumber()) {
                return NodeType.NUMBER;
            } else if (jsonNode instanceof JsonPrimitive && ((JsonPrimitive) jsonNode).isBoolean()) {
                return NodeType.BOOLEAN;
            } else if (jsonNode.isJsonNull()) {
                return NodeType.NULL;
            } else {
                throw new IllegalStateException("Unexpected node type " + jsonNode);
            }
        }

        public BigDecimal decimalValue() {
            return jsonNode.getAsBigDecimal();
        }

        public Boolean asBoolean() {
            return jsonNode.getAsBoolean();
        }

        @Override
        public String toString() {
            return jsonNode.toString();
        }
    }
}
