/**
 * Copyright 2009-2018 the original author or authors.
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

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;

import static net.javacrumbs.jsonunit.core.internal.Utils.closeQuietly;

/**
 * Deserializes node using Moshi
 */
class MoshiNodeFactory extends AbstractNodeFactory {
    private static final Moshi moshi = new Moshi.Builder().build();
    private static final NodeBuilder moshiNodeBuilder = new MoshiNodeBuilder();

    @Override
    protected Node convertValue(Object source) {
        return newNode(source);
    }

    @Override
    protected Node nullNode() {
        return newNode(null);
    }

    @Override
    protected Node readValue(String source, String label, boolean lenient) {
        try {
            JsonAdapter<Object> adapter = moshi.adapter(Object.class);
            if (lenient) {
                adapter = adapter.lenient();
            }
            return newNode(adapter.fromJson(source));
        } catch (IOException e) {
            throw new IllegalArgumentException("Can not parse " + label + " value.", e);
        }
    }

    protected Node readValue(Reader value, String label, boolean lenient) {
        try {
            return readValue(Utils.readAsString(value), label, lenient);
        } catch (IOException e) {
            throw new IllegalArgumentException("Can not parse " + label + " value.", e);
        } finally {
            closeQuietly(value);
        }
    }

    private Node newNode(Object source) {
        return moshiNodeBuilder.newNode(source);
    }

    @Override
    public boolean isPreferredFor(Object source) {
        return false;
    }

    private static class MoshiNodeBuilder extends GenericNodeBuilder {
        public Node newNode(Object object) {
            if (object instanceof Number) {
                return new MoshiNumberNode((Number) object);
            } else {
                return super.newNode(object);
            }
        }

        private static final class MoshiNumberNode extends GenericNodeBuilder.NumberNode {
            private MoshiNumberNode(Number value) {
                super(value);
            }

            @Override
            public BigDecimal decimalValue() {
                // Workaround for Moshi bug https://github.com/square/moshi/issues/192
                return super.decimalValue().stripTrailingZeros();
            }

            @Override
            public String toString() {
                return decimalValue().toString();
            }
        }
    }
}
