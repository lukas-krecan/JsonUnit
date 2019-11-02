/**
 * Copyright 2009-2019 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.javacrumbs.jsonunit.spring.test;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static net.javacrumbs.jsonunit.spring.JsonUnitRequestMatchers.json;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ClientTest {
    private static final String URI = "/sample";
    private final RestTemplate restTemplate = new RestTemplate();

    private final MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);

    private final String jsonResponse = "{\"response\": \"€\"}";
    private final String json = "{\"test\": 1}";

    @Test
    void shouldAssertClient() {
        mockServer.expect(requestTo(URI))
                          .andExpect(json().isEqualTo(json))
                          .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON_UTF8));

        assertThat(restTemplate.postForEntity(URI, json, String.class).getBody()).isEqualTo(jsonResponse);

    }

    @Test
    void shouldAssertClientComplex() {
        mockServer.expect(requestTo(URI))
                          .andExpect(method(HttpMethod.POST))
                          .andExpect(json().node("test").withTolerance(0.1).isEqualTo(0.99))
                          .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON_UTF8));

        assertThat(restTemplate.postForEntity(URI, json, String.class).getBody()).isEqualTo(jsonResponse);
    }
}
