package net.javacrumbs.jsonunit.test.base

import net.javacrumbs.jsonunit.assertj.assertThatJson
import net.javacrumbs.jsonunit.assertj.whenever
import net.javacrumbs.jsonunit.core.ConfigurationWhen.path
import net.javacrumbs.jsonunit.core.ConfigurationWhen.then
import net.javacrumbs.jsonunit.core.Option
import org.junit.jupiter.api.Test

abstract class AbstractAssertJKotlinTest {
    @Test
    fun `Should work with Kotlin`() {
        assertThatJson("""{"root":{"a":1, "b": 2}}""") {
            isObject
            node("root.a").isEqualTo(1)
            node("root.b").isEqualTo(2)
        }
    }

    @Test
    fun `Should work with Kotlin simple`() {
        assertThatJson("""{"root":{"a":1, "b": 2}}""").node("root").isObject.isEqualTo("""{"a":1, "b": 2}""")
    }

    @Test
    fun `When should have synonym`() {
        assertThatJson("{\"a\":[{\"b\": 1}, {\"c\": 1}, {\"d\": 1}]}")
            .whenever(Option.IGNORING_ARRAY_ORDER, Option.TREATING_NULL_AS_ABSENT)
            .node("a")
            .isArray
            .isEqualTo("[{\"c\": 1}, {\"b\": 1} ,{\"d\": 1}]")
    }

    @Test
    fun `Specific when should have synonym`() {
        assertThatJson("{\"obj\":{\"a\": [1, 2], \"b\": [3, 4]}}")
            .whenever(path("obj.a"), then(Option.IGNORING_ARRAY_ORDER))
            .isEqualTo("{\"obj\":{\"a\": [2, 1], \"b\": [3, 4]}}")
    }

    @Test
    fun `In-path should work`() {
        assertThatJson("{\"test1\":2, \"test2\":1}") {
            inPath("test1").isEqualTo(2)
            inPath("test2").isEqualTo(1)
        }
    }

    @Test
    fun assertSame() {
        val s = """{ "a": 0.0 }"""
        assertThatJson(s).isEqualTo(s)
    }
}
