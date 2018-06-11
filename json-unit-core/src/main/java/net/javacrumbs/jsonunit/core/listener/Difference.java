package net.javacrumbs.jsonunit.core.listener;

/**
 * Describes differences between documents.
 */
public interface Difference {
    enum Type {EXTRA, MISSING, DIFFERENT}

    /**
     * Path to the difference
     */
    String getActualPath();

    /**
     * Path to the expected element (may be different than actual path if IGNORE_ARRAY_ORDER is used)
     */
    String getExpectedPath();

    /**
     * Actual node serialized as Map&lt;String, Object&gt; for objects, BigDecimal for numbers, ...
     */
    Object getActual();


    /**
     * Expected node serialized as Map&lt;String, Object&gt; for objects, BigDecimal for numbers, ...
     */
    Object getExpected();

    /**
     * Type of the difference
     */
    Type getType();
}
