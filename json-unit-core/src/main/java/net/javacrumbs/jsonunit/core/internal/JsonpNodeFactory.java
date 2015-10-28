/**
 * Copyright 2009-2015 the original author or authors.
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

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import javax.json.stream.JsonParsingException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import static net.javacrumbs.jsonunit.core.internal.Nodes.parsePrimitiveString;
import static net.javacrumbs.jsonunit.core.internal.Utils.readAsString;

/**
 * Deserializes node using JSONP
 */
class JsonpNodeFactory extends AbstractNodeFactory {

    public static final int BUFFER_SIZE = 10 * 1024;

    @Override
    protected Node convertValue(Object source) {
        return Nodes.newNode(source);
    }

    protected Node readValue(Reader value, String label, boolean lenient) {
        Reader reader = value.markSupported() ? value : new BufferedReader(value, BUFFER_SIZE);
        try {
            reader.mark(BUFFER_SIZE);
            try {
                return newNode(createReader(value).read());
            } catch (JsonParsingException e) {
                // JSONP can not parse standalone number, boolean null and string
                reader.reset();
                return parsePrimitiveString(readAsString(value));
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Can not parse " + label + " value.", e);
        }

    }

    protected Node readValue(Reader value, String label) {
        return readValue(value, label, false);
    }

    private JsonReader createReader(Reader value) {
        Map<String, ?> config = Collections.emptyMap();
        return Json.createReaderFactory(config).createReader(value);
    }

    @Override
    protected Node nullNode() {
        return newNode(Json.createObjectBuilder().build());
    }

    private static Node newNode(JsonValue jsonObject) {
        if (jsonObject != null) {
            return new JsonpNode(jsonObject);
        } else {
            return Node.MISSING_NODE;
        }
    }

    public boolean isPreferredFor(Object source) {
        return source instanceof JsonStructure;
    }


    static final class JsonpNode extends AbstractNode {
        private final JsonValue jsonValue;

        JsonpNode(JsonValue jsonValue) {
            this.jsonValue = jsonValue;
        }

        public Node element(int index) {
            return newNode(((JsonArray) jsonValue).getJsonObject(index));
        }

        public Iterator<KeyValue> fields() {

            final Iterator<Map.Entry<String, JsonValue>> iterator = ((JsonObject) jsonValue).entrySet().iterator();
            return new Iterator<KeyValue>() {
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                public void remove() {
                    iterator.remove();
                }

                public KeyValue next() {
                    Map.Entry<String, JsonValue> entry = iterator.next();
                    return new KeyValue(entry.getKey(), newNode(entry.getValue()));
                }
            };
        }

        public Node get(String key) {
            return newNode(((JsonObject) jsonValue).get(key));
        }

        public boolean isMissingNode() {
            return false;
        }

        public boolean isNull() {
            return jsonValue.getValueType() == ValueType.NULL;
        }

        public Iterator<Node> arrayElements() {
            final Iterator<JsonValue> elements = ((JsonArray)jsonValue).iterator();
            return new Iterator<Node>() {
                public boolean hasNext() {
                    return elements.hasNext();
                }

                public Node next() {
                    return newNode(elements.next());
                }

                public void remove() {
                    elements.remove();
                }
            };
        }

        public int size() {
            return ((JsonArray)jsonValue).size();
        }

        public String asText() {
            return ((JsonString)jsonValue).getString();
        }

        public NodeType getNodeType() {
            ValueType valueType = jsonValue.getValueType();
            if (valueType == ValueType.OBJECT) {
                return NodeType.OBJECT;
            } else if (valueType == ValueType.ARRAY) {
                return NodeType.ARRAY;
            } else if (valueType == ValueType.STRING) {
                return NodeType.STRING;
            } else if (valueType == ValueType.NUMBER) {
                return NodeType.NUMBER;
            } else if (valueType == ValueType.TRUE || valueType == ValueType.FALSE) {
                return NodeType.BOOLEAN;
            } else if (valueType == ValueType.NULL) {
                return NodeType.NULL;
            } else {
                throw new IllegalStateException("Unexpected node type " + jsonValue.getValueType());
            }
        }

        public BigDecimal decimalValue() {
            return ((JsonNumber)jsonValue).bigDecimalValue();
        }

        public Boolean asBoolean() {
            ValueType valueType = jsonValue.getValueType();
            if (valueType == ValueType.TRUE) {
                return true;
            } else if (valueType == ValueType.FALSE) {
                return false;
            } else {
                throw new UnsupportedOperationException();
            }
        }

        @Override
        public String toString() {
            return jsonValue.toString();
        }
    }
}
