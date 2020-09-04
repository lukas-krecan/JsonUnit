package net.javacrumbs.jsonunit.test.junit4;

import org.assertj.core.api.Assumptions;
import org.junit.Test;

public class AssumptionsWithAssertJTest {
    @Test
    public void shouldBeIgnoredButFails() {
        Assumptions.assumeThat("Test").isEqualTo("NotEqualToTest");
    }
}
