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
package net.javacrumbs.jsonunit.assertj;

import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.internal.Diff;
import net.javacrumbs.jsonunit.core.internal.Node;
import net.javacrumbs.jsonunit.core.internal.Path;
import org.assertj.core.api.MapAssert;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static org.assertj.core.error.ShouldContainValue.shouldContainValue;
import static org.assertj.core.error.ShouldNotContainValue.shouldNotContainValue;

class JsonMapAssert extends MapAssert<String, Object> {
    private final Configuration configuration;
    private final Path path;

    JsonMapAssert(Map<String, Object> actual, Path path, Configuration configuration) {
        super(actual);
        this.path = path;
        this.configuration = configuration;
        usingComparator(new JsonComparator(configuration, path.asPrefix(), true));
    }

    @Override
    @NotNull
    public JsonMapAssert isEqualTo(@Nullable Object expected) {
        return compare(expected, configuration);
    }

    @Override
    @NotNull
    public MapAssert<String, Object> containsValue(@Nullable Object expected) {
        if (expected instanceof Node) {
            if (!contains(expected)) {
                throwAssertionError(shouldContainValue(actual, expected));
            }
            return this;
        } else {
            return super.containsValue(expected);
        }
    }

    @Override
    @NotNull
    public MapAssert<String, Object> doesNotContainValue(@Nullable Object expected) {
        if (expected instanceof Node) {
            if (contains(expected)) {
                throwAssertionError(shouldNotContainValue(actual, expected));
            }
            return this;
        } else {
            return super.doesNotContainValue(expected);
        }
    }

    @Override
    @NotNull
    @Deprecated
    public JsonMapAssert isEqualToIgnoringGivenFields(@Nullable Object other, @NotNull String... propertiesOrFieldsToIgnore) {
        return compare(other, configuration.whenIgnoringPaths(propertiesOrFieldsToIgnore));
    }

    @Override
    @NotNull
    @Deprecated
    public MapAssert<String, Object> isEqualToComparingOnlyGivenFields(@Nullable Object other, @NotNull String... propertiesOrFieldsUsedInComparison) {
        throw unsupportedOperation();
    }

    @Override
    @NotNull
    @Deprecated
    public MapAssert<String, Object> isEqualToIgnoringNullFields(@Nullable Object other) {
        throw unsupportedOperation();
    }

    @Override
    @NotNull
    @Deprecated
    public MapAssert<String, Object> isEqualToComparingFieldByField(@Nullable Object other) {
        throw unsupportedOperation();
    }

    @Override
    @NotNull
    @Deprecated
    public MapAssert<String, Object> isEqualToComparingFieldByFieldRecursively(@Nullable Object other) {
        throw unsupportedOperation();
    }


    /**
     * Does not work. Use {@link #containsKey(Object)} instead.
     */
    @Override
    @Deprecated
    public MapAssert<String, Object> hasFieldOrProperty(String name) {
        throw unsupportedOperation();
    }

    /**
     * Does not work. Use {@link #contains(Map.Entry[])} instead.
     */
    @Override
    @Deprecated
    public MapAssert<String, Object> hasFieldOrPropertyWithValue(String name, Object value) {
        throw unsupportedOperation();
    }

    @NotNull
    private UnsupportedOperationException unsupportedOperation() {
        return new UnsupportedOperationException("Operation not supported for JSON documents");
    }

    @NotNull
    private JsonMapAssert compare(@Nullable Object other, @NotNull Configuration configuration) {
        describedAs(null);
        Diff diff = Diff.create(other, actual, "fullJson", path, configuration);
        diff.failIfDifferent();
        return this;
    }

    private boolean contains(Object expected) {
        return actual.entrySet().stream().anyMatch(kv -> Diff.create(expected, kv.getValue(), "fullJson", path.asPrefix(), configuration).similar());
    }
}
