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

import java.util.function.Predicate;
import net.javacrumbs.jsonunit.core.internal.JsonUtils;
import net.javacrumbs.jsonunit.core.internal.Node;
import org.assertj.core.api.Condition;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * AssertJ conditions for the currently selected JSON node.
 *
 * <pre>{@code
 * assertThatJson(actual)
 *     .node("optional")
 *     .is(anyOf(absent(), nullValue()));
 * }</pre>
 */
@NullMarked
public final class JsonConditions {

    private JsonConditions() {}

    /**
     * Creates condition that matches when the selected node is absent.
     */
    public static Condition<Object> absent() {
        return nodeCondition(Node::isMissingNode, "node to be absent");
    }

    /**
     * Creates condition that matches when the selected node is present.
     */
    public static Condition<Object> present() {
        return nodeCondition(node -> !node.isMissingNode(), "node to be present");
    }

    /**
     * Creates condition that matches when the selected node is present and null.
     */
    public static Condition<Object> nullValue() {
        return nodeCondition(Node::isNull, "node to be null");
    }

    /**
     * Creates condition that matches when the selected node is present and not null.
     */
    public static Condition<Object> notNullValue() {
        return nodeCondition(node -> !node.isMissingNode() && !node.isNull(), "node to be not null");
    }

    private static Condition<Object> nodeCondition(Predicate<Node> predicate, String description) {
        return new Condition<>(actual -> predicate.test(toNode(actual)), description);
    }

    private static Node toNode(@Nullable Object actual) {
        return JsonUtils.convertToJson(actual, "actual");
    }
}
