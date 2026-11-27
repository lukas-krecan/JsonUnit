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

import static jdk.incubator.json.Json.parse;
import static net.javacrumbs.jsonunit.core.internal.Utils.closeQuietly;
import static net.javacrumbs.jsonunit.core.internal.Utils.readAsString;

import java.io.Reader;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import jdk.incubator.json.JsonArray;
import jdk.incubator.json.JsonBoolean;
import jdk.incubator.json.JsonNull;
import jdk.incubator.json.JsonNumber;
import jdk.incubator.json.JsonObject;
import jdk.incubator.json.JsonString;
import jdk.incubator.json.JsonValue;
import net.javacrumbs.jsonunit.core.internal.GenericNodeBuilder.ArrayNode;
import net.javacrumbs.jsonunit.core.internal.GenericNodeBuilder.ObjectNode;
import org.jspecify.annotations.Nullable;

/**
 * Deserializes nodes using the JEP-540 Simple JSON API available in JDK 28.
 */
class SimpleJsonApiIncubatorNodeFactory extends AbstractNodeFactory {

    @Override
    protected Node doConvertValue(Object source) {
        if (source instanceof JsonValue jsonValue) {
            return newNode(jsonValue);
        } else {
            return wrapObject(source);
        }
    }

    @SuppressWarnings("unchecked")
    private static Node wrapObject(@Nullable Object source) {
        return switch (source) {
            case JsonValue jsonValue -> newNode(jsonValue);
            case Map<?, ?> map ->
                new ObjectNode((Map<String, Object>) map, SimpleJsonApiIncubatorNodeFactory::wrapObject);
            case List<?> list -> new ArrayNode(list, SimpleJsonApiIncubatorNodeFactory::wrapObject);
            case Object[] array -> new ArrayNode(Arrays.asList(array), SimpleJsonApiIncubatorNodeFactory::wrapObject);
            case null, default -> GenericNodeBuilder.wrapDeserializedObject(source);
        };
    }

    @Override
    protected Node nullNode() {
        return newNode(JsonNull.of());
    }

    @Override
    protected Node readValue(Reader value, String label, boolean lenient) {
        try {
            return newNode(parse(readAsString(value)));
        } catch (Exception e) {
            throw newParseException(label, value, e);
        } finally {
            closeQuietly(value);
        }
    }

    private static Node newNode(JsonValue jsonValue) {
        return new SimpleJsonNode(jsonValue);
    }

    @Override
    public boolean isPreferredFor(@Nullable Object source) {
        return source instanceof JsonValue;
    }

    static final class SimpleJsonNode extends AbstractNode {
        private final JsonValue jsonValue;

        SimpleJsonNode(JsonValue jsonValue) {
            this.jsonValue = jsonValue;
        }

        @Override
        public Node element(int index) {
            if (jsonValue instanceof JsonArray jsonArray) {
                if (index >= 0 && index < jsonArray.asList().size()) {
                    return newNode(jsonArray.get(index));
                }
            }
            return MISSING_NODE;
        }

        @Override
        public Iterator<KeyValue> fields() {
            final Iterator<Map.Entry<String, JsonValue>> iterator =
                    jsonValue.asMap().entrySet().iterator();
            return new Iterator<>() {
                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                @Override
                public KeyValue next() {
                    var entry = iterator.next();
                    return new KeyValue(entry.getKey(), newNode(entry.getValue()));
                }
            };
        }

        @Override
        public Node get(String key) {
            if (jsonValue instanceof JsonObject jsonObject) {
                return jsonObject
                        .tryGet(key)
                        .map(SimpleJsonApiIncubatorNodeFactory::newNode)
                        .orElse(MISSING_NODE);
            }
            return MISSING_NODE;
        }

        @Override
        public boolean isMissingNode() {
            return false;
        }

        @Override
        public boolean isNull() {
            return jsonValue instanceof JsonNull;
        }

        @Override
        public boolean isObject() {
            return jsonValue instanceof JsonObject;
        }

        @Override
        public Iterator<Node> arrayElements() {
            final Iterator<JsonValue> elements = jsonValue.asList().iterator();
            return new Iterator<>() {
                @Override
                public boolean hasNext() {
                    return elements.hasNext();
                }

                @Override
                public Node next() {
                    return newNode(elements.next());
                }
            };
        }

        @Override
        public int size() {
            return jsonValue.asList().size();
        }

        @Override
        public String asText() {
            return ((JsonString) jsonValue).asString();
        }

        @Override
        public NodeType getNodeType() {
            // TODO: Rewrite to ifs
            return switch (jsonValue) {
                case JsonObject ignored -> NodeType.OBJECT;
                case JsonArray ignored -> NodeType.ARRAY;
                case JsonBoolean ignored1 -> NodeType.BOOLEAN;
                case JsonString ignored -> NodeType.STRING;
                case JsonNumber ignored -> NodeType.NUMBER;
                case JsonNull ignored -> NodeType.NULL;
            };
        }

        @Override
        public BigDecimal decimalValue() {
            return new BigDecimal(jsonValue.toString());
        }

        @Override
        public boolean isIntegralNumber() {
            String string = jsonValue.toString();
            return decimalValue().scale() == 0 && !string.contains("e") && !string.contains("E");
        }

        @Override
        public Boolean asBoolean() {
            return jsonValue.asBoolean();
        }

        @Override
        public String toString() {
            return jsonValue.toString();
        }
    }
}
