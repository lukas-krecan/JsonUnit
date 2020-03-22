/**
 * Copyright 2009-2019 the original author or authors.
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.javacrumbs.jsonunit.springkotlin.test

import net.javacrumbs.jsonunit.springkotlin.request.jsonContent
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod.POST
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestTemplate


internal class ClientTest {
    private val restTemplate = RestTemplate()
    private val mockServer = MockRestServiceServer.createServer(restTemplate)
    private val jsonResponse = "{\"response\": \"€\"}"
    private val json = "{\"test\": 1}"

    private val URI = "/sample"

    @Test
    fun shouldAssertClient() {
        mockServer.expect(requestTo(URI))
                .andExpect { request ->
                    request.jsonContent.isEqualTo(json)
                }
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON_UTF8))
        assertThat(restTemplate.postForEntity(URI, json, String::class.java).body).isEqualTo(jsonResponse)
    }

    @Test
    fun shouldFailOnMismatch() {
        mockServer.expect(requestTo(URI))
                .andExpect {
                    it.jsonContent.isEqualTo("[]")
                }.andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON))
        assertThatThrownBy { restTemplate.postForEntity(URI, json, String::class.java) }
                .hasMessage("""
    JSON documents are different:
    Different value found in node "", expected: <[]> but was: <{"test":1}>.

    """.trimIndent())
    }

    @Test
    fun shouldAssertClientComplex() {
        mockServer.expect(requestTo(URI))
                .andExpect(MockRestRequestMatchers.method(POST))
                .andExpect { it.jsonContent.withTolerance(0.1).node("test").isEqualTo(0.99) }
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON))
        assertThat(restTemplate.postForEntity(URI, json, String::class.java).body).isEqualTo(jsonResponse)
    }

}
