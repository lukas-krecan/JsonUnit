/**
 * Copyright 2009-2017 the original author or authors.
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

import net.javacrumbs.jsonunit.test.base.AbstractJsonFluentAssertTest;
import net.javacrumbs.jsonunit.test.base.JsonTestUtils;
import org.junit.Test;

import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;
import static net.javacrumbs.jsonunit.test.base.JsonTestUtils.readByGson;
import static net.javacrumbs.jsonunit.test.base.JsonTestUtils.readByJackson2;
import static net.javacrumbs.jsonunit.test.base.JsonTestUtils.readByJsonOrg;

public class AllJsonFluentAssertTest extends AbstractJsonFluentAssertTest {
    @Test
    public void testMixedGsonAndJackson() {
        assertThatJson(readByGson("{\"test\":1}")).isEqualTo(readByJackson2("{\"test\": 1}"));
    }

    @Test
    public void testMixedGsonAndJsonOrg() {
        assertThatJson(readByGson("{\"test\":1}")).isEqualTo(readByJsonOrg("{\"test\": 1}"));
    }

    protected Object readValue(String value) {
        return JsonTestUtils.readByJackson1(value);
    }
}