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

import org.jetbrains.annotations.Nullable;

interface NodeFactory {
    /**
     * Returns true if this factory is preferred for given source.
     *
     * @param source
     * @return
     */
    boolean isPreferredFor(Object source);

    /**
     * Creates node from given source.
     *
     * @param source
     * @param label
     * @param lenient
     * @return
     */
    Node convertToNode(@Nullable Object source, String label, boolean lenient);

    /**
     * Converts value to Json node. It can be Map, String, null, or primitive. Should not be parsed, just converted.
     *
     * @param source
     * @return
     */
    Node valueToNode(Object source);
}
