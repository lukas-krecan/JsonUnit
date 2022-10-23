package net.javacrumbs.jsonunit.assertj

import net.javacrumbs.jsonunit.assertj.JsonFileAssert.assertThatJsonFile
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.charset.Charset

class JsonFileAssertTest {

    @Test
    fun `assertJsonFile should allow JSON assertions on JSON file when path is provided`() {
        assertThatJsonFile("src/test/resources/test.json")
                .hasContent().node("key").isEqualTo("value")
    }

    @Test
    fun `assertJsonFile should allow JSON assertions on JSON file when file is provided`() {
        assertThatJsonFile(File("src/test/resources/test.json"))
                .hasContent().node("key").isEqualTo("value")
    }

    @Test
    fun `assertJsonFile should allow defining charset for the JSON file`() {
        assertThatJsonFile(File("src/test/resources/test.json"))
                .hasContent(Charset.forName("UTF-8")).node("key").isEqualTo("value")
    }

    @Test
    fun `assertJsonFile should throw an IllegalArgumentException when JSON file is invalid`() {
        assertThatThrownBy {
            assertThatJsonFile("src/test/resources/invalid.json").hasContent()
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `assertJsonFile should throw an AssertionError when JSON file is not found`() {
        assertThatThrownBy {
            assertThatJsonFile("src/test/resources/not_found.json").hasContent()
        }.isInstanceOf(AssertionError::class.java)
    }
}
