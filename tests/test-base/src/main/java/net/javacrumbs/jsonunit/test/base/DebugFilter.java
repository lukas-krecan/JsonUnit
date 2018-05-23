package net.javacrumbs.jsonunit.test.base;

import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.internal.Differences;
import net.javacrumbs.jsonunit.core.internal.Filter;
import net.javacrumbs.jsonunit.core.internal.Node;
import net.javacrumbs.jsonunit.core.internal.Path;

public class DebugFilter implements Filter {

    @Override
    public void process(Node expectedRoot, Node actualRoot, Path startPath, Configuration configuration, Differences differences) {
        //do nothing
    }
}
