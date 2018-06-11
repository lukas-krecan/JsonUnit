package net.javacrumbs.jsonunit.core.internal;

import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.listener.DifferenceContext;

public class DifferenceContextImpl implements DifferenceContext {

    private final Configuration configuration;
    private final Object actualSource;
    private final Object expectedSource;

    private DifferenceContextImpl(Configuration configuration, Object actualSource, Object expectedSource) {
        this.configuration = configuration;
        this.actualSource = actualSource;
        this.expectedSource = expectedSource;
    }


    static DifferenceContextImpl differenceContext(Configuration configuration, Object actualSource, Object expectedSource) {
        return new DifferenceContextImpl(configuration, actualSource, expectedSource);
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public Object getActualSource() {
        return actualSource;
    }

    @Override
    public Object getExpectedSource() {
        return expectedSource;
    }
}
