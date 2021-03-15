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

import net.javacrumbs.jsonunit.test.base.AbstractJsonMatchersTest;
import net.javacrumbs.jsonunit.test.base.JsonTestUtils;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static net.javacrumbs.jsonunit.core.util.ResourceUtils.resource;
import static net.javacrumbs.jsonunit.test.base.JsonTestUtils.readByJackson2;
import static net.javacrumbs.jsonunit.test.base.JsonTestUtils.readByJsonOrg;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

class AllJsonMatchersTest extends AbstractJsonMatchersTest {
    @Test
    void testJsonNodeJsonOrg() throws IOException {
        assertThat(readByJsonOrg("{\"test\":1}"), jsonEquals("{\"test\":1}"));
    }

    @Test
    void testJsonNodeJackson2() throws IOException {
        assertThat(readByJackson2("{\"test\":1}"), jsonEquals("{\"test\":1}"));
    }

    @Test
    void shouldCompareJSONArrays() {
        assertThat(readByJsonOrg("[{\"a\":1}, {\"a\":2}, {\"a\":2}]"), jsonEquals(readByJsonOrg("[{\"a\":1}, {\"a\":2}, {\"a\":2}]")));
    }

    @Test
    void testEqualsResource() throws Exception {
        assertThat("{\"test\":1}", jsonEquals(resource("test.json")));
    }

    @Test
    void testEqualsUnicodeResource() throws Exception {
        assertThat("{\"face\":\"\uD83D\uDE10\"}", jsonEquals(resource("unicode.json")));
    }

    @Test
    void testEqualsWithEqualItemButDifferentInstanceForDescribeMismatch() {
        Matcher<Object> matcher = jsonEquals("{\"test\":1}");
        String differentJson = "{\"test\":false}";
        assertThat(matcher.matches("{\"test\":false}"), is(false));

        String clonedJson = String.copyValueOf(differentJson.toCharArray());
        StringDescription description = new StringDescription();
        matcher.describeMismatch(clonedJson, description);
        assertThat(description.toString(), containsString("Different value found in node"));
    }

    @Override
    protected Object readValue(String value) {
            return JsonTestUtils.readByJackson2(value);
        }
}
