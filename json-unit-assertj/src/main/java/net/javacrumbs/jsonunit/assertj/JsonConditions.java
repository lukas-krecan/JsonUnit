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
import org.assertj.core.api.Condition;

/**
 * AssertJ {@link Condition} helpers for JsonUnit.
 */
public final class JsonConditions {

    private JsonConditions() {
        // utility class
    }

    /**
     * Condition that checks that the selected JSON node is present.
     *
     * <p>Intended to be used together with {@link JsonAssertions#assertThatJson(Object)} and
     * {@link JsonAssert#node(String)}:
     *
     * <pre>{@code
     * assertThatJson("{\"a\":1}")
     *     .node("a")
     *     .is(JsonConditions.isPresent());
     * }</pre>
     */
    public static Condition<Object> isPresent() {
        return new Condition<>(actual -> {
            if (actual instanceof Node node) {
                return !node.isMissingNode();
            }
            // Root or other representation (e.g. from inPath) — treat as present
            return true;
        }, "node to be present");
    }
}

