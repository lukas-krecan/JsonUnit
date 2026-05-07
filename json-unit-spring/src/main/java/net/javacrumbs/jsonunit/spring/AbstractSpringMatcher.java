/**
 * Copyright 2009-2019 the original author or authors.
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
package net.javacrumbs.jsonunit.spring;

import java.util.function.Consumer;
import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.internal.JsonUtils;
import net.javacrumbs.jsonunit.core.internal.Path;
import net.javacrumbs.jsonunit.core.internal.matchers.InternalMatcher;
import net.javacrumbs.jsonunit.jsonpath.InternalJsonPathUtils;
import org.jspecify.annotations.Nullable;

abstract class AbstractSpringMatcher {
    private final Configuration configuration;
    private final Consumer<InternalMatcher> matcher;
    private final JsonTransformer jsonTransformer;

    AbstractSpringMatcher(
            Configuration configuration, Consumer<InternalMatcher> matcher, JsonTransformer jsonTransformer) {
        this.configuration = configuration;
        this.matcher = matcher;
        this.jsonTransformer = jsonTransformer;
    }

    void doMatch(@Nullable Object actual) {
        Configuration config = InternalJsonPathUtils.resolveJsonPaths(actual, configuration);
        Object json = jsonTransformer.transform(actual);
        String pathPrefix = JsonUtils.getPathPrefix(json);
        Path path = Path.create("", pathPrefix);
        matcher.accept(new InternalMatcher(json, path, "", config));
    }
}
