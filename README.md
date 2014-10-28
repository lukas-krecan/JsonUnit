JsonUnit
===========

JsonUnit is a library that simplifies JSON comparison in unit tests. It's strongly inspired by XmlUnit, although it much more primitive. The usage is
simple:

    import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
    
    ...

    // compares two JSON documents
    assertJsonEquals("{\"test\":1}", "{\n\"foo\": 1\n}");

    // compares only part
    assertJsonPartEquals("2", "{\"test\":[{\"value\":1},{\"value\":2}]}", "test[1].value");

    // compares only the structure, not the values
    assertJsonStructureEquals("[{\"test\":1}, {\"test\":2}]", "[{\n\"test\": 1\n}, {\"TEST\": 4}]")
    
When the values are compared, order of elements and whitespaces are ignored. 

Hamcrests matchers
----------------
You use Hamcrest matchers in the following way

    import static net.javacrumbs.jsonunit.JsonMatchers.*;
    import static org.junit.Assert.*;
    ...

    assertThat("{\"test\":1}", jsonEquals("{\"test\": 1}"));
    assertThat("{\"test\":1}", jsonPartEquals("test", 1));
    assertThat("{\"test\":[1, 2, 3]}", jsonPartEquals("test[0]", 1));

Ignoring values
----------------
Sometimes you need to ignore certain values when comparing. It is possible to use ${json-unit.ignore}
placeholder like this

    assertJsonEquals("{\"test\":\"${json-unit.ignore}\"}",
        "{\n\"test\": {\"object\" : {\"another\" : 1}}}");

Fluent assertions
---------------
Fluent (FEST or AssertJ like) assertions are supported by a special module json-unit-fluent

    import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;
    ...

    // compares entire documents
    assertThatJson("{\"test\":1}").isEqualTo("{\"test\":2}");

    // compares only parts of the document
    assertThatJson("{\"test1\":2, \"test2\":1}")
        .node("test1").isEqualTo(2)
        .node("test2").isEqualTo(2);

    assertThatJson("{\"root\":{\"test\":[1,2,3}}")
        .node("root.test[0]").isEqualTo(1);

    // compares only the structure
    assertThatJson("{\"test\":1}").hasSameStructureAs("{\"test\":21}");

    // ignores a value
    assertThatJson("{\"test\":1}").isEqualTo("{\"test\":\"${json-unit.ignore}\"}");

    // ignores a value with a different placeholder
    assertThatJson("{\"test\":1}").ignoring("##IGNORE##").isEqualTo("{\"test\":\"##IGNORE##\"}")

Options
---------------
There are multiple options how you can configure the comparison

**TREAT_NULL_AS_ABSENT** - fields with null values are equivalent to absent fields. For example, this test passes
  
    JsonAssert.setOptions(TREAT_NULL_AS_ABSENT);
    assertJsonEquals("{\"test\":{\"a\":1}}",
                     "{\"test\":{\"a\":1, \"b\": null, \"c\": null}}");
    
**IGNORE_ARRAY_ORDER** - ignores order in arrays

    JsonAssert.setOptions(IGNORE_ARRAY_ORDER);
    assertJsonEquals("{\"test\":[1,2,3]}", 
                     "{\"test\":[3,2,1]}");
    
**IGNORE_EXTRA_FIELDS** - ignores extra fileds in the compared value

    JsonAssert.setOptions(IGNORE_EXTRA_FIELDS);
    assertJsonEquals("{\"test\":{\"b\":2}}", 
                     "{\"test\":{\"a\":1, \"b\":2, \"c\":3}}");
    
**IGNORE_VALUES** - ignores values and compares only types

    JsonAssert.setOptions(IGNORE_VALUES);
    assertJsonEquals("{\"test\":{\"a\":1,\"b\":2,\"c\":3}}", 
                     "{\"test\":{\"a\":3,\"b\":2,\"c\":1}}");
    
It is possible to combine options. 

    JsonAssert.setOptions(IGNORE_ARRAY_ORDER, IGNORE_EXTRA_FIELDS);
    assertJsonEquals("{\"test\":[{\"key\":1},{\"key\":2},{\"key\":3}]}", 
                     "{\"test\":[{\"key\":3},{\"key\":2, \"extraField\":2},{\"key\":1}]}");

In fluent assertion, you can set options like this

    assertThatJson("{\"test\":{\"a\":1, \"b\":2, \"c\":3}}")
        .when(IGNORE_EXTRA_FIELDS).isEqualTo("{\"test\":{\"b\":2}}");

Numeric comparison
--------------------
Numbers are by default compared in the following way:

* If the type differs, the number is different. So 1 and 1.0 are different (int vs. float)
* Floating number comparison is exact

You can change this behavior by setting tolerance

    setTolerance(0.01);
    assertJsonEquals("1", "\n1.009\n");

Or for fluent assertions

    assertThatJson("{\"test\":1.00001}").node("test").withTolerance(0.001).isEqualTo(1);

Logging
-------
Although the differences are printed out by the assert statement, sometimes you use JsonUnit with other libraries like
[Jadler](http://jadler.net) that do not print the differences between documents. In such case, you can switch on the
logging. JsonUnit uses [SLF4J](http://www.slf4j.org/). The only thing you need to do is to configure your logging
framework to log `net.javacrumbs.jsonunit.difference` on DEBUG level.

Maven dependency
----------------
JsonUnit is accessible in Maven central repository
	
	<dependency>
    	<groupId>net.javacrumbs.json-unit</groupId>
    	<artifactId>json-unit</artifactId>
    	<!-- Jackson 2.x -->
        <version>1.2.0</version>
        <!-- Jackson 1.x -->
        <!--<version>0.2.0</version>-->
    	<scope>test</scope>
	</dependency>

To use fluent assertions:

	<dependency>
    	<groupId>net.javacrumbs.json-unit</groupId>
    	<artifactId>json-unit-fluent</artifactId>
        <!-- Jackson 2.x -->
        <version>1.2.0</version>
        <!-- Jackson 1.x -->
        <!--<version>0.2.0</version>-->
    	<scope>test</scope>
	</dependency>

Licence
-------
JsonUnit is licensed under [Apache 2.0 licence](https://www.apache.org/licenses/LICENSE-2.0). It's built on top 
of [Jackson](http://jackson.codehaus.org/)


