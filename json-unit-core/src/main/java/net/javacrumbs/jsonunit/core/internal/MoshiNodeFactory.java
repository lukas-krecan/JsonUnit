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

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.io.Reader;

import static net.javacrumbs.jsonunit.core.internal.Nodes.newNode;
import static net.javacrumbs.jsonunit.core.internal.Utils.closeQuietly;

/**
 * Deserializes node using Moshi
 */
class MoshiNodeFactory extends AbstractNodeFactory {
    private static final Moshi moshi = new Moshi.Builder().build();

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

    public boolean isPreferredFor(Object source) {
        return false;
    }



}
