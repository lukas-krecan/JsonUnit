package net.javacrumbs.jsonunit.test.kotlin

import net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson
import net.javacrumbs.jsonunit.core.ConfigurationWhen.path
import net.javacrumbs.jsonunit.core.ConfigurationWhen.then
import net.javacrumbs.jsonunit.core.Option
import org.junit.jupiter.api.Test


class KotlinAssertJTest {
    @Test
    fun shouldIgnoreArrayOrderInSpecificPath() {
        assertThatJson("{\"obj\":{\"a\": [1, 2], \"b\": [3, 4]}}")
                .`when`(path("obj.a"), then(Option.IGNORING_ARRAY_ORDER))
                .isEqualTo("{\"obj\":{\"a\": [2, 1], \"b\": [3, 4]}}")
    }
}