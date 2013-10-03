/**
 * Copyright 2009-2012 the original author or authors.
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
package net.javacrumbs.jsonunit;

import org.junit.Test;

import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonPartEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class JsonAssertMatcherTest {

	@Test
	public void testEquals() {
		assertThat("{\"test\":1}", jsonEquals("{\n\"test\": 1\n}"));
		assertThat("{\"test\":1}", jsonPartEquals("test", "1"));
		assertThat("{\"test\":[1, 2, 3]}", jsonPartEquals("test[0]", "1"));
		assertThat("{\"foo\":\"bar\",\"test\": 2}", jsonEquals("{\n\"test\": 2,\n\"foo\":\"bar\"}"));
		assertThat("{}", jsonEquals("{}"));
	}


    @Test
    public void testDifferentValue() {
        try {
            assertThat("{\"test\":1}", jsonEquals("{\n\"test\": 2\n}"));
            fail("Exception expected");
        } catch (AssertionError e) {
            assertEquals("\nExpected: {\"test\":2}\n" +
                    "     but: JSON documents have different values:\n" +
                    "Different value found in node \"test\". Expected 2, got 1.\n", e.getMessage());
        }
    }

    @Test
    public void testDifferentStructure() {
        try {
            assertThat("{\"test\":1}", jsonEquals("{\n\"test2\": 2\n}"));
            fail("Exception expected");
        } catch (AssertionError e) {
            assertEquals("\nExpected: {\"test2\":2}\n" +
                    "     but: JSON documents have different structures:\n" +
                    "Different keys found in node \"\". Expected [test2], got [test]. Missing: \"test2\" Extra: \"test\"\n", e.getMessage());
        }
    }

    @Test
    public void testDifferentPartValue() {
        try {
            assertThat("{\"test\":1}", jsonPartEquals("test", "2"));
            fail("Exception expected");
        } catch (AssertionError e) {
            assertEquals("\nExpected: 2 in \"test\"\n" +
                    "     but: JSON documents have different values:\n" +
                    "Different value found in node \"test\". Expected 2, got 1.\n", e.getMessage());
        }
    }
}
