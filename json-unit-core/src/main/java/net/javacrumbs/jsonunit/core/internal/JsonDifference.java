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

import org.opentest4j.AssertionFailedError;

class JsonDifference {
    private final String message;
    private final Object[] args;
    private final Node expected;
    private final Node actual;

    private JsonDifference(String message, Object[] args, Node expected, Node actual) {
        this.message = message;
        this.args = args;
        this.expected = expected;
        this.actual = actual;
    }

    JsonDifference(Context context, String message, Object... args) {
        this(message, args, context.getExpectedNode(), context.getActualNode());
    }

    AssertionFailedError getError() {
        return new AssertionFailedError(getMessage(), expected.getValue(), actual.getValue());
    }

    public Node getExpected() {
        return expected;
    }

    public Node getActual() {
        return actual;
    }

    public String getMessage() {
        return String.format(message, args);
    }
}