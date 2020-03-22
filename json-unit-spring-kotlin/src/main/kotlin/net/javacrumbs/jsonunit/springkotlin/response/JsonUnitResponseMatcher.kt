package net.javacrumbs.jsonunit.springkotlin.response

import net.javacrumbs.jsonunit.assertj.JsonAssert
import net.javacrumbs.jsonunit.assertj.JsonAssertions
import org.springframework.test.web.servlet.MockMvcResultMatchersDsl
import org.springframework.test.web.servlet.ResultMatcher
import java.nio.charset.StandardCharsets

object JsonUnitResponseMatcher {
    fun MockMvcResultMatchersDsl.jsonContent(matcher: JsonAssert.ConfigurableJsonAssert.() -> Unit) {
        return match(ResultMatcher { result ->
            matcher(JsonAssertions.assertThatJson(result.response.getContentAsString(StandardCharsets.UTF_8)))
        })
    }
}
