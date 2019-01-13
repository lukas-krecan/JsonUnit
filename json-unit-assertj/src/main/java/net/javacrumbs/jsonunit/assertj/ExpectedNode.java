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
package net.javacrumbs.jsonunit.assertj;


import net.javacrumbs.jsonunit.core.internal.Node;

import java.math.BigDecimal;
import java.util.Iterator;

/**
 * In AssertJ we need to know which node is expected and which is actual. This class marks expected node.
 */
class ExpectedNode implements Node {

    private final Node wrappedNode;

    ExpectedNode(Node wrappedNode) {
        this.wrappedNode = wrappedNode;
    }

    public Node element(int index) {
        return wrappedNode.element(index);
    }

    public Iterator<Node.KeyValue> fields() {
        return wrappedNode.fields();
    }

    public Node get(String key) {
        return wrappedNode.get(key);
    }

    public boolean isMissingNode() {
        return wrappedNode.isMissingNode();
    }

    public boolean isNull() {
        return wrappedNode.isNull();
    }

    public Iterator<Node> arrayElements() {
        return wrappedNode.arrayElements();
    }

    public int size() {
        return wrappedNode.size();
    }

    public String asText() {
        return wrappedNode.asText();
    }

    public Node.NodeType getNodeType() {
        return wrappedNode.getNodeType();
    }

    public BigDecimal decimalValue() {
        return wrappedNode.decimalValue();
    }

    public Boolean asBoolean() {
        return wrappedNode.asBoolean();
    }

    public Object getValue() {
        return wrappedNode.getValue();
    }

    public void ___do_not_implement_this_interface_seriously() {
    }

    @Override
    public String toString() {
        return wrappedNode.toString();
    }
}
