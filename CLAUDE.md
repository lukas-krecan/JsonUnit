# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

```bash
# Build entire project
./mvnw clean install

# Run all tests
./mvnw test

# Build/test a single module (e.g. json-unit-assertj)
./mvnw -pl json-unit-assertj -am test

# Run a specific test class
./mvnw -pl json-unit-spring -am -Dtest=MockMvcTest test

# Run a specific test method
./mvnw -pl json-unit-kotest -am -Dtest=KotestTest#"Should assert path" test

# Skip tests
./mvnw clean install -DskipTests
```

## Code Formatting

Spotless is enforced: Palantir Java Format for Java, ktfmt (KOTLINLANG style, 120 cols) for Kotlin.

```bash
./mvnw spotless:apply      # fix formatting
./mvnw spotless:check      # check only
```

All source files must include the Apache 2.0 license header (see `header.txt`). Error Prone with NullAway runs at compile time — use `@Nullable` from JSpecify for nullable parameters/return values.

## Module Structure

```
json-unit-core          ← core comparison engine + JsonPath adapter
json-unit               ← classic Hamcrest API
json-unit-assertj       ← recommended AssertJ fluent API
json-unit-spring        ← MockMvc / WebTestClient / RestTestClient integration
json-unit-kotest        ← Kotlin/Kotest integration
json-unit-bom           ← Bill of Materials (published)
json-unit-json-path     ← deprecated, now a relocation shim to core

tests/test-base         ← shared base test classes (NOT published)
tests/test-jackson2     ← runs test-base suite against Jackson 2
tests/test-gson         ← runs test-base suite against Gson
tests/test-*            ← same pattern for Moshi, Johnzon, JSON.org, Kotlin, etc.
```

The `tests/` submodules are not published to Maven Central. Each `test-*` module instantiates the abstract test classes from `test-base` with a concrete JSON library.

## Architecture

### Core (`json-unit-core`)

- **`Node` / `NodeFactory`** — library-agnostic JSON value abstraction. Each supported library (Jackson2, Jackson3, Gson, Moshi, Johnzon, JSON.org) has its own `NodeFactory`. `Converter` detects what's on the classpath and wires the right factory.
- **`Diff`** — the recursive comparison engine. Walks expected vs actual node trees, applies options, resolves placeholders, and emits `Difference` events.
- **`Configuration`** — immutable settings object (numeric tolerance, `Option` flags, ignored paths, custom matchers, `DifferenceListener`). All `with*` methods return a new instance.
- **`Option`** — flags like `IGNORING_ARRAY_ORDER`, `IGNORING_EXTRA_FIELDS`, `IGNORING_VALUES`, `TREATING_NULL_AS_ABSENT`, `FAIL_FAST`.
- **`jsonpath/`** — `JsonPathAdapter.inPath()` and `InternalJsonPathUtils.resolveJsonPaths()` provide JsonPath integration using the Jayway library.

### Placeholder system (inside `Diff`)

Expected JSON strings can contain special tokens:
- `${json-unit.ignore}` — skip field
- `${json-unit.any-number}`, `${json-unit.any-string}`, `${json-unit.any-boolean}` — type-match
- `${json-unit.matches:name}` — delegate to a named Hamcrest matcher
- `${json-unit.regex}PATTERN` — regex match

### API modules

Each API module is a thin wrapper over the core engine:
- **AssertJ** (`JsonAssertions.assertThatJson()`) — `JsonAssert` / `ConfigurableJsonAssert` chain fluent calls to `InternalMatcher`.
- **Spring** — `AbstractSpringMatchers` is the shared base for `JsonUnitResultMatchers` (MockMvc), `WebTestClientJsonMatcher`, `RestTestClientJsonMatcher`, and `JsonUnitRequestMatchers`. `node()` is an alias for `inPath()`.
- **Kotest** — infix `inPath` operator returns a `JsonSourceWrapper` that preserves original JSON for wildcard path resolution. The `equalJson` matcher calls `InternalJsonPathUtils.resolveJsonPaths()`.

### Shared test pattern

`test-base/src/main/java/.../AbstractAssertJTest.java` (and siblings) define the canonical test suite. Each `test-*` module extends these with a concrete factory. To add a test that should run against all JSON libraries, add it to the abstract base class.

## JsonPath support

All API modules delegate to:
- `JsonPathAdapter.inPath(json, path)` — navigate to path
- `InternalJsonPathUtils.resolveJsonPaths(json, config)` — expand wildcard paths in `Configuration.whenIgnoringPaths` / `ConfigurationWhen.paths`

Both dot-notation (`result.array[0]`) and full JsonPath syntax (`$.result.array[*]`) are accepted by `inPath()` / `node()`.
