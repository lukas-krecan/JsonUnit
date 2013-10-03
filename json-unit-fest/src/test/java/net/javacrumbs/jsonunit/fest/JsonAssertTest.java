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
package net.javacrumbs.jsonunit.fest;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class JsonAssertTest {

    @Test
    public void testAssertString() {
        try {
            JsonAssert.assertThat("{\"test\":1}").isEqualTo("{\"test\":2}");
            fail("Exception expected");
        } catch(AssertionError e) {
            assertEquals("JSON documents have different values:\nDifferent value found in node \"test\". Expected 2, got 1.\n", e.getMessage());
        }
    }
}
