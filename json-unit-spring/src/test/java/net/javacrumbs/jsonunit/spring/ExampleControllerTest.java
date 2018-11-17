/**
 * Copyright 2009-2018 the original author or authors.
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
package net.javacrumbs.jsonunit.spring;

import net.javacrumbs.jsonunit.core.Option;
import net.javacrumbs.jsonunit.core.listener.Difference;
import net.javacrumbs.jsonunit.core.listener.DifferenceContext;
import net.javacrumbs.jsonunit.core.listener.DifferenceListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static java.math.BigDecimal.valueOf;
import static net.javacrumbs.jsonunit.spring.JsonUnitResultMatchers.json;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SpringConfig.class})
@WebAppConfiguration
public class ExampleControllerTest {

    public static final String CORRECT_JSON = "{\"result\":{\"string\":\"stringValue\", \"array\":[1, 2, 3],\"decimal\":1.00001, \"boolean\": true, \"null\" : null}}";
    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Test
    public void shouldPassIfEquals() throws Exception {
        exec().andExpect(json().isEqualTo(CORRECT_JSON));
    }

    @Test
    public void isEqualToShouldFailIfDoesNotEqual() {
        DifferenceListener listener = mock(DifferenceListener.class);
        assertThatThrownBy(() -> exec().andExpect(json().withDifferenceListener(listener).isEqualTo(CORRECT_JSON.replace("stringValue", "stringValue2"))))
            .hasMessage("JSON documents are different:\n" +
                "Different value found in node \"result.string\", expected: <\"stringValue2\"> but was: <\"stringValue\">.\n");

        verify(listener).diff(any(Difference.class), any(DifferenceContext.class));
    }

    @Test
    public void isEqualToInNodeFailIfDoesNotEqual() {
        assertThatThrownBy(() -> exec().andExpect(json().node("result.string").isEqualTo("stringValue2")))
            .hasMessage("JSON documents are different:\n" +
                "Different value found in node \"result.string\", expected: <\"stringValue2\"> but was: <\"stringValue\">.\n");

    }

    @Test
    public void isNullShouldPassOnNull() throws Exception {
        exec().andExpect(json().node("result.null").isNull());
    }

    @Test
    public void isNullShouldFailOnNonNull() {
        assertThatThrownBy(() -> exec().andExpect(json().node("result.string").isNull()))
            .hasMessage("Node \"result.string\" is different. Expected: <a null> but was: <\"stringValue\">.");
    }

    @Test
    public void isNullShouldFailOnMissing() {
        assertThatThrownBy(() -> exec().andExpect(json().node("result.missing").isNull()))
            .hasMessage("Node \"result.missing\" is missing.");
    }

    @Test
    public void isNotNullShouldPassOnString() throws Exception {
        exec().andExpect(json().node("result.string").isNotNull());
    }

    @Test
    public void isNotNullShouldFailOnNull() {
        assertThatThrownBy(() -> exec().andExpect(json().node("result.null").isNotNull()))
            .hasMessage("Node \"result.null\" is different. Expected: <not null> but was: <null>.");
    }

    @Test
    public void isStringEqualToShouldFailOnNumber() {
        assertThatThrownBy(() -> exec().andExpect(json().node("result.array[0]").isStringEqualTo("1")))
            .hasMessage("Node \"result.array[0]\" is different. Expected: <a string> but was: <1>.");
    }

    @Test
    public void isTrueShouldPassOnTrue() throws Exception {
        exec().andExpect(json().node("result.boolean").isTrue());
    }

    @Test
    public void isFalseShouldFailOnTrue() {
        assertThatThrownBy(() -> exec().andExpect(json().node("result.boolean").isFalse()))
            .hasMessage("JSON documents are different:\n" +
                "Different value found in node \"result.boolean\", expected: <false> but was: <true>.\n");
    }

    @Test
    public void isTrueShouldFailOnString() {
        assertThatThrownBy(() -> exec().andExpect(json().node("result.string").isTrue()))
            .hasMessage("JSON documents are different:\n" +
                "Different value found in node \"result.string\", expected: <true> but was: <\"stringValue\">.\n");
    }

    @Test
    public void isStringEqualToShouldPassIfEquals() throws Exception {
        exec().andExpect(json().node("result.string").isStringEqualTo("stringValue"));
    }

    @Test
    public void isAbsentShouldFailIfNodeExists() {
        assertThatThrownBy(() -> exec().andExpect(json().node("result.string").isAbsent()))
            .hasMessage("Node \"result.string\" is present.");
    }


    @Test
    public void isAbsentShouldPassIfNodeIsAbsent() throws Exception {
        exec().andExpect(json().node("result.string2").isAbsent());
    }

    @Test
    public void isPresentShouldFailIfNodeIsAbsent() {
        assertThatThrownBy(() -> exec().andExpect(json().node("result.string2").isPresent()))
            .hasMessage("Node \"result.string2\" is missing.");
    }

    @Test
    public void isPresentShouldPassIfPresent() throws Exception {
        exec().andExpect(json().node("result.string").isPresent());
    }

    @Test
    public void isArrayShouldFailOnNotArray() {
        assertThatThrownBy(() -> exec().andExpect(json().node("result.string").isArray()))
            .hasMessage("Node \"result.string\" is different. Expected: <an array> but was: <\"stringValue\">.");
    }

    @Test
    public void isArrayShouldFailIfNotPresent() {
        assertThatThrownBy(() -> exec().andExpect(json().node("result.array2").isArray()))
            .hasMessage("Node \"result.array2\" is missing.");
    }


    @Test
    public void isObjectShouldFailOnArray() {
        assertThatThrownBy(() -> exec().andExpect(json().node("result.array").isObject()))
            .hasMessage("Node \"result.array\" is different. Expected: <an object> but was: <[1,2,3]>.");
    }

    @Test
    public void isStringShouldFailOnArray() {
        assertThatThrownBy(() -> exec().andExpect(json().node("result.array").isString()))
            .hasMessage("Node \"result.array\" is different. Expected: <a string> but was: <[1,2,3]>.");
    }

    @Test
    public void isArrayShouldPassOnArray() throws Exception {
        exec().andExpect(json().node("result.array").isArray());
    }

    @Test
    public void ignoreShouldWork() throws Exception {
        exec().andExpect(json().isEqualTo("{\"result\":\"${json-unit.ignore}\"}"));
    }

    @Test
    public void ignoreStringShouldBeModifiable() throws Exception {
        exec().andExpect(json().ignoring("##IGNORE##").isEqualTo("{\"result\":\"##IGNORE##\"}"));
    }

    @Test
    public void shouldSetTolerance() throws Exception {
        exec().andExpect(json().node("result.decimal").withTolerance(0.001).isEqualTo(1));
    }

    @Test
    public void settingOptionShouldTakeEffect() throws Exception {
        exec().andExpect(json().node("result.array").when(Option.IGNORING_ARRAY_ORDER).isEqualTo(new int[]{3, 2, 1}));
    }

    @Test
    public void isNotEqualToShouldFailIfEquals() {
        assertThatThrownBy(() -> exec().andExpect(json().isNotEqualTo(CORRECT_JSON)))
            .hasMessage("JSON is equal.");
    }

    @Test
    public void isEqualToShouldFailIfNodeDoesNotEqual() {
        assertThatThrownBy(() -> exec()
            .andExpect(json().node("result.string").isEqualTo("stringValue2")))
            .hasMessage("JSON documents are different:\n" +
                "Different value found in node \"result.string\", expected: <\"stringValue2\"> but was: <\"stringValue\">.\n");
    }

    @Test
    public void intValueShouldMatch() throws Exception {
        exec().andExpect(json().node("result.array").matches(everyItem(lessThanOrEqualTo(valueOf(4)))));
    }

    @Test
    public void intValueShouldFailIfDoesNotMatch() {
        assertThatThrownBy(() -> exec().andExpect(json().node("result.array").matches(everyItem(lessThanOrEqualTo(valueOf(2))))))
            .hasMessage("Node \"result.array\" does not match.\n" +
                "Expected: every item is a value less than or equal to <2>\n" +
                "     but: an item <3> was greater than <2>");
    }

    private ResultActions exec() {
        try {
            return this.mockMvc.perform(get("/sample").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
