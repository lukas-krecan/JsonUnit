/**
 * Copyright 2009-2017 the original author or authors.
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

import java.util.ArrayList;
import java.util.List;

/**
 * List of differences
 */
class Differences {

    private final List<String> messages = new ArrayList<String>();

    Differences() {
    }

    public void add(String message, Object... args) {
        add(String.format(message, args));
    }

    void add(String message) {
        messages.add(message);
    }

    public boolean isEmpty() {
        return messages.isEmpty();
    }

    public void appendDifferences(StringBuilder builder) {
        if (!messages.isEmpty()) {
            builder.append("JSON documents are different:\n");
            for (String message : messages) {
                builder.append(message).append("\n");
            }
        }
    }

}
