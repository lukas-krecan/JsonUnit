# Vintage APIs
There are two API types that are still supported but not recommended to use for new tests.

  * [Standard assert](#standard)
  * [Fluent assertions](#fluent)

## <a name="fluent"></a>Fluent assertions (deprecated)
Fluent assertions were inspired by FEST and AssertJ. This API was created before AssertJ become so popular
so it does not depend on it. I would recommend to use AssertJ integration.

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

For more examples see [the tests](https://github.com/lukas-krecan/JsonUnit/blob/master/tests/test-base/src/main/java/net/javacrumbs/jsonunit/test/base/AbstractJsonFluentAssertTest.java).


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
    <version>2.14.0</version>
    <scope>test</scope>
</dependency>
```

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

//Numerical comparison
assertJsonEquals("1", "\n1.009\n", withTolerance(0.01));
```

To use import
```xml
<dependency>
    <groupId>net.javacrumbs.json-unit</groupId>
    <artifactId>json-unit</artifactId>
    <version>2.14.0</version>
    <scope>test</scope>
</dependency>
```

For more examples see [the tests](https://github.com/lukas-krecan/JsonUnit/blob/master/tests/test-base/src/main/java/net/javacrumbs/jsonunit/test/base/AbstractJsonAssertTest.java).
