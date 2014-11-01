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
package net.javacrumbs.jsonunit.test.jackson2;

import net.javacrumbs.jsonunit.test.base.JsonAssertTest;
import org.junit.Test;

import java.io.IOException;

import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static net.javacrumbs.jsonunit.test.base.JsonUtils.readByJackson1;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class Jackson1JsonAssertTest extends JsonAssertTest {

    @Test
    public void testEqualsNode() throws IOException {
        assertJsonEquals(readByJackson1("{\"test\":1}"), readByJackson1("{\"test\": 1}"));
    }

    @Test
    public void testEqualsNodeIgnore() throws IOException {
        assertJsonEquals(readByJackson1("{\"test\":\"${json-unit.ignore}\"}"), readByJackson1("{\"test\": 1}"));
    }

    @Test
    public void testEqualsNodeFail() throws IOException {
        try {
            assertJsonEquals(readByJackson1("{\"test\":1}"), "{\"test\": 2}");
            fail("Exception expected");
        } catch (AssertionError e) {
            assertEquals("JSON documents are different:\nDifferent value found in node \"test\". Expected 1, got 2.\n", e.getMessage());
        }
    }

    @Test
    public void testEqualsNodeStringFail() throws IOException {
        try {
            assertJsonEquals("{\"test\":\"a\"}", readByJackson1("{\"test\": \"b\"}"));
            fail("Exception expected");
        } catch (AssertionError e) {
            assertEquals("JSON documents are different:\nDifferent value found in node \"test\". Expected \"a\", got \"b\".\n", e.getMessage());
        }
    }
}
