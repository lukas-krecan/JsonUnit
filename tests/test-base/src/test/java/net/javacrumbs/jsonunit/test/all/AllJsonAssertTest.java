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
import static net.javacrumbs.jsonunit.test.base.JsonTestUtils.readByGson;
import static net.javacrumbs.jsonunit.test.base.JsonTestUtils.readByJackson1;
import static net.javacrumbs.jsonunit.test.base.JsonTestUtils.readByJackson2;
import static net.javacrumbs.jsonunit.test.base.JsonTestUtils.readByJsonOrg;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        assertThatThrownBy(() -> assertJsonEquals(readByJackson1("{\"test\":1}"), "{\"test\": 2}"))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"test\", expected: <1> but was: <2>.\n");
    }

    @Test
    public void testEqualsNodeFailGson() throws IOException {
        assertThatThrownBy(() -> assertJsonEquals(readByGson("{\"test\":1}"), "{\"test\": 2}"))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"test\", expected: <1> but was: <2>.\n");
    }

    @Test
    public void testEqualsNodeFailJsonOrg() throws IOException {
        assertThatThrownBy(() -> assertJsonEquals(readByJsonOrg("{\"test\":1}"), "{\"test\": 2}"))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"test\", expected: <1> but was: <2>.\n");
    }

    @Test
    public void testEqualsNodeFailJsonOrgArray() throws IOException {
        assertThatThrownBy(() -> assertJsonEquals(readByJsonOrg("[1, 2]"), readByJsonOrg("[1, 2, 3]")))
            .hasMessage("JSON documents are different:\nArray \"\" has different length, expected: <2> but was: <3>.\n" +
                "Array \"\" has different content, expected: <[1,2]> but was: <[1,2,3]>. Extra values [3]\n");
    }

    @Test
    public void testAssertStructureEqualsDifferentValues() {
        assertJsonStructureEquals("{\"test\": 3}", "{\"test\": {\"inner\": 5}}");
    }

    @Test
    public void assertEqualsDifferentTypesFailsOnDifferentTypes() {
        assertThatThrownBy(() -> assertJsonEquals("{\"test\": 3}", "{\"test\": {\"inner\": 5}}", when(IGNORING_VALUES)))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"test\", expected: <3> but was: <{\"inner\":5}>.\n");
    }

    @Test
    public void assertEqualsDifferentTypesPassesIfOnlyValuesDiffer() {
        assertJsonEquals("{\"test\": {\"inner\": 3}}", "{\"test\": {\"inner\": 5}}", when(IGNORING_VALUES));
    }

    @Test
    public void testEqualsNodeStringFail() throws IOException {
        assertThatThrownBy(() -> assertJsonEquals("{\"test\":\"a\"}", readByJackson2("{\"test\": \"b\"}")))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"test\", expected: <\"a\"> but was: <\"b\">.\n");
    }

    @Test
    public void testEqualsExtraNodeStringFail() {
        assertThatThrownBy(() -> assertJsonEquals("{\"test\":\"a\"}", "{\"test\": \"a\", \"test2\": \"aa\"}"))
            .hasMessage("JSON documents are different:\n" +
                "Different keys found in node \"\", expected: <[test]> but was: <[test, test2]>.  Extra: \"test2\"\n");
    }

    @Test
    public void testEqualsMissedNodeStringFail() {
        assertThatThrownBy(() -> assertJsonEquals("{\"test\": \"a\", \"test2\": \"aa\"}", "{\"test\":\"a\"}"))
            .hasMessage("JSON documents are different:\n" +
                "Different keys found in node \"\", expected: <[test, test2]> but was: <[test]>. Missing: \"test2\" \n");
    }

    @Test
    public void testStructureEquals() {
        JsonAssert.assertJsonStructureEquals("{\"test\": 123}", "{\"test\": 412}");
    }

    @Test
    public void shouldSerializeBasedOnAnnotation() {
        assertJsonEquals("{\"bean\": {\"property\": \"value\"}}", new Jackson1Bean("value"));
    }

    @Test
    public void shouldSerializeBasedOnMethodAnnotation() {
        assertJsonEquals("{\"property\": \"value\"}", new Jackson1IgnorepropertyBean("value"));
    }

    @Test
    public void dotInPathFailure() {
        assertThatThrownBy(() -> assertJsonEquals(
            "{\"root\":{\"test\":1, \".ignored\": 1}}",
                "{\"root\":{\"test\":1, \".ignored\": 2}}"
        ))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"root..ignored\", expected: <1> but was: <2>.\n");
    }

    @Test
    public void dotInPathMiddleFailure() {
        assertThatThrownBy(() -> assertJsonEquals(
            "{\"root\":{\"test\":1, \"igno.red\": 1}}",
            "{\"root\":{\"test\":1, \"igno.red\": 2}}"
        ))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"root.igno.red\", expected: <1> but was: <2>.\n");
    }

    @Test
    public void dotInPath() {
        assertJsonEquals(
        "{\"root\":{\"test\":1, \".ignored\": 1}}",
            "{\"root\":{\"test\":1, \".ignored\": 2}}",
        JsonAssert.whenIgnoringPaths("root..ignored")
        );
    }

    @Test
    public void dotInPathMiddle() {
        assertJsonEquals(
        "{\"root\":{\"test\":1, \"igno.red\": 1}}",
            "{\"root\":{\"test\":1, \"igno.red\": 2}}",
        JsonAssert.whenIgnoringPaths("root.igno.red")
        );
    }

    protected Object readValue(String value) {
        return JsonTestUtils.readByJackson1(value);
    }
}
