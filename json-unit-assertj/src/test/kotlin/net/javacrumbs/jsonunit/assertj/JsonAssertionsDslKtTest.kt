package net.javacrumbs.jsonunit.assertj

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class JsonAssertionsDslKtTest {
    @Test
    fun `Function should be accessible`() {
        assertThat(assertThatJson("1")).isNotNull()
    }
}
