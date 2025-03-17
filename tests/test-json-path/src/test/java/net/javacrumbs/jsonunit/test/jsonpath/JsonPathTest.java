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
package net.javacrumbs.jsonunit.test.jsonpath;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.jsonpath.JsonPathAdapter.inPath;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import net.javacrumbs.jsonunit.core.Option;
import org.junit.jupiter.api.Test;

class JsonPathTest {
    @Test
    void shouldBeAbleToUseSimpleValues() {
        assertThatJson(inPath(json, "$.store.book[*].author"))
                .isEqualTo("['Nigel Rees', 'Evelyn Waugh', 'Herman Melville', 'J. R. R. Tolkien']");
    }

    @Test
    void shouldBeAbleToUseSimpleValuesAndIgnoreArrayOrder() {
        assertThatJson(inPath(json, "$.store.book[*].author"))
                .when(Option.IGNORING_ARRAY_ORDER)
                .isEqualTo("['J. R. R. Tolkien', 'Nigel Rees', 'Evelyn Waugh', 'Herman Melville']");
    }

    @Test
    void shouldBeAbleToUseSimpleValuesFailure() {
        assertThatThrownBy(() -> assertThatJson(inPath(json, "$.store.book[*].author"))
                        .isEqualTo("['Nigel Rees', 'Evelyn Waugh', 'Herman Melville', 'Arthur C. Clark']"))
                .hasMessage(
                        """
                    JSON documents are different:
                    Different value found in node "$.store.book[*].author[3]", expected: <"Arthur C. Clark"> but was: <"J. R. R. Tolkien">.
                    """);
    }

    @Test
    void shouldBeAbleToUseObjects() {
        assertThatThrownBy(
                        () -> assertThatJson(inPath(json, "$.store.book[0]"))
                                .isEqualTo(
                                        """
                                    {
                                        "category": "reference",
                                        "author": "Nigel Rees",
                                        "title": "Sayings of the Century",
                                        "price": 8.96
                                    }\
                        """))
                .hasMessage(
                        """
                    JSON documents are different:
                    Different value found in node "$.store.book[0].price", expected: <8.96> but was: <8.95>.
                    """);
    }

    @Test
    void shouldIgnorePath() {
        assertThatJson(json)
                .whenIgnoringPaths("$.store.book[*].price")
                .inPath("$.store.book[0]")
                .isEqualTo(
                        """
                                {
                                    "category": "reference",
                                    "author": "Nigel Rees",
                                    "title": "Sayings of the Century",
                                    "price": 8.96
                                }\
                    """);
    }

    private static final String json =
            """
            {
                "store": {
                    "book": [
                        {
                            "category": "reference",
                            "author": "Nigel Rees",
                            "title": "Sayings of the Century",
                            "price": 8.95
                        },
                        {
                            "category": "fiction",
                            "author": "Evelyn Waugh",
                            "title": "Sword of Honour",
                            "price": 12.99
                        },
                        {
                            "category": "fiction",
                            "author": "Herman Melville",
                            "title": "Moby Dick",
                            "isbn": "0-553-21311-3",
                            "price": 8.99
                        },
                        {
                            "category": "fiction",
                            "author": "J. R. R. Tolkien",
                            "title": "The Lord of the Rings",
                            "isbn": "0-395-19395-8",
                            "price": 22.99
                        }
                    ],
                    "bicycle": {
                        "color": "red",
                        "price": 19.95
                    }
                },
                "expensive": 10
            }""";
}
