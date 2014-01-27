/**
 * Copyright 2009-2013 the original author or authors.
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
package net.javacrumbs.jsonunit;

import net.javacrumbs.jsonunit.core.internal.Diff;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import static net.javacrumbs.jsonunit.core.internal.Diff.create;

/**
 * Contains Hamcrest matchers to be used with Hamcrest assertThat and other tools.
 * <p/>
 * All the methods accept Objects as parameters. The supported types are:
 * <ol>
 * <li>Jackson JsonNode</li>
 * <li>Numbers, booleans and any other type parseable by Jackson's ObjectMapper.convertValue</li>
 * <li>String is parsed as JSON. For expected values the string is quoted if it contains obviously invalid JSON.</li>
 * <li>{@link java.io.Reader} similarly to String</li>
 * <li>null as null Node</li>
 * </ol>
 */
public class JsonMatchers {
    /**
     * Are the JSON structures equivalent?
     *
     * @param expected
     * @return
     */
    public static Matcher<Object> jsonEquals(Object expected) {
        return new JsonPartMatcher<Object>("", expected);
    }

    /**
     * Is the part of JSON structures equivalent?
     *
     * @param expected
     * @return
     */
    public static Matcher<Object> jsonPartEquals(String path, Object expected) {
        return new JsonPartMatcher<Object>(path, expected);
    }

    private static final class JsonPartMatcher<T> extends BaseMatcher<T> {
        private final Object expected;
        private final String path;
        private String differences;

        JsonPartMatcher(String path, Object expected) {
            this.expected = expected;
            this.path = path;
        }

        public boolean matches(Object item) {
            Diff diff = create(expected, item, "fullJson", path, JsonAssert.getIgnorePlaceholder());
            if (!diff.similar()) {
                differences = diff.differences();
            }
            return diff.similar();
        }

        public void describeTo(Description description) {
            if ("".equals(path)) {
                description.appendText(expected.toString());
            } else {
                description.appendText(expected.toString()).appendText(" in \"").appendText(path).appendText("\"");
            }
        }

        @Override
        public void describeMismatch(Object item, Description description) {
            description.appendText(differences);
        }
    }
}
