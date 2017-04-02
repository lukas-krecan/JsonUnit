JsonUnit [![Build Status](https://travis-ci.org/lukas-krecan/JsonUnit.png?branch=master)](https://travis-ci.org/lukas-krecan/JsonUnit) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/net.javacrumbs.json-unit/json-unit/badge.svg)](https://maven-badges.herokuapp.com/maven-central/net.javacrumbs.json-unit/json-unit)
========

JsonUnit is a library that simplifies JSON comparison in unit tests. The usage is
simple:

```java
import static net.javacrumbs.jsonunit.JsonAssert.*;
import static net.javacrumbs.jsonunit.core.Option.*;

...

// compares two JSON documents
assertJsonEquals("{\"test\":1}", "{\n\"test\": 1\n}");

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
    
When the values are compared, order of elements and whitespaces are ignored. 

Hamcrests matchers
----------------
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

Fluent assertions
---------------
Fluent (FEST or AssertJ like) assertions are supported by a special module json-unit-fluent

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
```

```java
import static java.math.BigDecimal.valueOf;
...
// Numbers sent to matchers are BigDecimals.
assertThatJson("{\"test\":[{\"value\":1},{\"value\":2},{\"value\":3}]}")
    .node("test")
    .matches(everyItem(jsonPartMatches("value", lessThanOrEqualTo(valueOf(4)))));
```

### Hamcrest matchers in fluent assertions

It is possible to combine fluent assertions with hamcrest matchers using `matches` method. For example

```java
assertThatJson("{\"test\":[1,2,3]}").node("test").matches(hasItem(valueOf(1)));

assertThatJson("{\"test\":[{\"value\":1},{\"value\":2},{\"value\":3}]}")
    .node("test")
    .matches(everyItem(jsonPartMatches("value", lessThanOrEqualTo(valueOf(4)))));
```

Spring MVC assertions
---------------------
Since version 1.7.0 JsonUnit supports Spring MVC test assertions. For example

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

Ignoring values
----------------
Sometimes you need to ignore certain values when comparing. It is possible to use ${json-unit.ignore}
placeholder like this

```java
assertJsonEquals("{\"test\":\"${json-unit.ignore}\"}",
    "{\n\"test\": {\"object\" : {\"another\" : 1}}}");
```

Regular expressions
-------------------
It is also possible to use regular expressions to compare string values

```java
assertJsonEquals("{\"test\": \"${json-unit.regex}[A-Z]+\"}", 
    "{\"test\": \"ABCD\"}");
```

Type placeholders
-----------------
If you want to assert just a type, but you do not care about the exact value, you can use any-* placehloder like this

```java
assertThatJson("{\"test\":\"value\"}")
    .isEqualTo("{test:'${json-unit.any-string}'}");

assertThatJson("{\"test\":true}")
    .isEqualTo("{\"test\":\"${json-unit.any-boolean}\"}");
    
assertThatJson("{\"test\":1.1}")
    .isEqualTo("{\"test\":\"${json-unit.any-number}\"}");
```


Options
---------------
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

**IGNORING_EXTRA_FIELDS** - ignores extra fileds in the compared value

```java
assertJsonEquals("{\"test\":{\"b\":2}}", 
    "{\"test\":{\"a\":1, \"b\":2, \"c\":3}}",
    when(IGNORING_EXTRA_FIELDS));
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

For standard asserts and Hamcrest matchers, it is possible to set the configuration globally

```java
JsonAssert.setOptions(IGNORING_ARRAY_ORDER, IGNORING_EXTRA_FIELDS);
```

In fluent assertion, you can set options in the following way

```java
assertThatJson("{\"test\":{\"a\":1, \"b\":2, \"c\":3}}")
    .when(IGNORING_EXTRA_FIELDS).isEqualTo("{\"test\":{\"b\":2}}");
```

Please note that `when` method has to be called **before** the actual comparison.

Numeric comparison
--------------------
Numbers are by default compared in the following way:

* If the type differs, the number is different. So 1 and 1.0 are different (int vs. float)
* Floating number comparison is exact

You can change this behavior by setting tolerance

```java
assertJsonEquals("1", "\n1.009\n", withTolerance(0.01));
```

or globally 

```java
JsonAssert.setTolerance(0.01);
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

Logging
-------
Although the differences are printed out by the assert statement, sometimes you use JsonUnit with other libraries like
[Jadler](http://jadler.net) that do not print the differences between documents. In such case, you can switch on the
logging. JsonUnit uses [SLF4J](http://www.slf4j.org/). The only thing you need to do is to configure your logging
framework to log `net.javacrumbs.jsonunit.difference` on DEBUG level.

Selecting underlying library
----------------------------
JsonUnit is trying to cleverly match which JSON library to use. In case you need to change the default behavior, you can use
json-unit.libraries system property. For example `-Djson-unit.libraries=jackson2,gson` or `System.setProperty("json-unit.libraries", "jackson1");`. Supported values are gson, json.org, jackson1, jackson2

Maven dependency
----------------
JsonUnit is accessible in Maven central repository. In order for it to work, you need either, [Jackson](http://jackson.codehaus.org/) 1.x,
Jackson 2.x, [Gson](https://code.google.com/p/google-gson/) or [JSONObject](https://developer.android.com/reference/org/json/JSONObject.html) on the classpath.

```xml	
<dependency>
    <groupId>net.javacrumbs.json-unit</groupId>
    <artifactId>json-unit</artifactId>
    <version>1.21.0</version>
    <scope>test</scope>
</dependency>
```

To use fluent assertions:

```xml
<dependency>
    <groupId>net.javacrumbs.json-unit</groupId>
    <artifactId>json-unit-fluent</artifactId>
    <version>1.21.0</version>
    <scope>test</scope>
</dependency>
```

To use Spring MVC assertions:

```xml
<dependency>
    <groupId>net.javacrumbs.json-unit</groupId>
    <artifactId>json-unit-spring</artifactId>
    <version>1.21.0</version>
    <scope>test</scope>
</dependency>
```

Licence
-------
JsonUnit is licensed under [Apache 2.0 licence](https://www.apache.org/licenses/LICENSE-2.0).

Release notes
=============
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

