package net.javacrumbs.jsonunit.core;

import java.math.BigDecimal;
import org.jspecify.annotations.Nullable;

/**
 * Interface enabling customization of number comparison.
 */
@FunctionalInterface
public interface NumberComparator {
    /**
     * Compares two numbers. Can be customized according to the needs, especially when comparing floating point numbers.
     * Is 7 == 7.0? Is 7.0 == 7.00? Now you can choose.
     *
     * @param expected expected value
     * @param actual actual value
     * @param tolerance tolerance
     * @return true if the numbers should be considered equal
     */
    boolean compare(BigDecimal expected, BigDecimal actual, @Nullable BigDecimal tolerance);
}
