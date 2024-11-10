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

import static net.javacrumbs.jsonunit.core.internal.JsonUtils.wrapDeserializedObject;
import static org.assertj.core.util.Lists.newArrayList;

import java.util.List;
import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.internal.Diff;
import net.javacrumbs.jsonunit.core.internal.Path;
import org.assertj.core.api.FactoryBasedNavigableListAssert;
import org.assertj.core.description.Description;
import org.assertj.core.error.BasicErrorMessageFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JsonListAssert extends FactoryBasedNavigableListAssert<JsonListAssert, List<?>, Object, JsonAssert> {
    private final Configuration configuration;
    private final Path path;

    JsonListAssert(List<?> actual, Path path, Configuration configuration) {
        super(actual, JsonListAssert.class, t -> new JsonAssert(path, configuration, t, true));
        this.path = path;
        this.configuration = configuration;
        usingComparator(new JsonComparator(configuration, path, true));
        usingElementComparator(new JsonComparator(configuration, path.asPrefix(), true));
    }

    @Override
    @NotNull
    public JsonListAssert isEqualTo(@Nullable Object expected) {
        describedAs((Description) null);
        Diff diff = createDiff(expected);
        diff.failIfDifferent();
        return this;
    }

    @Override
    @NotNull
    public JsonListAssert isNotEqualTo(@Nullable Object other) {
        Diff diff = createDiff(other);
        if (diff.similar()) {
            JsonComparisonStrategy strategy = new JsonComparisonStrategy(configuration);
            throwAssertionError(new BasicErrorMessageFactory(
                    "%nExpecting:%n <%s>%nnot to be equal to:%n <%s>%n%s", actual, other, strategy));
        }
        return this;
    }

    @Override
    protected JsonListAssert newAbstractIterableAssert(Iterable<?> iterable) {
        return new JsonListAssert(newArrayList(iterable), path, configuration);
    }

    @NotNull
    private Diff createDiff(Object other) {
        return Diff.create(other, wrapDeserializedObject(actual), "fullJson", path, configuration);
    }
}
