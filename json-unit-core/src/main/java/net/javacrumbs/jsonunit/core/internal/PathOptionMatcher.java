package net.javacrumbs.jsonunit.core.internal;


import net.javacrumbs.jsonunit.core.Option;

import java.util.Collection;
import java.util.stream.Stream;

class PathOptionMatcher {
    private final PathMatcher pathMatcher;
    private final Option option;

    /**
     *  true if this option is added, false if removed
     */
    private final boolean added;

    private PathOptionMatcher(Collection<String> paths, Option option, boolean added, Object actualRoot) {
        this.pathMatcher = PathMatcher.create(paths, actualRoot);
        this.option = option;
        this.added = added;
    }

    static Stream<PathOptionMatcher> createMatchersFromPathOption(PathOption pathOption, Object actualRoot) {
        return pathOption.getOptions().stream()
            .map(option -> new PathOptionMatcher(pathOption.getPaths(), option, pathOption.isIncluded(), actualRoot));
    }

    public boolean matches(String path) {
        return pathMatcher.matches(path);
    }

    public Option getOption() {
        return option;
    }

    boolean isAdded() {
        return added;
    }
}
