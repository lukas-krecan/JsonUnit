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
package net.javacrumbs.jsonunit.assertj;

import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.Option;
import org.assertj.core.internal.ComparisonStrategy;

import java.util.Set;

class JsonComparisonStrategy implements ComparisonStrategy {
    private final Configuration configuration;

    JsonComparisonStrategy(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public boolean areEqual(Object actual, Object other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isGreaterThan(Object actual, Object other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isGreaterThanOrEqualTo(Object actual, Object other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isLessThan(Object actual, Object other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isLessThanOrEqualTo(Object actual, Object other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean iterableContains(Iterable<?> collection, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void iterableRemoves(Iterable<?> iterable, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void iterablesRemoveFirst(Iterable<?> iterable, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<?> duplicatesFrom(Iterable<?> iterable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean arrayContains(Object array, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean stringContains(String string, String sequence) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean stringStartsWith(String string, String prefix) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean stringEndsWith(String string, String suffix) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isStandard() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        Set<Option> options = configuration.getOptions().values();
        return "when comparing as JSON" + (!options.isEmpty() ? " with " + options : "");
    }
}
