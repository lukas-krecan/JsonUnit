package net.javacrumbs.jsonunit.spring

import net.javacrumbs.jsonunit.assertj.JsonAssert
import net.javacrumbs.jsonunit.assertj.JsonAssertions
import net.javacrumbs.jsonunit.spring.Utils.getContentAsString
import org.springframework.test.web.reactive.server.WebTestClient

/**
 * Example usage:
 * ```
 * client.get().uri(path).accept(APPLICATION_JSON)
 * .exchange()
 * .jsonContent { isEqualTo(CORRECT_JSON) }
 * ```
 */
fun WebTestClient.BodyContentSpec.jsonContent(matcher: JsonAssert.ConfigurableJsonAssert.() -> Unit) =
        consumeWith { result ->
            matcher(JsonAssertions.assertThatJson(getContentAsString(result)))
        }

