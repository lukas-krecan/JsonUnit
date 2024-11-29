package net.javacrumbs.jsonunit.spring;

import static net.javacrumbs.jsonunit.spring.JsonUnitJsonComparator.comparator;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import net.javacrumbs.jsonunit.core.Configuration;
import org.junit.jupiter.api.Test;
import org.springframework.test.json.JsonComparator;
import org.springframework.test.json.JsonContent;
import org.springframework.test.json.JsonContentAssert;

class JsonUnitJsonComparatorTest {

    @Test
    void shouldWorkWithSpringJsonContentAssert() {
        new JsonContentAssert(json("{\"test\" : 1}")).isEqualTo("{test: 1}", comparator());
    }

    @Test
    void shouldFailWithMessage() {
        assertThatThrownBy(() -> new JsonContentAssert(json("{\"test\" : 1}")).isEqualTo("{test: 2}", comparator()))
                .hasMessageEndingWith(
                        """
            JSON documents are different:
            Different value found in node "test", expected: <2> but was: <1>.
            """);
    }

    @Test
    void shouldApplyConfiguration() {
        JsonComparator comparator = comparator(Configuration.empty().withTolerance(0.01));
        new JsonContentAssert(json("{\"test\" : 1.0001}")).isEqualTo("{test: 1}", comparator);
    }

    private static JsonContent json(String json) {
        return new JsonContent(json);
    }
}
