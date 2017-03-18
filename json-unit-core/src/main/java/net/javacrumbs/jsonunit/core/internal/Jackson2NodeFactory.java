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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;

import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Map;

import static net.javacrumbs.jsonunit.core.internal.Utils.closeQuietly;

/**
 * Deserializes node using Jackson 2
 */
class Jackson2NodeFactory extends AbstractNodeFactory {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final ObjectMapper lenientMapper = new ObjectMapper();

    static {
        lenientMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        lenientMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        lenientMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
    }


    @Override
    protected Node convertValue(Object source) {
        return newNode(mapper.convertValue(source, JsonNode.class));
    }

    @Override
    protected Node nullNode() {
        return newNode(NullNode.getInstance());
    }

    protected Node readValue(Reader value, String label, boolean lenient) {
        try {
            return newNode(getMapper(lenient).readTree(value));
        } catch (IOException e) {
            throw new IllegalArgumentException("Can not parse " + label + " value.", e);
        } finally {
            closeQuietly(value);
        }
    }

    private ObjectMapper getMapper(boolean lenient) {
        return lenient ? lenientMapper : mapper;
    }

    private static Node newNode(JsonNode jsonNode) {
        if (jsonNode != null && !jsonNode.isMissingNode()) {
            return new Jackson2Node(jsonNode);
        } else {
            return Node.MISSING_NODE;
        }
    }

    public boolean isPreferredFor(Object source) {
        return source instanceof JsonNode;
    }

    static final class Jackson2Node extends AbstractNode {
        private final JsonNode jsonNode;

        public Jackson2Node(JsonNode jsonNode) {
            this.jsonNode = jsonNode;
        }

        public Node element(int index) {
            return newNode(jsonNode.path(index));
        }

        public Iterator<KeyValue> fields() {
            final Iterator<Map.Entry<String, JsonNode>> iterator = jsonNode.fields();
            return new Iterator<KeyValue>() {
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                public void remove() {
                    iterator.remove();
                }

                public KeyValue next() {
                    Map.Entry<String, JsonNode> entry = iterator.next();
                    return new KeyValue(entry.getKey(), newNode(entry.getValue()));
                }
            };
        }

        public Node get(String key) {
            return newNode(jsonNode.get(key));
        }

        public boolean isMissingNode() {
            return false;
        }

        public boolean isNull() {
            return jsonNode.isNull();
        }

        public Iterator<Node> arrayElements() {
            final Iterator<JsonNode> elements = jsonNode.elements();
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
           return jsonNode.size();
        }

        public String asText() {
            return jsonNode.asText();
        }

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
            } else {
                throw new IllegalStateException("Unexpected node type " + jsonNode);
            }
        }

        public BigDecimal decimalValue() {
            return jsonNode.decimalValue();
        }

        public Boolean asBoolean() {
            return jsonNode.asBoolean();
        }

        @Override
        public String toString() {
            return jsonNode.toString();
        }
    }
}
