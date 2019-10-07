package net.javacrumbs.jsonunit.core.internal;


import net.javacrumbs.jsonunit.core.Option;

import java.util.*;

public class PathOption {
    private final List<String> paths;
    private final Set<Option> options;

    /**
     * True if this option is included, false if excluded.
     */
    private final boolean included;

    public PathOption(List<String> paths, EnumSet<Option> options, boolean included) {
        this.paths = Collections.unmodifiableList(paths);
        this.options = Collections.unmodifiableSet(EnumSet.copyOf(options));
        this.included = included;
    }

    List<String> getPaths() {
        return paths;
    }

    public Set<Option> getOptions() {
        return options;
    }

    boolean isIncluded() {
        return included;
    }
}
