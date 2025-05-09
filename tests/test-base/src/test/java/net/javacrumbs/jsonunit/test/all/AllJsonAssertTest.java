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
package net.javacrumbs.jsonunit.test.all;

import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static net.javacrumbs.jsonunit.JsonAssert.when;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_VALUES;
import static net.javacrumbs.jsonunit.test.base.JsonTestUtils.readByGson;
import static net.javacrumbs.jsonunit.test.base.JsonTestUtils.readByJackson2;
import static net.javacrumbs.jsonunit.test.base.JsonTestUtils.readByJackson3;
import static net.javacrumbs.jsonunit.test.base.JsonTestUtils.readByJsonOrg;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import net.javacrumbs.jsonunit.JsonAssert;
import net.javacrumbs.jsonunit.test.base.AbstractJsonAssertTest;
import net.javacrumbs.jsonunit.test.base.JsonTestUtils;
import net.javacrumbs.jsonunit.test.base.beans.Jackson2Bean;
import net.javacrumbs.jsonunit.test.base.beans.Jackson2IgnorePropertyBean;
import org.junit.jupiter.api.Test;

class AllJsonAssertTest extends AbstractJsonAssertTest {

    @Test
    void testEqualsNode() {
        assertJsonEquals(readByJsonOrg("{\"test\":1}"), readByJackson2("{\"test\": 1}"));
    }

    @Test
    void testEqualsNodeGsonJackson() {
        assertJsonEquals(readByGson("{\"test\":1}"), readByJackson2("{\"test\": 1}"));
    }

    @Test
    void testEqualsNodeGson() {
        assertJsonEquals(readByGson("{\"test\":1}"), readByGson("{\"test\": 1}"));
    }

    @Test
    void testEqualsNodeJsonOrg() {
        assertJsonEquals(readByJsonOrg("{\"test\":1}"), readByJsonOrg("{\"test\": 1}"));
    }

    @Test
    void testEqualsNodeIgnore() {
        assertJsonEquals(readByJackson2("{\"test\":\"${json-unit.ignore}\"}"), readByJackson2("{\"test\": 1}"));
    }

    @Test
    void testEqualsNodeFailJackson2() {
        assertThatThrownBy(() -> assertJsonEquals(readByJackson2("{\"test\":1}"), "{\"test\": 2}"))
                .hasMessage(
                        "JSON documents are different:\nDifferent value found in node \"test\", expected: <1> but was: <2>.\n");
    }

    @Test
    void testEqualsNodeFailJackson3() {
        assertThatThrownBy(() -> assertJsonEquals(readByJackson3("{\"test\":1}"), "{\"test\": 2}"))
                .hasMessage(
                        "JSON documents are different:\nDifferent value found in node \"test\", expected: <1> but was: <2>.\n");
    }

    @Test
    void testEqualsNodeFailGson() {
        assertThatThrownBy(() -> assertJsonEquals(readByGson("{\"test\":1}"), "{\"test\": 2}"))
                .hasMessage(
                        "JSON documents are different:\nDifferent value found in node \"test\", expected: <1> but was: <2>.\n");
    }

    @Test
    void testEqualsNodeFailJsonOrg() {
        assertThatThrownBy(() -> assertJsonEquals(readByJsonOrg("{\"test\":1}"), "{\"test\": 2}"))
                .hasMessage(
                        "JSON documents are different:\nDifferent value found in node \"test\", expected: <1> but was: <2>.\n");
    }

    @Test
    void testEqualsNodeFailJsonOrgArray() {
        assertThatThrownBy(() -> assertJsonEquals(readByJsonOrg("[1, 2]"), readByJsonOrg("[1, 2, 3]")))
                .hasMessage(
                        """
                JSON documents are different:
                Array "" has different length, expected: <2> but was: <3>.
                Array "" has different content. Extra values: [3], expected: <[1,2]> but was: <[1,2,3]>
                """);
    }

    @Test
    void assertEqualsDifferentTypesFailsOnDifferentTypes() {
        assertThatThrownBy(() -> assertJsonEquals("{\"test\": 3}", "{\"test\": {\"inner\": 5}}", when(IGNORING_VALUES)))
                .hasMessage(
                        "JSON documents are different:\nDifferent value found in node \"test\", expected: <3> but was: <{\"inner\":5}>.\n");
    }

    @Test
    void assertEqualsDifferentTypesPassesIfOnlyValuesDiffer() {
        assertJsonEquals("{\"test\": {\"inner\": 3}}", "{\"test\": {\"inner\": 5}}", when(IGNORING_VALUES));
    }

    @Test
    void testEqualsNodeStringFail() {
        assertThatThrownBy(() -> assertJsonEquals("{\"test\":\"a\"}", readByJackson2("{\"test\": \"b\"}")))
                .hasMessage(
                        "JSON documents are different:\nDifferent value found in node \"test\", expected: <\"a\"> but was: <\"b\">.\n");
    }

    @Test
    void testEqualsExtraNodeStringFail() {
        assertThatThrownBy(() -> assertJsonEquals("{\"test\":\"a\"}", "{\"test\": \"a\", \"test2\": \"aa\"}"))
                .hasMessage(
                        """
                JSON documents are different:
                Different keys found in node "", extra: "test2", expected: <{"test":"a"}> but was: <{"test":"a","test2":"aa"}>
                """);
    }

    @Test
    void testEqualsMissedNodeStringFail() {
        assertThatThrownBy(() -> assertJsonEquals("{\"test\": \"a\", \"test2\": \"aa\"}", "{\"test\":\"a\"}"))
                .hasMessage(
                        """
                JSON documents are different:
                Different keys found in node "", missing: "test2", expected: <{"test":"a","test2":"aa"}> but was: <{"test":"a"}>
                """);
    }

    @Test
    void shouldSerializeBasedOnAnnotation() {
        assertJsonEquals("{\"bean\": {\"property\": \"value\"}}", new Jackson2Bean("value"));
    }

    @Test
    void shouldSerializeBasedOnMethodAnnotation() {
        assertJsonEquals("{\"property\": \"value\"}", new Jackson2IgnorePropertyBean("value"));
    }

    @Test
    void dotInPathFailure() {
        assertThatThrownBy(() -> assertJsonEquals(
                        "{\"root\":{\"test\":1, \".ignored\": 1}}", "{\"root\":{\"test\":1, \".ignored\": 2}}"))
                .hasMessage(
                        "JSON documents are different:\nDifferent value found in node \"root..ignored\", expected: <1> but was: <2>.\n");
    }

    @Test
    void dotInPathMiddleFailure() {
        assertThatThrownBy(() -> assertJsonEquals(
                        "{\"root\":{\"test\":1, \"igno.red\": 1}}", "{\"root\":{\"test\":1, \"igno.red\": 2}}"))
                .hasMessage(
                        "JSON documents are different:\nDifferent value found in node \"root.igno.red\", expected: <1> but was: <2>.\n");
    }

    @Test
    void dotInPath() {
        assertJsonEquals(
                "{\"root\":{\"test\":1, \".ignored\": 1}}",
                "{\"root\":{\"test\":1, \".ignored\": 2}}",
                JsonAssert.whenIgnoringPaths("root..ignored"));
    }

    @Test
    void dotInPathMiddle() {
        assertJsonEquals(
                "{\"root\":{\"test\":1, \"igno.red\": 1}}",
                "{\"root\":{\"test\":1, \"igno.red\": 2}}",
                JsonAssert.whenIgnoringPaths("root.igno.red"));
    }

    @Override
    protected Object readValue(String value) {
        return JsonTestUtils.readByJackson2(value);
    }
}
