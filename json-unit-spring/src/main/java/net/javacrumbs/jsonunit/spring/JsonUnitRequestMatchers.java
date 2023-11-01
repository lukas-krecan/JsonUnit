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

import java.util.function.BiConsumer;
import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.internal.Path;
import net.javacrumbs.jsonunit.core.internal.matchers.InternalMatcher;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.test.web.client.RequestMatcher;

/**
 * Matchers compatible with Spring mocking framework.
 * <p/>
 * Sample usage:
 * <p/>
 * <code>
 *         mockServer.expect(requestTo(URI))
 *                           .andExpect(json().isEqualTo(json))
 *                           .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON_UTF8));
 * </code>
 */
public class JsonUnitRequestMatchers extends AbstractSpringMatchers<JsonUnitRequestMatchers, RequestMatcher> {

    private JsonUnitRequestMatchers(Path path, Configuration configuration) {
        super(path, configuration);
    }

    @NotNull
    @Override
    RequestMatcher matcher(@NotNull BiConsumer<Object, InternalMatcher> matcher) {
        return new JsonRequestMatcher(path, configuration, matcher);
    }

    @Override
    @NotNull
    JsonUnitRequestMatchers matchers(@NotNull Path path, @NotNull Configuration configuration) {
        return new JsonUnitRequestMatchers(path, configuration);
    }

    /**
     * Creates JsonUnitResultMatchers to be used for JSON assertions.
     */
    @NotNull
    public static JsonUnitRequestMatchers json() {
        return new JsonUnitRequestMatchers(Path.root(), Configuration.empty());
    }

    private static class JsonRequestMatcher extends AbstractSpringMatcher implements RequestMatcher {
        private JsonRequestMatcher(
                @NotNull Path path,
                @NotNull Configuration configuration,
                @NotNull BiConsumer<Object, InternalMatcher> matcher) {
            super(path, configuration, matcher);
        }

        @Override
        public void match(@NotNull ClientHttpRequest request) throws AssertionError {
            Object actual = ((MockClientHttpRequest) request).getBodyAsString();
            doMatch(actual);
        }
    }
}
