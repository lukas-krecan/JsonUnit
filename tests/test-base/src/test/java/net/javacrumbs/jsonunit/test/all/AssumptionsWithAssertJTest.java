package net.javacrumbs.jsonunit.test.all;

import org.assertj.core.api.Assumptions;
import org.junit.jupiter.api.Test;

public class AssumptionsWithAssertJTest {
    @Test
    void shouldBeIgnored() {
        Assumptions.assumeThat("Test").isEqualTo("NotEqualToTest");
    }
}
