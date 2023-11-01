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

import static net.javacrumbs.jsonunit.core.internal.Utils.closeQuietly;
import static net.javacrumbs.jsonunit.core.internal.Utils.toReader;

import java.io.Reader;
import java.math.BigDecimal;
import net.javacrumbs.jsonunit.core.internal.Utils.JsonStringReader;
import org.jetbrains.annotations.NotNull;

/**
 * Common superclass for node factories
 */
abstract class AbstractNodeFactory implements NodeFactory {
    @Override
    public Node convertToNode(Object source, String label, boolean lenient) {
        if (source == null) {
            return nullNode();
        } else if (source instanceof Node) {
            return (Node) source;
        } else if (source instanceof String && !((String) source).trim().isEmpty()) {
            return readValue((String) source, label, lenient);
        } else if (source instanceof Reader) {
            try {
                return readValue((Reader) source, label, lenient);
            } finally {
                closeQuietly((Reader) source);
            }
        } else {
            return convertValue(source);
        }
    }

    @Override
    public Node valueToNode(Object source) {
        if (source == null) {
            return nullNode();
        } else {
            return convertValue(source);
        }
    }

    final Node convertValue(Object source) {
        if (source instanceof BigDecimal) {
            return new GenericNodeBuilder.NumberNode((Number) source);
        } else {
            return doConvertValue(source);
        }
    }

    @NotNull
    protected IllegalArgumentException newParseException(String label, Reader value, Exception e) {
        if (value instanceof JsonStringReader) {
            return new IllegalArgumentException(
                    "Can not parse " + label + " value: '" + ((JsonStringReader) value).getString() + "'", e);
        } else {
            return new IllegalArgumentException("Can not parse " + label + " value.", e);
        }
    }

    protected abstract Node doConvertValue(Object source);

    protected abstract Node readValue(Reader reader, String label, boolean lenient);

    Node readValue(String source, String label, boolean lenient) {
        return readValue(toReader(source), label, lenient);
    }

    protected abstract Node nullNode();
}
