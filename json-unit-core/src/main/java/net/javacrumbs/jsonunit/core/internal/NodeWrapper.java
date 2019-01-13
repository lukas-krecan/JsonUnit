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

import java.math.BigDecimal;
import java.util.Iterator;

interface NodeWrapper extends Node {
    Node getWrappedNode();

    @Override
    default Node element(int index) {
        return getWrappedNode().element(index);
    }

    @Override
    default Iterator<KeyValue> fields() {
        return getWrappedNode().fields();
    }

    @Override
    default Node get(String key) {
        return getWrappedNode().get(key);
    }

    @Override
    default boolean isMissingNode() {
        return getWrappedNode().isMissingNode();
    }

    @Override
    default boolean isNull() {
        return getWrappedNode().isNull();
    }

    @Override
    default Iterator<Node> arrayElements() {
        return getWrappedNode().arrayElements();
    }

    @Override
    default int size() {
        return getWrappedNode().size();
    }

    @Override
    default String asText() {
        return getWrappedNode().asText();
    }

    @Override
    default NodeType getNodeType() {
        return getWrappedNode().getNodeType();
    }

    @Override
    default BigDecimal decimalValue() {
        return getWrappedNode().decimalValue();
    }

    @Override
    default Boolean asBoolean() {
        return getWrappedNode().asBoolean();
    }

    @Override
    default Object getValue() {
        return getWrappedNode().getValue();
    }

    @Override
    default void ___do_not_implement_this_interface_seriously() {

    }
}
