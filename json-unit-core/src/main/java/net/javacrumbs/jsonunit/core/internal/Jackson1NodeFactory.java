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

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.introspect.AnnotatedClass;
import org.codehaus.jackson.map.introspect.AnnotatedMethod;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.map.introspect.MethodFilter;
import org.codehaus.jackson.node.NullNode;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.Iterator;

import static net.javacrumbs.jsonunit.core.internal.Utils.closeQuietly;

/**
 * Deserializes node using Jackson 1
 */
class Jackson1NodeFactory extends AbstractNodeFactory {
    private static final JacksonAnnotationIntrospector ANNOTATION_INTROSPECTOR = new JacksonAnnotationIntrospector();
    private static final MinimalMethodFilter MINIMAL_METHOD_FILTER = new MinimalMethodFilter();

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
        if (jsonNode != null  && !jsonNode.isMissingNode()) {
            return new Jackson1Node(jsonNode);
        } else {
            return Node.MISSING_NODE;
        }
    }

    public boolean isPreferredFor(Object source) {
        return source instanceof JsonNode || hasJackson1Annotations(source);
    }

    private boolean hasJackson1Annotations(Object source) {
        if (source == null) {
            return false;
        }
        AnnotatedClass annotatedClass = AnnotatedClass.construct(source.getClass(), ANNOTATION_INTROSPECTOR, mapper.getSerializationConfig());
        return annotatedClass.hasAnnotations() || hasAnnotationOnMethod(annotatedClass);
    }

    private boolean hasAnnotationOnMethod(AnnotatedClass annotatedClass) {
        annotatedClass.resolveMemberMethods(MINIMAL_METHOD_FILTER, false);
        Iterable<AnnotatedMethod> annotatedMethods = annotatedClass.memberMethods();
        for (AnnotatedMethod method : annotatedMethods) {
            if (method.getAnnotationCount() > 0) {
                return true;
            }
        }
        return false;
    }

    static final class Jackson1Node extends AbstractNode {
        private final JsonNode jsonNode;

        public Jackson1Node(JsonNode jsonNode) {
            this.jsonNode = jsonNode;
        }

        public Node element(int index) {
            return newNode(jsonNode.path(index));
        }

        public Iterator<KeyValue> fields() {
            final Iterator<String> iterator = jsonNode.getFieldNames();
            return new Iterator<KeyValue>() {
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                public void remove() {
                    iterator.remove();
                }

                public KeyValue next() {
                    String fieldName = iterator.next();
                    return new KeyValue(fieldName, newNode(jsonNode.get(fieldName)));
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
            final Iterator<JsonNode> elements = jsonNode.getElements();
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
            return jsonNode.getValueAsText();
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
            return jsonNode.getDecimalValue();
        }

        public Boolean asBoolean() {
            return jsonNode.getBooleanValue();
        }

        @Override
        public String toString() {
            return jsonNode.toString();
        }
    }

    private static class MinimalMethodFilter implements MethodFilter {
        public boolean includeMethod(Method m) {
            if (Modifier.isStatic(m.getModifiers())) {
                return false;
            }
            int pcount = m.getParameterTypes().length;
            return (pcount <= 2);
        }
    }
}
