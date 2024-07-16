package net.javacrumbs.jsonunit.core.internal;

import static java.math.BigDecimal.valueOf;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import org.junit.jupiter.api.Test;

class NodeTest {

    @Test
    @SuppressWarnings("unchecked")
    void shouldKeepOrder() {
        Node node = new Jackson2NodeFactory().convertToNode("{\"b\":1, \"a\": 2, \"c\": 3, \"d\": 4}", "test", false);
        assertThat(((Map<String, Object>) node.getValue()).entrySet().iterator().next())
                .isEqualTo(new SimpleEntry<>("b", valueOf(1)));
    }
}
