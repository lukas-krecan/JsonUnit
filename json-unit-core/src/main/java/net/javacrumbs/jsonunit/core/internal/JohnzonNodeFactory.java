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
import org.jspecify.annotations.Nullable;

class JohnzonNodeFactory extends AbstractNodeFactory {

    private final Mapper mapper = new MapperBuilder().build();

    @Override
    protected Node doConvertValue(Object source) {
        if (source instanceof JsonValue jsonValue) {
            return newNode(jsonValue);
        } else if (source instanceof int[] array) {
            // Johnzon can't convert arrays but it support lists
            return newNodeFrom(toIntList(array));
        } else if (source instanceof double[] array) {
            // Johnzon can't convert arrays but it support lists
            return newNodeFrom(toDoubleList(array));
        } else if (source instanceof boolean[] array) {
            // Johnzon can't convert arrays but it support lists
            return newNodeFrom(toBoolList(array));
        } else if (source instanceof Object[] array) {
            // Johnzon can't convert arrays but it support lists
            return newNodeFrom(asList(array));
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
    public boolean isPreferredFor(@Nullable Object source) {
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
            if (jsonNode instanceof JsonObject jsonObject) {
                final Iterator<Map.Entry<String, JsonValue>> iterator =
                        jsonObject.entrySet().iterator();
                return new Iterator<>() {
                    @Override
                    public boolean hasNext() {
                        return iterator.hasNext();
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
            if (isObject()) {
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
        public boolean isObject() {
            return jsonNode instanceof JsonObject;
        }

        @Override
        public Iterator<Node> arrayElements() {
            if (jsonNode instanceof JsonArray jsonArray) {
                final Iterator<JsonValue> iterator = jsonArray.iterator();
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
            throw new IllegalStateException("Can call arrayElements() only on an JsonArray");
        }

        @Override
        public int size() {
            if (jsonNode instanceof JsonArray jsonArray) {
                return jsonArray.size();
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
