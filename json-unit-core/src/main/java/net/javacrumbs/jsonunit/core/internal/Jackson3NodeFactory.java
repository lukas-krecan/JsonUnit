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
import static tools.jackson.core.json.JsonReadFeature.ALLOW_JAVA_COMMENTS;
import static tools.jackson.core.json.JsonReadFeature.ALLOW_SINGLE_QUOTES;
import static tools.jackson.core.json.JsonReadFeature.ALLOW_UNQUOTED_PROPERTY_NAMES;
import static tools.jackson.databind.DeserializationFeature.FAIL_ON_TRAILING_TOKENS;
import static tools.jackson.databind.DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS;
import static tools.jackson.databind.cfg.JsonNodeFeature.STRIP_TRAILING_BIGDECIMAL_ZEROES;

import java.io.Reader;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import net.javacrumbs.jsonunit.providers.Jackson3ObjectMapperProvider;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.NullNode;

/**
 * Deserializes node using Jackson 3
 */
class Jackson3NodeFactory extends AbstractNodeFactory {
    private final ServiceLoader<Jackson3ObjectMapperProvider> serviceLoader =
            ServiceLoader.load(Jackson3ObjectMapperProvider.class);

    @Override
    protected Node doConvertValue(Object source) {
        if (source instanceof JsonNode jsonNode) {
            return newNode(jsonNode);
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
        } catch (Exception e) {
            throw newParseException(label, value, e);
        } finally {
            closeQuietly(value);
        }
    }

    private ObjectMapper getMapper(boolean lenient) {
        return getMapperProvider().getObjectMapper(lenient);
    }

    private Jackson3ObjectMapperProvider getMapperProvider() {
        synchronized (serviceLoader) {
            Iterator<Jackson3ObjectMapperProvider> iterator = serviceLoader.iterator();
            if (iterator.hasNext()) {
                return iterator.next();
            } else {
                return DefaultObjectMapperProvider.INSTANCE;
            }
        }
    }

    private static Node newNode(@Nullable JsonNode jsonNode) {
        if (jsonNode != null && !jsonNode.isMissingNode()) {
            return new Jackson3Node(jsonNode);
        } else {
            return Node.MISSING_NODE;
        }
    }

    @Override
    public boolean isPreferredFor(@Nullable Object source) {
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
                    jsonNode.propertyStream().iterator();
            return new Iterator<>() {
                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
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
            final Iterator<JsonNode> elements = jsonNode.iterator();
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
            return jsonNode.size();
        }

        @Override
        public String asText() {
            return jsonNode.asString();
        }

        @Override
        public NodeType getNodeType() {
            return switch (jsonNode.getNodeType()) {
                case OBJECT -> NodeType.OBJECT;
                case ARRAY -> NodeType.ARRAY;
                case BOOLEAN -> NodeType.BOOLEAN;
                case STRING, BINARY -> NodeType.STRING;
                case NUMBER -> NodeType.NUMBER;
                case NULL -> NodeType.NULL;
                default -> throw new IllegalStateException("Unexpected node type " + jsonNode);
            };
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

    private static class DefaultObjectMapperProvider implements Jackson3ObjectMapperProvider {
        static final Jackson3ObjectMapperProvider INSTANCE = new DefaultObjectMapperProvider();

        private static final ObjectMapper mapper = JsonMapper.builder()
                .configure(FAIL_ON_TRAILING_TOKENS, true)
                .configure(USE_BIG_DECIMAL_FOR_FLOATS, true)
                .configure(STRIP_TRAILING_BIGDECIMAL_ZEROES, false)
                .build();

        private static final ObjectMapper lenientMapper = JsonMapper.builder()
                .configure(ALLOW_UNQUOTED_PROPERTY_NAMES, true)
                .configure(ALLOW_JAVA_COMMENTS, true)
                .configure(ALLOW_SINGLE_QUOTES, true)
                .configure(USE_BIG_DECIMAL_FOR_FLOATS, true)
                .configure(STRIP_TRAILING_BIGDECIMAL_ZEROES, false)
                .build();

        @Override
        public ObjectMapper getObjectMapper(boolean lenient) {
            return lenient ? lenientMapper : mapper;
        }
    }
}
