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
package net.javacrumbs.jsonunit.core.internal;

import org.codehaus.jackson.node.BooleanNode;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static net.javacrumbs.jsonunit.core.internal.ClassUtils.isClassPresent;
import static net.javacrumbs.jsonunit.core.internal.Converter.LIBRARIES_PROPERTY_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ConverterTest {

    private static final String JSON = "{\"test\":1}";

    @Test(expected = IllegalStateException.class)
    public void shouldFailIfNoConverterSet() {
        new Converter(Collections.<NodeFactory>emptyList());
    }

    @Test
    public void shouldUseTheLastFactoryForNonPreferred() {
        Converter converter = new Converter(Arrays.<NodeFactory>asList(new Jackson1NodeFactory(), new Jackson2NodeFactory()));
        Node node = converter.convertToNode(JSON, "", false);
        assertEquals(Jackson2NodeFactory.Jackson2Node.class, node.getClass());
    }

    @Test
    public void shouldUsePreferredFactory() {
        Converter converter = new Converter(Arrays.<NodeFactory>asList(new Jackson1NodeFactory(), new Jackson2NodeFactory()));
        Node node = converter.convertToNode(BooleanNode.TRUE, "", false);
        assertEquals(Jackson1NodeFactory.Jackson1Node.class, node.getClass());
    }


    @Test
    public void shouldUseOnlyFactorySpecifiedBySystemProperty() {
        System.setProperty(LIBRARIES_PROPERTY_NAME,"gson");
        Converter converter = Converter.createDefaultConverter();
        assertThat(converter.getFactories()).extracting("class").containsExactly(GsonNodeFactory.class);
        System.setProperty(LIBRARIES_PROPERTY_NAME, "");
    }

    @Test
    public void shouldChangeOrderSpecifiedBySystemProperty() {
        System.setProperty(LIBRARIES_PROPERTY_NAME,"jackson2, gson ,json.org,\tjackson1");
        Converter converter = Converter.createDefaultConverter();
        assertThat(converter.getFactories()).extracting("class").containsExactly(Jackson2NodeFactory.class, GsonNodeFactory.class, JsonOrgNodeFactory.class, Jackson1NodeFactory.class);
        System.setProperty(LIBRARIES_PROPERTY_NAME, "");
    }

    @Test
    public void shouldFailOnUnknownFactory() {
        System.setProperty(LIBRARIES_PROPERTY_NAME,"unknown");
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
    public void classShouldBePresent() {
        assertTrue(isClassPresent("java.lang.String"));
    }

    @Test
    public void classShouldNotBePresent() {
        assertFalse(isClassPresent("garbage"));
    }
}