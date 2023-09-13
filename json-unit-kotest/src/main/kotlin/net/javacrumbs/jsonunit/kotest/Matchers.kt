package net.javacrumbs.jsonunit.kotest

import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import net.javacrumbs.jsonunit.core.Configuration
import net.javacrumbs.jsonunit.core.internal.Diff
import net.javacrumbs.jsonunit.core.internal.JsonUtils
import net.javacrumbs.jsonunit.core.internal.JsonUtils.getPathPrefix
import net.javacrumbs.jsonunit.core.internal.Path
import net.javacrumbs.jsonunit.jsonpath.JsonPathAdapter

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

infix fun Any.inPath(path: String): Any = JsonPathAdapter.inPath(this, path)

internal data class Json(val actual: Any, val path: Path)
