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
import net.javacrumbs.jsonunit.core.internal.Node;
import net.javacrumbs.jsonunit.core.internal.Path;
import org.assertj.core.api.MapAssert;
import org.assertj.core.internal.Failures;

import java.util.Map;

import static org.assertj.core.error.ShouldContainValue.shouldContainValue;
import static org.assertj.core.error.ShouldNotContainValue.shouldNotContainValue;

class JsonMapAssert extends MapAssert<String, Object> {
    private final Configuration configuration;
    private final Path path;
    private final Failures failures = Failures.instance();

    JsonMapAssert(Map<String, Object> actual, Path path, Configuration configuration) {
        super(actual);
        this.path = path;
        this.configuration = configuration;
        usingComparator(new JsonComparator(configuration, path.asPrefix(), true));
    }

    @Override
    public JsonMapAssert isEqualTo(Object expected) {
        describedAs(null);
        Diff diff = Diff.create(expected, actual, "fullJson", path, configuration);
        if (!diff.similar()) {
            failWithMessage(diff.toString());
        }
        return this;
    }

    @Override
    public MapAssert<String, Object> containsValue(Object expected) {
        if (expected instanceof Node) {
            if (!contains(expected)) {
                throw failures.failure(info, shouldContainValue(actual, expected));
            }
            return this;
        } else {
            return super.containsValue(expected);
        }
    }

    @Override
    public MapAssert<String, Object> doesNotContainValue(Object expected) {
        if (expected instanceof Node) {
            if (contains(expected)) {
                throw failures.failure(info, shouldNotContainValue(actual, expected));
            }
            return this;
        } else {
            return super.doesNotContainValue(expected);
        }
    }

    private boolean contains(Object expected) {
        return actual.entrySet().stream().anyMatch(kv -> Diff.create(expected, kv.getValue(), "fullJson", path.asPrefix(), configuration).similar());
    }
}
