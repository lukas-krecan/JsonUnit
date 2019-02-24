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
package net.javacrumbs.jsonunit.test.jackson2;

import net.javacrumbs.jsonunit.test.base.AbstractAssertJTest;
import org.junit.jupiter.api.Test;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.test.base.JsonTestUtils.readByJackson2;

public class Jackson2AssertJTest extends AbstractAssertJTest {


    @Test
    void arrayContainsWithObjects() {
        assertThatJson("{\"a\":[{\"b\": 1}, {\"c\": 1}]}")
            .inPath("$.a")
            .isArray()
            .containsExactlyInAnyOrder(
                json(readValue("{\"c\": 1}")),
                json(readValue("{\"b\": 1}"))
            );
    }

    @Override
    protected Object readValue(String value) {
            return readByJackson2(value);
        }
}
