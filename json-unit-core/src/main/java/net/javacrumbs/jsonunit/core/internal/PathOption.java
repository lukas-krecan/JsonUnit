package net.javacrumbs.jsonunit.core.internal;


import net.javacrumbs.jsonunit.core.Option;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class PathOption {
    private final List<String> paths;
    private final Set<Option> options;

    /**
     * True if this option is included, false if excluded.
     */
    private final boolean included;

    public PathOption(List<String> paths, Set<Option> options, boolean included) {
        this.paths = Collections.unmodifiableList(paths);
        this.options = Collections.unmodifiableSet(EnumSet.copyOf(options));
        this.included = included;
    }

    public List<String> getPaths() {
        return paths;
    }

    public Set<Option> getOptions() {
        return options;
    }

    boolean isIncluded() {
        return included;
    }

    public PathOption withPaths(List<String> newPoPaths) {
        return new PathOption(newPoPaths, options, included);
    }
}
