JsonUnit
===========

JsonUnit is a library that simplifies JSON comparison in unit tests. It's strongly inspired by XmlUnit, although it much more primitive. The usage is
simple:

    import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
    
    ...
    
    assertJsonEquals("{\"test\":1}", "{\n\"foo\": 1\n}");
    
When the values are compared, order of elements and whitespaces are ignored. On the other hand values 1 and 1.0 are considered to be different.  

Known limitations
-----------------
1. It's not much extensible. The interface is intentionally simple, extensibility might be added in the future. 
2. It's not possible to change number comparison strategy. 