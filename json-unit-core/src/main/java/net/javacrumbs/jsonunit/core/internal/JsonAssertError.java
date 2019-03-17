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

import org.opentest4j.MultipleFailuresError;

import static java.util.stream.Collectors.toList;
import static net.javacrumbs.jsonunit.core.internal.ExceptionUtils.formatDifferences;

class JsonAssertError extends MultipleFailuresError {
    private final String message;
    private final Differences differences;

    JsonAssertError(String message, Differences differences) {
        super(message, differences.getDifferences().stream().map(JsonDifference::getError).collect(toList()));
        this.message = message;
        this.differences = differences;
    }

    @Override
    public String getMessage() {
        return formatDifferences(message, differences);
    }
}
