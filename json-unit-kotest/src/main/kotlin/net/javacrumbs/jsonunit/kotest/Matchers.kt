package net.javacrumbs.jsonunit.kotest

import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.and
import io.kotest.matchers.should
import net.javacrumbs.jsonunit.core.Configuration
import net.javacrumbs.jsonunit.core.internal.Diff
import net.javacrumbs.jsonunit.core.internal.JsonUtils
import net.javacrumbs.jsonunit.core.internal.JsonUtils.getPathPrefix
import net.javacrumbs.jsonunit.core.internal.Node
import net.javacrumbs.jsonunit.core.internal.Node.NodeType
import net.javacrumbs.jsonunit.core.internal.Path
import net.javacrumbs.jsonunit.jsonpath.JsonPathAdapter
import java.math.BigDecimal

fun equalJson(
        expected: Any,
        configuration: Configuration = Configuration.empty()
): Matcher<Any> = Matcher { actual ->
    val diff =  Diff.create(expected, actual, "actual", Path.create("", getPathPrefix(actual)), configuration)
    MatcherResult(
            diff.similar(),
            { diff.differences() },
            { "Expected values to not match\n" }
    )
}

fun beJsonNumber(): Matcher<Any> = beType(NodeType.NUMBER)

fun beJsonNull(): Matcher<Any> = beType(NodeType.NULL)

fun beJsonArray(): Matcher<Any> = beType(NodeType.ARRAY)

fun beJsonString(): Matcher<Any> = beType(NodeType.STRING)

fun beJsonBoolean(): Matcher<Any> = beType(NodeType.BOOLEAN)

// todo: test
fun bePresent(): Matcher<Any> = Matcher { actual ->
    val node = getNode(actual)
    MatcherResult(
            !node.isMissingNode,
            { "Node \"${getPathPrefix(actual)}\" is missing." },
            { "Node \"${getPathPrefix(actual)}\" is present." }
    )
}

private fun beType(expectedType: NodeType): Matcher<Any> = bePresent() and Matcher { actual ->
    val node = getNode(actual)
    MatcherResult(
            node.nodeType == expectedType,
            { "Node \"${getPathPrefix(actual)}\" has invalid type, expected: <${expectedType.description}> but was: <$node>." },
            { "Node \"${getPathPrefix(actual)}\" has invalid type, expected to not be ${expectedType.description} but was: <$node>." }
    )
}

private fun getNode(actual: Any): Node = JsonUtils.getNode(actual, "")

fun Any.shouldBeJsonNumber(): BigDecimal {
    this should beJsonNumber()
    return getNode(this).decimalValue()
}

fun Any.shouldBeJsonString(): String {
    this should beJsonString()
    return getNode(this).asText()
}

fun Any.shouldBeJsonBoolean(): Boolean {
    this should beJsonBoolean()
    return getNode(this).asBoolean()
}

fun Any.shouldBeJsonArray(): List<*> {
    this should beJsonArray()
    return getNode(this).value as List<*>
}



infix fun Any.inPath(path: String): Any = JsonPathAdapter.inPath(this, path)

