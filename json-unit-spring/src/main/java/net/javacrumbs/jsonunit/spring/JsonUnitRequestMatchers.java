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
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.test.web.client.RequestMatcher;

import java.io.IOException;
import java.util.function.BiConsumer;


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

    private JsonUnitRequestMatchers(String path, Configuration configuration) {
        super(path, configuration);
    }

    @Override
    RequestMatcher matcher(BiConsumer<Object, AbstractMatcher> matcher) {
        return new JsonRequestMatcher(path, configuration, matcher);
    }

    @Override
    JsonUnitRequestMatchers matchers(String path, Configuration configuration) {
        return new JsonUnitRequestMatchers(path, configuration);
    }

    /**
     * Creates JsonUnitResultMatchers to be used for JSON assertions.
     *
     * @return
     */
    public static JsonUnitRequestMatchers json() {
        return new JsonUnitRequestMatchers("", Configuration.empty());
    }


    private static class JsonRequestMatcher extends AbstractMatcher implements RequestMatcher {
        JsonRequestMatcher(String path, Configuration configuration, BiConsumer<Object, AbstractMatcher> matcher) {
            super(path, configuration, matcher);
        }

        @Override
        public void match(ClientHttpRequest request) throws IOException, AssertionError {
            Object actual = ((MockClientHttpRequest) request).getBodyAsString();
            doMatch(actual);
        }
    }
}
