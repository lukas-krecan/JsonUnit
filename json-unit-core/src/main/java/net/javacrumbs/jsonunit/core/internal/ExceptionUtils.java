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

import java.util.List;
import org.jspecify.annotations.Nullable;

class ExceptionUtils {
    private static final String ROOT_MESSAGE = "JSON documents are different:\n";

    private static final ExceptionFactory exceptionFactory = new Opentest4jExceptionFactory();

    static String formatDifferences(@Nullable String message, Differences differences) {
        return formatDifferences(message, differences.getDifferences());
    }

    static String formatDifferences(@Nullable String message, List<JsonDifference> differences) {
        StringBuilder builder = new StringBuilder();
        if (!differences.isEmpty()) {
            addHeading(message, builder);
            builder.append(ROOT_MESSAGE);
            for (JsonDifference difference : differences) {
                builder.append(difference.getMessage()).append("\n");
            }
        }
        return builder.toString();
    }

    static AssertionError createException(@Nullable String message, Differences diffs) {
        return exceptionFactory.createException(message, diffs);
    }

    private static void addHeading(@Nullable String message, StringBuilder builder) {
        if (message != null && !message.isEmpty()) {
            if (message.startsWith("[") && message.endsWith("] ")) {
                builder.append(message);
            } else {
                builder.append('[').append(message).append("] ");
            }
        }
    }
}
