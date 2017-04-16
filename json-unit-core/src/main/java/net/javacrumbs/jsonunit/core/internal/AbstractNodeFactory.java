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

import java.io.Reader;
import java.io.StringReader;

/**
 * Common superclass for node factories
 */
abstract class AbstractNodeFactory implements NodeFactory {
    public Node convertToNode(Object source, String label, boolean lenient) {
        if (source == null) {
            return nullNode();
        } else if (source instanceof Node) {
            return (Node) source;
        } else if (source instanceof String && ((String) source).trim().length() > 0) {
            return readValue((String) source, label, lenient);
        } else if (source instanceof Reader) {
            return readValue((Reader) source, label, lenient);
        } else {
            return convertValue(source);
        }
    }

    protected abstract Node convertValue(Object source);

    protected abstract Node readValue(Reader reader, String label, boolean lenient);

    protected Node readValue(String source, String label, boolean lenient) {
        return readValue(new StringReader(source), label, lenient);
    }

    protected abstract Node nullNode();
}
