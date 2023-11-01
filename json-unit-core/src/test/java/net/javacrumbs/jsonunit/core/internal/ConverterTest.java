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
package net.javacrumbs.jsonunit.core.internal;

import static net.javacrumbs.jsonunit.core.internal.ClassUtils.isClassPresent;
import static net.javacrumbs.jsonunit.core.internal.Converter.LIBRARIES_PROPERTY_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.databind.node.BooleanNode;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class ConverterTest {

    private static final String JSON = "{\"test\":1}";

    @Test
    void shouldFailIfNoConverterSet() {
        assertThatThrownBy(() -> new Converter(Collections.emptyList())).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldUseTheLastFactoryForNonPreferred() {
        Converter converter = new Converter(Arrays.asList(new GsonNodeFactory(), new Jackson2NodeFactory()));
        Node node = converter.convertToNode(JSON, "", false);
        assertEquals(Jackson2NodeFactory.Jackson2Node.class, node.getClass());
    }

    @Test
    void shouldUsePreferredFactory() {
        Converter converter = new Converter(Arrays.asList(new Jackson2NodeFactory(), new GsonNodeFactory()));
        Node node = converter.convertToNode(BooleanNode.TRUE, "", false);
        assertEquals(Jackson2NodeFactory.Jackson2Node.class, node.getClass());
    }

    @Test
    void shouldUseOnlyFactorySpecifiedBySystemProperty() {
        System.setProperty(LIBRARIES_PROPERTY_NAME, "gson");
        Converter converter = Converter.createDefaultConverter();
        assertThat(converter.factories()).extracting("class").containsExactly(GsonNodeFactory.class);
        System.setProperty(LIBRARIES_PROPERTY_NAME, "");
    }

    @Test
    void shouldChangeOrderSpecifiedBySystemProperty() {
        System.setProperty(LIBRARIES_PROPERTY_NAME, "jackson2, gson ,json.org");
        Converter converter = Converter.createDefaultConverter();
        assertThat(converter.factories())
                .extracting("class")
                .containsExactly(Jackson2NodeFactory.class, GsonNodeFactory.class, JsonOrgNodeFactory.class);
        System.setProperty(LIBRARIES_PROPERTY_NAME, "");
    }

    @Test
    void shouldFailOnUnknownFactory() {
        System.setProperty(LIBRARIES_PROPERTY_NAME, "unknown");
        try {
            Converter.createDefaultConverter();
            fail("Exception expected");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("'unknown' library name not recognized.");
        } finally {
            System.setProperty(LIBRARIES_PROPERTY_NAME, "");
        }
    }

    @Test
    void classShouldBePresent() {
        assertTrue(isClassPresent("java.lang.String"));
    }

    @Test
    void classShouldNotBePresent() {
        assertFalse(isClassPresent("garbage"));
    }

    @Test
    void testDecimalValueAndIsIntegralNumber() {
        Converter converter = new Converter(Arrays.asList(new Jackson2NodeFactory(), new GsonNodeFactory()));
        Node node = converter.convertToNode(BooleanNode.TRUE, "", false);
        assertTrue(node.isIntegralNumber());
    }
}
