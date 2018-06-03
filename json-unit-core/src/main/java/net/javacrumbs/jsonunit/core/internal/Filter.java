package net.javacrumbs.jsonunit.core.internal;

import net.javacrumbs.jsonunit.core.Configuration;

public interface Filter {

    void process(Node expectedRoot, Node actualRoot, Path startPath, Configuration configuration, Differences differences);
}
