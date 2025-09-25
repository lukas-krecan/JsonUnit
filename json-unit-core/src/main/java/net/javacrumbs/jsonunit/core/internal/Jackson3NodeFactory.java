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

import static net.javacrumbs.jsonunit.core.internal.Utils.closeQuietly;

import java.io.Reader;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import net.javacrumbs.jsonunit.providers.Jackson3JsonMapperProvider;
import tools.jackson.core.JacksonException;
import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.cfg.JsonNodeFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.NullNode;

/**
 * Deserializes node using Jackson 3
 */
class Jackson3NodeFactory extends AbstractNodeFactory {
    private final ServiceLoader<Jackson3JsonMapperProvider> serviceLoader =
            ServiceLoader.load(Jackson3JsonMapperProvider.class);

    @Override
    protected Node doConvertValue(Object source) {
        if (source instanceof JsonNode) {
            return newNode((JsonNode) source);
        } else {
            return newNode(getMapper(false).convertValue(source, JsonNode.class));
        }
    }

    @Override
    protected Node nullNode() {
        return newNode(NullNode.getInstance());
    }

    @Override
    protected Node readValue(Reader value, String label, boolean lenient) {
        try {
            return newNode(getMapper(lenient).readTree(value));
        } catch (JacksonException e) {
            throw newParseException(label, value, e);
        } finally {
            closeQuietly(value);
        }
    }

    private JsonMapper getMapper(boolean lenient) {
        return getMapperProvider().getJsonMapper(lenient);
    }

    private Jackson3JsonMapperProvider getMapperProvider() {
        synchronized (serviceLoader) {
            Iterator<Jackson3JsonMapperProvider> iterator = serviceLoader.iterator();
            if (iterator.hasNext()) {
                return iterator.next();
            } else {
                return DefaultJsonMapperProvider.INSTANCE;
            }
        }
    }

    private static Node newNode(JsonNode jsonNode) {
        if (jsonNode != null && !jsonNode.isMissingNode()) {
            return new Jackson3Node(jsonNode);
        } else {
            return Node.MISSING_NODE;
        }
    }

    @Override
    public boolean isPreferredFor(Object source) {
        return source instanceof JsonNode;
    }

    static final class Jackson3Node extends AbstractNode {
        private final JsonNode jsonNode;

        Jackson3Node(JsonNode jsonNode) {
            this.jsonNode = jsonNode;
        }

        @Override
        public Node element(int index) {
            return newNode(jsonNode.path(index));
        }

        @Override
        public Iterator<KeyValue> fields() {
            final Iterator<Map.Entry<String, JsonNode>> iterator =
                    jsonNode.properties().iterator();
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
                    Map.Entry<String, JsonNode> entry = iterator.next();
                    return new KeyValue(entry.getKey(), newNode(entry.getValue()));
                }
            };
        }

        @Override
        public Node get(String key) {
            return newNode(jsonNode.get(key));
        }

        @Override
        public boolean isMissingNode() {
            return false;
        }

        @Override
        public boolean isNull() {
            return jsonNode.isNull();
        }

        @Override
        public boolean isObject() {
            return jsonNode.isObject();
        }

        @Override
        public Iterator<Node> arrayElements() {
            final Iterator<JsonNode> elements = jsonNode.values().iterator();
            return new Iterator<>() {
                @Override
                public boolean hasNext() {
                    return elements.hasNext();
                }

                @Override
                public Node next() {
                    return newNode(elements.next());
                }

                @Override
                public void remove() {
                    elements.remove();
                }
            };
        }

        @Override
        public int size() {
            return jsonNode.size();
        }

        @Override
        public String asText() {
            return jsonNode.asString();
        }

        @Override
        public NodeType getNodeType() {
            if (jsonNode.isObject()) {
                return NodeType.OBJECT;
            } else if (jsonNode.isArray()) {
                return NodeType.ARRAY;
            } else if (jsonNode.isString()) {
                return NodeType.STRING;
            } else if (jsonNode.isNumber()) {
                return NodeType.NUMBER;
            } else if (jsonNode.isBoolean()) {
                return NodeType.BOOLEAN;
            } else if (jsonNode.isNull()) {
                return NodeType.NULL;
            } else if (jsonNode.isBinary()) {
                return NodeType.STRING;
            } else {
                throw new IllegalStateException("Unexpected node type " + jsonNode);
            }
        }

        @Override
        public BigDecimal decimalValue() {
            return jsonNode.decimalValue();
        }

        @Override
        public Boolean asBoolean() {
            return jsonNode.asBoolean();
        }

        @Override
        public String toString() {
            return jsonNode.toString();
        }
    }

    private static class DefaultJsonMapperProvider implements Jackson3JsonMapperProvider {
        static final Jackson3JsonMapperProvider INSTANCE = new DefaultJsonMapperProvider();

        private static final JsonMapper mapper = JsonMapper.builder()
                .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
                .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
                .disable(JsonNodeFeature.STRIP_TRAILING_BIGDECIMAL_ZEROES)
                .findAndAddModules()
                .build();
        private static final JsonMapper lenientMapper = JsonMapper.builder()
                .enable(JsonReadFeature.ALLOW_UNQUOTED_PROPERTY_NAMES)
                .enable(JsonReadFeature.ALLOW_JAVA_COMMENTS)
                .enable(JsonReadFeature.ALLOW_SINGLE_QUOTES)
                .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
                .disable(JsonNodeFeature.STRIP_TRAILING_BIGDECIMAL_ZEROES)
                .findAndAddModules()
                .build();

        @Override
        public JsonMapper getJsonMapper(boolean lenient) {
            return lenient ? lenientMapper : mapper;
        }
    }
}
