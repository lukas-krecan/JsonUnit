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
package net.javacrumbs.jsonunit.test.moshi;

import net.javacrumbs.jsonunit.JsonAssert;
import net.javacrumbs.jsonunit.test.base.AbstractJsonAssertTest;
import net.javacrumbs.jsonunit.test.base.JsonTestUtils;
import org.junit.Ignore;
import org.junit.Test;

import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static net.javacrumbs.jsonunit.JsonAssert.when;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_ARRAY_ITEMS;
import static net.javacrumbs.jsonunit.test.base.JsonTestUtils.failIfNoException;
import static org.junit.Assert.assertEquals;

public class MoshiJsonAssertTest extends AbstractJsonAssertTest {

    protected Object readValue(String value) {
        return JsonTestUtils.readByMoshi(value);
    }

    @Override
    @Ignore
    public void testDifferentNumericTypes() {
        // https://github.com/square/moshi/issues/192
    }

    @Override
    @Ignore
    public void testNotEqualWhenToleranceNotSet() {
        // https://github.com/square/moshi/issues/192
    }

    @Test
    @Override
    @Ignore
    public void testBinary() {
        // no support for binary
    }

    @Test
    public void shouldFailIfIfExpectedIsLongerAndOrderIsIgnoredAndTheItemsDoNotMatch() {
        try {
            assertJsonEquals("{\"test\":[5,6,7,8]}", "{\"test\":[3,2,1]}", when(IGNORING_EXTRA_ARRAY_ITEMS, IGNORING_ARRAY_ORDER));
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("JSON documents are different:\n" +
                "Array \"test\" has invalid length, expected: <at least 4> but was: <3>.\n" +
                "Array \"test\" has different content, expected: <[5.0,6.0,7.0,8.0]> but was: <[3.0,2.0,1.0]>. Missing values [5, 6, 7, 8]\n", e.getMessage());
        }
    }

    @Test
    public void arraysMatchShouldReportErrorCorrectlyWhenIgnoringExtraFieldsComplex() {
        try {
            assertJsonEquals("[[3],[2],[1]]", "[[1,2],[2,3],[2,4]]", when(IGNORING_ARRAY_ORDER, IGNORING_EXTRA_ARRAY_ITEMS));
        } catch (AssertionError e) {
            assertEquals("JSON documents are different:\n" +
                "Different value found when comparing expected array element [2] to actual element [2].\n" +
                "Array \"[2]\" has different content, expected: <[1.0]> but was: <[2.0,4.0]>. Missing values [1]\n", e.getMessage());
        }
    }

    @Test
    public void shouldFailIfOneValueIsMissing() {
        try {
            assertJsonEquals("{\"test\":[1,2,3,9]}", "{\"test\":[5,5,4,4,3,3,2,2,1,1]}", when(IGNORING_EXTRA_ARRAY_ITEMS, IGNORING_ARRAY_ORDER));
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("JSON documents are different:\n" +
                "Array \"test\" has different content, expected: <[1.0,2.0,3.0,9.0]> but was: <[5.0,5.0,4.0,4.0,3.0,3.0,2.0,2.0,1.0,1.0]>. Missing values [9]\n", e.getMessage());
        }
    }

    @Test
     public void shouldFailIfIfExpectedIsLongerAndOrderIsIgnored() {
         try {
             assertJsonEquals("{\"test\":[1,2,3,9]}", "{\"test\":[3,2,1]}", when(IGNORING_EXTRA_ARRAY_ITEMS, IGNORING_ARRAY_ORDER));
             failIfNoException();
         } catch (AssertionError e) {
             assertEquals("JSON documents are different:\n" +
                 "Array \"test\" has invalid length, expected: <at least 4> but was: <3>.\n" +
                 "Array \"test\" has different content, expected: <[1.0,2.0,3.0,9.0]> but was: <[3.0,2.0,1.0]>. Missing values [9]\n", e.getMessage());
         }
     }

    @Test
    public void shouldFailIfArrayContentIsDifferentMoreThaOneDifference() {
        JsonAssert.setOptions(IGNORING_ARRAY_ORDER);
        try {
            assertJsonEquals("{\"test\":[1,2,3]}", "{\"test\":[3,4,5]}");
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("JSON documents are different:\n" +
                "Array \"test\" has different content, expected: <[1.0,2.0,3.0]> but was: <[3.0,4.0,5.0]>. Missing values [1, 2], extra values [4, 5]\n", e.getMessage());
        }
    }
}
