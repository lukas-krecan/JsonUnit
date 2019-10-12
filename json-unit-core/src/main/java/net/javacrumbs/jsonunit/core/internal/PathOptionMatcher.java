package net.javacrumbs.jsonunit.core.internal;


import net.javacrumbs.jsonunit.core.Option;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class PathOptionMatcher {
    private final PathMatcher matcher;
    private final Option option;

    /**
     *  true if this option is added, false if removed
     */
    private final boolean added;

    private PathOptionMatcher(String path, Option option, boolean added) {
        this.matcher = PathMatcher.create(path);
        this.option = option;
        this.added = added;
    }

    static Stream<PathOptionMatcher> createMatchersFromPathOption(PathOption pathOption) {
        List<PathOptionMatcher> result = new ArrayList<>();
        for (Option option : pathOption.getOptions()) {
            for (String path : pathOption.getPaths()) {
                result.add(new PathOptionMatcher(path, option, pathOption.isIncluded()));
            }
        }
        return result.stream();
    }

    public boolean matches(String path) {
        return matcher.matches(path);
    }

    public Option getOption() {
        return option;
    }

    boolean isAdded() {
        return added;
    }
}
