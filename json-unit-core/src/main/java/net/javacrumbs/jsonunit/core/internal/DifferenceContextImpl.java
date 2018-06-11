package net.javacrumbs.jsonunit.core.internal;

import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.listener.DifferenceContext;

class DifferenceContextImpl implements DifferenceContext {

    private final Configuration configuration;
    private final Node actualSource;
    private final Node expectedSource;

    private DifferenceContextImpl(Configuration configuration, Node actualSource, Node expectedSource) {
        this.configuration = configuration;
        this.actualSource = actualSource;
        this.expectedSource = expectedSource;
    }


    static DifferenceContextImpl differenceContext(Configuration configuration, Node actualSource, Node expectedSource) {
        return new DifferenceContextImpl(configuration, actualSource, expectedSource);
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public Object getActualSource() {
        return actualSource.getValue();
    }

    @Override
    public Object getExpectedSource() {
        return expectedSource.getValue();
    }
}
