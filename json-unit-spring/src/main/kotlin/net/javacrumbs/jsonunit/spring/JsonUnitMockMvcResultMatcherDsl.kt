package net.javacrumbs.jsonunit.spring

import java.nio.charset.StandardCharsets
import net.javacrumbs.jsonunit.assertj.JsonAssert
import net.javacrumbs.jsonunit.assertj.JsonAssertions
import org.springframework.test.web.servlet.MockMvcResultMatchersDsl

/**
 * Example usage:
 * ```
 * get("/uri").andExpect {
 *   jsonContent {
 *     isEqualTo(CORRECT_JSON)
 *   }
 * }
 * ```
 */
fun MockMvcResultMatchersDsl.jsonContent(matcher: JsonAssert.ConfigurableJsonAssert.() -> Unit) {
    match { result ->
        matcher(JsonAssertions.assertThatJson(result.response.getContentAsString(StandardCharsets.UTF_8)))
    }
}
