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
import net.javacrumbs.jsonunit.core.internal.Path;
import org.assertj.core.api.AbstractAssert;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JsonObjectAssert extends AbstractAssert<JsonObjectAssert, Object> {
    private final Configuration configuration;
    private final Path path;

    JsonObjectAssert(Object actual, Path path, Configuration configuration) {
        super(actual, JsonObjectAssert.class);
        this.path = path;
        this.configuration = configuration;
        usingComparator(new JsonComparator(configuration, path.asPrefix(), true));
    }

    @Override
    @NotNull
    public JsonObjectAssert isEqualTo(@Nullable Object expected) {
        return compare(expected, configuration);
    }

    @NotNull
    private UnsupportedOperationException unsupportedOperation() {
        return new UnsupportedOperationException("Operation not supported for JSON documents");
    }

    @NotNull
    private JsonObjectAssert compare(@Nullable Object other, @NotNull Configuration configuration) {
        describedAs(null);
        Diff diff = Diff.create(other, actual, "fullJson", path, configuration);
        diff.failIfDifferent();
        return this;
    }
}
