package net.javacrumbs.jsonunit.spring

import net.javacrumbs.jsonunit.assertj.JsonAssert
import net.javacrumbs.jsonunit.assertj.JsonAssertions
import org.springframework.test.web.servlet.MockMvcResultMatchersDsl
import java.nio.charset.StandardCharsets

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

