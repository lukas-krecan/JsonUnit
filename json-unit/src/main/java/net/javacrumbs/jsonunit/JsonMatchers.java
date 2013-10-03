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
import org.codehaus.jackson.JsonNode;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.io.Reader;

import static net.javacrumbs.jsonunit.JsonUtils.readValue;

/**
 * Contains Hamcrest matchers to be used with Hamcrest assertThat and other tools.
 */
public class JsonMatchers {
    /**
     * Are the JSON structures equivalent?
     *
     * @param jsonNode
     * @return
     */
    public static Matcher<JsonNode> jsonEquals(JsonNode jsonNode) {
        return new JsonPartMatcher<JsonNode>("", jsonNode);
    }

    /**
     * Are the JSON structures equivalent?
     *
     * @param json
     * @return
     */
    public static Matcher<String> jsonEquals(String json) {
        return new JsonPartMatcher<String>("", readValue(json, "expectedJson"));
    }

    /**
     * Are the JSON structures equivalent?
     *
     * @param json
     * @return
     */
    public static Matcher<String> jsonEquals(Reader json) {
        return new JsonPartMatcher<String>("", readValue(json, "expectedJson"));
    }

    /**
     * Is the part of JSON structures equivalent?
     *
     * @param jsonNode
     * @return
     */
    public static Matcher<JsonNode> jsonPartEquals(String path, JsonNode jsonNode) {
        return new JsonPartMatcher<JsonNode>(path, jsonNode);
    }

    /**
     * Is the part of JSON structures equivalent?
     *
     * @param json
     * @return
     */
    public static Matcher<String> jsonPartEquals(String path, String json) {
        return new JsonPartMatcher<String>(path, readValue(json, "expectedJson"));
    }

    /**
     * Is the part of JSON structures equivalent?
     *
     * @param json
     * @return
     */
    public static Matcher<String> jsonPartEquals(String path, Reader json) {
        return new JsonPartMatcher<String>(path, readValue(json, "expectedJson"));
    }


    private static final class JsonPartMatcher<T> extends BaseMatcher<T> {
        private final JsonNode expected;
        private final String path;
        private String differences;

        JsonPartMatcher(String path, JsonNode expected) {
            this.expected = expected;
            this.path = path;
        }

        public boolean matches(Object item) {
            JsonNode jsonNode;
            if (item instanceof String) {
                jsonNode = readValue((String) item, "fullJson");
            } else if (item instanceof Reader) {
                jsonNode = readValue((Reader) item, "fullJson");
            } else if (item instanceof JsonNode) {
                jsonNode = (JsonNode) item;
            } else if (item == null) {
                jsonNode = null;
            } else {
                throw new IllegalArgumentException("Type " + item.getClass() + " is not supported");
            }
            Diff diff = new Diff(expected, jsonNode, path, JsonAssert.getIgnorePlaceholder());
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
