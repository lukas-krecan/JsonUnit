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

import net.javacrumbs.jsonunit.JsonAssert;
import net.javacrumbs.jsonunit.test.base.AbstractJsonAssertTest;
import net.javacrumbs.jsonunit.test.base.JsonTestUtils;
import net.javacrumbs.jsonunit.test.base.beans.Jackson1Bean;
import net.javacrumbs.jsonunit.test.base.beans.Jackson1IgnorepropertyBean;
import org.junit.Test;

import java.io.IOException;

import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static net.javacrumbs.jsonunit.JsonAssert.assertJsonStructureEquals;
import static net.javacrumbs.jsonunit.JsonAssert.when;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_VALUES;
import static net.javacrumbs.jsonunit.test.base.JsonTestUtils.failIfNoException;
import static net.javacrumbs.jsonunit.test.base.JsonTestUtils.readByGson;
import static net.javacrumbs.jsonunit.test.base.JsonTestUtils.readByJackson1;
import static net.javacrumbs.jsonunit.test.base.JsonTestUtils.readByJackson2;
import static net.javacrumbs.jsonunit.test.base.JsonTestUtils.readByJsonOrg;
import static org.junit.Assert.assertEquals;

public class AllJsonAssertTest extends AbstractJsonAssertTest {

    @Test
    public void testEqualsNode() throws IOException {
        assertJsonEquals(readByJackson1("{\"test\":1}"), readByJackson2("{\"test\": 1}"));
    }

    @Test
    public void testEqualsNodeGsonJackson() throws IOException {
        assertJsonEquals(readByGson("{\"test\":1}"), readByJackson2("{\"test\": 1}"));
    }

    @Test
    public void testEqualsNodeGson() throws IOException {
        assertJsonEquals(readByGson("{\"test\":1}"), readByGson("{\"test\": 1}"));
    }

    @Test
    public void testEqualsNodeJsonOrg() throws IOException {
        assertJsonEquals(readByJsonOrg("{\"test\":1}"), readByJsonOrg("{\"test\": 1}"));
    }

    @Test
    public void testEqualsNodeIgnore() throws IOException {
        assertJsonEquals(readByJackson1("{\"test\":\"${json-unit.ignore}\"}"), readByJackson1("{\"test\": 1}"));
    }

    @Test
    public void testEqualsNodeFailJackson1() throws IOException {
        try {
            assertJsonEquals(readByJackson1("{\"test\":1}"), "{\"test\": 2}");
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("JSON documents are different:\nDifferent value found in node \"test\". Expected 1, got 2.\n", e.getMessage());
        }
    }

    @Test
    public void testEqualsNodeFailGson() throws IOException {
        try {
            assertJsonEquals(readByGson("{\"test\":1}"), "{\"test\": 2}");
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("JSON documents are different:\nDifferent value found in node \"test\". Expected 1, got 2.\n", e.getMessage());
        }
    }

    @Test
    public void testEqualsNodeFailJsonOrg() throws IOException {
        try {
            assertJsonEquals(readByJsonOrg("{\"test\":1}"), "{\"test\": 2}");
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("JSON documents are different:\nDifferent value found in node \"test\". Expected 1, got 2.\n", e.getMessage());
        }
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

    @Test
    public void testAssertStructureEqualsDifferentValues() {
        assertJsonStructureEquals("{\"test\": 3}", "{\"test\": {\"inner\": 5}}");
    }

    @Test
    public void assertEqualsDifferentTypesFailsOnDifferentTypes() {
        try {
            assertJsonEquals("{\"test\": 3}", "{\"test\": {\"inner\": 5}}", when(IGNORING_VALUES));
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("JSON documents are different:\nDifferent value found in node \"test\". Expected '3', got '{\"inner\":5}'.\n", e.getMessage());
        }
    }

    @Test
    public void assertEqualsDifferentTypesPassesIfOnlyValuesDiffer() {
        assertJsonEquals("{\"test\": {\"inner\": 3}}", "{\"test\": {\"inner\": 5}}", when(IGNORING_VALUES));
    }

    @Test
    public void testEqualsNodeStringFail() throws IOException {
        try {
            assertJsonEquals("{\"test\":\"a\"}", readByJackson2("{\"test\": \"b\"}"));
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("JSON documents are different:\nDifferent value found in node \"test\". Expected \"a\", got \"b\".\n", e.getMessage());
        }
    }

    @Test
    public void testStructureEquals() {
        JsonAssert.assertJsonStructureEquals("{\"test\": 123}", "{\"test\": 412}");
    }

    @Test
    public void testRegex() {
        assertJsonEquals("{\"test\": \"${json-unit.regex}[A-Z]+\"}", "{\"test\": \"ABCD\"}");
    }

    @Test
    public void regexShouldFail() {
        try {
            assertJsonEquals("{\"test\": \"${json-unit.regex}[A-Z]+\"}", "{\"test\": \"123\"}");
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("JSON documents are different:\n" +
                "Different value found in node \"test\". Pattern \"[A-Z]+\" did not match \"123\".\n", e.getMessage());
        }
    }

    @Test
    public void regexShouldFailOnNullGracefully() {
        try {
            assertJsonEquals("{\"test\": \"${json-unit.regex}[A-Z]+\"}", "{\"test\": null}");
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("JSON documents are different:\n" +
                "Different value found in node \"test\". Expected '\"${json-unit.regex}[A-Z]+\"', got 'null'.\n", e.getMessage());
        }
    }

    @Test
    public void regexShouldFailOnNumberGracefully() {
        try {
            assertJsonEquals("{\"test\": \"${json-unit.regex}[A-Z]+\"}", "{\"test\": 123}");
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("JSON documents are different:\n" +
                "Different value found in node \"test\". Expected '\"${json-unit.regex}[A-Z]+\"', got '123'.\n", e.getMessage());
        }
    }

    @Test
    public void regexShouldFailOnNonexistingGracefully() {
        try {
            assertJsonEquals("{\"test\": \"${json-unit.regex}[A-Z]+\"}", "{\"test2\": 123}");
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("JSON documents are different:\n" +
                "Different keys found in node \"\". Expected [test], got [test2]. Missing: \"test\" Extra: \"test2\"\n", e.getMessage());
        }
    }

    @Test
    public void shouldSerializeBasedOnAnnotation() {
        assertJsonEquals("{\"bean\": {\"property\": \"value\"}}", new Jackson1Bean("value"));
    }

    @Test
    public void shouldSerializeBasedOnMethodAnnotation() {
        assertJsonEquals("{\"property\": \"value\"}", new Jackson1IgnorepropertyBean("value"));
    }

    protected Object readValue(String value) {
        return JsonTestUtils.readByJackson1(value);
    }
}
