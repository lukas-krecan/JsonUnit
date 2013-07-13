JsonUnit
===========

JsonUnit is a library that simplifies JSON comparison in unit tests. It's strongly inspired by XmlUnit, although it much more primitive. The usage is
simple:

    import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
    
    ...
    
    assertJsonEquals("{\"test\":1}", "{\n\"foo\": 1\n}");
    
    assertJsonPartEquals("2", "{\"test\":[{\"value\":1},{\"value\":2}]}", "test[1].value");
    
When the values are compared, order of elements and whitespaces are ignored. On the other hand values 1 and 1.0 are considered to be different.

Hamcers matchers
----------------
Since 0.0.9 Hamcrest matchers are supported, so you write the tests in the following way

    import static net.javacrumbs.jsonunit.JsonMatchers.*;
    import static org.junit.Assert.*;

    assertThat("{\"test\":1}", jsonEquals("{\"test\": 1}"));
    assertThat("{\"test\":1}", jsonPartEquals("test", "1"));



Sample output
-------------
For example 

    assertJsonEquals("{\n" +
			"   \"test\":[\n" +
			"      1,\n" +
			"      2,\n" +
			"      {\n" +
			"         \"child\":{\n" +
			"            \"value1\":1,\n" +
			"            \"value2\":true,\n" +
			"            \"value3\":\"test\",\n" +
			"            \"value4\":{\n" +
			"               \"leaf\":5\n" +
			"            }\n" +
			"         }\n" +
			"      }\n" +
			"   ],\n" +
			"   \"root2\":false,\n" +
			"   \"root3\":1\n" +
			"}",
			"{\n" +
			"   \"test\":[\n" +
			"      5,\n" +
			"      false,\n" +
			"      {\n" +
			"         \"child\":{\n" +
			"            \"value1\":5,\n" +
			"            \"value2\":\"true\",\n" +
			"            \"value3\":\"test\",\n" +
			"            \"value4\":{\n" +
			"               \"leaf2\":5\n" +
			"            }\n" +
			"         },\n" +
			"         \"child2\":{\n" +
			"\n" +
			"         }\n" +
			"      }\n" +
			"   ],\n" +
			"   \"root4\":\"bar\"\n" +
			"}");
			
Results in

	JSON documents have different structures:
	Different keys found in node "". Expected [root2, root3, test], got [root4, test].
	Different keys found in node "test[2]". Expected [child], got [child, child2].
	Different keys found in node "test[2].child.value4". Expected [leaf], got [leaf2].
	JSON documents have different values:
	Different value found in node "test[0]". Expected 1, got 5.
	Different values found in node "test[1]". Expected '2', got 'false'.
	Different value found in node "test[2].child.value1". Expected 1, got 5.
	Different values found in node "test[2].child.value2". Expected 'true', got '"true"'.

Ignoring values
----------------
Sometimes you need to ignore certain values when comparing. It is possible to use ${json-unit.ignore}" 
placeholder like this 

    assertJsonEquals("{\"test\":\"${json-unit.ignore}\"}", "{\n\"test\": {\"object\" : {\"another\" : 1}}}");

Logging
-------
Although the differences are printed out by the assert statement, sometimes you use JsonUnit with other libraries like
[Jadler](http://jadler.net) that do not print the differences between documents. In such case, you can switch the
logging on. JsonUnit uses [SLF4J](http://www.slf4j.org/). The only thing you need to do is to configure your logging
framework to log `net.javacrumbs.jsonunit.Difference` on INFO or DEBUG level. On info level it will log the differences,
on DEBUG level compared parts of the document will be logged.

Maven dependency
----------------
JsonUnit is accessible in Maven central repository
	
	<dependency>
    	<groupId>net.javacrumbs.json-unit</groupId>
    	<artifactId>json-unit</artifactId>
    	<version>0.0.12</version>
    	<scope>test</scope>
	</dependency>
	
Licence
-------
JsonUnit is licensed under [Apache 2.0 licence](https://www.apache.org/licenses/LICENSE-2.0). It's built on top 
of [Jackson](http://jackson.codehaus.org/)


Known limitations
-----------------
1. It's not much extensible. The interface is intentionally simple, extensibility might be added in the future. 
2. It's not possible to change number comparison strategy. 
