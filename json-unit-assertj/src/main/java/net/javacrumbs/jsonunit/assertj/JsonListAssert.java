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
import net.javacrumbs.jsonunit.core.internal.Diff;
import net.javacrumbs.jsonunit.core.internal.Path;
import org.assertj.core.api.ListAssert;
import org.assertj.core.internal.Failures;

import java.util.List;

import static net.javacrumbs.jsonunit.core.internal.JsonUtils.wrapDeserializedObject;
import static org.assertj.core.error.ShouldNotBeEqual.shouldNotBeEqual;

class JsonListAssert extends ListAssert<Object> {
    private final Configuration configuration;
    private final Path path;

    JsonListAssert(List<?> actual, Path path, Configuration configuration) {
        super(actual);
        this.path = path;
        this.configuration = configuration;
        usingComparator(new JsonComparator(configuration, path, true));
        usingElementComparator(new JsonComparator(configuration, path.asPrefix(), true));
    }

    @Override
    public JsonListAssert isEqualTo(Object expected) {
        describedAs(null);
        Diff diff = createDiff(expected);
        if (!diff.similar()) {
            failWithMessage(diff.toString());
        }
        return this;
    }

    @Override
    public JsonListAssert isNotEqualTo(Object other) {
        Diff diff = createDiff(other);
        if (diff.similar()) {
            JsonComparisonStrategy strategy = new JsonComparisonStrategy(configuration);
            throw Failures.instance().failure(info, shouldNotBeEqual(actual, other, strategy));
        }
        return this;
    }

    private Diff createDiff(Object other) {
        return Diff.create(other, wrapDeserializedObject(actual), "fullJson", path, configuration);
    }
}
