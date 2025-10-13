Release notes
=============
## 5.0.0 (2025-10-13)
* Jackson 3 support
* Kotest 6 support
* Spring RestTestClient support with Java and Kotlin DSL
* (Breaking) Removed deprecated fluent assertions API
* Hamcrest dependency made optional
* Migrated to JSpecify (please report if it breaks anything)
* Dependency updates

## 4.1.1 (2025-05-09)
* Publishing to OSS central
* Dependency updates

## 4.1.0 (2024-11-29)
* Support for JsonPath in Spring related modules
* Dependency updates

## 4.0.0 (2024-11-16)
* See the [documentation changes](https://github.com/lukas-krecan/JsonUnit/commit/1b76844b3f07a1049b08eaf61cade97d1a3609a2)
* Support for [Spring MockMvc AssertJ assertions](https://github.com/lukas-krecan/JsonUnit?tab=readme-ov-file#spring-assertj-for-mockmvc) introduced in Spring 6.2.0
* Support for [AssertJ `asInstanceOf`](https://github.com/lukas-krecan/JsonUnit?tab=readme-ov-file#support-for-asinstanceof)
* Added `REPORTING_DIFFERENCE_AS_NORMALIZED_STRING` option
* Opentest4j added as mandatory dependency
* Johnzon upgraded to 2.0.2 (backwards incompatible due to migration from javax to jakarta namespace)
* Fluent assertions marked as deprecated
* Dependency updates

## 3.5.0 (2024-11-01)
* Direct support for `when` path option in Spring matchers
* #820 Fix Hamcrest matcher error message
* Dependency updates

## 3.4.1 + 2.40.1 (2024-07-16)
* #796 fix order of elements in JsonMap (thanks @glhez)


## 3.4.0 + 2.40.0 (2024-07-03)
* Added FAIL_FAST option

## 3.3.0 (2024-06-27)
* Perf improvements when comparing arrays of nested objects`
* Dependency updates

## 2.39.0 (2024-07-01)
* Perf improvements when comparing arrays of nested objects`

## 3.2.7 (2024-02-21)
* #483 More optimal fix for JsonPath arrays matching in `whenIgnoringPaths`
* Version 3.2.6 skipped due tue a mistake

## 3.2.5 (2024-02-20)
* #483 Fix JsonPath arrays matching in `whenIgnoringPaths`
* Dependency updates

## 3.2.4 (2024-02-05)
* Dependency updates
* Version 3.2.3 skipped due to release issues

## 3.2.2 (2023-09-14)
* Fix Kotest module dependencies

## 3.2.1 (2023-09-14)
* Support for Kotest
* Dependency upgrades

## 3.2.0
* Skipped for technical reasons

## 3.1.0 (2023-09-12)
* Support for custom matchers in Spring assertions
* Dependency upgrades

## 3.0.0 (2023-07-05)
* Requires Java 17
* Requires Spring 5.2 (when used with Spring)
* `Options` class hidden
* Deprecated methods and classes removed
* Dependency upgrades

## 2.38.0 (2023-05-22)
* Support for NumberComparator
* Dependency updates

## 2.37.0 (2023-03-23)
* Make custom matcher regexp DOTALL (#617)
* Dependency updates

## 2.36.1 (2023-01-29)
* #595 Fix slf4j dependency
* Dependency updates

## 2.36.0 (2022-10-05)
* Support for `node` method in JsonMapAssert #560
* Fixed number parsing in Jackson, so it works as [intended](https://github.com/lukas-krecan/JsonUnit#numbers) (see https://github.com/lukas-krecan/JsonUnit/issues/564 for details)
* Dependency updates

## 2.35.0 (2022-05-06)
* Special handling of numeric values in containsEntry #512
* Dependency updates

## 2.34.0 (2022-04-12)
* Prevent re-parsing of a value #502
* Dependency updates

## 2.33.0 (2022-04-02)
* Fixed #493 comparison of Tuples from extracting function

## 2.32.0 (2022-02-17)
* Fixed #474 ClassCastException in isArray()

## 2.31.0 (2022-02-07)
* Replaced JsonObjectAssert by JsonAssert

## 2.30.0 (2022-02-06)
* Parent type of JsonObjectAssert changed to AbstractAssert

## 2.29.0 (2022-02-05)
* #465 fixed assertion on array of objects. May introduce some backward incompatibility when using `isArray().element(i)`.
* Dependency updates

## 2.28.0

* Automatically register available Jackson 2 modules
* Dependency updates

## 2.27.0

* Made compatible with AssertJ 3.20.x - due to braking changes in AssertJ this version also **requires** AssertJ 3.20.x
  and higher.

## 2.26.0

* Fixed `containsEntries` in AssertJ object assert. This required to change the return type of `isObject()` method which
  may be an incompatible change for tests which store the result in a variable.

## 2.25.0

* Hamcrest matcher made compatible with Rest Assured #338
* Various library upgrades

## 2.24.0

* Fix OSGi configuration
* AssertJ updated to 3.19.0

## 2.23.0

* Better error message for multiple matches in isAbsent check
* Various library upgrades

## 2.22.1

* Better exception message in case of JSON that can not be parsed

## 2.22.0

* Support for Spring WebTestClient

## 2.21.0

* Fixed Kotlin AssertJ bundle #299

## 2.20.0
* assertThatJson accepts null as the actual value

## 2.19.0
* opentest4j made optional #276
* updated all dependencies

## 2.18.1
* Fix multiple `when` method application #234

## 2.18.0
* Support for URI assertions

## 2.17.0
* Do not append tolerance with zero value to diff message (thanks @valfirst)

## 2.16.2
* Fix regex issues on older Androids #227 (thanks @Sirrah)

## 2.16.1
* Add missing Kotlin classes to distribution Jars

## 2.16.0
* Kotlin DSL for AssertJ API

## 2.15.0
* Spring DSL Koltin support
* JUnit upgraded to 5.6.0

## 2.14.0
* Allow differentiating between number and integer

## 2.13.1
* Fix Jackson2NodeFactory thread safety issue in usage of ServiceLoader (thanks @cnauroth)

## 2.13.0
* Recompiled with AssertJ 3.15.0 to fix https://github.com/lukas-krecan/JsonUnit/issues/216
* (Not)Null annotations added
* Spring dependency updated to 5.2.3.RELEASE

## 2.12.0
* Updated dependencies
* Jackson node is not reparsed when compared #214

## 2.11.1
* Parse content as UTF-8 in Spring MVC test if not specified otherwise #212

## 2.11.0
* Fix Kotlin 'Inaccessible type' warning in when-path (@Vladiatro)
* Load resources as UTF-8 (@bencampion)

## 2.10.0
* Support for PathOptions
* AssertJ - support for chaining assertions in the same root
* Support for json-path in AssertJ `whenIgnoringPaths`

## 2.9.0
* Hamcrest upgraded to 2.1
* AssertJ dependency upgraded to 3.12.3 (requires AssertJ > 3.10.0)

## 2.8.1
* hamcrest-core dependency marked as required

## 2.8.0
* #185 JsonUnitRequestMatchers for client-side REST testing
* Support for array (non)emptiness in Fluent assert

## 2.7.0
* Support for Johnzon (requires 1.1.12) (thanks to elexx)


## 2.6.3
* Ignoring paths even when present in expected value #182

## 2.6.2
* Fixed AssertionErrors messages in MultipleFailuresError #181

## 2.6.1
* Path with backspaces matching fixed #176

## 2.6.0
* ${json-unit.ignore-elements} introduced

## 2.5.1
* Array comparison optimization

## 2.5.0
* Fix bug and performance issues in array comparison
* Performance optimizations

## 2.4.0
* Introduced JsonAssertions.value()
* Fixed AssertJ withFailMessage

## 2.3.0
* Support for Jackson 2 ObjectMapper customization
* Some AbstractObjectAssert marked as unsupported

## 2.2.0
* Using opentest4j
* Refactored exception reporting

## 2.1.1
* Better exception reporting
* Fixed invalid Automatic-Module-Name

## 2.0.3
* Fixed missing node handling with JsonPath
* Fixed some complex AsserJ comaprisons

## 2.0.2
* Fixed #144 (AssertJ object handling)

## 2.0.1
* Support for # instead of $ in placeholders

## 2.0.0.RC5
* More expressive Spring assertions (isNull, isNotNull, isTrue, isFalse)

## 2.0.0.RC4
* AssertJ - fix bug with '%' in error message
* Removed support for Jackson 1

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
  Please do not hesitate to report issues.
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
* Support for parametres in custom matchers ${json-unit.matches:matcherName}param

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

