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
package net.javacrumbs.jsonunit.test.jsonorg;

import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static net.javacrumbs.jsonunit.test.base.JsonTestUtils.readByJsonOrg;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import net.javacrumbs.jsonunit.test.base.AbstractJsonAssertTest;
import net.javacrumbs.jsonunit.test.base.JsonTestUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class JsonOrgJsonAssertTest extends AbstractJsonAssertTest {
    @Override
    protected Object readValue(String value) {
        return JsonTestUtils.readByJsonOrg(value);
    }

    @Override
    @Test
    public void shouldParseExpectedValueLeniently() {
        // JSONObject lenient parsing works differently
    }

    @Override
    @Test
    public void shouldFailIfQuotationMarksMissingOnActualKeys() {
        // it's lenient by default
    }

    @Test
    public void testEqualsNodeFailJsonOrgArray() {
        assertThatThrownBy(() -> assertJsonEquals(readByJsonOrg("[1, 2]"), readByJsonOrg("[1, 2, 3]")))
                .hasMessage(
                        """
                JSON documents are different:
                Array "" has different length, expected: <2> but was: <3>.
                Array "" has different content. Extra values: [3], expected: <[1,2]> but was: <[1,2,3]>
                """);
    }

    @Test
    @Override
    @Disabled
    public void testBinary() {
        // no support for binary
    }
}
