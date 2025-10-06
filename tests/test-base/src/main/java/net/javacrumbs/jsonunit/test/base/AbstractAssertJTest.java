/**
 * Copyright 2009-2019 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.javacrumbs.jsonunit.test.base;

import static java.math.BigDecimal.valueOf;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.JSON;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.value;
import static net.javacrumbs.jsonunit.core.ConfigurationWhen.path;
import static net.javacrumbs.jsonunit.core.ConfigurationWhen.paths;
import static net.javacrumbs.jsonunit.core.ConfigurationWhen.rootPath;
import static net.javacrumbs.jsonunit.core.ConfigurationWhen.then;
import static net.javacrumbs.jsonunit.core.ConfigurationWhen.thenIgnore;
import static net.javacrumbs.jsonunit.core.ConfigurationWhen.thenNot;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_ARRAY_ITEMS;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_VALUES;
import static net.javacrumbs.jsonunit.core.Option.REPORTING_DIFFERENCE_AS_NORMALIZED_STRING;
import static net.javacrumbs.jsonunit.core.Option.TREATING_NULL_AS_ABSENT;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.jsonSource;
import static net.javacrumbs.jsonunit.test.base.RegexBuilder.regex;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.InstanceOfAssertFactories.BIG_DECIMAL;
import static org.assertj.core.api.InstanceOfAssertFactories.BOOLEAN;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.javacrumbs.jsonunit.assertj.JsonAssert;
import net.javacrumbs.jsonunit.assertj.JsonAssert.ConfigurableJsonAssert;
import net.javacrumbs.jsonunit.core.NumberComparator;
import net.javacrumbs.jsonunit.core.Option;
import net.javacrumbs.jsonunit.test.base.AbstractJsonAssertTest.DivisionMatcher;
import org.hamcrest.Matcher;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.opentest4j.MultipleFailuresError;

public abstract class AbstractAssertJTest {

    @Test
    protected void demo() {
        // Both actual value (in the left) and expected value (in the right) are parsed as JSON
        assertThatJson("{\"root\":{\"a\":1}}").node("root").isEqualTo("{a:1}");

        // Works with arrays too
        assertThatJson("{\"root\":[{\"a\":1}]}").node("root").isEqualTo("[{a:1}]");

        // Strings passed to AssertJ methods like containsExactly are parsed as JSON too
        assertThatJson("{\"root\":[{\"a\":1}]}").node("root").isArray().containsExactly("{a:1}");

        // Primitive boolean? No problem
        assertThatJson("{\"root\":[true]}").node("root").isArray().containsExactly(true);

        // Boolean in string? Tricky, "true" is valid JSON so it gets parsed to primitive `true`
        // Have to wrap it to JsonAssertions.value() in order to make sure it's not parsed
        assertThatJson("{\"root\":[\"true\"]}").node("root").isArray().containsExactly(value("true"));
    }

    @Test
    void shouldAssertSimple() {
        assertThatJson("{\"a\":1, \"b\":2}").isEqualTo("{\"b\":2, \"a\":1}");
    }

    @Test
    protected void shouldAssertLenient() {
        assertThatJson("{\"a\":\"1\", \"b\":2}").isEqualTo("{b:2, a:'1'}");
    }

    @Test
    void shouldAssertObject() {
        assertThatJson("{\"a\":1}").isObject().containsEntry("a", valueOf(1));
    }

    @Test
    void objectShouldContainKeys() {
        assertThatJson("{\"a\":1, \"b\": 2}").isObject().containsKeys("a", "b");
    }

    @Test
    void shouldAssertNullValue() {
        assertThatJson(null).isNull();
    }

    @Test
    void shouldAssertNullValueNode() {
        assertThatJson(null).node("a").isAbsent();
    }

    @Test
    void shouldFailCorrectlyOnNull() {
        assertThatThrownBy(() -> assertThatJson(null).isEqualTo(1))
                .hasMessage(
                        "JSON documents are different:\nDifferent value found in node \"\", expected: <1> but was: <null>.\n");
    }

    @Test
    void containsEntryShouldWork() {
        String entryValue = "{\n" + "  \"approvable\" : true," + "  \"rejectable\" : false" + "}";

        String input = "[{\"allowedActions\":" + entryValue + "}]";

        assertThatJson(
                input,
                body -> body.isArray().hasSize(1),
                body -> body.inPath("[0]").isObject().containsEntry("allowedActions", json(entryValue)),
                body -> body.inPath("[0]").isObject().contains(entry("allowedActions", json(entryValue))),
                body -> body.inPath("[0]")
                        .isObject()
                        .containsAllEntriesOf(singletonMap("allowedActions", json(entryValue))),
                body -> body.inPath("[0]")
                        .isObject()
                        .containsAnyOf(entry("allowedActions", json(entryValue)), entry("test", 1)),
                body -> body.inPath("[0]")
                        .isObject()
                        .containsExactlyInAnyOrderEntriesOf(singletonMap("allowedActions", json(entryValue))),
                body -> body.inPath("[0]").isObject().containsOnly(entry("allowedActions", json(entryValue))),
                body -> body.inPath("[0]").isObject().containsValues(json(entryValue)),
                body -> body.inPath("[0]").isObject().containsValue(json(entryValue)),
                body -> body.inPath("[0].allowedActions").isObject().isEqualTo(json(entryValue)));
    }

    @Test
    void containsEntryShouldWorkWithMatcher() {
        String json = "{\"a\": 1, \"b\": 2}";
        assertThatJson(json).isObject().containsEntry("a", json("\"${json-unit.any-number}\""));
        assertThatJson(json).isObject().contains(entry("a", json("\"${json-unit.any-number}\"")));
    }

    @Test
    void containsOnlyShouldWorkWithMatcher() {
        String json = "{\"a\": 1, \"b\": 2}";
        assertThatJson(json)
                .isObject()
                .containsOnly(
                        entry("a", json("\"${json-unit.any-number}\"")),
                        entry("b", json("\"${json-unit.any-number}\"")));
    }

    @Test
    void containsEntryShouldFailWithMatcher() {
        String json = "{\"a\": 1, \"b\": 2}";

        assertThatThrownBy(() -> assertThatJson(json)
                        .isObject()
                        .contains(
                                entry("a", json("\"${json-unit.any-string}\"")),
                                entry("b", json("\"${json-unit.any-number}\""))))
                .hasMessage(
                        """
                    [Different value found in node ""]\s
                    Expecting map:
                      {"a":1,"b":2}
                    to contain:
                      ["a"="${json-unit.any-string}", "b"="${json-unit.any-number}"]
                    but could not find the following map entries:
                      ["a"="${json-unit.any-string}"]
                    """);
    }

    @Test
    void containsAnyOfShouldWorkWithMatcher() {
        String json = "{\"a\": 1, \"b\": 2}";
        assertThatJson(json)
                .isObject()
                .containsAnyOf(
                        entry("a", json("\"${json-unit.any-string}\"")),
                        entry("a", json("\"${json-unit.any-number}\"")));
    }

    @Test
    void containsAnyOfShouldFailWithMatcher() {
        String json = "{\"a\": 1, \"b\": 2}";

        assertThatThrownBy(() -> assertThatJson(json)
                        .isObject()
                        .containsAnyOf(
                                entry("a", json("\"${json-unit.any-string}\"")),
                                entry("b", json("\"${json-unit.any-string}\""))))
                .hasMessage(
                        """
                    [Different value found in node ""]\s
                    Expecting actual:
                      {"a":1,"b":2}
                    to contain at least one of the following elements:
                      ["a"="${json-unit.any-string}", "b"="${json-unit.any-string}"]
                    but none were found""");
    }

    @Test
    void containsValuesShouldPass() {
        String json = "{\"a\": 1, \"b\": 2}";
        assertThatJson(json).isObject().containsValues(valueOf(1), valueOf(2), json("\"${json-unit.any-number}\""));
    }

    @Test
    void containsValuesShouldFail() {
        String json = "{\"a\": 1, \"b\": 2}";
        assertThatThrownBy(() -> assertThatJson(json)
                        .isObject()
                        .containsValues(valueOf(1), valueOf(2), json("\"${json-unit.any-string}\"")))
                .hasMessage(
                        """
                    [Different value found in node ""]\s
                    Expecting actual:
                      {"a":1,"b":2}
                    to contain value:
                      "${json-unit.any-string}\"""");
    }

    @Test
    void absentOnArray() {
        String json = "[{\"a\":1},{\"b\":1}]";

        assertThatJson(json).inPath("[*].c").isArray().isEmpty();
    }

    @Test
    void invalidExpectedValue() {
        assertThatThrownBy(() -> assertThatJson(json).isEqualTo("{\"broken\":"))
                .hasMessage("Can not parse expected value: '{\"broken\":'");
    }

    @Test
    void objectShouldContainValue() {
        assertThatJson("{\"a\":1, \"b\": 2}").isObject().containsValue(valueOf(2));
    }

    @Test
    void objectShouldContainComplexValue() {
        assertThatJson("{\"a\":1, \"b\": {\"c\" :3}}")
                .isObject()
                .containsValue(json("{\"c\" :\"${json-unit.any-number}\"}"));
    }

    @Test
    void objectShouldContainComplexValueError() {
        assertThatThrownBy(() -> assertThatJson("{\"root\":{\"a\":1, \"b\": {\"c\" :3}}}")
                        .node("root")
                        .isObject()
                        .containsValue(json("{\"c\" :5}")))
                .hasMessage(
                        """
                    [Different value found in node "root"]\s
                    Expecting actual:
                      {"a":1,"b":{"c":3}}
                    to contain value:
                      {"c":5}""");
    }

    @Test
    protected void objectFieldsShouldBeKeptInOrder() {
        assertThatJson("{\"root\":{\"key3\": 3, \"key2\": 2, \"key1\": 1 }}")
                .node("root")
                .isObject()
                .containsExactly(entry("key3", valueOf(3)), entry("key2", valueOf(2)), entry("key1", valueOf(1)));
    }

    @Test
    void objectDoesContainComplexValue() {
        assertThatJson("{\"a\":1, \"b\": {\"c\" :3}}")
                .isObject()
                .doesNotContainValue(json("{\"c\" :\"${json-unit.any-string}\"}"));
    }

    @Test
    void objectDoesContainComplexValueError() {
        assertThatThrownBy(() -> assertThatJson("{\"root\":{\"a\":1, \"b\": {\"c\" :3}}}")
                        .node("root")
                        .isObject()
                        .doesNotContainValue(json("{\"c\" :3}")))
                .hasMessage(
                        """
                    [Different value found in node "root"]\s
                    Expecting actual:
                      {"a":1,"b":{"c":3}}
                    not to contain value:
                      {"c":3}""");
    }

    @Test
    void compareJsonInJsonPathArray() {
        assertThatJson("{\"root\": [{\"target\": 450} ]}")
                .inPath("$.root")
                .isArray()
                .containsExactly("{\"target\": 450 }");
    }

    @Test
    void compareJsonInJsonPathShallowArray() {
        assertThatJson("{\"root\": [450]}").inPath("$.root").isArray().containsExactly(json("450"));
    }

    @Test
    void compareJsonInJsonPathShallowArrayString() {
        assertThatThrownBy(() -> assertThatJson("{\"root\": [450]}")
                        .inPath("$.root")
                        .isArray()
                        .containsExactly(value("450")))
                .hasMessage(
                        """
                    [Node "$.root"]\s
                    Expecting actual:
                      [450]
                    to contain exactly (and in same order):
                      ["450"]
                    but some elements were not found:
                      ["450"]
                    and others were not expected:
                      [450]
                    when comparing values using JsonComparator""");
    }

    @Test
    void compareJsonInPathArrayOfArrays() {
        assertThatJson("{\"root\": [[{\"target\": 450} ]]}")
                .inPath("$.root")
                .isArray()
                .containsExactly("[{\"target\": 450 }]");
    }

    @Test
    void compareJsonInNodeArray() {
        assertThatJson("{\"root\": [{\"target\": 450} ]}")
                .node("root")
                .isArray()
                .containsExactly("{\"target\": 450 }");
    }

    @Test
    void compareJsonInNodeShallowArray() {
        assertThatJson("{\"root\": [450]}").node("root").isArray().containsExactly(valueOf(450));
    }

    @Test
    void compareJsonInNodeShallowArrayBigDecimal() {
        assertThatJson("{\"root\": [450]}").node("root").isArray().containsExactly(valueOf(450));
    }

    @Test
    void compareJsonInNodeShallowArrayBoolean() {
        assertThatJson("{\"root\": [true]}").node("root").isArray().containsExactly(true);
    }

    @Test
    void compareJsonInNodeArrayOfArrays() {
        assertThatJson("{\"root\": [[{\"target\": 450} ]]}")
                .node("root")
                .isArray()
                .containsExactly("[{\"target\": 450 }]");
    }

    @Test
    void compareJsonArray() {
        assertThatJson("{\"root\": [{\"target\": 450} ]}")
                .node("root")
                .isEqualTo(singletonList(singletonMap("target", 450)));
    }

    @Test
    void shouldAssertDirectEqual() {
        assertThatJson("{\"a\":1}").isEqualTo("{\"a\":\"${json-unit.ignore}\"}");
    }

    @Test
    void shouldIgnoreIfMissing() {
        assertThatThrownBy(() -> assertThatJson("{\"root\":{\"test\":1}}")
                        .isEqualTo("{\"root\":{\"test\":1, \"ignored\": \"${json-unit.ignore}\"}}"))
                .hasMessage(
                        """
                    JSON documents are different:
                    Different keys found in node "root", missing: "root.ignored", expected: <{"ignored":"${json-unit.ignore}","test":1}> but was: <{"test":1}>
                    """);
    }

    @Test
    void shouldIgnoreIfNull() {
        assertThatJson("{\"root\":{\"test\":1, \"ignored\": null}}")
                .isEqualTo("{\"root\":{\"test\":1, \"ignored\": \"${json-unit.ignore}\"}}");
    }

    @Test
    void shouldIgnoreIfObject() {
        assertThatJson("{\"root\":{\"test\":1, \"ignored\": {\"a\": 1}}}")
                .isEqualTo("{\"root\":{\"test\":1, \"ignored\": \"${json-unit.ignore}\"}}");
    }

    @Test
    void shouldIgnoreElementIfMissing() {
        assertThatJson("{\"root\":{\"test\":1}}")
                .isEqualTo("{\"root\":{\"test\":1, \"ignored\": \"${json-unit.ignore-element}\"}}");
    }

    @Test
    void shouldIgnoreElementIfNull() {
        assertThatJson("{\"root\":{\"test\":1, \"ignored\": null}}")
                .isEqualTo("{\"root\":{\"test\":1, \"ignored\": \"${json-unit.ignore-element}\"}}");
    }

    @Test
    void shouldIgnoreElementIfObject() {
        assertThatJson("{\"root\":{\"test\":1, \"ignored\": {\"a\": 1}}}")
                .isEqualTo("{\"root\":{\"test\":1, \"ignored\": \"${json-unit.ignore-element}\"}}");
    }

    @Test
    void shouldAssertObjectJson() {
        assertThatThrownBy(() -> assertThatJson("{\"a\":{\"b\": 1}}")
                        .node("a")
                        .isObject()
                        .isEqualTo(json("{\"b\": 2}")))
                .hasMessage(
                        """
                    JSON documents are different:
                    Different value found in node "a.b", expected: <2> but was: <1>.
                    """);
    }

    @Test
    void shouldAssertContainsEntry() {
        assertThatJson("{\"a\":{\"b\": 1}}").node("a").isObject().contains(entry("b", valueOf(1)));
    }

    @Test
    void shouldAssertContainsJsonError() {
        assertThatThrownBy(() -> assertThatJson("{\"a\":{\"b\": 1}}")
                        .node("a")
                        .isObject()
                        .contains(entry("b", valueOf(2))))
                .hasMessage(
                        """
                    [Different value found in node "a"]\s
                    Expecting map:
                      {"b":1}
                    to contain:
                      ["b"=2]
                    but could not find the following map entries:
                      ["b"=2]
                    """);
    }

    @Test
    void shouldAssertContainsOnlyKeys() {
        assertThatJson("{\"a\":{\"b\": 1, \"c\": true}}").node("a").isObject().containsOnlyKeys("b", "c");
    }

    @Test
    void shouldAssertContainsOnlyKeysError() {
        assertThatThrownBy(() -> assertThatJson("{\"a\":{\"b\": 1, \"c\": true}}")
                        .node("a")
                        .isObject()
                        .containsOnlyKeys("b", "c", "d"))
                .hasMessage(
                        """
                    [Different value found in node "a"]\s
                    Expecting actual:
                      {"b":1,"c":true}
                    to contain only following keys:
                      ["b", "c", "d"]
                    but could not find the following keys:
                      ["d"]
                    """);
    }

    @Test
    void shouldAssertContainsAllEntries() {
        assertThatJson("{\"a\":{\"b\": 1, \"c\": true}}")
                .node("a")
                .isObject()
                .containsAllEntriesOf(singletonMap("c", true));
    }

    @Test
    void shouldAssertContainsAllEntriesError() {
        assertThatThrownBy(() -> assertThatJson("{\"a\":{\"b\": 1, \"c\": true}}")
                        .node("a")
                        .isObject()
                        .containsAllEntriesOf(singletonMap("c", false)))
                .hasMessage(
                        """
                    [Different value found in node "a"]\s
                    Expecting map:
                      {"b":1,"c":true}
                    to contain:
                      ["c"=false]
                    but could not find the following map entries:
                      ["c"=false]
                    """);
    }

    @Test
    void shouldAssertJson() {
        assertThatThrownBy(() -> assertThatJson("{\"a\":{\"b\": 1}}").node("a").isEqualTo(json("{\"b\": 2}")))
                .hasMessage(
                        """
                    JSON documents are different:
                    Different value found in node "a.b", expected: <2> but was: <1>.
                    """);
    }

    @Test
    void shouldAssertObjectJsonWithPlaceholder() {
        assertThatJson("{\"a\":{\"b\": \"ignored\"}}")
                .node("a")
                .isObject()
                .isEqualTo(json("{\"b\":\"${json-unit.any-string}\"}"));
    }

    @Test
    void shouldAssertObjectIsNotEqualToJsonWithPlaceholder() {
        assertThatJson("{\"a\":{\"b\": 1}}")
                .node("a")
                .isObject()
                .isNotEqualTo(json("{\"b\":\"${json-unit.any-string}\"}"));
    }

    @Test
    void shouldAssertObjectIsNotEqualToJsonWithPlaceholderError() {
        assertThatThrownBy(() -> assertThatJson("{\"a\":{\"b\": \"string\"}}")
                        .node("a")
                        .isObject()
                        .isNotEqualTo(json("{\"b\":\"${json-unit.any-string}\"}")))
                .hasMessage(
                        """
                    [Different value found in node "a"]\s
                    Expecting actual:
                      {"b":"string"}
                    not to be equal to:
                      {"b":"${json-unit.any-string}"}
                    when comparing values using JsonComparator""");
    }

    @Test
    void shouldAssertObjectJsonWithPlaceholderFailure() {
        assertThatThrownBy(() -> assertThatJson("{\"a\":{\"b\": 1}}")
                        .node("a")
                        .isObject()
                        .isEqualTo(json("{\"b\":\"${json-unit.any-string}\"}")))
                .hasMessage(
                        """
                    JSON documents are different:
                    Different value found in node "a.b", expected: <a string> but was: <1>.
                    """);
    }

    @Test
    void shouldAssertString() {
        assertThatThrownBy(() -> assertThatJson("{\"a\":{\"b\": \"foo\"}}")
                        .node("a.b")
                        .isString()
                        .startsWith("bar"))
                .hasMessage(
                        """
                    [Different value found in node "a.b"]\s
                    Expecting actual:
                      "foo"
                    to start with:
                      "bar"
                    """);
    }

    @Test
    void shouldAssertStringCustomDescription() {
        assertThatThrownBy(() -> assertThatJson("{\"a\":{\"b\": \"foo\"}}")
                        .node("a.b")
                        .isString()
                        .as("Sad!")
                        .startsWith("bar"))
                .hasMessage(
                        """
                    [Sad!]\s
                    Expecting actual:
                      "foo"
                    to start with:
                      "bar"
                    """);
    }

    @Test
    void shouldAssertArray() {
        assertThatJson("{\"a\":[1, 2, 3]}").node("a").isArray().contains(valueOf(3));
    }

    @Test
    void shouldFindObjectInArray() {
        assertThatJson("{\"a\":[{\"b\": 1}, {\"c\": 1}, {\"d\": 1}]}")
                .node("a")
                .isArray()
                .contains(json("{\"c\": 1}"));
    }

    @Test
    void shouldFindObjectInArrayWithPlaceholder() {
        assertThatJson("{\"a\":[{\"b\": 1}, {\"c\": 1}, {\"d\": 1}]}")
                .node("a")
                .isArray()
                .contains(json("{\"c\": \"${json-unit.any-number}\"}"));
    }

    @Test
    void arraySimpleIgnoringOrderComparisonExample() {
        assertThatJson("{\"test\":[1,2,3]}").when(IGNORING_ARRAY_ORDER).isEqualTo("{\"test\":[3,2,1]}");
    }

    @Test
    void arrayIgnoringOrderComparison() {
        assertThatJson("{\"a\":[{\"b\": 1}, {\"c\": 1}, {\"d\": 1}]}")
                .node("a")
                .isArray()
                .containsExactlyInAnyOrder(json("{\"c\": 1}"), json("{\"b\": 1}"), json("{\"d\": 1}"));
    }

    @Test
    void arraySimpleIgnoringOrderComparison() {
        assertThatJson("{\"a\":[{\"b\": 1}, {\"c\": 1}, {\"d\": 1}]}")
                .when(Option.IGNORING_ARRAY_ORDER)
                .node("a")
                .isArray()
                .isEqualTo(json("[{\"c\": 1}, {\"b\": 1} ,{\"d\": 1}]"));
    }

    @Test
    void arraySimpleIgnoringOrderComparisonError() {
        assertThatThrownBy(() -> assertThatJson("{\"a\":[{\"b\": 1}, {\"c\": 1}, {\"d\": 1}]}")
                        .when(Option.IGNORING_ARRAY_ORDER)
                        .node("a")
                        .isArray()
                        .isEqualTo(json("[{\"c\": 2}, {\"b\": 1} ,{\"d\": 1}]")))
                .hasMessage(
                        """
                    JSON documents are different:
                    Different value found when comparing expected array element a[0] to actual element a[1].
                    Different value found in node "a[1].c", expected: <2> but was: <1>.
                    """);
    }

    @Test
    void multipleFailuresErrorShouldBeCorrectlyFormatted() {
        assertThatExceptionOfType(MultipleFailuresError.class)
                .isThrownBy(() -> assertThatJson("{\"a\":[{\"b\": 1}, {\"c\": 1}, {\"d\": 1}]}")
                        .when(Option.IGNORING_ARRAY_ORDER)
                        .node("a")
                        .isArray()
                        .isEqualTo(json("[{\"c\": 2}, {\"b\": 1} ,{\"d\": 1}]")))
                .satisfies(e -> {
                    List<Throwable> failures = e.getFailures();
                    assertThat(failures.get(0).getMessage())
                            .isEqualTo(
                                    "Different value found when comparing expected array element a[0] to actual element a[1].");
                    assertThat(failures.get(1).getMessage())
                            .isEqualTo("Different value found in node \"a[1].c\", expected: <2> but was: <1>.");
                });
    }

    @Test
    void shouldIgnoreMissingPathEvenIfItIsInExpectedValue() {
        assertThatJson("{\"root\":{\"foo\":1}}")
                .whenIgnoringPaths("root.bar", "missing")
                .isEqualTo("{\"root\":{\"foo\":1, \"bar\":2}, \"missing\":{\"quux\":\"test\"}}");
    }

    @Test
    void shouldIgnoreArrayElement() {
        assertThatJson("{\"root\":[0, 1, 2]}").whenIgnoringPaths("root[1]").isEqualTo("{\"root\":[0, 8, 2]}");
    }

    @Test
    void shouldIgnoreJsonPaths() {
        String expected = "[{\"name\":\"123\",\"age\":2},{\"name\":\"321\",\"age\":5}]";
        String actual = "[{\"name\":\"123\",\"age\":5},{\"name\":\"321\",\"age\":8}]";

        assertThatJson(expected).whenIgnoringPaths("$..age").isEqualTo(actual);
    }

    @Test
    void arraySimpleIgnoringOrderNotEqualComparison() {
        assertThatJson("{\"a\":[{\"b\": 1}, {\"c\": 1}, {\"d\": 1}]}")
                .when(Option.IGNORING_ARRAY_ORDER)
                .node("a")
                .isArray()
                .isNotEqualTo(json("[{\"c\": 2}, {\"b\": 1} ,{\"d\": 1}]"));
    }

    @Test
    void arraySimpleIgnoringOrderNotEqualComparisonError() {
        assertThatThrownBy(() -> assertThatJson("{\"a\":[{\"b\": 1}, {\"c\": 1}, {\"d\": 1}]}")
                        .when(Option.IGNORING_ARRAY_ORDER)
                        .node("a")
                        .isArray()
                        .isNotEqualTo(json("[{\"c\": 1}, {\"b\": 1} ,{\"d\": 1}]")))
                .hasMessage(
                        """
                    [Node "a"]\s
                    Expecting:
                     <[{"b":1}, {"c":1}, {"d":1}]>
                    not to be equal to:
                     <[{"c":1},{"b":1},{"d":1}]>
                    when comparing as JSON with [IGNORING_ARRAY_ORDER]""");
    }

    @Test
    void shouldAssertBoolean() {
        assertThatThrownBy(() -> assertThatJson("{\"a\":{\"b\": true}}")
                        .node("a.b")
                        .isBoolean()
                        .isFalse())
                .hasMessage("[Different value found in node \"a.b\"] \n" + "Expecting value to be false but was true");
    }

    @Test
    void shouldAssertNull() {
        assertThatJson("{\"a\":{\"b\": null}}").node("a.b").isNull();
    }

    @Test
    void shouldAssertNullFail() {
        assertThatThrownBy(
                        () -> assertThatJson("{\"a\":{\"b\": 1}}").node("a.b").isNull())
                .hasMessage("Node \"a.b\" has invalid type, expected: <null> but was: <1>.");
    }

    @Test
    void shouldAssertNotNull() {
        assertThatThrownBy(() ->
                        assertThatJson("{\"a\":{\"b\": null}}").node("a.b").isNotNull())
                .hasMessage("Node \"a.b\" has invalid type, expected: <not null> but was: <null>.");
    }

    @Test
    void shouldAssertNotNullChain() {
        assertThatThrownBy(() -> assertThatJson("{\"a\":{\"b\": null}}")
                        .node("a")
                        .isPresent()
                        .node("b")
                        .isNotNull())
                .hasMessage("Node \"a.b\" has invalid type, expected: <not null> but was: <null>.");
    }

    @Test
    void shouldAssertNotNullChaining() {
        assertThatThrownBy(() -> assertThatJson("{\"a\":{\"b\": 1}}")
                        .node("a")
                        .isNotNull()
                        .node("b")
                        .isNumber()
                        .isEqualByComparingTo("2"))
                .hasMessage(
                        """
                    [Different value found in node "a.b"]\s
                    expected: 2
                     but was: 1""");
    }

    @Test
    void shouldAssertNotNullChainingSuccess() {
        assertThatJson("{\"a\":{\"b\": 1}}")
                .node("a")
                .isNotNull()
                .node("b")
                .isNumber()
                .isEqualByComparingTo("1");
    }

    @Test
    void shouldAssertUri() {
        assertThatJson("{\"a\":{\"b\":\"http://exampl.org?a=1\"}}")
                .node("a.b")
                .isUri()
                .hasScheme("http")
                .hasParameter("a", "1");
    }

    @Test
    void shouldAssertUriFail() {
        assertThatThrownBy(() -> assertThatJson("{\"a\":{\"b\":\"test\"}}")
                        .node("a.b")
                        .isUri()
                        .hasScheme("http"))
                .hasMessage(
                        """
                    [Different value found in node "a.b"]\s
                    Expecting scheme of
                      <test>
                    to be:
                      <"http">
                    but was:
                      <null>""");
    }

    @Test
    void shouldAssertUriFailOnType() {
        assertThatThrownBy(
                        () -> assertThatJson("{\"a\":{\"b\": 1}}").node("a.b").isUri())
                .hasMessage("Node \"a.b\" has invalid type, expected: <string> but was: <1>.");
    }

    @Test
    void canNotConfigureAfterAssertion() {
        // do not want to allow this
        // assertThatJson("[1, 2]").isEqualTo("[2, 1]").when(IGNORING_ARRAY_ORDER);
    }

    @Test
    void shouldAssertNotNullMissing() {
        assertThatThrownBy(() ->
                        assertThatJson("{\"a\":{\"b\": null}}").node("a.c").isNotNull())
                .hasMessage("Different value found in node \"a.c\", expected: <not null> but was: <missing>.");
    }

    @Test
    void shouldAssertObjectFailure() {
        assertThatThrownBy(() -> assertThatJson("true").isObject())
                .hasMessage("Node \"\" has invalid type, expected: <object> but was: <true>.");
    }

    @Test
    void absentInPathShouldFailOnSimpleJson() {
        assertThatThrownBy(() ->
                        assertThatJson("{\"a\":{\"b\": 1}}").inPath("$.a.b").isAbsent())
                .hasMessage("Different value found in node \"$.a.b\", expected: <node to be absent> but was: <1>.");
    }

    @Test
    void absentInPathShouldFailOnArray() {
        assertThatThrownBy(() -> assertThatJson("[{\"b\": 1}, {\"c\": 1}]")
                        .inPath("[*].c")
                        .isAbsent())
                .hasMessage("Different value found in node \"$[1].c\", expected: <node to be absent> but was: <[1]>.");
    }

    @Test
    void absentInPathShouldFailOnMultipleMatches() {
        assertThatThrownBy(() -> assertThatJson("[{\"c\": {\"x\": 2}}, {\"b\": {\"x\": 2}}, {\"c\": {\"x\": 2}}]")
                        .inPath("$.[*].c")
                        .isAbsent())
                .hasMessage(
                        "Different value found in nodes \"[$[0].c, $[2].c]\", expected: <node to be absent> but was: <[{\"x\":2},{\"x\":2}]>.");
    }

    @Test
    void shouldWorkIfPathNotMatching() {
        assertThatJson("{\"a\": 1}").inPath("$.c").isAbsent();
    }

    @Test
    void shouldWorkWithNull() {
        assertThatJson(null).inPath("[*].c").isAbsent();
    }

    @Test
    void shouldAssertNumber() {
        assertThatJson("{\"a\":1}").node("a").isNumber().isEqualByComparingTo("1");
    }

    @Test
    void shouldAssertAsNumber() {
        assertThatJson("{\"a\":1}").node("a").asNumber().isEqualByComparingTo("1");
    }

    @Test
    protected void shouldAssertInteger() {
        assertThatJson("{\"a\":1}").node("a").isIntegralNumber().isEqualTo(1);
        assertThatJson("{\"a\":10}").node("a").isIntegralNumber();
        assertThatJson("{\"a\":0}").node("a").isIntegralNumber();
        assertThatJson("{\"a\":-10}").node("a").isIntegralNumber();
    }

    @Test
    void shouldAllowNodeInJsonMapAssert() {
        assertThatThrownBy(() -> assertThatJson("{\"data\":{\"id\": \"1234\", \"relationships\": false}}")
                        .inPath("$.data")
                        .isObject()
                        .containsEntry("id", "1234")
                        .node("relationships")
                        .isObject())
                .hasMessage("Node \"$.data.relationships\" has invalid type, expected: <object> but was: <false>.");
    }

    @Test
    protected void shouldAssertIntegerFailure() {
        assertThatThrownBy(() -> assertThatJson("{\"a\":1.0}").node("a").isIntegralNumber())
                .hasMessage("Node \"a\" has invalid type, expected: <integer> but was: <1.0>.");

        assertThatThrownBy(() -> assertThatJson("{\"a\":0.0}").node("a").isIntegralNumber())
                .hasMessage("Node \"a\" has invalid type, expected: <integer> but was: <0.0>.");

        assertThatThrownBy(() -> assertThatJson("{\"a\":10.0}").node("a").isIntegralNumber())
                .hasMessage("Node \"a\" has invalid type, expected: <integer> but was: <10.0>.");

        assertThatThrownBy(() -> assertThatJson("{\"a\":-10.0}").node("a").isIntegralNumber())
                .hasMessage("Node \"a\" has invalid type, expected: <integer> but was: <-10.0>.");

        assertThatThrownBy(() -> assertThatJson("{\"a\":1.1}").node("a").isIntegralNumber())
                .hasMessage("Node \"a\" has invalid type, expected: <integer> but was: <1.1>.");

        assertThatThrownBy(() -> assertThatJson("{\"a\":1e3}").node("a").isIntegralNumber())
                .hasMessageStartingWith("Node \"a\" has invalid type, expected: <integer> but was:");

        assertThatThrownBy(() -> assertThatJson("{\"a\":1e-3}").node("a").isIntegralNumber())
                .hasMessage("Node \"a\" has invalid type, expected: <integer> but was: <0.001>.");
    }

    @Test
    protected void shouldUseCustomNumberComparator() {
        NumberComparator numberComparator = (expected, actual, tolerance) -> {
            if (expected.scale() == 0 && actual.scale() > 0) {
                // Expected is int actual is float
                return false;
            } else {
                return expected.compareTo(actual) == 0;
            }
        };
        assertThatJson("{\"a\":1.0}")
                .withConfiguration(c -> c.withNumberComparator(numberComparator))
                .isEqualTo("{\"a\":1.00}");

        assertThatThrownBy(() -> assertThatJson("{\"a\":1.0}")
                        .withConfiguration(c -> c.withNumberComparator(numberComparator))
                        .isEqualTo("{\"a\":1}"))
                .hasMessage(
                        "JSON documents are different:\nDifferent value found in node \"a\", expected: <1> but was: <1.0>.\n");
    }

    @Test
    protected void shouldAssert1e0() {
        assertThatThrownBy(() -> assertThatJson("{\"a\":1e0}").node("a").isIntegralNumber())
                .hasMessageStartingWith("Node \"a\" has invalid type, expected: <integer> but was:");
    }

    @Test
    protected void shouldFailOnTrainingToken() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":{}} SOME GARBAGE").isEqualTo("{\"test\": {}}"));
    }

    @Test
    void arrayExtractingShouldPass() {
        assertThatJson(
                        """
                [
                      {"id": 1, "name":{"first":"Aaron"}},
                      {"id": 2, "name":{"first":"Paul"}}
                    ]""")
                .isArray()
                .extracting("id", "name")
                .contains(tuple(valueOf(1), "{\"first\":\"Aaron\"}"), tuple(valueOf(2), "{\"first\":\"Paul\"}"));
    }

    @Test
    void arrayExtractingShouldFail() {
        assertThatThrownBy(() -> assertThatJson(
                                """
                [
                      {"id": 1, "name":{"first":"Aaron"}},
                      {"id": 2, "name":{"first":"John"}}
                    ]""")
                        .isArray()
                        .extracting("id", "name")
                        .contains(
                                tuple(valueOf(1), "{\"first\":\"Aaron\"}"), tuple(valueOf(2), "{\"first\":\"Paul\"}")))
                .hasMessage(
                        """
                    [Node ""]\s
                    Expecting ArrayList:
                      [(1, {"first":"Aaron"}), (2, {"first":"John"})]
                    to contain:
                      [(1, "{"first":"Aaron"}"), (2, "{"first":"Paul"}")]
                    but could not find the following element(s):
                      [(2, "{"first":"Paul"}")]
                    when comparing values using JsonComparator""");
    }

    @Test
    void arrayExtractingShouldFailOnDifferentLengthTuple() {
        assertThatThrownBy(() -> assertThatJson(
                                """
                [
                      {"id": 1, "name":{"first":"Aaron"}},
                      {"id": 2, "name":{"first":"John"}}
                    ]""")
                        .isArray()
                        .extracting("id", "name")
                        .contains(
                                tuple(valueOf(1), "{\"first\":\"Aaron\"}", 3),
                                tuple(valueOf(2), "{\"first\":\"John\"}")))
                .hasMessage(
                        """
                    [Node ""]\s
                    Expecting ArrayList:
                      [(1, {"first":"Aaron"}), (2, {"first":"John"})]
                    to contain:
                      [(1, "{"first":"Aaron"}", 3), (2, "{"first":"John"}")]
                    but could not find the following element(s):
                      [(1, "{"first":"Aaron"}", 3)]
                    when comparing values using JsonComparator""");
    }

    @Test
    void shouldNotParseTwice() {
        String result = "{\"bundles\":[\"http://localhost:33621/rms/framework/bundle/0\"]}";
        assertThatJson(result).and(JsonAssert::isObject, j -> j.node("bundles")
                .isArray()
                .element(0)
                .asString()
                .contains("http://", "/framework/bundle/0"));
    }

    @Test
    void elementWithTypeAssertShouldWork() {
        String result = "{\"bundles\":[\"http://localhost:33621/rms/framework/bundle/0\"]}";
        // FIXME: Better path in the message
        assertThatThrownBy(() -> assertThatJson(result)
                        .node("bundles")
                        .isArray()
                        .element(0)
                        .isNumber())
                .hasMessage(
                        "Node \"bundles\" element at index 0 has invalid type, expected: <number> but was: <\"http://localhost:33621/rms/framework/bundle/0\">.");
    }

    @Test
    void shouldAssertStringNumber() {
        assertThatJson("{\"a\":\"1\"}").node("a").asNumber().isEqualByComparingTo("1");
    }

    @Test
    void shouldAssertStringNumberFailure() {
        assertThatThrownBy(() ->
                        assertThatJson("{\"a\":\"x\"}").node("a").asNumber().isEqualByComparingTo("1"))
                .hasMessage("Node \"a\" can not be converted to number expected: <a number> but was: <\"x\">.");
    }

    @Test
    protected void shouldDiffCloseNumbers() {
        assertThatThrownBy(
                () -> assertThatJson("{\"result\":{\"a\": 0.299999999999999988897769753748434595763683319091796875}}")
                        .isEqualTo("{result: {a: 0.3}}"));
    }

    // https://github.com/assertj/assertj-core/issues/2111
    @Test
    void containsValue() {
        assertThatThrownBy(() -> assertThatJson("{\"a\": 1}").isObject().containsKey("lastModified2"))
                .hasMessage(
                        """
                    [Different value found in node ""]\s
                    Expecting actual:
                      {"a":1}
                    to contain key:
                      "lastModified2\"""");

        assertThatThrownBy(() -> assertThatJson("{\"a\": 1}").isObject().contains(entry("lastModified2", null)))
                .hasMessage(
                        """
                    [Different value found in node ""]\s
                    Expecting map:
                      {"a":1}
                    to contain:
                      ["lastModified2"=null]
                    but could not find the following map entries:
                      ["lastModified2"=null]
                    """);
    }

    @Test
    void shouldAssertNumberFailure() {
        assertThatThrownBy(
                        () -> assertThatJson("{\"a\":1}").node("a").isNumber().isEqualByComparingTo("2"))
                .hasMessage(
                        """
                    [Different value found in node "a"]\s
                    expected: 2
                     but was: 1""");
    }

    @Test
    void testAssertToleranceSimple() {
        assertThatJson("{\"test\":1.00001}").withTolerance(0.001).isEqualTo("{\"test\":1}");
    }

    @Test
    void testAssertTolerance() {
        assertThatJson("{\"test\":1.00001}")
                .withConfiguration(c -> c.withTolerance(0.001))
                .isEqualTo("{\"test\":1}");
    }

    @Test
    void shouldFailCorrectlyOnMissingNode() {
        assertThatThrownBy(() -> assertThatJson("[{\"a\":\"01\", \"b\":\"text\"}]")
                        .node("[0].a")
                        .isEqualTo("\"01\"")
                        .node("[0].b")
                        .isEqualTo("text"))
                .hasMessage("JSON documents are different:\nMissing node in path \"[0].a[0].b\".\n")
                .matches(e -> ((AssertionFailedError) e)
                        .getActual()
                        .getStringRepresentation()
                        .equals("<missing>"));
    }

    @Test
    public void shouldAllowUnquotedKeysAndCommentInExpectedValue() {
        assertThatJson("{\"test\":1, \"x\":\"a\"}").isEqualTo("{//comment\ntest:1, x:'a'}");
    }

    @Test
    void testAssertNode() {
        assertThatThrownBy(() -> assertThatJson(readValue("{\"test\":1}")).isEqualTo(readValue("{\"test\":2}")))
                .hasMessage(
                        "JSON documents are different:\nDifferent value found in node \"test\", expected: <2> but was: <1>.\n");
    }

    @Test
    void testAssertNodeInExpectOnly() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":1}").isEqualTo(readValue("{\"test\":2}")))
                .hasMessage(
                        "JSON documents are different:\nDifferent value found in node \"test\", expected: <2> but was: <1>.\n");
    }

    @Test
    void testAssertPathWithDescription() {
        assertThatThrownBy(() -> assertThatJson(jsonSource("{\"test\":1}", "$"))
                        .node("test")
                        .isEqualTo("2"))
                .hasMessage(
                        "JSON documents are different:\nDifferent value found in node \"$.test\", expected: <2> but was: <1>.\n");
    }

    @Test
    void testPresentWithDescription() {
        assertThatThrownBy(() -> assertThatJson(jsonSource("{\"test\":1}", "$"))
                        .node("test2")
                        .isPresent())
                .hasMessage(
                        "Different value found in node \"$.test2\", expected: <node to be present> but was: <missing>.");
    }

    @Test
    protected void testNotEqualTo() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":1}").isNotEqualTo("{\"test\": \"${json-unit.any-number}\"}"))
                .hasMessage(
                        """

                    Expecting actual:
                      {"test":1}
                    not to be equal to:
                      "{"test": "${json-unit.any-number}"}"
                    when comparing values using JsonComparator""");
    }

    @Test
    void testAssertPathArray() {
        assertThatThrownBy(() -> assertThatJson("{\"root\":{\"test\":[1,2,3]}}")
                        .node("root.test[0]")
                        .isEqualTo(2))
                .hasMessage(
                        "JSON documents are different:\nDifferent value found in node \"root.test[0]\", expected: <2> but was: <1>.\n");
    }

    @Test
    void testLongRegexp() {
        assertThatJson("{\"test\": \"This is some text followed by: ABCD, followed by this\"}")
                .isEqualTo(
                        "{\"test\": \"${json-unit.regex}^\\\\QThis is some text followed by: \\\\E[A-Z]+\\\\Q, followed by this\\\\E$\"}");
    }

    @Test
    void testLongRegexpBuilder() {
        assertThatJson("{\"test\": \"This is some text followed by: ABCD, followed by this\"}")
                .isEqualTo("{\"test\": "
                        + regex().str("This is some text followed by: ")
                                .exp("[A-Z]+")
                                .str(", followed by this") + "}");
    }

    @Test
    public void verifyIsArrayContainsString() {
        assertThatJson("{\"id\":\"1\", \"children\":[{\"parentId\":\"1\"}]}")
                .inPath("children[*].parentId")
                .isArray()
                .containsOnly(value("1"));
    }

    @Test
    void testNodeAbsent() {
        assertThatThrownBy(() -> assertThatJson("{\"test1\":2, \"test2\":1}")
                        .node("test2")
                        .isAbsent())
                .hasMessage("Different value found in node \"test2\", expected: <node to be absent> but was: <1>.");
    }

    @Test
    void testNodeAbsentOk() {
        assertThatJson("{\"test1\":2, \"test2\":1}").node("test3").isAbsent();
    }

    @Test
    void shouldTreatNullAsAbsent() {
        assertThatJson("{\"a\":1, \"b\": null}")
                .when(Option.TREATING_NULL_AS_ABSENT)
                .node("b")
                .isAbsent();
    }

    @Test
    void shouldNotTreatNullAsAbsent() {
        assertThatThrownBy(
                        () -> assertThatJson("{\"a\":1, \"b\": null}").node("b").isAbsent())
                .hasMessage("Different value found in node \"b\", expected: <node to be absent> but was: <null>.");
    }

    @Test
    void testNodePresent() {
        assertThatThrownBy(() -> assertThatJson("{\"test1\":2, \"test2\":1}")
                        .node("test3")
                        .isPresent())
                .hasMessage(
                        "Different value found in node \"test3\", expected: <node to be present> but was: <missing>.");
    }

    @Test
    void testNodePresentOk() {
        assertThatJson("{\"test1\":2, \"test2\":1}").node("test2").isPresent();
    }

    @Test
    void testAnd() {
        assertThatJson(
                "{\"test1\":2, \"test2\":1}", json -> json.inPath("test1").isEqualTo(2), json -> json.inPath("test2")
                        .isEqualTo(1));
    }

    @Test
    void testNodePresentNull() {
        assertThatJson("{\"test1\":2, \"test2\":null}").node("test2").isPresent();
    }

    @Test
    void isPresentShouldTreatNullAsAbsentWhenSpecified() {
        assertThatThrownBy(() -> assertThatJson("{\"test1\":2, \"test2\":null}")
                        .when(Option.TREATING_NULL_AS_ABSENT)
                        .node("test2")
                        .isPresent())
                .hasMessage(
                        "Different value found in node \"test2\", expected: <node to be present> but was: <missing>.");
    }

    @Test
    void testMessage() {
        assertThatThrownBy(() ->
                        assertThatJson("{\"test\":1}").as("Test is different").isEqualTo("{\"test\":2}"))
                .hasMessage(
                        "[Test is different] JSON documents are different:\nDifferent value found in node \"test\", expected: <2> but was: <1>.\n");
    }

    @Test
    void testIgnoreDifferent() {
        assertThatJson("{\"test\":1}")
                .withConfiguration(c -> c.withIgnorePlaceholder("##IGNORE##"))
                .isEqualTo("{\"test\":\"##IGNORE##\"}");
    }

    @Test
    void anyNumberShouldFailOnNull() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":null}").isEqualTo("{\"test\":\"${json-unit.any-number}\"}"))
                .hasMessage(
                        "JSON documents are different:\nDifferent value found in node \"test\", expected: <a number> but was: <null>.\n");
    }

    @Test
    void anyNumberShouldFailOnObject() {
        assertThatThrownBy(() ->
                        assertThatJson("{\"test\":{\"a\":1}}").isEqualTo("{\"test\":\"${json-unit.any-number}\"}"))
                .hasMessage(
                        "JSON documents are different:\nDifferent value found in node \"test\", expected: <a number> but was: <{\"a\":1}>.\n");
    }

    @Test
    void anyArrayShouldAcceptArray() {
        assertThatJson("{\"test\":[1, 2]}").isEqualTo("{\"test\":\"${json-unit.any-array}\"}");
    }

    @Test
    void anyArrayShouldFailOnObject() {
        assertThatThrownBy(
                        () -> assertThatJson("{\"test\":{\"a\":1}}").isEqualTo("{\"test\":\"${json-unit.any-array}\"}"))
                .hasMessage(
                        "JSON documents are different:\nDifferent value found in node \"test\", expected: <an array> but was: <{\"a\":1}>.\n");
    }

    @Test
    void anyArrayShouldFailOnNull() {
        assertThatThrownBy(() -> assertThatJson("{\"test\": null}").isEqualTo("{\"test\":\"${json-unit.any-array}\"}"))
                .hasMessage(
                        "JSON documents are different:\nDifferent value found in node \"test\", expected: <an array> but was: <null>.\n");
    }

    @Test
    void anyBooleanShouldAcceptTrue() {
        assertThatJson("{\"test\":true}").isEqualTo("{\"test\":\"${json-unit.any-boolean}\"}");
    }

    @Test
    void emptinessCheck() {
        assertThatJson("{\"test\":{}}").node("test").isEqualTo("{}");
    }

    @Test
    void emptinessCheck2() {
        assertThatJson("{\"test\":{}}").node("test").isObject().isEmpty();
    }

    @Test
    void ifMatcherDoesNotMatchReportDifference() {
        RecordingDifferenceListener listener = new RecordingDifferenceListener();
        assertThatThrownBy(() -> assertThatJson("{\"test\":-1}")
                        .withMatcher("positive", greaterThan(valueOf(0)))
                        .withDifferenceListener(listener)
                        .isEqualTo("{\"test\": \"${json-unit.matches:positive}\"}"))
                .hasMessage(
                        "JSON documents are different:\nMatcher \"positive\" does not match value -1 in node \"test\". Expected a value greater than <0> but <-1> was less than <0>\n");

        assertThat(listener.getDifferenceList()).hasSize(1);
        assertThat(listener.getDifferenceList().get(0).toString())
                .isEqualTo("DIFFERENT Expected ${json-unit.matches:positive} in test got -1 in test");
    }

    @Test
    void parametrizedMatcherShouldFail() {
        Matcher<?> divisionMatcher = new DivisionMatcher();
        try {
            assertThatJson("{\"test\":5}")
                    .withMatcher("isDivisibleBy", divisionMatcher)
                    .isEqualTo("{\"test\": \"${json-unit.matches:isDivisibleBy}3\"}");
        } catch (AssertionError e) {
            assertEquals(
                    "JSON documents are different:\nMatcher \"isDivisibleBy\" does not match value 5 in node \"test\". Expected value divisible by <3> but was <5>\n",
                    e.getMessage());
        }
    }

    @Test
    void shouldEscapeDot() {
        assertThatJson("{\"name.with.dot\": \"value\"}")
                .node("name\\.with\\.dot")
                .isEqualTo("value");
    }

    @Test
    void pathShouldBeIgnoredForExtraKey() {
        assertThatJson("{\"root\":{\"test\":1, \"ignored\": 1}}")
                .withConfiguration(c -> c.whenIgnoringPaths("root.ignored"))
                .isEqualTo("{\"root\":{\"test\":1}}");
    }

    @Test
    void withFailMessageShouldWork() {
        assertThatThrownBy(() -> assertThatJson("{\"a\":{\"b\": 1}}")
                        .withFailMessage("It's broken")
                        .isEqualTo("{\"b\": 2}"))
                .hasMessage("It's broken");
    }

    @Test
    void describedAsShouldWork() {
        assertThatThrownBy(() -> assertThatJson("{\"a\":{\"b\": 1}}")
                        .describedAs("It's broken")
                        .isEqualTo("{\"b\": 2}"))
                .hasMessage(
                        """
                    [It's broken] JSON documents are different:
                    Different keys found in node "", missing: "b", extra: "a", expected: <{"b":2}> but was: <{"a":{"b":1}}>
                    """);
    }

    @Test
    void pathShouldBeIgnoredForDifferentValue() {
        assertThatJson("{\"root\":{\"test\":1, \"ignored\": 1}}")
                .whenIgnoringPaths("root.ignored")
                .isEqualTo("{\"root\":{\"test\":1, \"ignored\": 2}}");
    }

    @Test
    void pathShouldBeIgnoredForArrayExample() {
        assertThatJson("[{\"a\":1, \"b\":2},{\"a\":1, \"b\":3}]")
                .whenIgnoringPaths("[*].b")
                .isEqualTo("[{\"a\":1, \"b\":0},{\"a\":1, \"b\":0}]");
    }

    @SuppressWarnings({"UnusedMethod", "EffectivelyPrivate"})
    private static class TestBean {
        final BigDecimal demo;

        TestBean(BigDecimal demo) {
            this.demo = demo;
        }

        public BigDecimal getDemo() {
            return demo;
        }
    }

    @Test
    protected void shouldEqualNumberInObject() {
        TestBean actual = new TestBean(new BigDecimal("2.00"));
        String expected = "{ \"demo\": 2.00 }";
        assertThatJson(actual).withTolerance(0).isEqualTo(expected);
    }

    @Test
    void testEqualsToArray() {
        assertThatJson("{\"test\":[1,2,3]}").node("test").isEqualTo(new int[] {1, 2, 3});
    }

    @Test
    void assertContainsEntryNumber() {
        assertThatJson("{\"a\":1, \"b\":2.0}")
                .withTolerance(0)
                .isObject()
                .containsEntry("a", 1)
                .containsEntry("b", 2);
    }

    @Test
    protected void assertContainsEntryNumberFailure() {
        assertThatThrownBy(() -> assertThatJson("{\"a\":1, \"b\":2.0}")
                        .isObject()
                        .containsEntry("a", 1)
                        .containsEntry("b", 2))
                .hasMessage(
                        """
                    [Different value found in node ""]\s
                    Expecting map:
                      {"a":1,"b":2.0}
                    to contain:
                      ["b"=2]
                    but could not find the following map entries:
                      ["b"=2]
                    """);
    }

    @Test
    void testEqualsToList() {
        assertThatJson("{\"test\":[1,2,3]}").node("test").isEqualTo(asList(1, 2, 3));
    }

    @Test
    void testEqualsToObjectList() {
        assertThatJson("{\"test\":[{\"a\":1}, {\"b\":2}]}")
                .node("test")
                .isEqualTo(asList(readValue("{\"a\":1}"), readValue("{\"b\":2}")));
    }

    @Test
    void testEqualsToDoubleArray() {
        assertThatJson("{\"test\":[1.0,2.0,3.0]}").node("test").isEqualTo(new double[] {1, 2, 3});
    }

    @Test
    void testEqualsToBooleanArray() {
        assertThatJson("{\"test\":[true, false]}").node("test").isEqualTo(new boolean[] {true, false});
    }

    @Test
    void testEqualsToObjectArray() {
        assertThatJson("{\"test\":[{\"a\":1}, {\"b\":2}]}")
                .node("test")
                .isEqualTo(new @Nullable Object[] {readValue("{\"a\":1}"), readValue("{\"b\":2}")});
    }

    @Test
    void isArrayShouldFailIfArrayDoesNotExist() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":1}").node("test2").isArray())
                .hasMessage("Different value found in node \"test2\", expected: <array> but was: <missing>.");
    }

    @Test
    void isArrayShouldFailIfItIsNotArray() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":\"1\"}").node("test").isArray())
                .hasMessage("Node \"test\" has invalid type, expected: <array> but was: <\"1\">.");
    }

    @Test
    void arrayOfLengthShouldFailOnIncorrectSize() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":[1,2,3]}")
                        .node("test")
                        .isArray()
                        .hasSize(2))
                .hasMessage(
                        """
                    [Node "test"]\s
                    Expected size: 2 but was: 3 in:
                    [1, 2, 3]""");
    }

    @Test
    void shouldReportExtraArrayItemsWhenNotIgnoringOrder() {
        assertThatThrownBy(
                        () -> assertThatJson("{\"test\":[1,2,3]}").node("test").isEqualTo("[1]"))
                .hasMessage(
                        """
                    JSON documents are different:
                    Array "test" has different length, expected: <1> but was: <3>.
                    Array "test" has different content. Extra values: [2, 3], expected: <[1]> but was: <[1,2,3]>
                    """);
    }

    @Test
    void shouldReportExtraArrayItemsWhenIgnoringOrder() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":[1,2,3]}")
                        .when(IGNORING_ARRAY_ORDER)
                        .node("test")
                        .isEqualTo("[1]"))
                .hasMessage(
                        """
                    JSON documents are different:
                    Array "test" has different length, expected: <1> but was: <3>.
                    Array "test" has different content. Missing values: [], extra values: [2, 3], expected: <[1]> but was: <[1,2,3]>
                    """);
    }

    @Test
    void shouldReportMissingArrayItemsWhenNotIgnoringOrder() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":[1]}").node("test").isEqualTo("[1, 2, 3]"))
                .hasMessage(
                        """
                    JSON documents are different:
                    Array "test" has different length, expected: <3> but was: <1>.
                    Array "test" has different content. Missing values: [2, 3], expected: <[1,2,3]> but was: <[1]>
                    """);
    }

    @Test
    void shouldReportMissingArrayItemsWhenIgnoringOrder() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":[1]}")
                        .when(IGNORING_ARRAY_ORDER)
                        .node("test")
                        .isEqualTo("[1, 2, 3]"))
                .hasMessage(
                        """
                    JSON documents are different:
                    Array "test" has different length, expected: <3> but was: <1>.
                    Array "test" has different content. Missing values: [2, 3], extra values: [], expected: <[1,2,3]> but was: <[1]>
                    """);
    }

    @Test
    void shouldReportExtraArrayItemsAndDifferencesWhenNotIgnoringOrder() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":[\"x\",\"b\",\"c\"]}")
                        .node("test")
                        .isEqualTo("[\"a\"]"))
                .hasMessage(
                        """
                    JSON documents are different:
                    Array "test" has different length, expected: <1> but was: <3>.
                    Array "test" has different content. Extra values: ["b", "c"], expected: <["a"]> but was: <["x","b","c"]>
                    Different value found in node "test[0]", expected: <"a"> but was: <"x">.
                    """);
    }

    @Test
    void negativeArrayIndexShouldCountBackwards() {
        assertThatJson("{\"root\":{\"test\":[1,2,3]}}").node("root.test[-1]").isEqualTo(3);
    }

    @Test
    void negativeArrayIndexShouldCountBackwardsAndReportFailure() {
        assertThatThrownBy(() -> assertThatJson("{\"root\":{\"test\":[1,2,3]}}")
                        .node("root.test[-3]")
                        .isEqualTo(3))
                .hasMessage(
                        """
                    JSON documents are different:
                    Different value found in node "root.test[-3]", expected: <3> but was: <1>.
                    """);
    }

    @Test
    void negativeArrayIndexOutOfBounds() {
        assertThatThrownBy(() -> assertThatJson("{\"root\":{\"test\":[1,2,3]}}")
                        .node("root.test[-5]")
                        .isEqualTo(3))
                .hasMessage(
                        """
                    JSON documents are different:
                    Missing node in path "root.test[-5]".
                    """);
    }

    @Test
    void positiveArrayIndexOutOfBounds() {
        assertThatThrownBy(() -> assertThatJson("{\"root\":{\"test\":[1,2,3]}}")
                        .node("root.test[5]")
                        .isEqualTo(3))
                .hasMessage(
                        """
                    JSON documents are different:
                    Missing node in path "root.test[5]".
                    """);
    }

    @Test
    void arrayThatContainsShouldFailOnMissingNode() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":[{\"id\":36},{\"id\":37},{\"id\":38}]}")
                        .node("test")
                        .isArray()
                        .contains("{\"id\":42}"))
                .hasMessage(
                        """
                    [Node "test"]\s
                    Expecting JsonList:
                      [{"id":36}, {"id":37}, {"id":38}]
                    to contain:
                      ["{"id":42}"]
                    but could not find the following element(s):
                      ["{"id":42}"]
                    when comparing values using JsonComparator""");
    }

    @Test
    void testContains() {
        assertThatJson("[\"foo\", \"bar\", null, false, 1]")
                .isArray()
                .hasSize(5)
                .contains("foo", "bar", null, false, valueOf(1));
    }

    @Test
    void arrayContainsShouldMatch() {
        assertThatJson("[{\"a\": 7}, 8]").isArray().containsAnyOf(json("{\"a\": \"${json-unit.any-number}\"}"));
    }

    @Test
    void shouldNotParseValueTwice() {
        assertThatJson("{\"json\": \"{\\\"a\\\" : 1}\"}")
                .node("json")
                .isString()
                .isEqualTo("{\"a\" : 1}");
    }

    @Test
    void regexExample() {
        assertThatJson("{\"test\": \"ABCD\"}").isEqualTo("{\"test\": \"${json-unit.regex}[A-Z]+\"}");
    }

    @Test
    void pathEscapingWorks() {
        final String json = "{\"C:\\\\path\\\\file.ext\": {\"Status\": \"OK\"}}";
        final String pomPath = "C:\\path\\file.ext";

        System.out.println(json);
        assertThatJson(json).isObject().containsKey(pomPath);

        assertThatJson(json)
                .node(pomPath.replace(".", "\\."))
                .isPresent()
                .isObject()
                .contains(entry("Status", "OK"));
    }

    @Test
    void testArrayShouldMatchRegardlessOfOrder() {

        final String actual =
                "{\"response\":[{\"attributes\":null,\"employees\":[{\"dob\":\"1987-03-21\",\"firstName\":\"Joe\",\"lastName\":\"Doe\"},{\"dob\":\"1986-02-12\",\"firstName\":\"Jason\",\"lastName\":\"Kowalski\"},{\"dob\":\"1985-01-11\",\"firstName\":\"Kate\",\"lastName\":\"Smith\"}],\"id\":123}]}";
        final String expected =
                "{\"response\":[{\"attributes\":null,\"employees\":[{\"dob\":\"1985-01-11\",\"firstName\":\"Kate\",\"lastName\":\"Smith\"},{\"dob\":\"1986-02-12\",\"firstName\":\"Jason\",\"lastName\":\"Kowalski\"},{\"dob\":\"1987-03-21\",\"firstName\":\"Joe\",\"lastName\":\"Doe\"}],\"id\":123}]}";

        assertThatJson(actual).when(Option.IGNORING_ARRAY_ORDER).isEqualTo(expected);
    }

    @Test
    void objectShouldMatch() {
        assertThatJson("{\"test\":[{\"value\":1},{\"value\":2},{\"value\":3}]}")
                .node("test")
                .isArray()
                .allSatisfy(v -> assertThatJson(v).node("value").isNumber().isLessThan(valueOf(4)));
    }

    @Test
    void isStringShouldFailIfItIsNotAString() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":1}").node("test").isString())
                .hasMessage("Node \"test\" has invalid type, expected: <string> but was: <1>.");
    }

    @Test
    void isStringEqualToShouldPass() {
        assertThatJson("{\"test\":\"1\"}").node("test").isString().isEqualTo("1");
    }

    @Test
    void isSimplifiedStringEqualToShouldPass() {
        assertThatJson("{\"test\":\"1\"}").node("test").isStringEqualTo("1");
    }

    @Test
    void testIssue3SpaceStrings() {
        assertThatJson("{\"someKey\":\"a b\"}").node("someKey").isEqualTo("a b");
    }

    @Test
    void testTreatNullAsAbsent() {
        assertThatJson("{\"test\":{\"a\":1, \"b\": null}}")
                .when(TREATING_NULL_AS_ABSENT)
                .isEqualTo("{\"test\":{\"a\":1}}");
    }

    @Test
    void shouldIgnoreExtraFields() {
        assertThatJson("{\"test\":{\"a\":1, \"b\":2, \"c\":3}}")
                .when(IGNORING_EXTRA_FIELDS)
                .isEqualTo("{\"test\":{\"b\":2}}");
    }

    @Test
    void shouldIgnoreExtraFieldsAndOrderExample() {
        assertThatJson("{\"test\":[{\"key\":3},{\"key\":2, \"extraField\":2},{\"key\":1}]}")
                .when(IGNORING_EXTRA_FIELDS, IGNORING_ARRAY_ORDER)
                .isEqualTo("{\"test\":[{\"key\":1},{\"key\":2},{\"key\":3}]}");
    }

    @Test
    void andFailure() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":{\"a\":1, \"b\":2, \"c\":3}}")
                        .and(a -> a.node("test").isObject(), a -> a.node("test.b")
                                .isEqualTo(3)))
                .hasMessage(
                        """
                    JSON documents are different:
                    Different value found in node "test.b", expected: <3> but was: <2>.
                    """);
    }

    @Test
    void andSuccess() {
        assertThatJson("{\"test\":{\"a\":1, \"b\":2, \"c\":3}}")
                .and(a -> a.node("test").isObject(), a -> a.node("test.b").isEqualTo(2));
    }

    @Test
    void andNestedFailure() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":{\"a\":1, \"b\":2, \"c\":3}}")
                        .node("test")
                        .and(a -> a.node("a").isEqualTo(1), a -> a.node("b").isEqualTo(3)))
                .hasMessage(
                        """
                    JSON documents are different:
                    Different value found in node "test.b", expected: <3> but was: <2>.
                    """);
    }

    @Test
    void andNestedSuccess() {
        assertThatJson("{\"test\":{\"a\":1, \"b\":2, \"c\":3}}")
                .node("test")
                .and(a -> a.node("a").isEqualTo(1), a -> a.node("b").isEqualTo(2));
    }

    @Test
    void shouldAcceptEscapedPath() {
        assertThatJson("{\"foo.bar\":\"baz\"}").node("foo\\.bar").isEqualTo("baz");
    }

    @Test
    void shouldAcceptEscapedPathWithTwoDots() {
        assertThatJson("{\"foo.bar.baz\":\"baz\"}").node("foo\\.bar\\.baz").isEqualTo("baz");
    }

    @Test
    void shouldAcceptEscapedPathAndShowCorrectErrorMessage() {
        assertThatThrownBy(() -> assertThatJson("{\"foo.bar\":\"boo\"}")
                        .node("foo\\.bar")
                        .isEqualTo("baz"))
                .hasMessage(
                        """
                    JSON documents are different:
                    Different value found in node "foo\\.bar", expected: <"baz"> but was: <"boo">.
                    """);
    }

    @Test
    void testCompareArrays() {
        assertThatJson("[{\"b\": 10}]").isArray().isEqualTo(json("[{\"b\": 10}]"));
    }

    @Test
    void asStringShouldWork() {
        String json = "{\"myNum\": \"1140.53\"}";
        assertThatJson(json).node("myNum").asString().isEqualTo("1140.53");
    }

    @Test
    void shouldWorkWithPercentSign() {
        assertThatThrownBy(() -> assertThatJson("{\"a\": \"1\"}").isEqualTo("{\"%\": \"2\"}"))
                .hasMessage(
                        """
                    JSON documents are different:
                    Different keys found in node "", missing: "%", extra: "a", expected: <{"%":"2"}> but was: <{"a":"1"}>
                    """);
    }

    @SuppressWarnings("deprecation")
    @Test
    void shouldIgnoreFields() {
        assertThatJson("{\"root\":{\"test\":1, \"ignored\": 1}}")
                .isObject()
                .isEqualToIgnoringGivenFields("{\"root\":{\"test\":1, \"ignored\": 2}}", "root.ignored");
    }

    @Test
    void arrayWithStringBooleansShouldBeComparable() {
        assertThatJson("{\"array\": [\"true\"]}").node("array").isArray().containsExactly(value("true"));
    }

    @Test
    void stringArray() {
        assertThatJson("""
                            ["abc"]
            """)
                .isArray()
                .first()
                .isEqualTo(value("abc"));
    }

    // *****************************************************************************************************************
    // ********************************************** JSON Path ********************************************************
    // *****************************************************************************************************************
    @Test
    void jsonPathShouldBeAbleToUseObjects() {
        assertThatThrownBy(
                        () -> assertThatJson(json)
                                .describedAs("My little assert")
                                .inPath("$.store.book[0]")
                                .isEqualTo(
                                        """
                                    {
                                        "category": "reference",
                                        "author": "Nigel Rees",
                                        "title": "Sayings of the Century",
                                        "price": 8.96
                                    }\
                        """))
                .hasMessage(
                        """
                    JSON documents are different:
                    Different value found in node "$.store.book[0].price", expected: <8.96> but was: <8.95>.
                    """);
    }

    @Test
    void jsonPathWithIgnoredPaths() {
        assertThatJson(json)
                .withConfiguration(c -> c.whenIgnoringPaths("$.store.book[*].price"))
                .inPath("$.store.book[0]")
                .isEqualTo(
                        """
                                {
                                    "category": "reference",
                                    "author": "Nigel Rees",
                                    "title": "Sayings of the Century",
                                    "price": 999
                                }\
                    """);
    }

    @Test
    void ignoredJsonPaths() {
        assertThatJson(
                        """
                {
                     "category": "reference",
                     "author": "Nigel Rees",
                     "title": "Sayings of the Century",
                     "price": 1111
                }""")
                .withConfiguration(c -> c.whenIgnoringPaths("$..price"))
                .isEqualTo(
                        """
                                {
                                    "category": "reference",
                                    "author": "Nigel Rees",
                                    "title": "Sayings of the Century",
                                    "price": 999
                                }\
                    """);
    }

    @Test
    public void ignoredJsonPathComplex() {
        assertThatJson("{\"fields\":[" + "{\"key\":1, \"name\":\"AA\"},"
                        + "{\"key\":2, \"name\":\"AB\"},"
                        + "{\"key\":3, \"name\":\"AC\"}"
                        + "]}")
                .whenIgnoringPaths("$.fields[?(@.name=='AA')].key")
                .isEqualTo("{\"fields\":[" + "{\"key\":2, \"name\":\"AA\"},"
                        + "{\"key\":2, \"name\":\"AB\"},"
                        + "{\"key\":3, \"name\":\"AC\"}"
                        + "]}");
    }

    @Test
    void jsonPathWithIgnoredNonexistentPaths() {
        assertThatJson(json)
                .withConfiguration(c -> c.whenIgnoringPaths("$.rubbish"))
                .inPath("$.store.book[0]")
                .isEqualTo(
                        """
                                {
                                    "category": "reference",
                                    "author": "Nigel Rees",
                                    "title": "Sayings of the Century",
                                    "price": 8.95
                                }\
                    """);
    }

    @Test
    void jsonPathWithIgnoredPathsDeep() {
        assertThatJson(json)
                .whenIgnoringPaths("$..price")
                .withTolerance(0.01)
                .inPath("$.store.book[0]")
                .isEqualTo(
                        """
                                {
                                    "category": "reference",
                                    "author": "Nigel Rees",
                                    "title": "Sayings of the Century",
                                    "price": 999
                                }\
                    """);
    }

    @Test
    void jsonPathWithIgnoredPathsDeepBracketNotation() {
        assertThatJson(json)
                .whenIgnoringPaths("$..price")
                .inPath("$['store']['book'][0]")
                .isEqualTo(
                        """
                                {
                                    "category": "reference",
                                    "author": "Nigel Rees",
                                    "title": "Sayings of the Century",
                                    "price": 999
                                }\
                    """);
    }

    @Test
    void jsonPathWithNode() {
        assertThatJson(json).inPath("$.store.book[0]").node("title").isEqualTo("Sayings of the Century");
    }

    @Test
    void jsonPathWithNodeError() {
        assertThatThrownBy(() -> assertThatJson(json)
                        .inPath("$.store.book[0]")
                        .node("title")
                        .isEqualTo("Sayings of the Century2"))
                .hasMessage(
                        "JSON documents are different:\nDifferent value found in node \"$.store.book[0].title\", expected: <\"Sayings of the Century2\"> but was: <\"Sayings of the Century\">.\n");
    }

    @Test
    void jsonPathNumber() {
        assertThatJson(json).inPath("$..book.length()").isEqualTo(valueOf(4));
    }

    @Test
    void booleanArrayExtractionShouldWork() {
        assertThatJson("{\"fields\": [{ \"id\": \"test\", \"value\": \"true\" }]}")
                .inPath("$.fields[?(@.id==\"test\")].value")
                .isArray()
                .containsExactly(value("true"));
    }

    @Test
    void stringComparisonShouldWork() {
        final Map<String, Integer> map = new LinkedHashMap<>();
        map.put("FOO", 4);
        map.put("BAR", 3);
        assertThatJson("{\"fields\": [{ \"id\": \"test\", \"value\": \"{FOO=4, BAR=3}\" }]}")
                .inPath("$.fields[?(@.id==\"test\")].value")
                .isArray()
                .containsExactly(value(map.toString()));
    }

    @Test
    void testAbsentInJsonPath() {
        assertThatJson("{}").inPath("$.abc").isAbsent();
    }

    @Test
    void testAbsentInJsonPathEquals() {
        assertThatThrownBy(() -> assertThatJson("{}").inPath("$.abc").isEqualTo("value"))
                .hasMessage(
                        """
                    JSON documents are different:
                    Missing node in path "$.abc".
                    """);
    }

    @Test
    void testAbsentInJsonPathIsArray() {
        assertThatThrownBy(() -> assertThatJson("{}").inPath("$.abc").isArray())
                .hasMessage("Different value found in node \"$.abc\", expected: <array> but was: <missing>.");
    }

    @Test
    void testAbsentInJsonPathNotAbsent() {
        assertThatThrownBy(() -> assertThatJson("{\"abc\": 1}").inPath("$.abc").isAbsent())
                .hasMessage("Different value found in node \"$.abc\", expected: <node to be absent> but was: <1>.");
    }

    @Test
    protected void jsonPathShouldBeAbleToUseArrays() {
        assertThatThrownBy(
                        () -> assertThatJson(json)
                                .inPath("$.store.book")
                                .isArray()
                                .contains(
                                        json(
                                                """
                                        {
                                            "category": "reference",
                                            "author": "Nigel Rees",
                                            "title": "Sayings of the Century",
                                            "price": 8.96
                                        }\
                            """)))
                .hasMessage(
                        """
                    [Node "$.store.book"]\s
                    Expecting JsonList:
                      [{"author":"Nigel Rees","category":"reference","price":8.95,"title":"Sayings of the Century"},
                        {"author":"Evelyn Waugh","category":"fiction","price":12.99,"title":"Sword of Honour"},
                        {"author":"Herman Melville","category":"fiction","isbn":"0-553-21311-3","price":8.99,"title":"Moby Dick"},
                        {"author":"J. R. R. Tolkien","category":"fiction","isbn":"0-395-19395-8","price":22.99,"title":"The Lord of the Rings"}]
                    to contain:
                      [{"category":"reference","author":"Nigel Rees","title":"Sayings of the Century","price":8.96}]
                    but could not find the following element(s):
                      [{"category":"reference","author":"Nigel Rees","title":"Sayings of the Century","price":8.96}]
                    when comparing values using JsonComparator""");
    }

    @Test
    void jsonPathShouldBeAbleToUseArraysDeep() {
        assertThatJson(json)
                .inPath("$.store.book[*].category")
                .isArray()
                .containsExactlyInAnyOrder("fiction", "reference", "fiction", "fiction");
    }

    @Test
    void jsonPathShouldBeAbleToUseArraysFromObject() {
        assertThatJson(readValue(json))
                .inPath("$.store.book[*].category")
                .isArray()
                .containsExactlyInAnyOrder("fiction", "reference", "fiction", "fiction");
    }

    @Test
    void ignoreExtraArrayItemsExample() {
        assertThatJson("{\"test\":[1,2,3,4]}").when(IGNORING_EXTRA_ARRAY_ITEMS).isEqualTo("{\"test\":[1,2,3]}");
    }

    @Test
    void ignoreExtraArrayItemsAndOrderExample() {
        assertThatJson("{\"test\":[5,5,4,4,3,3,2,2,1,1]}")
                .when(IGNORING_EXTRA_ARRAY_ITEMS, IGNORING_ARRAY_ORDER)
                .isEqualTo("{\"test\":[1,2,3]}");
    }

    @Test
    void testArrayBug() {
        assertThatJson(
                        """
                [
                      {"value": "1", "title": "Entity", "info": "Entity info"},
                      {"value": "2", "title": "Column", "info": "Column info"},
                      {"value": "3", "title": "Table", "info": "Table info"},
                      {"value": "4", "title": "Schema", "info": "Schema info"}
                    ]""")
                .inPath("$[?(@.value =='1')]")
                .isArray()
                .last()
                .isEqualTo(json("{\"value\": \"1\", \"title\": \"Entity\", \"info\": \"Entity info\"}"));
    }

    @Test
    void testArrayNode() {
        assertThatJson(
                        """
                [
                      {"value": "1", "title": "Entity", "info": "Entity info"},
                      {"value": "2", "title": "Column", "info": "Column info"},
                      {"value": "3", "title": "Table", "info": "Table info"},
                      {"value": "4", "title": "Schema", "info": "Schema info"}
                    ]""")
                .inPath("$[?(@.value =='1')]")
                .isArray()
                .first()
                .node("title")
                .isString()
                .isEqualTo("Entity");
    }

    @Test
    void assertSame() {
        String s = "{ \"a\": 0.0 }";
        assertThatJson(s).isEqualTo(s);
    }

    @Test
    void testArrayElements() {
        String json = "[ [ \"a\", \"b\", \"c\", \"d\"," + " \"e\", \"f\", \"g\", \"h\", \"i\" ] ]";

        assertThatJson(json)
                .inPath("$.[0]")
                .isArray()
                .elements(3 /* d */, 4 /* e */, 5 /* f */, 6 /* g */, 7 /* h */, 8 /* i */)
                .containsExactly("d", "e", "f", "g", "h", "i");
    }

    @Test
    void testInnerString() {
        String json = "{\"myNode\":{\"inner\":\"foo\"}}";
        assertThatJson(json).inPath("$.myNode.inner").isString().isEqualTo("foo");
    }

    @Test
    void testInnerQuotedString() {
        String json = "{\"myNode\":{\"inner\":\"\\\"foo\\\"\"}}";
        assertThatJson(json).inPath("$.myNode.inner").isString().isEqualTo("\"foo\"");
    }

    @Test
    void testInnerNumber() {
        final String json = "{\"myNode\":{\"inner\":123}}";
        assertThatJson(json).inPath("$.myNode.inner").isNumber().isEqualByComparingTo("123");
    }

    @Test
    void shouldUseInPathInAnd() {
        ConfigurableJsonAssert json = assertThatJson("{\"key1\":\"foo\",\"key2\":\"bar\"}");
        json.inPath("key1").isEqualTo("foo");
        json.inPath("key2").isEqualTo("bar");
    }

    @Test
    void shouldIgnoreArrayOrderInSpecificPath() {
        assertThatJson("{\"obj\":{\"a\": [1, 2], \"b\": [3, 4]}}")
                .when(path("obj.a"), then(IGNORING_ARRAY_ORDER))
                .isEqualTo("{\"obj\":{\"a\": [2, 1], \"b\": [3, 4]}}");
    }

    @Test
    void shouldNotIgnoreArrayOrderWhenNotSpecified() {
        assertThatJson("{\"obj\":{\"a\": [1, 2], \"b\": [3, 4]}}")
                .when(path("obj.a"), then(IGNORING_ARRAY_ORDER))
                .isNotEqualTo("{\"obj\":{\"a\": [2, 1], \"b\": [4, 3]}}");
    }

    @Test
    void shouldExcludeIgnoringArrayOrderFromPath() {
        assertThatJson("[{\"b\":[4,5,6]},{\"b\":[1,2,3]}]")
                .when(IGNORING_ARRAY_ORDER)
                .when(path("[*].b"), thenNot(IGNORING_ARRAY_ORDER))
                .isEqualTo("[{\"b\":[1,2,3]},{\"b\":[4,5,6]}]");
    }

    @Test
    void shouldIgnoreArrayOrderInSpecificJsonPath() {
        assertThatJson("{\"obj\":{\"a\": [1, 2], \"b\": [3, 4]}}")
                .when(path("$..a"), then(IGNORING_ARRAY_ORDER))
                .isEqualTo("{\"obj\":{\"a\": [2, 1], \"b\": [3, 4]}}");
    }

    @Test
    void shouldExcludeIgnoringArrayOrderFromPathAndIgnoreInRoot() {
        assertThatJson("[{\"b\":[4,5,6]},{\"b\":[1,2,3]},{\"b\":[7,8,9]}]")
                .when(rootPath(), then(IGNORING_ARRAY_ORDER, IGNORING_EXTRA_ARRAY_ITEMS))
                .when(path("[*].b"), thenNot(IGNORING_ARRAY_ORDER))
                .isEqualTo("[{\"b\":[1,2,3]},{\"b\":[4,5,6]}]");
    }

    @Test
    void shouldIgnoreArrayOrderEverywhereButTheFirstElement() {
        assertThatThrownBy(() -> assertThatJson("[{\"b\":[1,3,2]},{\"b\":[5,4,6]},{\"b\":[8,7,9]}]")
                        .when(path("[*].b"), then(IGNORING_ARRAY_ORDER))
                        .when(path("[0].b"), thenNot(IGNORING_ARRAY_ORDER))
                        .isEqualTo("[{\"b\":[1,2,3]},{\"b\":[4,5,6]},{\"b\":[7,8,9]}]"))
                .hasMessage(
                        """
                    JSON documents are different:
                    Different value found in node "[0].b[1]", expected: <2> but was: <3>.
                    Different value found in node "[0].b[2]", expected: <3> but was: <2>.
                    """);
    }

    @Test
    void shouldIgnoreArrayOrderInSeveralSpecificPaths() {
        assertThatJson("[{\"b\":[3,1,2],\"c\":[-2,-3,-1]},{\"b\":[6,5,4],\"c\":[-5,-4,-6]}]")
                .when(paths("[*].b", "[*].c"), then(IGNORING_ARRAY_ORDER))
                .isEqualTo("[{\"b\":[1,2,3],\"c\":[-1,-2,-3]},{\"b\":[4,5,6],\"c\":[-4,-5,-6]}]");
    }

    @Test
    void shouldTreatNullAsAbsentInSpecificPath() {
        assertThatJson("{\"a\":1,\"b\":null}")
                .when(path("b"), then(TREATING_NULL_AS_ABSENT))
                .isEqualTo("{\"a\":1}");
    }

    @Test
    void shouldNotTreatNullAsAbsentWhenNotSpecified() {
        assertThatThrownBy(() -> assertThatJson("{\"a\":1,\"b\":null,\"c\":null}")
                        .when(path("b"), then(TREATING_NULL_AS_ABSENT))
                        .isEqualTo("{\"a\":1}"))
                .hasMessage(
                        """
                    JSON documents are different:
                    Different keys found in node "", extra: "c", expected: <{"a":1}> but was: <{"a":1,"b":null,"c":null}>
                    """);
    }

    @Test
    void shouldIgnoreExtraFieldsInSpecificPath() {
        assertThatJson("{\"a\":{\"a1\":1,\"a2\":2}}")
                .when(path("a"), then(IGNORING_EXTRA_FIELDS))
                .isEqualTo("{\"a\":{\"a1\":1}}");
    }

    @Test
    void shouldNotIgnoreExtraFieldsWhenNotSpecified() {
        assertThatThrownBy(() -> assertThatJson("{\"a\":{\"a1\":1,\"a2\":2},\"b\":{\"b1\":1,\"b2\":2}}")
                        .when(path("a"), then(IGNORING_EXTRA_FIELDS))
                        .isEqualTo("{\"a\":{\"a1\":1},\"b\":{\"b1\":1}}"))
                .hasMessage(
                        """
                    JSON documents are different:
                    Different keys found in node "b", extra: "b.b2", expected: <{"b1":1}> but was: <{"b1":1,"b2":2}>
                    """);
    }

    @Test
    void shouldIgnoreExtraArrayItemsInSpecificPath() {
        assertThatJson("{\"a\":[1,2,3]}")
                .when(path("a"), then(IGNORING_EXTRA_ARRAY_ITEMS))
                .isEqualTo("{\"a\":[1,2]}");
    }

    @Test
    void shouldUseAsInstanceOfToMoveToJsonUnit() {
        record DummyResponse(String trackingId, String json) {}
        DummyResponse resp = new DummyResponse("abcd-0001", "{ \"foo\": \"bar\" }");

        assertThat(resp)
                .hasFieldOrPropertyWithValue("trackingId", "abcd-0001") // <- Assertj API
                .extracting("json")
                .asInstanceOf(JSON) // <- JsonUnit API
                .isObject()
                .containsEntry("foo", "bar");
    }

    @Test
    void shouldUseAsInstanceOfToMoveFromJsonUnit() {
        assertThatJson("{\"a\":[1, 2, 3]}")
                .inPath("a")
                .isArray()
                .first(BIG_DECIMAL)
                .isEqualTo("1");
        assertThatJson("{\"a\":[1, 2, true]}")
                .inPath("a")
                .isArray()
                .last(BOOLEAN)
                .isEqualTo(true);
        assertThatJson("{\"a\":[1, \"s\", true]}")
                .inPath("a")
                .isArray()
                .element(1, STRING)
                .startsWith("s");
        assertThatJson("{\"a\":{\"b\": \"c\"}}")
                .inPath("a")
                .isObject()
                .extracting("b", STRING)
                .endsWith("c");
        assertThatJson("{\"a\":[1, 2, 3]}").inPath("a").asInstanceOf(LIST).hasSize(3);
        assertThatJson("{\"a\":[1, 2, 3]}")
                .inPath("a")
                .asInstanceOf(LIST)
                .first(BIG_DECIMAL)
                .isEqualTo("1");
        assertThatThrownBy(() -> assertThatJson("{\"a\": null}").inPath("a").asInstanceOf(BIG_DECIMAL))
                .isInstanceOf(AssertionError.class)
                .hasMessage("\nExpecting actual not to be null");
    }

    @Test
    void shouldNotIgnoreExtraArrayItemsWhenNotSpecified() {
        assertThatThrownBy(() -> assertThatJson("{\"a\":[1,2,3],\"b\":[1,2,3]}")
                        .when(path("a"), then(IGNORING_EXTRA_ARRAY_ITEMS))
                        .isEqualTo("{\"a\":[1,2],\"b\":[1,2]}"))
                .hasMessage(
                        """
                    JSON documents are different:
                    Array "b" has different length, expected: <2> but was: <3>.
                    Array "b" has different content. Extra values: [3], expected: <[1,2]> but was: <[1,2,3]>
                    """);
    }

    @Test
    void shouldIgnoreValuesInSpecificPath() {
        assertThatJson("{\"a\":2,\"b\":\"string2\"}")
                .when(paths("a", "b"), then(IGNORING_VALUES))
                .isEqualTo("{\"a\":1,\"b\":\"string\"}");
    }

    @Test
    void shouldIgnoreValuesInSpecificPathWrittenSeparately() {
        assertThatJson("{\"a\":2,\"b\":\"string2\"}")
                .when(path("a"), then(IGNORING_VALUES))
                .when(path("b"), then(IGNORING_VALUES))
                .isEqualTo("{\"a\":1,\"b\":\"string\"}");
    }

    @Test
    void shouldIgnoreAbsentB() {
        assertThatJson("{\"A\":1,\"B\":null}")
                .when(path("B"), then(TREATING_NULL_AS_ABSENT))
                .isEqualTo("{\"A\":1}");
    }

    @Test
    void shouldNotIgnoreValuesWhenNotSpecified() {
        assertThatThrownBy(() -> assertThatJson("{\"a\":2,\"b\":\"string2\",\"c\":3}")
                        .when(paths("a", "b"), then(IGNORING_VALUES))
                        .isEqualTo("{\"a\":1,\"b\":\"string\",\"c\":2}"))
                .hasMessage(
                        """
                    JSON documents are different:
                    Different value found in node "c", expected: <2> but was: <3>.
                    """);
    }

    @Test
    void shouldIgnoreMultiplePaths() {
        assertThatJson("{\"a\":1,\"b\":2,\"c\":3}")
                .when(paths("a", "b"), thenIgnore())
                .isEqualTo("{\"c\":3}");
    }

    @Test
    void shouldIgnoreMultiplePathsWrittenSeparately() {
        assertThatJson("{\"a\":1,\"b\":2,\"c\":3}")
                .when(path("a"), thenIgnore())
                .when(path("b"), thenIgnore())
                .isEqualTo("{\"c\":3}");
    }

    @Test
    void shouldFailFast() {
        assertThatThrownBy(() -> assertThatJson("{\"a\":{\"a1\": 1},\"b\":{\"b1\": 1}}")
                        .withOptions(Option.FAIL_FAST)
                        .isEqualTo("{\"a\":{\"a1\": 2},\"b\":{\"b1\": 2}}"))
                .hasMessage(
                        """
                    JSON documents are different:
                    Different value found in node "a.a1", expected: <2> but was: <1>.
                    """);
    }

    @Test
    void hamcrestMessageTest() {
        assertThatThrownBy(
                        () -> assertThatJson(
                                        """
                {
                  "someText": "abc123"
                }
                """)
                                .withMatcher("exampleMatcher", org.hamcrest.Matchers.equalTo("def456"))
                                .isEqualTo(
                                        """
                        {
                          "someText": "${json-unit.matches:exampleMatcher}"
                        }
                        """))
                .hasMessage(
                        """
                    JSON documents are different:
                    Matcher "exampleMatcher" does not match value "abc123" in node "someText". Expected "def456" but was "abc123"
                    """);
    }

    @Nested
    protected class ReportAsString {
        @Test
        protected void shouldNormalizeComplexJsons() {
            assertThatThrownBy(() -> assertThatJson("{\"a\": {\"c\": [{\"e\": 2, \"f\": 3}, 3]}, \"b\": false}")
                            .when(REPORTING_DIFFERENCE_AS_NORMALIZED_STRING)
                            .isEqualTo("{\"b\": true, \"a\": {\"c\": [{\"f\": 2, \"e\": 3}, 5]}}"))
                    .hasMessage(
                            """
                        JSON documents are different: expected <{
                          "b": true,
                          "a": {
                            "c": [
                              {
                                "f": 2,
                                "e": 3
                              },
                              5
                            ]
                          }
                        }>but was <{
                          "b": false,
                          "a": {
                            "c": [
                              {
                                "f": 3,
                                "e": 2
                              },
                              3
                            ]
                          }
                        }>""");
        }

        @Test
        void shouldPrintMapKeysInTheSameOrderAsExpected() {
            assertThatThrownBy(() -> assertThatJson("{\"d\": 1, \"c\": 2, \"b\": 3}")
                            .when(REPORTING_DIFFERENCE_AS_NORMALIZED_STRING)
                            .isEqualTo("{\"a\": 1, \"b\": 3, \"c\": 2}"))
                    .hasMessage(
                            """
                        JSON documents are different: expected <{
                          "a": 1,
                          "b": 3,
                          "c": 2
                        }>but was <{
                          "b": 3,
                          "c": 2,
                          "d": 1
                        }>""");
        }

        @Test
        void shouldPrintMapKeysInTheSameOrderAsInArrays() {
            assertThatThrownBy(() -> assertThatJson("{\"root\":[{\"d\": 1, \"c\": 2, \"b\": 3}]}")
                            .when(REPORTING_DIFFERENCE_AS_NORMALIZED_STRING)
                            .isEqualTo("{\"root\":[{\"a\": 1, \"b\": 3, \"c\": 2}]}"))
                    .hasMessage(
                            """
                        JSON documents are different: expected <{
                          "root": [
                            {
                              "a": 1,
                              "b": 3,
                              "c": 2
                            }
                          ]
                        }>but was <{
                          "root": [
                            {
                              "b": 3,
                              "c": 2,
                              "d": 1
                            }
                          ]
                        }>""");
        }

        @Test
        void shouldNotBreakOnDifferentType() {
            assertThatThrownBy(() -> assertThatJson("{\"a\": {\"b\": 1}}")
                            .when(REPORTING_DIFFERENCE_AS_NORMALIZED_STRING)
                            .isEqualTo("{\"a\": [\"b\", 1]}"))
                    .hasMessage(
                            """
                        JSON documents are different: expected <{
                          "a": [
                            "b",
                            1
                          ]
                        }>but was <{
                          "a": {
                            "b": 1
                          }
                        }>""");
        }

        @Test
        void shouldWorkWithMissingPath() {
            assertThatThrownBy(() -> assertThatJson("{\"a\": 1}")
                            .when(REPORTING_DIFFERENCE_AS_NORMALIZED_STRING)
                            .inPath("c")
                            .isEqualTo("{\"b\": true}"))
                    .hasMessage(
                            """
                        JSON documents are different:
                        Missing node in path "c".
                        """);
        }

        @Test
        void shouldWorkWithPaths() {
            assertThatThrownBy(() -> assertThatJson("{\"a\": {\"c\": [{\"e\": 2}, 3]}, \"b\": false}")
                            .when(REPORTING_DIFFERENCE_AS_NORMALIZED_STRING)
                            .inPath("a.c")
                            .isEqualTo("[{\"e\": 3}, 5]"))
                    .hasMessage(
                            """
                        JSON documents are different: expected <[
                          {
                            "e": 3
                          },
                          5
                        ]>but was <[
                          {
                            "e": 2
                          },
                          3
                        ]>""");
        }
    }

    private static final String json =
            """
            {
                "store": {
                    "book": [
                        {
                            "category": "reference",
                            "author": "Nigel Rees",
                            "title": "Sayings of the Century",
                            "price": 8.95
                        },
                        {
                            "category": "fiction",
                            "author": "Evelyn Waugh",
                            "title": "Sword of Honour",
                            "price": 12.99
                        },
                        {
                            "category": "fiction",
                            "author": "Herman Melville",
                            "title": "Moby Dick",
                            "isbn": "0-553-21311-3",
                            "price": 8.99
                        },
                        {
                            "category": "fiction",
                            "author": "J. R. R. Tolkien",
                            "title": "The Lord of the Rings",
                            "isbn": "0-395-19395-8",
                            "price": 22.99
                        }
                    ],
                    "bicycle": {
                        "color": "red",
                        "price": 19.95
                    }
                },
                "expensive": 10
            }""";

    protected abstract @Nullable Object readValue(String value);
}
