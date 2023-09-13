package net.javacrumbs.jsonunit.kotest

import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import net.javacrumbs.jsonunit.core.Configuration
import net.javacrumbs.jsonunit.core.internal.Diff
import net.javacrumbs.jsonunit.core.internal.Path

fun equalJson(
        expected: Any,
        configuration: Configuration = Configuration.empty()
): Matcher<Any> = Matcher { actual ->
    val diff = Diff.create(expected, actual, "actual", "", configuration)
    MatcherResult(
            diff.similar(),
            { diff.differences() },
            { "Expected values to not match\n" }
    )
}

