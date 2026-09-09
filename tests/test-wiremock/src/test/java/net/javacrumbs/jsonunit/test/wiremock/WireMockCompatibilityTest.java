package net.javacrumbs.jsonunit.test.wiremock;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;

class WireMockCompatibilityTest {

    @Test
    void wireMockJsonMatchingWorksWithCurrentJsonUnit() throws Exception {
        WireMockServer wireMockServer =
                new WireMockServer(WireMockConfiguration.options().dynamicPort());

        try {
            wireMockServer.start();

            wireMockServer.stubFor(post(urlEqualTo("/json"))
                    .withRequestBody(equalToJson("{\"id\":1,\"name\":\"test\"}", true, true))
                    .willReturn(ok("matched")));

            HttpResponse<String> response =
                    sendJson(wireMockServer.baseUrl() + "/json", "{\"name\":\"test\",\"id\":1}");

            assertEquals(200, response.statusCode());
            assertEquals("matched", response.body());

            wireMockServer.verify(postRequestedFor(urlEqualTo("/json"))
                    .withRequestBody(equalToJson("{\"id\":1,\"name\":\"test\"}", true, true)));
        } finally {
            wireMockServer.stop();
        }
    }

    private static HttpResponse<String> sendJson(String url, String body) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }
}
