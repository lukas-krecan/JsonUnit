/**
 * Copyright 2009-2019 the original author or authors.
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

import static java.util.Arrays.asList;
import static net.javacrumbs.jsonunit.core.internal.ArrayUtils.toBoolList;
import static net.javacrumbs.jsonunit.core.internal.ArrayUtils.toDoubleList;
import static net.javacrumbs.jsonunit.core.internal.ArrayUtils.toIntList;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.json.stream.JsonParsingException;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Map;
import org.apache.johnzon.core.JsonLongImpl;
import org.apache.johnzon.mapper.Mapper;
import org.apache.johnzon.mapper.MapperBuilder;

class JohnzonNodeFactory extends AbstractNodeFactory {

    private final Mapper mapper = new MapperBuilder().build();

    @Override
    protected Node doConvertValue(Object source) {
        if (source instanceof JsonValue) {
            return newNode((JsonValue) source);
        } else if (source instanceof int[]) {
            // Johnzon can't convert arrays but it support lists
            return newNodeFrom(toIntList((int[]) source));
        } else if (source instanceof double[]) {
            // Johnzon can't convert arrays but it support lists
            return newNodeFrom(toDoubleList((double[]) source));
        } else if (source instanceof boolean[]) {
            // Johnzon can't convert arrays but it support lists
            return newNodeFrom(toBoolList((boolean[]) source));
        } else if (source instanceof Object[]) {
            // Johnzon can't convert arrays but it support lists
            return newNodeFrom(asList((Object[]) source));
        } else {
            return newNodeFrom(source);
        }
    }

    private Node newNodeFrom(Object source) {
        return newNode(mapper.toStructure(source));
    }

    @Override
    protected Node nullNode() {
        return newNode(JsonValue.NULL);
    }

    @Override
    protected Node readValue(Reader reader, String label, boolean lenient) {
        try (JsonReader parser = Json.createReader(reader)) {
            try {
                return newNode(parser.readValue());
            } catch (JsonParsingException e) {
                throw newParseException(label, reader, e);
            }
        }
    }

    @Override
    public boolean isPreferredFor(Object source) {
        return source instanceof JsonValue;
    }

    private static Node newNode(JsonValue value) {
        return new JavaxJsonNode(value);
    }

    static final class JavaxJsonNode extends AbstractNode {
        private final JsonValue jsonNode;

        JavaxJsonNode(JsonValue jsonNode) {
            this.jsonNode = jsonNode;
        }

        @Override
        public Node element(int index) {
            if (jsonNode instanceof JsonArray) {
                try {
                    return newNode(((JsonArray) jsonNode).get(index));
                } catch (IndexOutOfBoundsException e) {
                    return MISSING_NODE;
                }
            } else {
                return MISSING_NODE;
            }
        }

        @Override
        public Iterator<KeyValue> fields() {
            if (jsonNode instanceof JsonObject) {
                final Iterator<Map.Entry<String, JsonValue>> iterator =
                        ((JsonObject) jsonNode).entrySet().iterator();
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
                        Map.Entry<String, JsonValue> entry = iterator.next();
                        return new KeyValue(entry.getKey(), newNode(entry.getValue()));
                    }
                };
            }
            throw new IllegalStateException("Can call fields() only on an JsonObject");
        }

        @Override
        public Node get(String key) {
            if (jsonNode instanceof JsonObject) {
                JsonObject jsonObject = (JsonObject) this.jsonNode;
                if (jsonObject.containsKey(key)) {
                    return newNode(jsonObject.get(key));
                } else {
                    return Node.MISSING_NODE;
                }
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
            return jsonNode.equals(JsonValue.NULL);
        }

        @Override
        public Iterator<Node> arrayElements() {
            if (jsonNode instanceof JsonArray) {
                final Iterator<JsonValue> iterator = ((JsonArray) jsonNode).iterator();
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
                    public Node next() {
                        return newNode(iterator.next());
                    }
                };
            }
            throw new IllegalStateException("Can call arrayElements() only on an JsonArray");
        }

        @Override
        public int size() {
            if (jsonNode instanceof JsonArray) {
                return ((JsonArray) jsonNode).size();
            }
            throw new IllegalStateException("Can call size() only on an JsonArray");
        }

        @Override
        public NodeType getNodeType() {
            return switch (jsonNode.getValueType()) {
                case OBJECT -> NodeType.OBJECT;
                case ARRAY -> NodeType.ARRAY;
                case STRING -> NodeType.STRING;
                case NUMBER -> NodeType.NUMBER;
                case TRUE, FALSE -> NodeType.BOOLEAN;
                case NULL -> NodeType.NULL;
            };
        }

        @Override
        public String asText() {
            if (jsonNode.getValueType() == JsonValue.ValueType.STRING) return ((JsonString) jsonNode).getString();
            else throw new IllegalStateException("Not a JsonString: " + jsonNode);
        }

        @Override
        public BigDecimal decimalValue() {
            if (isNumber()) {
                return ((JsonNumber) jsonNode).bigDecimalValue();
            } else {
                throw new IllegalStateException("Not a JsonNumber: " + jsonNode);
            }
        }

        @Override
        public boolean isIntegralNumber() {
            return jsonNode instanceof JsonLongImpl;
        }

        private boolean isNumber() {
            return jsonNode.getValueType() == JsonValue.ValueType.NUMBER;
        }

        @Override
        public Boolean asBoolean() {
            if (jsonNode.getValueType() == JsonValue.ValueType.TRUE) return true;
            else if (jsonNode.getValueType() == JsonValue.ValueType.FALSE) return false;
            else throw new IllegalStateException("Not a JsonBoolean: " + jsonNode);
        }

        @Override
        public String toString() {
            return jsonNode.toString();
        }
    }
}
