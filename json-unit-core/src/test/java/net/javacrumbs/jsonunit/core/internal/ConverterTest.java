package net.javacrumbs.jsonunit.core.internal;

import org.codehaus.jackson.node.BooleanNode;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static net.javacrumbs.jsonunit.core.internal.Converter.isClassPresent;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ConverterTest {

    public static final String JSON = "{\"test\":1}";

    @Test(expected = IllegalStateException.class)
    public void shouldFailIfNoConverterSet() {
        new Converter(Collections.<NodeFactory>emptyList());
    }

    @Test
    public void shouldUseTheLastFactoryForNonPreferred() {
        Converter converter = new Converter(Arrays.<NodeFactory>asList(new Jackson1NodeFactory(), new Jackson2NodeFactory()));
        Node node = converter.convertToNode(JSON, "");
        assertEquals(Jackson2NodeFactory.Jackson2Node.class, node.getClass());
    }

    @Test
    public void shouldUsePreferredFactory() {
        Converter converter = new Converter(Arrays.<NodeFactory>asList(new Jackson1NodeFactory(), new Jackson2NodeFactory()));
        Node node = converter.convertToNode(BooleanNode.TRUE, "");
        assertEquals(Jackson1NodeFactory.Jackson1Node.class, node.getClass());
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