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
