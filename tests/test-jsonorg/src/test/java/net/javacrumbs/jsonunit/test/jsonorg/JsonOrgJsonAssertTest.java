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
package net.javacrumbs.jsonunit.test.jsonorg;

import net.javacrumbs.jsonunit.test.base.AbstractJsonAssertTest;
import net.javacrumbs.jsonunit.test.base.JsonTestUtils;
import org.junit.Test;

import java.io.IOException;

import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static net.javacrumbs.jsonunit.test.base.JsonTestUtils.failIfNoException;
import static net.javacrumbs.jsonunit.test.base.JsonTestUtils.readByJsonOrg;
import static org.junit.Assert.assertEquals;

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
    public void testEqualsNodeFailJsonOrgArray() throws IOException {
        try {
            assertJsonEquals(readByJsonOrg("[1, 2]"), readByJsonOrg("[1, 2, 3]"));
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("JSON documents are different:\nArray \"\" has different length. Expected 2, got 3.\n", e.getMessage());
        }
    }
}
