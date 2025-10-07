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
import java.util.function.Function;
import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.internal.matchers.InternalMatcher;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.test.web.client.RequestMatcher;

/**
 * Matchers compatible with Spring mocking framework.
 * <p>
 * Sample usage:
 * <p>
 * <code>
 *         mockServer.expect(requestTo(URI))
 *                           .andExpect(json().isEqualTo(json))
 *                           .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON_UTF8));
 * </code>
 */
public class JsonUnitRequestMatchers extends AbstractSpringMatchers<JsonUnitRequestMatchers, RequestMatcher> {

    private JsonUnitRequestMatchers(Configuration configuration, Function<Object, Object> jsonTransformer) {
        super(configuration, jsonTransformer);
    }

    @Override
    RequestMatcher matcher(Consumer<InternalMatcher> matcher) {
        return new JsonRequestMatcher(configuration, matcher, jsonTransformer);
    }

    @Override
    JsonUnitRequestMatchers matchers(Configuration configuration, Function<Object, Object> jsonTransformer) {
        return new JsonUnitRequestMatchers(configuration, jsonTransformer);
    }

    /**
     * Creates JsonUnitResultMatchers to be used for JSON assertions.
     */
    public static JsonUnitRequestMatchers json() {
        return new JsonUnitRequestMatchers(Configuration.empty(), Function.identity());
    }

    private static class JsonRequestMatcher extends AbstractSpringMatcher implements RequestMatcher {
        private JsonRequestMatcher(
                Configuration configuration,
                Consumer<InternalMatcher> matcher,
                Function<Object, Object> jsonTransformer) {
            super(configuration, matcher, jsonTransformer);
        }

        @Override
        public void match(ClientHttpRequest request) throws AssertionError {
            Object actual = ((MockClientHttpRequest) request).getBodyAsString();
            doMatch(actual);
        }
    }
}
