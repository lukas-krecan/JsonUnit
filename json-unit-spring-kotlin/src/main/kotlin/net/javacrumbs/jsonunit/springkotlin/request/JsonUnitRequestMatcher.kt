package net.javacrumbs.jsonunit.springkotlin.request

import net.javacrumbs.jsonunit.assertj.JsonAssert
import net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson
import org.springframework.http.client.ClientHttpRequest
import org.springframework.mock.http.client.MockClientHttpRequest


val ClientHttpRequest.jsonContent: JsonAssert.ConfigurableJsonAssert
    get() = assertThatJson((this as MockClientHttpRequest).bodyAsString)
