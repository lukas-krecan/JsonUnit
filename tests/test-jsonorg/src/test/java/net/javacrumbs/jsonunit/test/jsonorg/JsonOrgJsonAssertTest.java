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
package net.javacrumbs.jsonunit.test.jsonorg;

import net.javacrumbs.jsonunit.test.base.AbstractJsonAssertTest;
import net.javacrumbs.jsonunit.test.base.JsonTestUtils;
import org.junit.Ignore;
import org.junit.Test;

import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static net.javacrumbs.jsonunit.test.base.JsonTestUtils.readByJsonOrg;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class JsonOrgJsonAssertTest extends AbstractJsonAssertTest {
    protected Object readValue(String value) {
        return JsonTestUtils.readByJsonOrg(value);
    }

    @Test
    public void shouldParseExpectedValueLeniently() {
        // JSONObject lenient parsing works differently
    }

    @Test
    public void shouldFailIfQuotationMarksMissingOnActualKeys() {
        // it's lenient by default
    }

    @Test
    public void testEqualsNodeFailJsonOrgArray() {
        assertThatThrownBy(() -> assertJsonEquals(readByJsonOrg("[1, 2]"), readByJsonOrg("[1, 2, 3]")))
            .hasMessage("JSON documents are different:\nArray \"\" has different length, expected: <2> but was: <3>.\n" +
                "Array \"\" has different content, expected: <[1,2]> but was: <[1,2,3]>. Extra values [3]\n");
    }

    @Test
    @Override
    @Ignore
    public void testBinary() {
        // no support for binary
    }

}
