/**
 * Copyright 2009-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package net.javacrumbs.jsonunit.spring.testit

import net.javacrumbs.jsonunit.core.listener.Difference
import net.javacrumbs.jsonunit.core.listener.DifferenceContext
import net.javacrumbs.jsonunit.core.listener.DifferenceListener
import net.javacrumbs.jsonunit.spring.jsonContent
import net.javacrumbs.jsonunit.spring.testit.demo.ExampleController
import net.javacrumbs.jsonunit.spring.testit.demo.ExampleController.CORRECT_JSON
import net.javacrumbs.jsonunit.spring.testit.demo.SpringConfig
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.servlet.client.MockMvcWebTestClient
import org.springframework.web.context.WebApplicationContext

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [SpringConfig::class])
@WebAppConfiguration
internal class KotlinWebTestClientTest {
    @Autowired lateinit var wac: WebApplicationContext

    lateinit var client: WebTestClient

    @BeforeEach
    fun setUp() {
        client = MockMvcWebTestClient.bindToApplicationContext(wac).build()
    }

    @Test
    fun shouldPassIfEqualsWithProduces() {
        exec("/sampleProduces")
            .expectBody()
            .jsonContent { isEqualTo(CORRECT_JSON) }
            .jsonContent { node("result.string").isString().isEqualTo("stringValue") }
    }

    @Test
    fun shouldPassIfEqualsWithIsoEncoding() {
        exec("/sampleIso").expectBody().jsonContent { node("result").isEqualTo(ExampleController.ISO_VALUE) }
    }

    @Test
    fun shouldPassIfEquals() {
        exec().expectBody().jsonContent { isEqualTo(CORRECT_JSON) }
    }

    @Test
    fun isEqualToShouldFailIfDoesNotEqual() {
        val listener = mock(DifferenceListener::class.java)
        assertThatThrownBy {
                exec().expectBody().jsonContent {
                    withDifferenceListener(listener).isEqualTo(CORRECT_JSON.replace("stringValue", "stringValue2"))
                }
            }
            .hasMessageStartingWith(
                """
                JSON documents are different:
                Different value found in node "result.string", expected: <"stringValue2"> but was: <"stringValue">.

                """
                    .trimIndent()
            )
        verify(listener).diff(any(Difference::class.java), any(DifferenceContext::class.java))
    }

    @Test
    fun isEqualToInNodeFailIfDoesNotEqual() {
        assertThatThrownBy {
                exec().expectBody().jsonContent { node("result.string").isString().isEqualTo("stringValue2") }
            }
            .hasMessage(
                "[Different value found in node \"result.string\"] \n" +
                    "expected: \"stringValue2\"\n" +
                    " but was: \"stringValue\""
            )
    }

    @Test
    fun errorOnEmptyResponse() {
        assertThatThrownBy { exec("/empty").expectBody().jsonContent { isObject() } }
            .hasMessageStartingWith("Node \"\" has invalid type, expected: <object> but was: <\"\">.")
    }

    @Test
    fun isNullShouldPassOnNull() {
        exec().expectBody().jsonContent { node("result.null").isNull() }
    }

    @Test
    fun isNullShouldFailOnNonNull() {
        assertThatThrownBy { exec().expectBody().jsonContent { node("result.string").isNull() } }
            .hasMessageStartingWith(
                "Node \"result.string\" has invalid type, expected: <null> but was: <\"stringValue\">."
            )
    }

    private fun exec(path: String = "/sample"): WebTestClient.ResponseSpec {
        try {
            return client.get().uri(path).accept(APPLICATION_JSON).exchange().expectStatus().isOk()
        } catch (e: Exception) {
            throw IllegalStateException(e)
        }
    }
}
