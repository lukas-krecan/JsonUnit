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

import net.javacrumbs.jsonunit.test.base.AbstractJsonMatchersTest;
import net.javacrumbs.jsonunit.test.base.JsonTestUtils;
import org.junit.Test;

import java.io.IOException;

import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static net.javacrumbs.jsonunit.core.util.ResourceUtils.resource;
import static net.javacrumbs.jsonunit.test.base.JsonTestUtils.readByJackson1;
import static net.javacrumbs.jsonunit.test.base.JsonTestUtils.readByJackson2;
import static net.javacrumbs.jsonunit.test.base.JsonTestUtils.readByJsonOrg;
import static org.junit.Assert.assertThat;

public class AllJsonMatchersTest extends AbstractJsonMatchersTest {
    @Test
    public void testJsonNodeJackson1() throws IOException {
        assertThat(readByJackson1("{\"test\":1}"), jsonEquals("{\"test\":1}"));
    }

    @Test
    public void testJsonNodeJackson2() throws IOException {
        assertThat(readByJackson2("{\"test\":1}"), jsonEquals("{\"test\":1}"));
    }

    @Test
    public void shouldCompareJSONArrays() {
        assertThat(readByJsonOrg("[{\"a\":1}, {\"a\":2}, {\"a\":2}]"), jsonEquals(readByJsonOrg("[{\"a\":1}, {\"a\":2}, {\"a\":2}]")));
    }

    @Test
    public void testEqualsResource() throws Exception {
        assertThat("{\"test\":1}", jsonEquals(resource("test.json")));
    }

    protected Object readValue(String value) {
            return JsonTestUtils.readByJackson1(value);
        }
}
