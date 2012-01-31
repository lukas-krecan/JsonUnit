JsonUnit
===========

JsonUnit is a library that simplifies JSON comparison in unit tests. It's strongly inspired by XmlUnit, although it much more primitive. The usage is
simple:

    import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
    
    ...
    
    assertJsonEquals("{\"test\":1}", "{\n\"foo\": 1\n}");
    
When the values are compared, order of elements and whitespaces are ignored. On the other hand values 1 and 1.0 are considered to be different.  

Maven dependency
----------------
JsonUnit is accessible in Maven central repository
	
	<dependency>
    	<groupId>net.javacrumbs.json-unit</groupId>
    	<artifactId>json-unit</artifactId>
    	<version>0.0.2</version>
	</dependency>
	
Licence
-------
JsonUnit is licensed under [Apache 2.0 licence](https://www.apache.org/licenses/LICENSE-2.0)


Known limitations
-----------------
1. It's not much extensible. The interface is intentionally simple, extensibility might be added in the future. 
2. It's not possible to change number comparison strategy. 