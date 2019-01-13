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
package net.javacrumbs.jsonunit.core.listener;

/**
 * Describes differences between documents.
 */
public interface Difference {
    enum Type {EXTRA, MISSING, DIFFERENT}

    /**
     * Path to the difference
     */
    String getActualPath();

    /**
     * Path to the expected element (may be different than actual path if IGNORE_ARRAY_ORDER is used)
     */
    String getExpectedPath();

    /**
     * Actual node serialized as Map&lt;String, Object&gt; for objects, BigDecimal for numbers, ...
     */
    Object getActual();


    /**
     * Expected node serialized as Map&lt;String, Object&gt; for objects, BigDecimal for numbers, ...
     */
    Object getExpected();

    /**
     * Type of the difference
     */
    Type getType();
}
