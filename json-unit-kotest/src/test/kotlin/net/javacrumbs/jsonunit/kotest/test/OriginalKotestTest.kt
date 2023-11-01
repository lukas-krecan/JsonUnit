package net.javacrumbs.jsonunit.kotest.test

import io.kotest.assertions.json.shouldEqualJson
import org.junit.jupiter.api.Test

class OriginalKotestTest {
    @Test
    fun `Should assert JSON original`() {
        """{"test":1}""" shouldEqualJson """{"test":1}"""
    }
}
