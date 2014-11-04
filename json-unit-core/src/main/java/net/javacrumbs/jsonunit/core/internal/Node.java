/**
 * Copyright 2009-2012 the original author or authors.
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

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;


/**
 * For internal use only!!! Abstract node representation.
 */
public interface Node {

    enum NodeType {OBJECT, ARRAY, STRING, NUMBER, BOOLEAN, NULL}

    Node element(int index);

    Iterator<KeyValue> fields();

    Node get(String key);

    boolean isMissingNode();

    boolean isNull();

    Iterator<Node> arrayElements();

    String asText();

    NodeType getNodeType();

    BigDecimal decimalValue();

    Boolean asBoolean();

    static class KeyValue {
        private final String key;
        private final Node value;

        public KeyValue(String key, Node value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public Node getValue() {
            return value;
        }
    }

    static final Node MISSING_NODE = new Node() {

        public boolean isArray() {
            return false;
        }

        public Node element(int index) {
            return null;
        }

        public Iterator<KeyValue> fields() {
            Set<KeyValue> emptySet = Collections.emptySet();
            return emptySet.iterator();
        }

        public Node get(String key) {
            return this;
        }

        public boolean isMissingNode() {
            return true;
        }

        public boolean isNull() {
            return false;
        }

        public Iterator<Node> arrayElements() {
            Set<Node> emptySet = Collections.emptySet();
            return emptySet.iterator();
        }

        public String asText() {
            throw new UnsupportedOperationException();
        }

        public NodeType getNodeType() {
            throw new UnsupportedOperationException();
        }

        public BigDecimal decimalValue() {
            throw new UnsupportedOperationException();
        }

        public Boolean asBoolean() {
            throw new UnsupportedOperationException();
        }
    };
}
