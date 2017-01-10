/**
 * Copyright 2009-2017 the original author or authors.
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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static java.math.BigDecimal.valueOf;
import static net.javacrumbs.jsonunit.spring.JsonUnitResultMatchers.json;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SpringConfig.class})
@WebAppConfiguration
public class ExampleControllerTest {

    public static final String CORRECT_JSON = "{\"result\":{\"string\":\"stringValue\", \"array\":[1, 2, 3],\"decimal\":1.00001}}";
    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Test
    public void shouldPassIfEquals() throws Exception {
        exec().andExpect(json().isEqualTo(CORRECT_JSON));
    }

    @Test
    public void isEqualToShouldFailIfDoesNotEqual() throws Exception {
        try {
            exec().andExpect(json().isEqualTo(CORRECT_JSON.replace("stringValue", "stringValue2")));
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals(
                "JSON documents are different:\n" +
                    "Different value found in node \"result.string\". Expected \"stringValue2\", got \"stringValue\".\n",
                e.getMessage());
        }
    }

    @Test
    public void isStringEqualToShouldFailOnNumber() throws Exception {
        try {
            exec().andExpect(json().node("result.array[0]").isStringEqualTo("1"));
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("Node \"result.array[0]\" is not a string. The actual value is '1'.", e.getMessage());
        }
    }

    @Test
    public void isStringEqualToShouldPassIfEquals() throws Exception {
        exec().andExpect(json().node("result.string").isStringEqualTo("stringValue"));
    }

    @Test
    public void isAbsentShouldFailIfNodeExists() throws Exception {
        try {
            exec().andExpect(json().node("result.string").isAbsent());
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("Node \"result.string\" is present.", e.getMessage());
        }
    }


    @Test
    public void isAbsentShouldPassIfNodeIsAbsent() throws Exception {
        exec().andExpect(json().node("result.string2").isAbsent());
    }

    @Test
    public void isPresentShouldFailIfNodeIsAbsent() throws Exception {
        try {
            exec().andExpect(json().node("result.string2").isPresent());
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("Node \"result.string2\" is missing.", e.getMessage());
        }
    }

    @Test
    public void isPresentShouldPassIfPresent() throws Exception {
        exec().andExpect(json().node("result.string").isPresent());
    }

    @Test
    public void isArrayShouldFailOnNotArray() throws Exception {
        try {
            exec().andExpect(json().node("result.string").isArray());
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("Node \"result.string\" is not an array. The actual value is '\"stringValue\"'.", e.getMessage());
        }
    }

    @Test
    public void isArrayShouldFailIfNotPresent() throws Exception {
        try {
            exec().andExpect(json().node("result.array2").isArray());
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("Node \"result.array2\" is missing.", e.getMessage());
        }
    }


    @Test
    public void isObjectShouldFailOnArray() throws Exception {
        try {
            exec().andExpect(json().node("result.array").isObject());
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("Node \"result.array\" is not an object. The actual value is '[1,2,3]'.", e.getMessage());
        }
    }

    @Test
    public void isStringShouldFailOnArray() throws Exception {
        try {
            exec().andExpect(json().node("result.array").isString());
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("Node \"result.array\" is not a string. The actual value is '[1,2,3]'.", e.getMessage());
        }
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
    public void isNotEqualToShouldFailIfEquals() throws Exception {
        try {
            exec().andExpect(json().isNotEqualTo(CORRECT_JSON));
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals(
                "JSON is equal.", e.getMessage());
        }
    }

    @Test
    public void isEqualToShouldFailIfNodeDoesNotEqual() throws Exception {
        try {
            exec()
                .andExpect(json().node("result.string").isEqualTo("stringValue2"));
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("JSON documents are different:\n" +
                    "Different value found in node \"result.string\". Expected \"stringValue2\", got \"stringValue\".\n",
                e.getMessage());
        }
    }

    @Test
    public void intValueShouldMatch() throws Exception {
        exec().andExpect(json().node("result.array").matches(everyItem(lessThanOrEqualTo(valueOf(4)))));
    }

    @Test
    public void intValueShouldFailIfDoesNotMatch() throws Exception {
        try {
            exec().andExpect(json().node("result.array").matches(everyItem(lessThanOrEqualTo(valueOf(2)))));
            failIfNoException();
        } catch (AssertionError e) {
            assertEquals("Node \"result.array\" does not match.\n" +
                    "Expected: every item is a value less than or equal to <2>\n" +
                    "     but: an item <3> was greater than <2>",
                e.getMessage());
        }
    }

    private ResultActions exec() throws Exception {
        return this.mockMvc.perform(get("/sample").accept(MediaType.APPLICATION_JSON));
    }

    private void failIfNoException() {
        fail("Exception expected");
    }

}
