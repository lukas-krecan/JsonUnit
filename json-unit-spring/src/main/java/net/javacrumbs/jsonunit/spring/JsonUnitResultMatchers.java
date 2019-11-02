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
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.function.BiConsumer;

/**
 * Matchers compatible with Spring MVC test framework.
 * <p/>
 * Sample usage:
 * <p/>
 * <code>
 * this.mockMvc.perform(get("/sample").accept(MediaType.APPLICATION_JSON)).andExpect(json().isEqualTo(CORRECT_JSON));
 * </code>
 */
public class JsonUnitResultMatchers extends AbstractSpringMatchers<JsonUnitResultMatchers, ResultMatcher> {
    private JsonUnitResultMatchers(Path path, Configuration configuration) {
        super(path, configuration);
    }

    /**
     * Creates JsonUnitResultMatchers to be used for JSON assertions.
     */
    public static JsonUnitResultMatchers json() {
        return new JsonUnitResultMatchers(Path.root(), Configuration.empty());
    }

    @Override
    ResultMatcher matcher(BiConsumer<Object, InternalMatcher> matcher) {
        return new JsonResultMatcher(path, configuration, matcher);
    }

    @Override
    JsonUnitResultMatchers matchers(Path path, Configuration configuration) {
        return new JsonUnitResultMatchers(path, configuration);
    }


    private static class JsonResultMatcher extends AbstractSpringMatcher implements ResultMatcher {
        private JsonResultMatcher(Path path, Configuration configuration, BiConsumer<Object, InternalMatcher> matcher) {
            super(path, configuration, matcher);
        }

        @Override
        public void match(MvcResult result) throws Exception {
            String actual = getContentAsString(result.getResponse());
            doMatch(actual);
        }

        private String getContentAsString(MockHttpServletResponse response) throws UnsupportedEncodingException {
            // copy from MockHttpServletResponse.getContentAsString(Charset) in order to keep compatibility with older Spring versions
            String charset = response.isCharset() && response.getCharacterEncoding() != null ? response.getCharacterEncoding() : StandardCharsets.UTF_8.name();
            return new String(response.getContentAsByteArray(), charset);
        }
    }
}
