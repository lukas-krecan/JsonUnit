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
package net.javacrumbs.jsonunit.test.all;

import net.javacrumbs.jsonunit.ConfigurableJsonMatcher;
import net.javacrumbs.jsonunit.core.ParametrizedMatcher;
import net.javacrumbs.jsonunit.test.base.AbstractAssertJTest;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.jupiter.api.Test;

import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.test.base.JsonTestUtils.readByJackson2;

public class AllAssertJTest extends AbstractAssertJTest {
    @Override
    protected Object readValue(String value) {
        return readByJackson2(value);
    }

    @Test
        void embeddedJsonShouldBeParsed() {
            String actual = "{" +
                "\"response\": {" +
                "\"body\": \"{'message':'Success','time-sent':'2018-12-10T14:49:11.537Z[GMT]'}\"" +
                "}" +
                "}";

            String expected = "{" +
                "\"response\": {" +
                "\"body\": \"${json-unit.matches:embeddedJson}{'message':'Success','time-sent':'${json-unit.ignore}'}\"" +
                "}" +
                "}";

            assertThatJson(actual).withConfiguration(c->c.withMatcher("embeddedJson", new EmbeddedJsonMatcher())).isEqualTo(expected);
        }


        static private class EmbeddedJsonMatcher extends BaseMatcher<Object> implements ParametrizedMatcher {
            private ConfigurableJsonMatcher<Object> embeddedMatcher;


            @Override
            public void setParameter(String parameter) {
                embeddedMatcher = jsonEquals(parameter);
            }

            @Override
            public boolean matches(Object item) {
                // leniently parse
                return embeddedMatcher.matches(new org.json.JSONObject((String)item));
            }

            @Override
            public void describeTo(Description description) {
                embeddedMatcher.describeTo(description);
            }

            @Override
            public void describeMismatch(Object item, Description description) {
                embeddedMatcher.describeMismatch(item, description);
            }
        }
}
