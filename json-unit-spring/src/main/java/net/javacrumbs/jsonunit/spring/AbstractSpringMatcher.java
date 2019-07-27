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

import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.internal.Path;
import net.javacrumbs.jsonunit.core.internal.matchers.InternalMatcher;
import org.springframework.test.web.servlet.MvcResult;

import java.util.function.BiConsumer;

abstract class AbstractSpringMatcher {
    private final Path path;
    private final Configuration configuration;
    private final BiConsumer<Object, InternalMatcher> matcher;

    AbstractSpringMatcher(Path path, Configuration configuration, BiConsumer<Object, InternalMatcher> matcher) {
        this.path = path;
        this.configuration = configuration;
        this.matcher = matcher;
    }

    void doMatch(Object actual) {
        matcher.accept(actual, new InternalMatcher(actual, path, "", configuration));
    }
}
