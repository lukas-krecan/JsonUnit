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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

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
    private JsonUnitResultMatchers(String path, Configuration configuration) {
        super(path, configuration);
    }

    /**
     * Creates JsonUnitResultMatchers to be used for JSON assertions.
     *
     * @return
     */
    public static JsonUnitResultMatchers json() {
        return new JsonUnitResultMatchers("", Configuration.empty());
    }


    @Override
    JsonResultMatcher matcher(BiConsumer<Object, AbstractMatcher> matcher) {
        return new JsonResultMatcher(path, configuration, matcher);
    }

    @Override
    JsonUnitResultMatchers matchers(String path, Configuration configuration) {
        return new JsonUnitResultMatchers(path, configuration);
    }


    private static class JsonResultMatcher extends AbstractMatcher implements ResultMatcher {
        JsonResultMatcher(String path, Configuration configuration, BiConsumer<Object, AbstractMatcher> matcher) {
            super(path, configuration, matcher);
        }

        public void match(MvcResult result) throws Exception {
            Object actual = result.getResponse().getContentAsString();
            doMatch(actual);
        }
    }
}
