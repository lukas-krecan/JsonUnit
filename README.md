JsonUnit [![Apache License 2](https://img.shields.io/badge/license-ASF2-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.txt) [![Build Status](https://travis-ci.org/lukas-krecan/JsonUnit.png?branch=master)](https://travis-ci.org/lukas-krecan/JsonUnit) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/net.javacrumbs.json-unit/json-unit/badge.svg)](https://maven-badges.herokuapp.com/maven-central/net.javacrumbs.json-unit/json-unit)
========

JsonUnit is a library that simplifies JSON comparison in unit tests.

- [APIs](#apis)
  * [AssertJ integration (beta)](#assertj)
  * [Fluent assertions](#fluent)
  * [Spring MVC assertions](#spring)
  * [Standard assert](#standard)
- [Features](#features)
  * [JsonPath support (beta)](#jsonpath)
  * [Ignoring values](#ignorevalues)
  * [Ignoring paths](#ignorepaths)
  * [Regular expressions](#regexp)
  * [Type placeholders](#typeplc)
  * [Custom matchers](#matchers)
  * [Options](#options)
  * [Array indexing](#arrayIndexing)
  * [Numerical comparison](#numbers)
  * [Escaping dots](#dots)
  * [Lenient parsing of expected value](#lenient)


# <a name="apis"></a>APIs
There are several different APIs you can use. They all have more or less the same features, just the usage is 
slightly different.

## <a name="assertj"></a>AssertJ integration (beta)
This is brand new API which combines power of JsonUnit and AssertJ. It's currently in beta since there 
might be some obscure features of AssertJ which might not work correctly with some obscure features of JsonUnit. Please let me know if you 
encounter any.    


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
assertThatJson("{\"a\":1, \"b\": {\"c\" :3}}").isObject().containsValue(json("{\"c\" :\"${json-unit.any-number}\"}"));

// AssertJ array assertion
assertThatJson("{\"a\":[{\"b\": 1}, {\"c\": 1}, {\"d\": 1}]}").node("a").isArray().contains(json("{\"c\": 1}"));

// Can ignore array order
assertThatJson("{\"a\":[{\"b\": 1}, {\"c\": 1}, {\"d\": 1}]}").when(Option.IGNORING_ARRAY_ORDER).node("a").isArray()
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

To use AssertJ integration, import

```xml
<dependency>
    <groupId>net.javacrumbs.json-unit</groupId>
    <artifactId>json-unit-assertj</artifactId>
    <version>2.0.0.RC3</version>
    <scope>test</scope>
</dependency>
```
For more examples see [the tests](https://github.com/lukas-krecan/JsonUnit/blob/master/tests/test-base/src/main/java/net/javacrumbs/jsonunit/test/base/AbstractAssertJTest.java).

## <a name="fluent"></a>Fluent assertions
Fluent assertions were inspired by FEST and AssertJ. This API was created before AssertJ become so popular
so it does not depend on it. I would recommend to use AssertJ integration described above once it leaves beta.

```java
import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;
...

// compares entire documents
assertThatJson("{\"test\":1}").isEqualTo("{\"test\":2}");

// compares only parts of the document
assertThatJson("{\"test1\":2, \"test2\":1}")
    .node("test1").isEqualTo(2)
    .node("test2").isEqualTo(2);

// compare node indexed from start of array
assertThatJson("{\"root\":{\"test\":[1,2,3]}}")
    .node("root.test[0]").isEqualTo(1);

// compare node indexed from end of array
assertThatJson("{\"root\":{\"test\":[1,2,3]}}")
    .node("root.test[-1]").isEqualTo(3);

// compares only the structure
assertThatJson("{\"test\":1}")
    // Options have to be specified before the assertion
    .when(IGNORING_VALUES)
    .isEqualTo("{\"test\":21}");

// ignores a value
assertThatJson("{\"test\":1}").isEqualTo("{\"test\":\"${json-unit.ignore}\"}");

// ignores extra fields
assertThatJson("{\"test\":{\"a\":1, \"b\":2, \"c\":3}}")
    // Options have to be specified before the assertion
    .when(IGNORING_EXTRA_FIELDS)
    .isEqualTo("{\"test\":{\"b\":2}}");
    	
// array length comparison
assertThatJson("{\"test\":[1,2,3]}").node("test")
    .isArray().ofLength(2);
    
// array contains node
assertThatJson("{\"test\":[{\"id\":36},{\"id\":37}]}").node("test")
.isArray().thatContains("{\"id\":37}");

// using Hamcrest matcher
assertThatJson("{\"test\":\"one\"}").node("test")
    .matches(equalTo("one"));

// Numbers sent to matchers are BigDecimals.
assertThatJson("{\"test\":[{\"value\":1},{\"value\":2},{\"value\":3}]}")
    .node("test")
    .matches(everyItem(jsonPartMatches("value", lessThanOrEqualTo(BigDecimal.valueOf(4)))));
```

### Hamcrest matchers in fluent assertions

It is possible to combine fluent assertions with hamcrest matchers using `matches` method. For example

```java
assertThatJson("{\"test\":[1,2,3]}").node("test").matches(hasItem(valueOf(1)));

assertThatJson("{\"test\":[{\"value\":1},{\"value\":2},{\"value\":3}]}")
    .node("test")
    .matches(everyItem(jsonPartMatches("value", lessThanOrEqualTo(valueOf(4)))));
```

To use import
```xml
<dependency>
    <groupId>net.javacrumbs.json-unit</groupId>
    <artifactId>json-unit-fluent</artifactId>
    <version>2.0.0.RC3</version>
    <scope>test</scope>
</dependency>
```

For more examples see [the tests](https://github.com/lukas-krecan/JsonUnit/blob/master/tests/test-base/src/main/java/net/javacrumbs/jsonunit/test/base/AbstractJsonFluentAssertTest.java).

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
    <version>2.0.0.RC3</version>
    <scope>test</scope>
</dependency>
```

For more examples see [the tests](https://github.com/lukas-krecan/JsonUnit/blob/master/tests/test-base/src/main/java/net/javacrumbs/jsonunit/test/base/AbstractJsonMatchersTest.java).

## <a name="spring"></a>Spring MVC assertions
JsonUnit supports Spring MVC test assertions. For example

```java
import static net.javacrumbs.jsonunit.spring.JsonUnitResultMatchers.json;
...

this.mockMvc.perform(get("/sample").andExpect(
    json().isEqualTo("{\"result\":{\"string\":\"stringValue\", \"array\":[1, 2, 3],\"decimal\":1.00001}}")
);
this.mockMvc.perform(get("/sample").andExpect(
    json().node("result.string2").isAbsent()
);
this.mockMvc.perform(get("/sample").andExpect(
    json().node("result.array").when(Option.IGNORING_ARRAY_ORDER).isEqualTo(new int[]{3, 2, 1})
);
this.mockMvc.perform(get("/sample").andExpect(
    json().node("result.array").matches(everyItem(lessThanOrEqualTo(valueOf(4))))
);
```

To use import
```xml
<dependency>
    <groupId>net.javacrumbs.json-unit</groupId>
    <artifactId>json-unit-spring</artifactId>
    <version>2.0.0.RC3</version>
    <scope>test</scope>
</dependency>
```

For more examples see [the tests](https://github.com/lukas-krecan/JsonUnit/blob/master/json-unit-spring/src/test/java/net/javacrumbs/jsonunit/spring/ExampleControllerTest.java).

## <a name="standard"></a>Standard assert
This is old, JUnit-like API, for those of us who love traditions and do not like fluent APIs. 

```java
import static net.javacrumbs.jsonunit.JsonAssert.*;
import static net.javacrumbs.jsonunit.core.Option.*;

...

// compares two JSON documents
assertJsonEquals("{\"test\":1}", "{\n\"test\": 1\n}");

// objects are automatically serialized before comparison
assertJsonEquals(jsonObject, "{\n\"test\": 1\n}");

// compares only part
assertJsonPartEquals("2", "{\"test\":[{\"value\":1},{\"value\":2}]}",
    "test[1].value");
    
// extra options can be specified
assertJsonEquals("{\"test\":{\"a\":1}}",
    "{\"test\":{\"a\":1, \"b\": null}}",
    when(TREATING_NULL_AS_ABSENT));

// compares only the structure, not the values
assertJsonEquals("[{\"test\":1}, {\"test\":2}]",
    "[{\n\"test\": 1\n}, {\"TEST\": 4}]", when(IGNORING_VALUES))

// Lenient parsing of expected value
assertJsonEquals("{//Look ma, no quotation marks\n test:'value'}", 
    "{\n\"test\": \"value\"\n}");
```

To use import 
```xml
<dependency>
    <groupId>net.javacrumbs.json-unit</groupId>
    <artifactId>json-unit</artifactId>
    <version>2.0.0.RC3</version>
    <scope>test</scope>
</dependency>
```

For more examples see [the tests](https://github.com/lukas-krecan/JsonUnit/blob/master/tests/test-base/src/main/java/net/javacrumbs/jsonunit/test/base/AbstractJsonAssertTest.java).

# Features
JsonUnit support all this features regardless of API you use.

## <a name="jsonpath"></a>JsonPath support (beta)
You can use JsonPath navigation together with JsonUnit. It has native support in AssertJ integration so you can do something like this:

```java
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

For other API styles you have to first import JsonPath support module
```xml
<dependency>
    <groupId>net.javacrumbs.json-unit</groupId>
    <artifactId>json-unit-json-path</artifactId>
    <version>2.0.0.RC3</version>
</dependency>
```

and then use instead of actual value

```xml
import static net.javacrumbs.jsonunit.jsonpath.JsonPathAdapter.inPath;

...

assertThatJson(inPath(json, "$.store.book[*].author"))
    .when(Option.IGNORING_ARRAY_ORDER)
    .isEqualTo("['J. R. R. Tolkien', 'Nigel Rees', 'Evelyn Waugh', 'Herman Melville']");
```

## <a name="ignorevalues"></a>Ignoring values
Sometimes you need to ignore certain values when comparing. It is possible to use `${json-unit.ignore}`
placeholder like this

```java
assertJsonEquals("{\"test\":\"${json-unit.ignore}\"}",
    "{\n\"test\": {\"object\" : {\"another\" : 1}}}");
```

## <a name="ignorepaths"></a>Ignoring paths

```java
// AssertJ style
assertThatJson("{\"root\":{\"test\":1, \"ignored\": 1}}")
    .withConfiguration(c -> c.whenIgnoringPaths("root.ignored"))
    .isEqualTo("{\"root\":{\"test\":1}}");

// Hamcrest matcher
assertThat(
  "{\"root\":{\"test\":1, \"ignored\": 2}}", 
  jsonEquals("{\"root\":{\"test\":1, \"ignored\": 1}}").whenIgnoringPaths("root.ignored")
);
```

Array index placeholder
```java
// standard assert
assertJsonEquals(
    "[{\"a\":1, \"b\":0},{\"a\":1, \"b\":0}]", 
    "[{\"a\":1, \"b\":2},{\"a\":1, \"b\":3}]", 
    JsonAssert.whenIgnoringPaths("[*].b")
);
```
Please note, that if you use JsonPath, you should start the path to be ignored by `$.`.

## <a name="regexp"></a>Regular expressions
It is also possible to use regular expressions to compare string values

```java
assertJsonEquals("{\"test\": \"${json-unit.regex}[A-Z]+\"}", 
    "{\"test\": \"ABCD\"}");
```

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
## <a name="matchers"></a>Custom matchers
In some special cases you might want to use your own matcher in the expected document.
```java
 assertJsonEquals(
     "{\"test\": \"${json-unit.matches:positive}\"}", 
     "{\"test\":1}", 
     JsonAssert.withMatcher("positive", greaterThan(valueOf(0)))
 );

```

In even more special cases, you might want to parametrize your matcher.
```java
 Matcher<?> divisionMatcher = new DivisionMatcher();
 assertJsonEquals(
     "{test: '${json-unit.matches:isDivisibleBy}3'}", 
     "{\"test\":5}", 
     JsonAssert.withMatcher("isDivisibleBy", divisionMatcher)
 );
 
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

## <a name="options"></a>Options

There are multiple options how you can configure the comparison

**TREATING_NULL_AS_ABSENT** - fields with null values are equivalent to absent fields. For example, this test passes
  
```java
assertJsonEquals("{\"test\":{\"a\":1}}",
    "{\"test\":{\"a\":1, \"b\": null, \"c\": null}}",
    when(TREATING_NULL_AS_ABSENT));
```

**IGNORING_ARRAY_ORDER** - ignores order in arrays

```java
assertJsonEquals("{\"test\":[1,2,3]}", 
    "{\"test\":[3,2,1]}",
    when(IGNORING_ARRAY_ORDER));
```

**IGNORING_EXTRA_ARRAY_ITEMS** - ignores unexpected array items
```java
assertJsonEquals("{\"test\":[1,2,3]}",
    "{\"test\":[1,2,3,4]}",
    when(IGNORING_EXTRA_ARRAY_ITEMS));


assertJsonEquals("{\"test\":[1,2,3]}",
    "{\"test\":[5,5,4,4,3,3,2,2,1,1]}",
    when(IGNORING_EXTRA_ARRAY_ITEMS, IGNORING_ARRAY_ORDER));
```

**IGNORING_EXTRA_FIELDS** - ignores extra fields in the compared value

```java
assertThatJson("{\"test\":{\"a\":1, \"b\":2, \"c\":3}}")
    .when(IGNORING_EXTRA_FIELDS)
    .isEqualTo("{\"test\":{\"b\":2}}");
```

**IGNORE_VALUES** - ignores values and compares only types

```java
assertJsonEquals("{\"test\":{\"a\":1,\"b\":2,\"c\":3}}", 
    "{\"test\":{\"a\":3,\"b\":2,\"c\":1}}",
    when(IGNORING_VALUES));
```

It is possible to combine options. 

```java
assertJsonEquals("{\"test\":[{\"key\":1},{\"key\":2},{\"key\":3}]}", 
    "{\"test\":[{\"key\":3},{\"key\":2, \"extraField\":2},{\"key\":1}]}",
    when(IGNORING_ARRAY_ORDER, IGNORING_EXTRA_FIELDS));
```
                     
In Hamcrest assertion you can set the option like this

```java
assertThat("{\"test\":{\"a\":1, \"b\":2, \"c\":3}}",
    jsonEquals("{\"test\":{\"b\":2}}").when(IGNORING_EXTRA_FIELDS));
```

## <a name="arrayIndexing"></a>Array indexing
You can use negative numbers to index arrays form the end
```java
assertThatJson("{\"root\":{\"test\":[1,2,3]}}")
    .node("root.test[-1]").isEqualTo(3);
```

## <a name="numbers"></a>Numerical comparison
Numbers are by default compared in the following way:

* If the type differs, the number is different. So 1 and 1.0 are different (int vs. float). This does not apply when Moshi is used since it [parses all numbers as Doubles](https://github.com/square/moshi/issues/192).
* Floating number comparison is exact

You can change this behavior by setting tolerance

```java
assertJsonEquals("1", "\n1.009\n", withTolerance(0.01));
```

or for fluent assertions

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

## <a name="dots"></a>Escaping dots
Sometimes you have dots in JSON element names and you need to address those elements. It is possible to escape dots like this

```java
assertThatJson("{\"name.with.dot\": \"value\"}").node("name\\.with\\.dot").isStringEqualTo("value");
```

## <a name="lenient"></a>Lenient parsing of expected value
Writing JSON string in Java is huge pain. JsonUnit parses expected values leniently so you do not have to quote keys 
and you can use single quotes instead of double quotes. Please note that the actual value being compared is parsed in strict mode.  

```java
assertThatJson("{\"a\":\"1\", \"b\":2}").isEqualTo("{b:2, a:'1'}");
```

## Logging

Although the differences are printed out by the assert statement, sometimes you use JsonUnit with other libraries like
[Jadler](http://jadler.net) that do not print the differences between documents. In such case, you can switch on the
logging. JsonUnit uses [SLF4J](http://www.slf4j.org/). The only thing you need to do is to configure your logging
framework to log `net.javacrumbs.jsonunit.difference` on DEBUG level.

## Selecting underlying library
JsonUnit is trying to cleverly match which JSON library to use. In case you need to change the default behavior, you can use
json-unit.libraries system property. For example `-Djson-unit.libraries=jackson2,gson` or `System.setProperty("json-unit.libraries", "jackson1");`. Supported values are gson, json.org, moshi, jackson1, jackson2

Licence
-------
JsonUnit is licensed under [Apache 2.0 licence](https://www.apache.org/licenses/LICENSE-2.0).

Release notes
=============
## 2.0.0.RC3
* Support for and() in AssertJ assert
* asNumber() in AssertJ added
* Allow description before inPath() in AssertJ

## 2.0.0.RC2
* Fixed JsonPath bug #132 
* Fixed AssertJ number comparison with Jackson 2 #130
* Fixed AssertJ asString() #131

## 2.0.0.RC1
* Depends on Java 8
* Some deprecated APis removed
* Introduces AssertJ module
* Introduces JsonPath module

Please do not hesitate to report issues.

## 1.31.0
* Introduced DifferenceContext into DifferenceListener

## 1.30.0
* Introduced DifferenceListener
* Array comparison reports extra/missing elements when comparing with array order preserved. 

## 1.29.1
* Fixed error in JsonFluentAssert.ofLength error message
* Fixed matcher handling when comparing arrays #111

## 1.29.0
* [*] placeholder works even when ignoring array order 

## 1.28.2
* Fixing matcher pattern

## 1.28.1
* Fixing NPE when accessing element of nonexistent array

## 1.28.0
* Support for [*] placeholder in ignored path

## 1.27.0
* Better array comparison and error messages

## 1.26.0
* IDE friendly error messages
* isStringEqualTo is chainable (thanks to @gsson)
* Dropped support of Java 5
* Automatic module names added

## 1.25.1
* Support for Jackson BinaryNode

## 1.25.0
* Support for ignoring paths whenIgnoringPaths()

## 1.24.0
* Support for parametrers in custom matchers ${json-unit.matches:matcherName}param

## 1.23.0
* Support for custom matchers ${json-unit.matches:matcherName}

## 1.22.0
* Support for Moshi

## 1.21.0
* Better diff reporting for unordered arrays with single difference

## 1.20.0
* Negative array indexes added (thanks [roxspring](https://github.com/roxspring))

## 1.19.0
* isArray().thatContains(...) fluent assert added

## 1.18.0
* Resource reading helper added

## 1.17.0
* System property to specify JSON libraries to be used

## 1.16.1
* Array pattern accepts non-word characters

## 1.16.0
* isAbsent and isPresent checks take TREAT_NULL_AS_ABSENT into account

## 1.15.0
* Dependency on slf4j made optional

## 1.14.1
* Preferring org.json library to process JSONArray

## 1.14.0
* Support for org.json library
* Fix: Element out of array bounds is treated as missing

## 1.13.0
* Support for any-* placeholders

## 1.12.1
* Single quote values in expected String allowed

## 1.12.0
* Lenient parsing of expected values

## 1.11.0
* Option setting methods made deprecated if called after assertion in JsonFluentAssert
* JsonFluentAssert constructors made private. Please file an issue if you need them.

## 1.10.0
* Added support for IGNORING_EXTRA_ARRAY_ITEMS

## 1.9.0
* Made compatible with Jackson 1.4

## 1.8.0
* OSGi support thanks to @amergey

## 1.7.0
* Support for Spring MVC tests assertions

## 1.6.1
* Gson nodes are not reconverted

## 1.6.0
* Added support for Hamcrest matchers

## 1.5.6
* Fixed handling of empty value in the expected parameter

## 1.5.5
* Support for dot in node name

## 1.5.4
* Added isObject method to fluent assertions

## 1.5.3
* Jackson 1 is preferred if the serialized class contains Jackson1 annotation

## 1.5.2
*  Added support for regular expressions

## 1.5.1
* isStringEqualTo() added to fluent assertions
* isArray  added to fluent assertions

## 1.5.0
* One runtime now supports Jackson 1.x, Jackson 2.x and Gson
* Internal JsonUnit class changed in backwards incompatible way

## 1.3.0 + 0.3.0
* Options renamed
* assertJsonNot* asserts added
* Support for online configuration in Hamcrest and standard asserts added

## 1.2.0 + 0.2.0
* Error messages changed a bit when comparing structures
* Refactoring of internal classes
* Support for ignoring array order
* Support for ignoring values
* Support for ignoring extra fields

## 1.1.6 + 0.1.6
* Treat null as absent added

## 1.1.5 + 0.1.5
* Absence/presence tests added

## 1.1.4 + 0.1.4
* Path to array in root fixed

## 1.1.3 + 0.1.3
* Numeric comparison tolerance added

## 1.1.2 + 0.1.2
* jsonStringEquals and jsonStringPartEquals added

## 1.1.1 + 0.1.1
* Generics in JsonMatchers fixed

## 1.1.0 + 0.1.0
* Simplified API
* Invalid JSONs in String comparison quoted
* Runtime incompatible (compile-time compatible) changes

## 1.0.0
* Switched to Jackson 2.x
* Fluent JsonAssert renamed to JsonFluentAssert

## 0.0.16
* Fluent assertions made framework independent.

## 0.0.15
* Switched from FEST to AssertJ

## 0.0.14
* Modules refactored
* Added support for FEST assert

## 0.0.13
* Logging categories changed

## 0.0.12
* Added logging

## 0.0.11
* Ignore placeholder "${json-unit.ignore}" added 

## 0.0.10
* Text differences are closed in quotes

## 0.0.9
* Matchers added

## 0.0.7
* Made Java 5 compatible

## 0.0.6
* assertJsonPartStructureEquals added

## 0.0.5
* assertJsonPartEquals added

## 0.0.4
* Better error messages in case of different types

## 0.0.3
* Support for array types and other oddities in root

## 0.0.2
* Support for nulls

