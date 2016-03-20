/**
 * Copyright 2009-2015 the original author or authors.
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
package net.javacrumbs.jsonunit.core;

/**
 * Comparison options.
 */
public enum Option {
    /**
     * Treats null nodes in actual value as absent. In other words
     * if you expect {"test":{"a":1}} this {"test":{"a":1, "b": null}} will pass the test.
     */
    TREATING_NULL_AS_ABSENT,

    /**
     * When comparing arrays, ignores array order. In other words, treats arrays as sets.
     */
    IGNORING_ARRAY_ORDER,

    /**
     * Ignores extra fields in the actual value.
     */
    IGNORING_EXTRA_FIELDS,

    /**
     * Compares only structures. Completely ignores both values and types.
     *  Is too lenient, ignores types, prefer {@link Option#IGNORING_VALUES} instead.
     */
    COMPARING_ONLY_STRUCTURE,

    /**
     * Ignores array size differences. Compares only first element if it exists.
     */
    IGNORING_ARRAY_SIZE,

    /**
     * Ignores values but fails if value types are different.
     */
    IGNORING_VALUES
}
