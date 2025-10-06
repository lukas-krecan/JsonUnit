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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.JsonNodeFeature;
import com.fasterxml.jackson.databind.node.NullNode;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import net.javacrumbs.jsonunit.providers.Jackson2ObjectMapperProvider;
import org.jspecify.annotations.Nullable;

/**
 * Deserializes node using Jackson 2
 */
class Jackson2NodeFactory extends AbstractNodeFactory {
    private final ServiceLoader<Jackson2ObjectMapperProvider> serviceLoader =
            ServiceLoader.load(Jackson2ObjectMapperProvider.class);

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
        } catch (IOException e) {
            throw newParseException(label, value, e);
        } finally {
            closeQuietly(value);
        }
    }

    private ObjectMapper getMapper(boolean lenient) {
        return getMapperProvider().getObjectMapper(lenient);
    }

    private Jackson2ObjectMapperProvider getMapperProvider() {
        synchronized (serviceLoader) {
            Iterator<Jackson2ObjectMapperProvider> iterator = serviceLoader.iterator();
            if (iterator.hasNext()) {
                return iterator.next();
            } else {
                return DefaultObjectMapperProvider.INSTANCE;
            }
        }
    }

    private static Node newNode(@Nullable JsonNode jsonNode) {
        if (jsonNode != null && !jsonNode.isMissingNode()) {
            return new Jackson2Node(jsonNode);
        } else {
            return Node.MISSING_NODE;
        }
    }

    @Override
    public boolean isPreferredFor(@Nullable Object source) {
        return source instanceof JsonNode;
    }

    static final class Jackson2Node extends AbstractNode {
        private final JsonNode jsonNode;

        Jackson2Node(JsonNode jsonNode) {
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
            final Iterator<JsonNode> elements = jsonNode.elements();
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
            return jsonNode.asText();
        }

        @Override
        public NodeType getNodeType() {
            if (jsonNode.isObject()) {
                return NodeType.OBJECT;
            } else if (jsonNode.isArray()) {
                return NodeType.ARRAY;
            } else if (jsonNode.isTextual()) {
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

    private static class DefaultObjectMapperProvider implements Jackson2ObjectMapperProvider {
        static final Jackson2ObjectMapperProvider INSTANCE = new DefaultObjectMapperProvider();

        private static final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        private static final ObjectMapper lenientMapper = new ObjectMapper().findAndRegisterModules();

        static {
            mapper.configure(DeserializationFeature.FAIL_ON_TRAILING_TOKENS, true);
            mapper.configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true);
            mapper.configure(JsonNodeFeature.STRIP_TRAILING_BIGDECIMAL_ZEROES, false);

            lenientMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
            lenientMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
            lenientMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
            lenientMapper.configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true);
            lenientMapper.configure(JsonNodeFeature.STRIP_TRAILING_BIGDECIMAL_ZEROES, false);
        }

        @Override
        public ObjectMapper getObjectMapper(boolean lenient) {
            return lenient ? lenientMapper : mapper;
        }
    }
}
