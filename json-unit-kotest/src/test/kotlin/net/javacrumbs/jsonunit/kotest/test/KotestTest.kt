package net.javacrumbs.jsonunit.kotest.test

import io.kotest.matchers.comparables.shouldBeEqualComparingTo
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.should
import io.kotest.matchers.shouldNot
import io.kotest.matchers.string.shouldBeLowerCase
import io.kotest.matchers.throwable.shouldHaveMessage
import net.javacrumbs.jsonunit.core.Configuration
import net.javacrumbs.jsonunit.kotest.beJsonNull
import net.javacrumbs.jsonunit.kotest.beJsonNumber
import net.javacrumbs.jsonunit.kotest.beJsonString
import net.javacrumbs.jsonunit.kotest.equalJson
import net.javacrumbs.jsonunit.kotest.inPath
import net.javacrumbs.jsonunit.kotest.shouldBeJsonBoolean
import net.javacrumbs.jsonunit.kotest.shouldBeJsonNumber
import net.javacrumbs.jsonunit.kotest.shouldBeJsonString
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.math.BigDecimal.valueOf

class KotestTest {
    @Test
    fun `Should assert JSON`() {
        assertThrows<AssertionError> {
            """{"test":1}""" should equalJson("""{"test":2}""")
        }.shouldHaveMessage("""JSON documents are different:
Different value found in node "test", expected: <2> but was: <1>.""")
    }

    @Test
    fun `Should assert JSON negate`() {
        assertThrows<AssertionError> {
            """{"test":1}""" shouldNot equalJson("""{"test":1}""")
        }.shouldHaveMessage("""Expected values to not match""")
    }

    @Test
    fun `Should assert JSON configuration`() {
        """{"test":1.01}""" should equalJson("""{"test":1}""", Configuration.empty().withTolerance(0.1))
    }

    @Test
    fun `Should assert path`() {
        assertThrows<AssertionError> {
            """{"test":1}""" inPath ("test") should equalJson("""2""")
        }.shouldHaveMessage("""JSON documents are different:
Different value found in node "test", expected: <2> but was: <1>.""")
    }

    @Test
    fun `Should assert nested`() {
        assertThrows<AssertionError> {
            """{"test": {"nested": 1}}""" inPath "test" inPath "nested" should equalJson("""2""")
        }.shouldHaveMessage("""JSON documents are different:
Different value found in node "nested", expected: <2> but was: <1>.""")
    }

    @Test
    fun `Should assert JSON path`() {
        assertThrows<AssertionError> {
            """{"test":1}""" inPath ("$.test") should equalJson("""2""")
        }.shouldHaveMessage("""JSON documents are different:
Different value found in node "$.test", expected: <2> but was: <1>.""")
    }

    @Test
    fun `Should assert number`() {
        """{"test":1}""" inPath ("test") should beJsonNumber()
    }

    @Test
    fun `Should assert null`() {
        """{"test":null}""" inPath ("test") should beJsonNull()
    }

    @Test
    fun `Should assert number chain`() {
        """{"test":1}""".inPath("test").shouldBeJsonNumber().shouldBeEqualComparingTo(valueOf(1))
    }

    @Test
    fun `Should assert string chain`() {
        """{"test":"abc"}""".inPath("test").shouldBeJsonString().shouldBeLowerCase()
    }

    @Test
    fun `Should assert boolean chain`() {
        """{"test": true}""".inPath("test").shouldBeJsonBoolean().shouldBeEqual(true)
    }

    @Test
    fun `Should assert number fail`() {
        assertThrows<AssertionError> {
            """{"test": true}""" inPath ("test") should beJsonNumber()
        }.shouldHaveMessage("""Node "test" has invalid type, expected: <number> but was: <true>.""")
    }

    @Test
    fun `Should assert not number fail`() {
        assertThrows<AssertionError> {
            """{"test": 1}""" inPath ("test") shouldNot beJsonNumber()
        }.shouldHaveMessage("""Node "test" has invalid type, expected to not be number but was: <1>.""")
    }

    @Test
    fun `Should assert number fail missing`() {
        assertThrows<AssertionError> {
            """{"test": true}""" inPath ("missing") should beJsonNumber()
        }.shouldHaveMessage("""Node "missing" is missing.""")
    }
}
