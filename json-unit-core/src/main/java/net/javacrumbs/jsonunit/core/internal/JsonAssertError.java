/**
 * Copyright 2009-2017 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.javacrumbs.jsonunit.core.internal;

import org.opentest4j.MultipleFailuresError;

class JsonAssertError extends MultipleFailuresError {
    private final String heading;
    private final Differences differences;

    JsonAssertError(String heading, Differences differences) {
        super(heading, differences.getDifferences());
        this.heading = heading;
        this.differences = differences;
    }

    @Override
    public String getMessage() {
        StringBuilder builder = new StringBuilder();
        if (heading != null && !heading.isEmpty()) {
            builder.append('[').append(heading).append("] ");
        }
        differences.appendDifferences(builder);
        return builder.toString();
    }
}
