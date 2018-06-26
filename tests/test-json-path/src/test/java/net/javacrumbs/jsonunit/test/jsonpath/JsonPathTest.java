/**
 * Copyright 2009-2018 the original author or authors.
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
package net.javacrumbs.jsonunit.test.jsonpath;

import net.javacrumbs.jsonunit.core.Option;
import org.junit.Test;

import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;
import static net.javacrumbs.jsonunit.jsonpath.JsonPathAdapter.inPath;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class JsonPathTest {
    @Test
    public void shouldBeAbleToUseSimpleValues() {
        assertThatJson(inPath(json, "$.store.book[*].author"))
            .isEqualTo("['Nigel Rees', 'Evelyn Waugh', 'Herman Melville', 'J. R. R. Tolkien']");

    }

    @Test
    public void shouldBeAbleToUseSimpleValuesAndIgnoreArrayOrder() {
        assertThatJson(inPath(json, "$.store.book[*].author"))
            .when(Option.IGNORING_ARRAY_ORDER)
            .isEqualTo("['J. R. R. Tolkien', 'Nigel Rees', 'Evelyn Waugh', 'Herman Melville']");

    }

    @Test
    public void shouldBeAbleToUseSimpleValuesFailure() {
        assertThatThrownBy(() -> assertThatJson(inPath(json, "$.store.book[*].author"))
            .isEqualTo("['Nigel Rees', 'Evelyn Waugh', 'Herman Melville', 'Arthur C. Clark']"))
            .hasMessage("JSON documents are different:\n" +
                "Different value found in node \"$.store.book[*].author[3]\", expected: <\"Arthur C. Clark\"> but was: <\"J. R. R. Tolkien\">.\n");
    }

    @Test
    public void shouldBeAbleToUseObjects() {
        assertThatThrownBy(() -> assertThatJson(inPath(json, "$.store.book[0]"))
            .isEqualTo(
                "            {\n" +
                    "                \"category\": \"reference\",\n" +
                    "                \"author\": \"Nigel Rees\",\n" +
                    "                \"title\": \"Sayings of the Century\",\n" +
                    "                \"price\": 8.96\n" +
                    "            }"))
            .hasMessage("JSON documents are different:\n" +
                "Different value found in node \"$.store.book[0].price\", expected: <8.96> but was: <8.95>.\n");
    }

    @Test
    public void shouldIgnorePath() {
        assertThatJson(inPath(json, "$.store.book[0]"))
            .whenIgnoringPaths("$.store.book[*].price")
            .isEqualTo(
                "            {\n" +
                "                \"category\": \"reference\",\n" +
                "                \"author\": \"Nigel Rees\",\n" +
                "                \"title\": \"Sayings of the Century\",\n" +
                "                \"price\": 8.96\n" +
                "            }");
    }


    private static final String json = "{\n" +
        "    \"store\": {\n" +
        "        \"book\": [\n" +
        "            {\n" +
        "                \"category\": \"reference\",\n" +
        "                \"author\": \"Nigel Rees\",\n" +
        "                \"title\": \"Sayings of the Century\",\n" +
        "                \"price\": 8.95\n" +
        "            },\n" +
        "            {\n" +
        "                \"category\": \"fiction\",\n" +
        "                \"author\": \"Evelyn Waugh\",\n" +
        "                \"title\": \"Sword of Honour\",\n" +
        "                \"price\": 12.99\n" +
        "            },\n" +
        "            {\n" +
        "                \"category\": \"fiction\",\n" +
        "                \"author\": \"Herman Melville\",\n" +
        "                \"title\": \"Moby Dick\",\n" +
        "                \"isbn\": \"0-553-21311-3\",\n" +
        "                \"price\": 8.99\n" +
        "            },\n" +
        "            {\n" +
        "                \"category\": \"fiction\",\n" +
        "                \"author\": \"J. R. R. Tolkien\",\n" +
        "                \"title\": \"The Lord of the Rings\",\n" +
        "                \"isbn\": \"0-395-19395-8\",\n" +
        "                \"price\": 22.99\n" +
        "            }\n" +
        "        ],\n" +
        "        \"bicycle\": {\n" +
        "            \"color\": \"red\",\n" +
        "            \"price\": 19.95\n" +
        "        }\n" +
        "    },\n" +
        "    \"expensive\": 10\n" +
        "}";
}
