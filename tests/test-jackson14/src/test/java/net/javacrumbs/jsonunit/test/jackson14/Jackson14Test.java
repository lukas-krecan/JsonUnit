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
package net.javacrumbs.jsonunit.test.jackson14;

import org.junit.Test;

import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class Jackson14Test {
    @Test
    public void shouldIgnoreIgnoredProperty() {
        assertJsonEquals("{\"value1\":\"value1\"}", new Jackson14Bean());
    }

    @Test
    public void testObjectName() {
        try {
            assertJsonEquals("{\"test\":1}", "{\n\"foo\": 1\n}");
            fail("Exception expected");
        } catch (AssertionError e) {
            assertEquals("JSON documents are different:\nDifferent keys found in node \"\". Expected [test], got [foo]. Missing: \"test\" Extra: \"foo\"\n", e.getMessage());
        }
    }
}
