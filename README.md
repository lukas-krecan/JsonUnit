JsonUnit [![Apache License 2](https://img.shields.io/badge/license-ASF2-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.txt) [![Build Status](https://github.com/lukas-krecan/JsonUnit/workflows/CI/badge.svg)](https://github.com/lukas-krecan/JsonUnit/actions) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/net.javacrumbs.json-unit/json-unit/badge.svg)](https://maven-badges.herokuapp.com/maven-central/net.javacrumbs.json-unit/json-unit)
========

JsonUnit is a library that simplifies JSON comparison in tests.

- [APIs](#apis)
  * [AssertJ integration](#assertj)
  * [Hamcrest matchers](#hamcrest)
  * [Spring MVC assertions](#spring)
  * [Spring WebTestClient](#spring-web-client)
  * [Spring RestTestClient](#spring-rest-client)
  * [Spring REST client assertions](#spring-client)
  * [Kotest assertions](#kotest)
- [Features](#features)
  * [JsonPath support](#jsonpath)
  * [Ignoring values](#ignorevalues)
  * [Ignoring elements](#ignoreelements)
  * [Ignoring paths](#ignorepaths)
  * [Regular expressions](#regexp)
  * [Type placeholders](#typeplc)
  * [Custom matchers](#matchers)
  * [Options](#options)
  * [Array indexing](#arrayIndexing)
  * [Numerical comparison](#numbers)
  * [Escaping dots](#dots)
  * [Lenient parsing of expected value](#lenient-parsing-of-expected-value)
  * [DifferenceListener](#differencelistener)
  * [Jackson Object Mapper customization](#jackson-object-mapper-customization)
- [Release notes](#release-notes)


# <a name="apis"></a>APIs
There are several different APIs you can use. They all have more or less the same features, just the usage is
slightly different.

## <a name="assertj"></a>AssertJ integration
The recommended API is AssertJ integration which combines the power of JsonUnit and AssertJ.

```java
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;

...

// compares two JSON documents (note lenient parsing of expected value)
assertThatJson("{\"a\":1, \"b\":2}").isEqualTo("{b:2, a:1}");

// objects are automatically serialized before comparison
assertThatJson(jsonObject).isEqualTo("{\n\"test\": 1\n}");

// AssertJ map assertions (numbers are converted to BigDecimals)
assertThatJson("{\"a\":1}").isObject().containsEntry("a", BigDecimal.valueOf(1));

// Type placeholders
assertThatJson("{\"a\":1, \"b\": {\"c\" :3}}")
    .isObject().containsValue(json("{\"c\" :\"${json-unit.any-number}\"}"));

// AssertJ string assertion
assertThatJson("{\"a\": \"value\"")
    .node("a").isString().isEqualTo("value");

// AssertJ array assertion
assertThatJson("{\"a\":[{\"b\": 1}, {\"c\": 1}, {\"d\": 1}]}")
    .node("a").isArray().contains(json("{\"c\": 1}"));

// Can ignore array order
assertThatJson("{\"a\":[{\"b\": 1}, {\"c\": 1}, {\"d\": 1}]}")
    .when(Option.IGNORING_ARRAY_ORDER).node("a").isArray()
    .isEqualTo(json("[{\"c\": 1}, {\"b\": 1} ,{\"d\": 1}]"));

// custom matcher
assertThatJson("{\"test\":-1}")
    .withConfiguration(c -> c.withMatcher("positive", greaterThan(valueOf(0))))
    .isEqualTo("{\"test\": \"${json-unit.matches:positive}\"}");

// and
assertThatJson("{\"test\":{\"a\":1, \"b\":2, \"c\":3}}").and(
    a -> a.node("test.a").isEqualTo(1),
    a -> a.node("test.b").isEqualTo(2)
);

// JsonPath support
assertThatJson(json)
    .inPath("$.store.book")
    .isArray()
    .contains(json(
        "            {\n" +
            "                \"category\": \"reference\",\n" +
            "                \"author\": \"Nigel Rees\",\n" +
            "                \"title\": \"Sayings of the Century\",\n" +
            "                \"price\": 8.96\n" +
            "            }"
    ));
```

JsonUnit tries to be clever when parsing the expected value. If the value can be parsed as valid JSON, it's
parsed so. If it can't be parsed, it's considered to be just a string to be compared. It usually works,
but it can lead to unexpected situations, usually with primitive values like numbers and booleans.

```java
// This test does NOT pass. "1" is parsed as JSON containing number 1, the actual value is a string.
assertThatJson("{\"id\":\"1\", \"children\":[{\"parentId\":\"1\"}]}")
    .inPath("children[*].parentId")
    .isArray()
    .containsOnly("1");

// You have to wrap the expected value by `JsonAssertions.value()`
// to prevent parsing
assertThatJson("{\"id\":\"1\", \"children\":[{\"parentId\":\"1\"}]}")
    .inPath("children[*].parentId")
    .isArray()
    .containsOnly(value("1"));

// "true" is valid JSON so it gets parsed to primitive `true`
// Have to wrap it to JsonAssertions.value() in order to make sure it's not parsed
assertThatJson("{\"root\":[\"true\"]}").node("root").isArray().containsExactly(value("true"));
```

On the other hand, if you want to make sure that the expected value is parsed as JSON, use `JsonAssertions.json()`.

### Support for asInstanceOf
You can move between vanilla AssertJ and JsonUnit using `asInstanceOf` as in the following example:
```java
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.JSON;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

...

record DummyResponse(String trackingId, String json) {}
DummyResponse resp = new DummyResponse("abcd-0001", "{ \"foo\": \"bar\" }");

assertThat(resp)
        .hasFieldOrPropertyWithValue("trackingId", "abcd-0001") // <- Assertj API
        .extracting("json")
        .asInstanceOf(JSON)
        .isObject() // <- JsonUnit API
        .containsEntry("foo", "bar");
```


### Kotlin support
Following Kotlin API is supported (notice different import)

```kotlin
// Kotlin
import net.javacrumbs.jsonunit.assertj.assertThatJson

assertThatJson("""{"root":{"a":1, "b": 2}}""") {
    isObject
    node("root.a").isEqualTo(1)
    node("root.b").isEqualTo(2)
}
```


To use AssertJ integration, import

```xml
<dependency>
    <groupId>net.javacrumbs.json-unit</groupId>
    <artifactId>json-unit-assertj</artifactId>
    <version>5.0.0</version>
    <scope>test</scope>
</dependency>
```
For more examples see [the tests](https://github.com/lukas-krecan/JsonUnit/blob/master/tests/test-base/src/main/java/net/javacrumbs/jsonunit/test/base/AbstractAssertJTest.java).

## <a name="hamcrest"></a>Hamcrests matchers
You use Hamcrest matchers in the following way

```java
import static net.javacrumbs.jsonunit.JsonMatchers.*;
import static org.junit.Assert.*;
import static net.javacrumbs.jsonunit.core.util.ResourceUtils.resource;
...

assertThat("{\"test\":1}", jsonEquals("{\"test\": 1}"));
assertThat("{\"test\":1}", jsonPartEquals("test", 1));
assertThat("{\"test\":[1, 2, 3]}", jsonPartEquals("test[0]", 1));

assertThat("{\"test\":{\"a\":1, \"b\":2, \"c\":3}}",
    jsonEquals("{\"test\":{\"b\":2}}").when(IGNORING_EXTRA_FIELDS));

// Can use other Hamcrest matchers too
assertThat("{\"test\":1}", jsonPartMatches("test", is(valueOf(1))))

assertThat("{\"test\":1}", jsonEquals(resource("test.json")));
```

To use import
```xml
<dependency>
    <groupId>net.javacrumbs.json-unit</groupId>
    <artifactId>json-unit</artifactId>
    <version>5.0.0</version>
    <scope>test</scope>
</dependency>
```

For more examples see [the tests](https://github.com/lukas-krecan/JsonUnit/blob/master/tests/test-base/src/main/java/net/javacrumbs/jsonunit/test/base/AbstractJsonMatchersTest.java).

## <a name="spring"></a>Spring MVC assertions
JsonUnit supports Spring MVC test assertions. For example

```java
import static net.javacrumbs.jsonunit.spring.JsonUnitResultMatchers.json;
...

mockMvc.perform(get("/sample").andExpect(
    json().isEqualTo("{\"result\":{\"string\":\"stringValue\", \"array\":[1, 2, 3],\"decimal\":1.00001}}")
);
mockMvc.perform(get("/sample").andExpect(
    json().node("result.string2").isAbsent()
);
mockMvc.perform(get("/sample").andExpect(
    json().node("result.array").when(Option.IGNORING_ARRAY_ORDER).isEqualTo(new int[]{3, 2, 1})
);
mockMvc.perform(get("/sample").andExpect(
    json().node("result.array").matches(everyItem(lessThanOrEqualTo(valueOf(4))))
);
```

Following Kotlin DSL is supported:

```kotlin
mockMvc.get(path).andExpect {
    jsonContent {
        node("root").isEqualTo(CORRECT_JSON)
    }
}
```

Inside `jsonContent` you have access to all AssertJ API capabilities as described [here](#assertj).

To use import
```xml
<dependency>
    <groupId>net.javacrumbs.json-unit</groupId>
    <artifactId>json-unit-spring</artifactId>
    <version>5.0.0</version>
    <scope>test</scope>
</dependency>
```

For more examples see [the tests](https://github.com/lukas-krecan/JsonUnit/blob/master/json-unit-spring/src/test/java/net/javacrumbs/jsonunit/spring/testit/MockMvcTest.java).

## <a name="spring-web-client"></a>Spring WebTestClient
To integrate with Spring WebTest client do

```java
import static net.javacrumbs.jsonunit.spring.WebTestClientJsonMatcher.json;
...

client.get().uri(path).exchange().expectBody().consumeWith(
    json().isEqualTo("{\"result\":{\"string\":\"stringValue\", \"array\":[1, 2, 3],\"decimal\":1.00001}}")
);
client.get().uri(path).exchange().expectBody().consumeWith(
    json().node("result.string2").isAbsent()
);
client.get().uri(path).exchange().expectBody().consumeWith(
    json().node("result.array").when(Option.IGNORING_ARRAY_ORDER).isEqualTo(new int[]{3, 2, 1})
);
client.get().uri(path).exchange().expectBody().consumeWith(
    json().node("result.array").matches(everyItem(lessThanOrEqualTo(valueOf(4))))
);
```

For Kotlin, you can use our bespoke DSL

```kotlin
import net.javacrumbs.jsonunit.spring.jsonContent
...
client.get().uri(path).exchange().expectBody()
    .jsonContent {
        isEqualTo(CORRECT_JSON)
    }
```

Import
```xml
<dependency>
    <groupId>net.javacrumbs.json-unit</groupId>
    <artifactId>json-unit-spring</artifactId>
    <version>5.0.0</version>
    <scope>test</scope>
</dependency>
```

For more examples see [the tests](https://github.com/lukas-krecan/JsonUnit/blob/master/json-unit-spring/src/test/java/net/javacrumbs/jsonunit/spring/testit/WebTestClientTest.java).

## <a name="spring-rest-client"></a>Spring RestTestClient
Spring RestTestClient is a new testing utility introduced in Spring Framework that combines features of MockMvc and WebTestClient. JsonUnit provides seamless integration with it.

```java
import static net.javacrumbs.jsonunit.spring.RestTestClientJsonMatcher.json;
...

client.get().uri(path).accept(MediaType.APPLICATION_JSON)
    .exchange()
    .expectBody()
    .consumeWith(json().isEqualTo(CORRECT_JSON));

client.get().uri(path).exchange().expectBody()
    .consumeWith(json().node("result.string").isEqualTo("stringValue"));

client.get().uri(path).exchange().expectBody()
    .consumeWith(json().node("result.array")
        .when(Option.IGNORING_ARRAY_ORDER)
        .isEqualTo(new int[]{3, 2, 1}));
```

For Kotlin, you can use the DSL:

```kotlin
import net.javacrumbs.jsonunit.spring.jsonContent
...

client.get().uri(path).accept(APPLICATION_JSON)
    .exchange()
    .expectBody()
    .jsonContent { isEqualTo(CORRECT_JSON) }

client.get().uri(path).exchange()
    .expectBody()
    .jsonContent { 
        node("result.string").isString().isEqualTo("stringValue") 
    }
```

To use import
```xml
<dependency>
    <groupId>net.javacrumbs.json-unit</groupId>
    <artifactId>json-unit-spring</artifactId>
    <version>5.0.0</version>
    <scope>test</scope>
</dependency>
```

For more examples see [the tests](https://github.com/lukas-krecan/JsonUnit/blob/master/json-unit-spring/src/test/java/net/javacrumbs/jsonunit/spring/testit/RestTestClientTest.java).

## <a name="spring-mockmvc-assertj"></a>Spring AssertJ for MockMvc
Since version 4.0.0 JsonUnit supports Spring MockMvc AssertJ assertions.
You can use it like this:

```java
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.jsonUnitJson;

...

assertThat(mvc.get().uri("/sample"))
    .hasStatusOk()
    .bodyJson()
    .convertTo(jsonUnitJson()) // Switch to JsonUnit assert
    .inPath("result.array") // This is JsonUnit
    .isArray()
    .containsExactly(1, 2, 3);
```

## <a name="spring-client"></a>Spring REST client assertions

```java
import static net.javacrumbs.jsonunit.spring.JsonUnitRequestMatchers.json;
...
mockServer.expect(requestTo(URI))
      .andExpect(json().isEqualTo(json))
      .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON_UTF8));
```

To use import
```xml
<dependency>
    <groupId>net.javacrumbs.json-unit</groupId>
    <artifactId>json-unit-spring</artifactId>
    <version>5.0.0</version>
    <scope>test</scope>
</dependency>
```

For more examples see [the tests](https://github.com/lukas-krecan/JsonUnit/blob/master/json-unit-spring/src/test/java/net/javacrumbs/jsonunit/spring/testit/ClientTest.java).

## <a name="kotest"></a>Kotest assertions
JsonUnit supports [Kotest assertions](https://kotest.io/docs/assertions/assertions.html).

Import:

```xml
<dependency>
    <groupId>net.javacrumbs.json-unit</groupId>
    <artifactId>json-unit-kotest</artifactId>
    <version>5.0.0</version>
    <scope>test</scope>
</dependency>
```

And enjoy:

```kotlin
"""{"test":1}""" should equalJson("""{"test": 1}""")

// Provide configuration
"""{"test":1.01}""" should equalJson("""{"test":1}""", configuration { withTolerance(0.1) })

// Use inPath
"""{"test":1}""" inPath "test" should equalJson("1")

// Clues with nesting
"""{"test": {"nested": 1}}""".inPath("test").asClue {
    it inPath "nested" should equalJson("2")
}

"""{"test":1}""".inPath("test").shouldBeJsonNumber()
    // shouldBeJsonNumber returns BigDecimal, so we can use standard kotest assertions
    // PLease note that numbers are converted to BigDecimals
    .shouldBeEqualComparingTo(valueOf(1))

// The same for arrays generated by JsonPath
"""{"test": [{"a": "a"}, {"a": true}, {"a": null}, {"a": 4}]}""".inPath("$.test[*].a")
    .shouldBeJsonArray()
    .shouldContainExactly("a", true, null, valueOf(4))

// ... and objects
"""{"a":1, "b": true}""".shouldBeJsonObject().shouldMatchAll(
    "a" to { it should beJsonNumber() },
    "b" to { it should beJsonBoolean() }
)
```

See the [tests](https://github.com/lukas-krecan/JsonUnit/blob/master/json-unit-kotest/src/test/kotlin/net/javacrumbs/jsonunit/kotest/test/KotestTest.kt) for more examples.

# Features
JsonUnit support all this features regardless of API you use.

## <a name="jsonpath"></a>JsonPath support
You can use JsonPath navigation together with JsonUnit. It has native support in AssertJ and Spring integration, so you can do something like this:

```java
// AssertJ style
assertThatJson(json)
    .inPath("$.store.book")
    .isArray()
    .contains(json(
        "            {\n" +
            "                \"category\": \"reference\",\n" +
            "                \"author\": \"Nigel Rees\",\n" +
            "                \"title\": \"Sayings of the Century\",\n" +
            "                \"price\": 8.96\n" +
            "            }"
    ));
```

## <a name="ignorevalues"></a>Ignoring values
Sometimes you need to ignore certain values when comparing. It is possible to use `${json-unit.ignore}` or `#{json-unit.ignore}`
placeholder like this

```java
// AssertJ API
assertThatJson("{\"a\":1}")
    .isEqualTo(json("{\"a\":\"${json-unit.ignore}\"}"));
```
Please note that the assertion will fail if the `test` element is missing in the actual value.

## <a name="ignoreelements"></a>Ignoring elements
If the element needs to be ignored completely you can use `${json-unit.ignore-element}`
placeholder.

```java
// AssertJ API
assertThatJson("{\"root\":{\"test\":1, \"ignored\": null}}")
      .isEqualTo("{\"root\":{\"test\":1, \"ignored\": \"${json-unit.ignore-element}\"}}");
```

The assertion will not fail if the element is missing in the actual value.

## <a name="ignorepaths"></a>Ignoring paths

`whenIgnoringPaths` configuration option makes JsonUnit ignore the specified paths in the actual value. If the path
matches, it's completely ignored. It may be missing, null or have any value. Also `when(paths(...), thenIgnore()` can be used.

```java
// AssertJ style
assertThatJson("{\"root\":{\"test\":1, \"ignored\": 1}}")
    .whenIgnoringPaths("root.ignored"))
    .isEqualTo("{\"root\":{\"test\":1}}");

// Hamcrest matcher
assertThat(
  "{\"root\":{\"test\":1, \"ignored\": 2}}",
  jsonEquals("{\"root\":{\"test\":1, \"ignored\": 1}}").whenIgnoringPaths("root.ignored")
);
```

Array index placeholder
```java
assertThatJson("[{\"a\":1, \"b\":2},{\"a\":1, \"b\":3}]")
    .whenIgnoringPaths("[*].b")
    .isEqualTo("[{\"a\":1, \"b\":0},{\"a\":1, \"b\":0}]");
```
Please note that if you use JsonPath, you should start the path to be ignored by `$`
Also note that `whenIgnoringPaths` method supports full JsonPath syntax only in AssertJ API, all the other flavors support only
exact path or array index placeholder as described above.

JsonPath with whenIgnoringPaths example:
```java
// AssertJ API
assertThatJson("{\"fields\":[" +
        "{\"key\":1, \"name\":\"AA\"}," +
        "{\"key\":2, \"name\":\"AB\"}," +
        "{\"key\":3, \"name\":\"AC\"}" +
    "]}")
    .whenIgnoringPaths("$.fields[?(@.name=='AA')].key")
    .isEqualTo("{\"fields\":[" +
        "{\"key\":2, \"name\":\"AA\"}," +
        "{\"key\":2, \"name\":\"AB\"}," +
        "{\"key\":3, \"name\":\"AC\"}" +
    "]}");
```


## <a name="regexp"></a>Regular expressions
It is also possible to use regular expressions to compare string values

```java
assertThatJson("{\"test\": \"ABCD\"}")
    .isEqualTo("{\"test\": \"${json-unit.regex}[A-Z]+\"}");
```


For matching just part of the string, you can use this (we have to escape twice, once for Java, once for JSON)
```java
assertThatJson("{\"test\": \"This is some text followed by: ABCD, followed by this\"}")
            .isEqualTo("{\"test\": \"${json-unit.regex}^\\\\QThis is some text followed by: \\\\E[A-Z]+\\\\Q, followed by this\\\\E$\"}");
```

Since this is quite hard to write, you can implement an expression builder like
[this](https://github.com/lukas-krecan/JsonUnit/commit/75d68ef1852ade004e93ca42d676f4b996631974#diff-642a52fede8473f98c5a7b25f34c6bd68f33160cf22733127f7f7f13f3cb2fc6R713).


## <a name="typeplc"></a>Type placeholders
If you want to assert just a type, but you do not care about the exact value, you can use any-* placeholder like this

```java
assertThatJson("{\"test\":\"value\"}")
    .isEqualTo("{test:'${json-unit.any-string}'}");

assertThatJson("{\"test\":true}")
    .isEqualTo("{\"test\":\"${json-unit.any-boolean}\"}");

assertThatJson("{\"test\":1.1}")
    .isEqualTo("{\"test\":\"${json-unit.any-number}\"}");

```

You can also use hash instead of string `#{json-unit.any-string}` for example if you are using language with string interpolation
like Kotlin.

## <a name="matchers"></a>Custom matchers
In some special cases you might want to use your own matcher in the expected document.
```java
 assertThatJson("{\"test\":-1}")
             .withMatcher("positive", greaterThan(valueOf(0)))
             .isEqualTo("{\"test\": \"${json-unit.matches:positive}\"}");
```

In even more special cases, you might want to parametrize your matcher.
```java
 Matcher<?> divisionMatcher = new DivisionMatcher();
 assertThatJson("{\"test\":5}")
    .withMatcher("isDivisibleBy", divisionMatcher)
    .isEqualTo("{\"test\": \"${json-unit.matches:isDivisibleBy}3\"}");

 private static class DivisionMatcher extends BaseMatcher<Object> implements ParametrizedMatcher {
     private BigDecimal param;

     public boolean matches(Object item) {
         return ((BigDecimal)item).remainder(param).compareTo(ZERO) == 0;
     }

     public void describeTo(Description description) {
         description.appendValue(param);
     }

     @Override
     public void describeMismatch(Object item, Description description) {
         description.appendText("It is not divisible by ").appendValue(param);
     }

     public void setParameter(String parameter) {
         this.param = new BigDecimal(parameter);
     }
 }
```
If you need a matcher with more than one parameter, you can implement it
like [this](https://stackoverflow.com/a/66183629/277042).

## <a name="options"></a>Options

There are multiple options how you can configure the comparison

**TREATING_NULL_AS_ABSENT** - fields with null values are equivalent to absent fields. For example, this test passes

```java
assertThatJson("{\"test\":{\"a\":1, \"b\": null}}")
    .when(TREATING_NULL_AS_ABSENT)
    .isEqualTo("{\"test\":{\"a\":1}}");
```

**IGNORING_ARRAY_ORDER** - ignores order in arrays

```java
assertThatJson("{\"test\":[1,2,3]}")
    .when(IGNORING_ARRAY_ORDER)
    .isEqualTo("{\"test\":[3,2,1]}");
```

**IGNORING_EXTRA_ARRAY_ITEMS** - ignores unexpected array items
```java
assertThatJson("{\"test\":[1,2,3,4]}")
    .when(IGNORING_EXTRA_ARRAY_ITEMS)
    .isEqualTo("{\"test\":[1,2,3]}");


assertThatJson("{\"test\":[5,5,4,4,3,3,2,2,1,1]}")
    .when(IGNORING_EXTRA_ARRAY_ITEMS, IGNORING_ARRAY_ORDER)
    .isEqualTo("{\"test\":[1,2,3]}");
```

**IGNORING_EXTRA_FIELDS** - ignores extra fields in the compared value

```java
assertThatJson("{\"test\":{\"a\":1, \"b\":2, \"c\":3}}")
    .when(IGNORING_EXTRA_FIELDS)
    .isEqualTo("{\"test\":{\"b\":2}}");
```

**IGNORE_VALUES** - ignores values and compares only types

```java
assertThatJson("{\"a\":2,\"b\":\"string2\"}")
    .when(paths("a", "b"), then(IGNORING_VALUES))
    .isEqualTo("{\"a\":1,\"b\":\"string\"}");
```

**FAIL_FAST** - stops comparison at the first difference. May provide performance benefits for large JSONs and use-cases that
do not require to know all the differences.

It is possible to combine options.

```java
assertThatJson("{\"test\":[{\"key\":3},{\"key\":2, \"extraField\":2},{\"key\":1}]}")
    .when(IGNORING_EXTRA_FIELDS, IGNORING_ARRAY_ORDER)
    .isEqualTo("{\"test\":[{\"key\":1},{\"key\":2},{\"key\":3}]}");
```

**REPORTING_DIFFERENCE_AS_NORMALIZED_STRING** - reports errors as normalized strings. Applies some basic normalization
to compared values and then reports full json strings, so you can see the difference in the IDE diff.


In Hamcrest assertion you can set the option like this

```java
assertThat("{\"test\":{\"a\":1, \"b\":2, \"c\":3}}",
    jsonEquals("{\"test\":{\"b\":2}}").when(IGNORING_EXTRA_FIELDS));
```

You can define options locally (for specific paths) by using `when(path(...), then(...))`:
```java
// ignore array order for [*].a
// AssertJ
assertThatJson("{\"test\":{\"a\":1,\"b\":2,\"c\":3}}").when(paths("test.c"), then(IGNORING_VALUES))
    .isEqualTo("{\"test\":{\"a\":1,\"b\":2,\"c\":4}}");
// ignore array order everywhere but [*].b
assertThatJson("[{\"b\":[4,5,6]},{\"b\":[1,2,3]}]")
    .when(IGNORING_ARRAY_ORDER)
    .when(path("[*].b"), thenNot(IGNORING_ARRAY_ORDER))
    .isEqualTo("[{\"b\":[1,2,3]},{\"b\":[4,5,6]}]");
// ignore extra fields in the object "a"
assertThatJson("{\"a\":{\"a1\":1,\"a2\":2},\"b\":{\"b1\":1,\"b2\":2}}")
    .when(path("a"), then(IGNORING_EXTRA_FIELDS))
    .isEqualTo("{\"a\":{\"a1\":1},\"b\":{\"b1\":1}}"))
// ignore extra array items in the array
assertThatJson("{\"a\":[1,2,3]}")
    .when(path("a"), then(IGNORING_EXTRA_ARRAY_ITEMS))
    .isEqualTo("{\"a\":[1,2]}");
// Hamcrest
assertThat("{\"test\":{\"a\":1,\"b\":2,\"c\":3}}",
    jsonEquals("{\"test\":{\"a\":1,\"b\":2,\"c\":4}}").when(path("test.c"), then(IGNORING_VALUES)));

```
Note that **TREATING_NULL_AS_ABSENT** and **IGNORING_VALUES** require exact paths to ignored fields:
```java
// ignoring number and str
assertThatJson("{\"a\":2,\"b\":\"string2\"}")
    .when(paths("a", "b"), then(IGNORING_VALUES))
    .isEqualTo("{\"a\":1,\"b\":\"string\"}");
// treat null B as absent B
assertThatJson("{\"A\":1,\"B\":null}")
    .when(path("B"), then(TREATING_NULL_AS_ABSENT))
    .isEqualTo("{\"A\":1}");
```
All other options require paths to objects or arrays where values or order should be ignored.

## <a name="arrayIndexing"></a>Array indexing
You can use negative numbers to index arrays form the end
```java
assertThatJson("{\"root\":{\"test\":[1,2,3]}}")
    .node("root.test[-1]").isEqualTo(3);
```

## <a name="numbers"></a>Numerical comparison
Numbers are by default compared in the following way:

* If the type differs, the number is different. So 1 and 1.0 are different (int vs. float). This does not apply when Moshi is used since it [parses all numbers as Doubles](https://github.com/square/moshi/issues/192).
* Floating number comparison is exact, down to the scale - 1.0 and 1.00 are considered to be different.

You can change this behavior by setting tolerance. If you set tolerance to `0` two numbers are considered equal if they are
equal mathematically even though they have different type or precision (`a.compareTo(b) == 0`).

```java
assertThatJson("{\"test\":1.00}").node("test").withTolerance(0).isEqualTo(1);
```

If you set tolerance to non-zero value, the values are considered equal if `abs(a-b) <= tolerance`.
```java
assertThatJson("{\"test\":1.00001}").node("test").withTolerance(0.001).isEqualTo(1);
```

Or you can use Hamcrest matcher

```java
import static java.math.BigDecimal.valueOf;
...
assertThatJson("{\"test\":1.10001}").node("test")
    .matches(closeTo(valueOf(1.1), valueOf(0.001)));
```

If you are interested why 1 and 1.0 are treated as different numbers please read this [comment](https://github.com/lukas-krecan/JsonUnit/issues/229#issuecomment-623882801).

If you want to have special handling of numerical values, you can inject your own number comparator.

```java
assertThatJson("{\"a\":1.0}")
    .withConfiguration(c -> c.withNumberComparator(numberComparator))
    .isEqualTo("{\"a\":1.00}");
```

## <a name="dots"></a>Escaping dots
Sometimes you have dots in JSON element names, and you need to address those elements. It is possible to escape dots like this

```java
assertThatJson("{\"name.with.dot\": \"value\"}").node("name\\.with\\.dot").isStringEqualTo("value");
```

## <a name="lenient"></a>Lenient parsing of expected value
Writing JSON string in Java is huge pain. JsonUnit parses expected values leniently, so you do not have to quote keys,
and you can use single quotes instead of double quotes. Please note that the actual value being compared is parsed in strict mode.

```java
assertThatJson("{\"a\":\"1\", \"b\":2}").isEqualTo("{b:2, a:'1'}");
```

## <a name="object-mapper-cust"></a>Jackson Object Mapper customization
If you need to customize Jackson 2 Object Mapper, you can do using [SPI](https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html).
Implement `net.javacrumbs.jsonunit.providers.Jackson2ObjectMapperProvider`.

```java
public class Java8ObjectMapperProvider implements Jackson2ObjectMapperProvider {
    private final ObjectMapper mapper;

    private final ObjectMapper lenientMapper;


    public Java8ObjectMapperProvider() {
        mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        lenientMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        lenientMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        lenientMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        lenientMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
    }

    @Override
    public ObjectMapper getObjectMapper(boolean lenient) {
        return lenient ? lenientMapper : mapper;
    }
}
```

and register it in `META-INF/services/net.javacrumbs.jsonunit.providers.Jackson2ObjectMapperProvider`.
See [this example](https://github.com/lukas-krecan/JsonUnit/commit/8dc7c884448c7373886dcf3b0eabfecf47c0710b).

## Jackson 3 Object Mapper customization
If you need to customize Jackson 3 Object Mapper, you can do using [SPI](https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html).
Implement `net.javacrumbs.jsonunit.providers.Jackson3ObjectMapperProvider`.

```java
public class Jackson3CustomProvider implements Jackson3ObjectMapperProvider {
    private final ObjectMapper mapper;
    private final ObjectMapper lenientMapper;

    public Jackson3CustomProvider() {
        mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        lenientMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        lenientMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        lenientMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        lenientMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
    }

    @Override
    public ObjectMapper getObjectMapper(boolean lenient) {
        return lenient ? lenientMapper : mapper;
    }
}
```

and register it in `META-INF/services/net.javacrumbs.jsonunit.providers.Jackson3ObjectMapperProvider`.

Note: Jackson 3 uses the `tools.jackson` package namespace instead of `com.fasterxml.jackson`.

## Logging

Although the differences are printed out by the assert statement, sometimes you use JsonUnit with other libraries like
[Jadler](https://github.com/jadler-mocking/jadler) that do not print the differences between documents. In such case, you can switch on the
logging. JsonUnit uses [SLF4J](http://www.slf4j.org/). The only thing you need to do is to configure your logging
framework to log `net.javacrumbs.jsonunit.difference` on DEBUG level.

## DifferenceListener

If you need better difference reporting, you can implement [`DifferenceListener`](https://github.com/lukas-krecan/JsonUnit/blob/master/json-unit-core/src/main/java/net/javacrumbs/jsonunit/core/listener/DifferenceListener.java) and use it this way

```java
assertThatJson("{\"test\":-1}")
    .withDifferenceListener(listener)
    .isEqualTo(json);
```

## Selecting underlying library

JsonUnit is trying to cleverly match which JSON library to use. In case you need to change the default behavior, you can
use `json-unit.libraries` system property. For example `-Djson-unit.libraries=jackson2,jackson3,gson`
or `System.setProperty("json-unit.libraries", "jackson2");`. Supported values are gson, json.org, moshi, jackson2, jackson3

Licence
-------
JsonUnit is licensed under [Apache 2.0 licence](https://www.apache.org/licenses/LICENSE-2.0).

Release notes
=============
Released notes moved to a [separate file](RELEASES.md)
