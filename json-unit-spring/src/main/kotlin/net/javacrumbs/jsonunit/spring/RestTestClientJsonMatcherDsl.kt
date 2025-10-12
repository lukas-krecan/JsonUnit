package net.javacrumbs.jsonunit.spring

import net.javacrumbs.jsonunit.assertj.JsonAssert
import net.javacrumbs.jsonunit.assertj.JsonAssertions
import net.javacrumbs.jsonunit.spring.RestTestClientUtils.getContentAsString
import org.springframework.test.web.servlet.client.RestTestClient

/**
 * Example usage:
 * ```
 * client.get().uri(path).accept(APPLICATION_JSON)
 * .exchange()
 * .jsonContent { isEqualTo(CORRECT_JSON) }
 * ```
 */
fun RestTestClient.BodyContentSpec.jsonContent(
    matcher: JsonAssert.ConfigurableJsonAssert.() -> Unit
): RestTestClient.BodyContentSpec = consumeWith { result ->
    matcher(JsonAssertions.assertThatJson(getContentAsString(result)))
}
