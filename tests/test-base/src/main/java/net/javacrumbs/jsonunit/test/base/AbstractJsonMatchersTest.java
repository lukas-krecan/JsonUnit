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
package net.javacrumbs.jsonunit.test.base;

import net.javacrumbs.jsonunit.JsonAssert;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static java.math.BigDecimal.valueOf;
import static java.util.Arrays.asList;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonNodeAbsent;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonNodePresent;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonPartEquals;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonPartMatches;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonStringEquals;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonStringPartEquals;
import static net.javacrumbs.jsonunit.core.ConfigurationWhen.path;
import static net.javacrumbs.jsonunit.core.ConfigurationWhen.then;
import static net.javacrumbs.jsonunit.core.ConfigurationWhen.thenNot;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_VALUES;
import static net.javacrumbs.jsonunit.core.Option.TREATING_NULL_AS_ABSENT;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.jsonSource;
import static net.javacrumbs.jsonunit.core.util.ResourceUtils.resource;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public abstract class AbstractJsonMatchersTest {
    @AfterEach
    void reset() {
        JsonAssert.setTolerance(null);
        JsonAssert.resetOptions();
    }

    @Test
    void testEquals() {
        assertThat("{\"test\":1}", jsonEquals("{\n\"test\": 1\n}"));
        assertThat("{\"test\":1}", not(jsonEquals("{\n\"test\": 2\n}")));
        assertThat("{\"test\":1}", jsonPartEquals("test", "1"));
        assertThat("{\"test\":1}", jsonPartEquals("test", 1));
        assertThat("{\"test\":[1, 2, 3]}", jsonPartEquals("test[0]", "1"));
        assertThat("{\"foo\":\"bar\",\"test\": 2}", jsonEquals("{\n\"test\": 2,\n\"foo\":\"bar\"}"));
        assertThat("{}", jsonEquals("{}"));
        assertThat("{\"test\":1}", jsonEquals(resource("test.json")));
        assertThat("{\"test\":2}", not(jsonEquals(resource("test.json"))));
    }

    @Test
    void testJsonPartMatches() {
        assertThat("{\"test\":1}", jsonPartMatches("test", is(valueOf(1))));
    }

    @Test
    void jsonPartMatchesShouldReturnNiceException() {
        assertThatThrownBy(() -> assertThat("{\"test\":1}", jsonPartMatches("test", is(valueOf(2)))))
            .hasMessage("\nExpected: node \"test\" is <2>\n" +
                "     but: was <1>");
    }

    @Test
    void jsonPartMatchesShouldFailOnMissing() {
        assertThatThrownBy(() -> assertThat("{\"test\":1}", jsonPartMatches("test2", is(valueOf(2)))))
            .hasMessage("\n" +
                "Expected: node \"test2\" is <2>\n" +
                "     but: Node \"test2\" is missing.");
    }

    @Test
    void shouldAddPathPrefixToPath() {
        assertThatThrownBy(() -> assertThat(jsonSource("{\"test\":1}", "$"), jsonPartMatches("test", is(valueOf(2)))))
            .hasMessage("\n" +
                "Expected: node \"$.test\" is <2>\n" +
                "     but: was <1>");
    }

    @Test
    void testJsonPartMatchesArray() {
        assertThat("{\"test\":[1, 2, 3]}", jsonPartMatches("test", hasItems(valueOf(1), valueOf(2), valueOf(3))));
    }

    @Test
    void jsonPartMatchesShouldReturnNiceExceptionForArray() {
        assertThatThrownBy(() -> assertThat("{\"test\":[1, 2, 3]}", jsonPartMatches("test", hasItems(valueOf(1), valueOf(2), valueOf(4)))))
            .hasMessage("\nExpected: node \"test\" (a collection containing <1> and a collection containing <2> and a collection containing <4>)\n" +
                "     but: was <[1, 2, 3]>");
    }

    @Test
    void ifMatcherDoesNotMatchReportDifference() {
        assertThatThrownBy(() -> assertThat("{\"test\":-1}", jsonEquals("{\"test\": \"${json-unit.matches:positive}\"}").withMatcher("positive", greaterThan(valueOf(0)))))
            .hasMessage("\n" +
                "Expected: {\"test\": \"${json-unit.matches:positive}\"}\n" +
                "     but: JSON documents are different:\n" +
                "Matcher \"positive\" does not match value -1 in node \"test\". <-1> was less than <0>\n");
    }

    @Test
    void pathShouldBeIgnoredForDifferentValue() {
        assertThat("{\"root\":{\"test\":1, \"ignored\": 2}}", jsonEquals("{\"root\":{\"test\":1, \"ignored\": 1}}").whenIgnoringPaths("root.ignored"));
     }

    @Test
    void shouldNotFailOnEmptyInput() {
        assertThatThrownBy(() -> assertThat("", jsonEquals("{\"test\":1}")))
            .hasMessage("\nExpected: {\"test\":1}\n" +
                "     but: JSON documents are different:\n" +
                "Different value found in node \"\", expected <{\"test\":1}> but was <\"\">.\n");
    }

    @Test
    void testGenericsStringInference() {
        doAssertThat("{\"test\":1}", jsonStringPartEquals("test", "1"));
        doAssertThat("{\"test\":1}", jsonStringEquals("{\"test\" : 1}"));
        doAssertThat("{\"test\":1}", jsonPartEquals("test", "1"));
        doAssertThat("{\"test\":1}", jsonEquals(("{\"test\" : 1}")));
    }

    private void doAssertThat(String text, Matcher<String> matcher) {
        assertThat(text, matcher);
    }

    @Test
    void testToleranceStatic() {
        JsonAssert.setTolerance(0.001);
        assertThat("{\"test\":1.00001}", jsonEquals("{\"test\":1}"));
    }

    @Test
    void testTolerance() {
        assertThat("{\"test\":1.00001}", jsonEquals("{\"test\":1}").withTolerance(0.001).when(IGNORING_EXTRA_FIELDS));
    }

    @Test
    void shouldIgnoreExtraFields() {
        assertThat("{\"test\":{\"a\":1, \"b\":2, \"c\":3}}", jsonEquals("{\"test\":{\"b\":2}}").when(IGNORING_EXTRA_FIELDS));
    }

    @Test
    void hasItemShouldWork() {
        //assertThat(asList("{\"test\":1}"), hasItem(jsonEquals("{\"test\":1}"))); //does not compile
        assertThat(asList("{\"test\":1}"), contains(jsonEquals("{\"test\":1}")));
    }

    @Test
    void testAssertDifferentTypeInt() {
        assertThatThrownBy(() -> assertThat("{\"test\":\"1\"}", jsonPartEquals("test", 1)))
            .hasMessage("\n" +
                "Expected: 1 in \"test\"\n" +
                "     but: JSON documents are different:\n" +
                "Different value found in node \"test\", expected <1> but was <\"1\">.\n");
    }

    @Test
    void testGenericsInt() {
        Matcher<Integer> intMatcher = jsonEquals(1);
        assertThat(1, intMatcher);
    }

    @Test
    void testGenericsIntAndString() {
        Matcher<String> stringMatcher = jsonPartEquals("test", 1);
        assertThat("{\"test\":1}", stringMatcher);
    }


    @Test
    void testDifferentValue() {
        assertThatThrownBy(() -> assertThat("{\"test\":1}", jsonEquals("{\n\"test\": 2\n}")))
            .hasMessage("\nExpected: {\n\"test\": 2\n}\n" +
                "     but: JSON documents are different:\n" +
                "Different value found in node \"test\", expected <2> but was <1>.\n");
    }

    @Test
    void testDifferentStructure() {
        assertThatThrownBy(() -> assertThat("{\"test\":1}", jsonEquals("{\n\"test2\": 2\n}")))
            .hasMessage("\nExpected: {\n\"test2\": 2\n}\n" +
                "     but: JSON documents are different:\n" +
                "Different keys found in node \"\", missing: \"test2\", extra: \"test\", expected <{\"test2\":2}> but was <{\"test\":1}>\n");
    }

    @Test
    void testDifferentPartValue() {
        RecordingDifferenceListener listener = new RecordingDifferenceListener();
        assertThatThrownBy(() -> assertThat("{\"test\":1}", jsonPartEquals("test", "2").withDifferenceListener(listener)))
            .hasMessage("\nExpected: 2 in \"test\"\n" +
                "     but: JSON documents are different:\n" +
                "Different value found in node \"test\", expected <2> but was <1>.\n");

        assertEquals(1, listener.getDifferenceList().size());
        assertEquals("DIFFERENT Expected 2 in test got 1 in test", listener.getDifferenceList().get(0).toString());
    }

    @Test
    void testAbsent() {
        assertThatThrownBy(() -> assertThat("{\"test\":1}", jsonNodeAbsent("test")))
            .hasMessage("\n" +
                "Expected: Node \"test\" is absent.\n" +
                "     but: Node \"test\" is \"1\".");
    }

    @Test
    void testAbsentShouldAddPathToThePrefix() {
        assertThatThrownBy(() -> assertThat(jsonSource("{\"test\":1}", "$"), jsonNodeAbsent("test")))
            .hasMessage("\n" +
                "Expected: Node \"$.test\" is absent.\n" +
                "     but: Node \"$.test\" is \"1\".");
    }

    @Test
    void testAbsentOk() {
        assertThat("{\"test\":1}", jsonNodeAbsent("different"));
    }

    @Test
    void testPresent() {
        assertThatThrownBy(() -> assertThat("{\"test\":1}", jsonNodePresent("test.a")))
            .hasMessage("\n" +
                "Expected: Node \"test.a\" is present.\n" +
                "     but: Node \"test.a\" is missing.");
    }


    @Test
    void testPresentShouldAddPathToThePrefix() {
        assertThatThrownBy(() -> assertThat(jsonSource("{\"test\":1}", "$"), jsonNodePresent("test.a")))
            .hasMessage("\n" +
                "Expected: Node \"$.test.a\" is present.\n" +
                "     but: Node \"$.test.a\" is missing.");
    }

    @Test
    void testPresentIfNullAndTreatingNullAsAbsent() {
        assertThatThrownBy(() -> assertThat("{\"test\":null}", jsonNodePresent("test").when(TREATING_NULL_AS_ABSENT)))
            .hasMessage("\n" +
                "Expected: Node \"test\" is present.\n" +
                "     but: Node \"test\" is missing.");
    }

    @Test
    void testPresentOk() {
        assertThat("{\"test\":1}", jsonNodePresent("test"));
    }

    @Test
    void testNullAndAbsent() {
        assertThatThrownBy(() -> assertThat("{\"test\":{\"a\":1, \"b\": null}}", jsonEquals("{\"test\":{\"a\":1}}")))
            .hasMessage("\n" +
                "Expected: {\"test\":{\"a\":1}}\n" +
                "     but: JSON documents are different:\n" +
                "Different keys found in node \"test\", extra: \"test.b\", expected <{\"a\":1}> but was <{\"a\":1,\"b\":null}>\n");
    }

    @Test
    void shouldIgnoreValues() {
        assertThat("{\"test\":{\"a\":3,\"b\":2,\"c\":1}}", jsonEquals("{\"test\":{\"a\":1,\"b\":2,\"c\":3}}").when(IGNORING_VALUES));
    }

    @Test
    void testTreatNullAsAbsent() {
        JsonAssert.setOptions(TREATING_NULL_AS_ABSENT);
        assertThat("{\"test\":{\"a\":1, \"b\": null}}", jsonEquals("{\"test\":{\"a\":1}}"));
    }

    @Test
    void testJsonNode() {
        assertThat(readValue("{\"test\":1}"), jsonEquals("{\"test\":1}"));
    }

    @Test
    void jsonEqualsResourceShouldReturnReasonWhenDiffers() {
        try {
            assertThat("{\"test\":2}", jsonEquals(resource("test.json")));
            expectException();
        } catch (AssertionError e) {
            assertThat(e.getMessage(), containsString("\n" +
                                "     but: JSON documents are different:\n" +
                                "Different value found in node \"test\", expected <1> but was <2>.\n"));
        }
    }

    @Test
    void jsonEqualsResourceShouldReturnReasonWhenResourceIsMissing() {
        try {
            assertThat("{\"test\":2}", jsonEquals(resource("nonsense")));
            expectException();
        } catch (IllegalArgumentException e) {
            assertEquals("resource 'nonsense' not found", e.getMessage());
        }
    }

    @Test
    void jsonEqualsResourceShouldReturnReasonWhenNullPassedAsParameter() {
        try {
            assertThat("{\"test\":2}", jsonEquals(resource(null)));
            expectException();
        } catch (NullPointerException e) {
            assertEquals("'null' passed instead of resource name", e.getMessage());
        }
    }

    @Test
    void nullPointerExceptionTest() {
        String message = "{"
            + "     \"properties\":{"
            + "         \"attr\":\"123\""
            + "     }"
            + "}";
        String path = "properties.another[0]";
        String expected = "VALUE";
        jsonPartEquals(path, expected).matches(message);
    }

    @Test
    void testSpecificPath() {
        assertThat("{\"test\":{\"a\":1,\"b\":2,\"c\":3}}",
            jsonEquals("{\"test\":{\"a\":1,\"b\":2,\"c\":4}}").when(path("test.c"), then(IGNORING_VALUES)));
    }

    @Test
    void testSpecificPathNot() {
        assertThat("{\"test\":{\"a\":1,\"b\":2,\"c\":3}}",
            jsonEquals("{\"test\":{\"a\":5,\"b\":6,\"c\":3}}").when(IGNORING_VALUES).when(path("test.c"), thenNot(IGNORING_VALUES)));
    }

    private void expectException() {
        fail("Exception expected");
    }

    protected abstract Object readValue(String value);
}
