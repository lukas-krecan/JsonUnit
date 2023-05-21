/**
 * Copyright 2009-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.javacrumbs.jsonunit.spring.testit;

import net.javacrumbs.jsonunit.core.Option;
import net.javacrumbs.jsonunit.core.listener.Difference;
import net.javacrumbs.jsonunit.core.listener.DifferenceContext;
import net.javacrumbs.jsonunit.core.listener.DifferenceListener;
import net.javacrumbs.jsonunit.spring.testit.demo.SpringConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import org.springframework.web.context.WebApplicationContext;

import static java.math.BigDecimal.valueOf;
import static net.javacrumbs.jsonunit.spring.WebTestClientJsonMatcher.json;
import static net.javacrumbs.jsonunit.spring.testit.demo.ExampleController.CORRECT_JSON;
import static net.javacrumbs.jsonunit.spring.testit.demo.ExampleController.ISO_VALUE;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SpringConfig.class})
@WebAppConfiguration
class WebTestClientTest {

    @Autowired
    private WebApplicationContext wac;

    private WebTestClient client;

    @BeforeEach
    void setup() {
        client = MockMvcWebTestClient.bindToApplicationContext(wac).build();
    }

    @Test
    void shouldPassIfEqualsWithProduces() {
        exec("/sampleProduces").consumeWith(json().isEqualTo(CORRECT_JSON));
    }

    @Test
    void shouldPassIfEqualsWithIsoEncoding() {
        exec("/sampleIso").consumeWith(json().node("result").isEqualTo(ISO_VALUE));
    }

    @Test
    void shouldPassIfEquals() {
        exec().consumeWith(json().isEqualTo(CORRECT_JSON));
    }

    @Test
    void isEqualToShouldFailIfDoesNotEqual() {
        DifferenceListener listener = mock(DifferenceListener.class);
        assertThatThrownBy(() -> exec().consumeWith(json().withDifferenceListener(listener).isEqualTo(CORRECT_JSON.replace("stringValue", "stringValue2"))))
            .hasMessageStartingWith("""
                JSON documents are different:
                Different value found in node "result.string", expected: <"stringValue2"> but was: <"stringValue">.
                """);

        verify(listener).diff(any(Difference.class), any(DifferenceContext.class));
    }

    @Test
    void isEqualToInNodeFailIfDoesNotEqual() {
        assertThatThrownBy(() -> exec().consumeWith(json().node("result.string").isEqualTo("stringValue2")))
            .hasMessageStartingWith("""
                JSON documents are different:
                Different value found in node "result.string", expected: <"stringValue2"> but was: <"stringValue">.
                """);

    }

    @Test
    void isNullShouldPassOnNull() {
        exec().consumeWith(json().node("result.null").isNull());
    }

    @Test
    void isNullShouldFailOnNonNull() {
        assertThatThrownBy(() -> exec().consumeWith(json().node("result.string").isNull()))
            .hasMessageStartingWith("Node \"result.string\" has invalid type, expected: <a null> but was: <\"stringValue\">.");
    }

    @Test
    void isNullShouldFailOnMissing() {
        assertThatThrownBy(() -> exec().consumeWith(json().node("result.missing").isNull()))
            .hasMessageStartingWith("Different value found in node \"result.missing\", expected: <node to be present> but was: <missing>.");
    }

    @Test
    void isNotNullShouldPassOnString() {
        exec().consumeWith(json().node("result.string").isNotNull());
    }

    @Test
    void isNotNullShouldFailOnNull() {
        assertThatThrownBy(() -> exec().consumeWith(json().node("result.null").isNotNull()))
            .hasMessageStartingWith("Node \"result.null\" has invalid type, expected: <not null> but was: <null>.");
    }

    @Test
    void isStringEqualToShouldFailOnNumber() {
        assertThatThrownBy(() -> exec().consumeWith(json().node("result.array[0]").isStringEqualTo("1")))
            .hasMessageStartingWith("Node \"result.array[0]\" has invalid type, expected: <string> but was: <1>.");
    }

    @Test
    void isTrueShouldPassOnTrue() {
        exec().consumeWith(json().node("result.boolean").isTrue());
    }

    @Test
    void isFalseShouldFailOnTrue() {
        assertThatThrownBy(() -> exec().consumeWith(json().node("result.boolean").isFalse()))
            .hasMessageStartingWith("""
                JSON documents are different:
                Different value found in node "result.boolean", expected: <false> but was: <true>.
                """);
    }

    @Test
    void isTrueShouldFailOnString() {
        assertThatThrownBy(() -> exec().consumeWith(json().node("result.string").isTrue()))
            .hasMessageStartingWith("""
                JSON documents are different:
                Different value found in node "result.string", expected: <true> but was: <"stringValue">.
                """);
    }

    @Test
    void isStringEqualToShouldPassIfEquals() {
        exec().consumeWith(json().node("result.string").isStringEqualTo("stringValue"));
    }

    @Test
    void isAbsentShouldFailIfNodeExists() {
        assertThatThrownBy(() -> exec().consumeWith(json().node("result.string").isAbsent()))
            .hasMessageStartingWith("Different value found in node \"result.string\", expected: <node to be absent> but was: <\"stringValue\">.");
    }


    @Test
    void isAbsentShouldPassIfNodeIsAbsent() {
        exec().consumeWith(json().node("result.string2").isAbsent());
    }

    @Test
    void isPresentShouldFailIfNodeIsAbsent() {
        assertThatThrownBy(() -> exec().consumeWith(json().node("result.string2").isPresent()))
            .hasMessageStartingWith("Different value found in node \"result.string2\", expected: <node to be present> but was: <missing>.");
    }

    @Test
    void isPresentShouldPassIfPresent() {
        exec().consumeWith(json().node("result.string").isPresent());
    }

    @Test
    void isArrayShouldFailOnNotArray() {
        assertThatThrownBy(() -> exec().consumeWith(json().node("result.string").isArray()))
            .hasMessageStartingWith("Node \"result.string\" has invalid type, expected: <array> but was: <\"stringValue\">.");
    }

    @Test
    void isArrayShouldFailIfNotPresent() {
        assertThatThrownBy(() -> exec().consumeWith(json().node("result.array2").isArray()))
            .hasMessageStartingWith("Different value found in node \"result.array2\", expected: <array> but was: <missing>.");
    }


    @Test
    void isObjectShouldFailOnArray() {
        assertThatThrownBy(() -> exec().consumeWith(json().node("result.array").isObject()))
            .hasMessageStartingWith("Node \"result.array\" has invalid type, expected: <object> but was: <[1, 2, 3]>.");
    }

    @Test
    void isStringShouldFailOnArray() {
        assertThatThrownBy(() -> exec().consumeWith(json().node("result.array").isString()))
            .hasMessageStartingWith("Node \"result.array\" has invalid type, expected: <string> but was: <[1, 2, 3]>.");
    }

    @Test
    void isArrayShouldPassOnArray() {
        exec().consumeWith(json().node("result.array").isArray());
    }

    @Test
    void ignoreShouldWork() {
        exec().consumeWith(json().isEqualTo("{\"result\":\"${json-unit.ignore}\"}"));
    }

    @Test
    void ignoreStringShouldBeModifiable() {
        exec().consumeWith(json().ignoring("##IGNORE##").isEqualTo("{\"result\":\"##IGNORE##\"}"));
    }

    @Test
    void shouldSetTolerance() {
        exec().consumeWith(json().node("result.decimal").withTolerance(0.001).isEqualTo(1));
    }

    @Test
    void settingOptionShouldTakeEffect() {
        exec().consumeWith(json().node("result.array").when(Option.IGNORING_ARRAY_ORDER).isEqualTo(new int[]{3, 2, 1}));
    }

    @Test
    void isNotEqualToShouldFailIfEquals() {
        assertThatThrownBy(() -> exec().consumeWith(json().isNotEqualTo(CORRECT_JSON)))
            .hasMessageStartingWith("JSON is equal.");
    }

    @Test
    void isEqualToShouldFailIfNodeDoesNotEqual() {
        assertThatThrownBy(() -> exec()
            .consumeWith(json().node("result.string").isEqualTo("stringValue2")))
            .hasMessageStartingWith("""
                JSON documents are different:
                Different value found in node "result.string", expected: <"stringValue2"> but was: <"stringValue">.
                """);
    }

    @Test
    void intValueShouldMatch() {
        exec().consumeWith(json().node("result.array").matches(everyItem(lessThanOrEqualTo(valueOf(4)))));
    }

    @Test
    void intValueShouldFailIfDoesNotMatch() {
        assertThatThrownBy(() -> exec().consumeWith(json().node("result.array").matches(everyItem(lessThanOrEqualTo(valueOf(2))))))
            .hasMessageStartingWith("""
                Node "result.array" does not match.
                Expected: every item is a value less than or equal to <2>
                     but: an item <3> was greater than <2>""");
    }

    private WebTestClient.BodyContentSpec exec() {
        return exec("/sample");
    }

    private WebTestClient.BodyContentSpec exec(String path) {
        try {
            return this.client.get().uri(path).accept(MediaType.APPLICATION_JSON).exchange().expectBody();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
