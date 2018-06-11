package net.javacrumbs.jsonunit.core.listener;

import net.javacrumbs.jsonunit.core.Configuration;

public interface DifferenceContext {

    /**
     * Configuration used for comparison.
     */
    Configuration getConfiguration();

    /**
     * Actual source serialized as Map&lt;String, Object&gt; for objects, BigDecimal for numbers, ...
     */
    Object getActualSource();


    /**
     * Expected source serialized as Map&lt;String, Object&gt; for objects, BigDecimal for numbers, ...
     */
    Object getExpectedSource();
}
