package net.javacrumbs.jsonunit.kotest

import io.kotest.assertions.print.Printed
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.DiffableMatcherResult
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.and
import io.kotest.matchers.should
import java.math.BigDecimal
import net.javacrumbs.jsonunit.core.Configuration
import net.javacrumbs.jsonunit.core.Configuration.empty as default
import net.javacrumbs.jsonunit.core.internal.Diff
import net.javacrumbs.jsonunit.core.internal.JsonUtils
import net.javacrumbs.jsonunit.core.internal.JsonUtils.getPathPrefix
import net.javacrumbs.jsonunit.core.internal.Node
import net.javacrumbs.jsonunit.core.internal.Node.NodeType
import net.javacrumbs.jsonunit.core.internal.Path
import net.javacrumbs.jsonunit.jsonpath.InternalJsonPathUtils
import net.javacrumbs.jsonunit.jsonpath.JsonPathAdapter
import org.opentest4j.AssertionFailedError

/**
 * Returns a [Matcher] that verifies that two JSON objects are equal. Can be customized by providing configuration like
 * this:
 *
 *  ```kotlin
 *  """{"test":1.01}""" should equalJson("""{"test":1}""", configuration { withTolerance(0.1).withOptions(IGNORING_ARRAY_ORDER) })
 *  ```
 */
fun equalJson(expected: Any?, configuration: Configuration = default()): Matcher<Any?> = Matcher { actual ->
    match(actual, configuration, expected)
}

private fun match(actual: Any?, configuration: Configuration, expected: Any?): MatcherResult {
    val originalJson = if (actual is JsonSourceWrapper) actual.originalJson else actual
    val resolvedJson = actual.resolveJson()
    val config = InternalJsonPathUtils.resolveJsonPaths(originalJson, configuration)
    val diff = Diff.create(expected, resolvedJson, "actual", Path.create("", getPathPrefix(resolvedJson)), config)
    return MatcherResult(diff.similar(), { diff.differences() }, { "Expected values to not match\n" })
}

private fun Any?.resolveJson(): Any? = if (this is JsonSourceWrapper) this.jsonSource else this

/** Helper method to create [Configuration] object. */
fun configuration(configurer: Configuration.() -> Configuration): Configuration {
    return configurer(default())
}

/**
 * Takes given JSON and moves assertion to given path. For example:
 * ```kotlin
 *  """{"test":1}""" inPath ("test") should beJsonNumber()
 * ```
 */
infix fun Any?.inPath(path: String): Any =
    if (this is JsonSourceWrapper) {
        JsonSourceWrapper(this.originalJson, JsonPathAdapter.inPath(this.jsonSource, path))
    } else {
        JsonSourceWrapper(this, JsonPathAdapter.inPath(this, path))
    }

/** Returns matcher that asserts that given JSON node is a JSON object. */
fun beJsonObject(): Matcher<Any?> = beType(NodeType.OBJECT)

/** Returns matcher that asserts that given JSON node is a JSON array. */
fun beJsonArray(): Matcher<Any?> = beType(NodeType.ARRAY)

/** Returns matcher that asserts that given JSON node is a string. */
fun beJsonString(): Matcher<Any?> = beType(NodeType.STRING)

/** Returns matcher that asserts that given JSON node is a number. */
fun beJsonNumber(): Matcher<Any?> = beType(NodeType.NUMBER)

/** Returns matcher that asserts that given JSON node is a boolean. */
fun beJsonBoolean(): Matcher<Any?> = beType(NodeType.BOOLEAN)

/** Returns matcher that asserts that given JSON node is present and null. */
fun beJsonNull(): Matcher<Any?> = beType(NodeType.NULL)

/** Returns matcher that asserts that given JSON node is present. */
fun bePresent(): Matcher<Any?> = Matcher { actual ->
    val actualValue = actual.resolveJson()
    val node = getNode(actualValue)
    MatcherResult(
        !node.isMissingNode,
        { "Node \"${getPathPrefix(actualValue)}\" is missing." },
        { "Node \"${getPathPrefix(actualValue)}\" is present." },
    )
}

private fun beType(expectedType: NodeType): Matcher<Any?> =
    bePresent() and
        Matcher { actual ->
            val actualValue = actual.resolveJson()
            val node = getNode(actualValue)
            MatcherResult(
                node.nodeType == expectedType,
                {
                    "Node \"${getPathPrefix(actualValue)}\" has invalid type, expected: <${expectedType.description}> but was: <$node>."
                },
                {
                    "Node \"${getPathPrefix(actualValue)}\" has invalid type, expected to not be ${expectedType.description} but was: <$node>."
                },
            )
        }

private fun getNode(actual: Any?): Node = JsonUtils.getNode(actual.resolveJson(), "")

/** Asserts that JSON node is present, is a number and returns the value as [BigDecimal]. */
fun Any?.shouldBeJsonNumber(): BigDecimal {
    this should beJsonNumber()
    return shouldNotThrowAny { getNode(this).decimalValue() }
}

/** Asserts that JSON node is present, is a string and returns the value as [String]. */
fun Any?.shouldBeJsonString(): String {
    this should beJsonString()
    return shouldNotThrowAny { getNode(this).asText() }
}

/** Asserts that JSON node is present, is a boolean and returns the value as [Boolean]. */
fun Any?.shouldBeJsonBoolean(): Boolean {
    this should beJsonBoolean()
    return shouldNotThrowAny { getNode(this).asBoolean() }
}

/** Asserts that JSON node is present, is an array and returns the value as [List]. */
fun Any?.shouldBeJsonArray(): List<*> {
    this should beJsonArray()
    return shouldNotThrowAny { getNode(this).value as List<*> }
}

/** Asserts that JSON node is present, is an object and returns the value as [Map]. */
fun Any?.shouldBeJsonObject(): Map<String, *> {
    this should beJsonObject()
    @Suppress("UNCHECKED_CAST")
    return shouldNotThrowAny { getNode(this).value as Map<String, *> }
}

private class JsonSourceWrapper(val originalJson: Any?, val jsonSource: Any?) {
     override fun toString(): String {
        return jsonSource.toString()
    }
}
