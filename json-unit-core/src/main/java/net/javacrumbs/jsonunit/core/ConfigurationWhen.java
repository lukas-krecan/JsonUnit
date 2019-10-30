package net.javacrumbs.jsonunit.core;

import net.javacrumbs.jsonunit.core.internal.PathOption;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

/**
 * Everything for {@link Configuration#when(PathsParam, ApplicableForPath...)}
 */
public class ConfigurationWhen {
    private ConfigurationWhen() {}

    /**
     * Adds path conditions. Examples:
     *
     * <ul>
     *   <li>Ignore order for [*].a: <code>when(path("[*].a"), then(IGNORING_ARRAY_ORDER))`</code></li>
     *   <li>Fully ignore multiple paths: <code>when(paths("[*].b", "[*].c"), thenIgnore())`</code></li>
     *   <li>Ignore array order for every path except [*].b: <code>when(IGNORING_ARRAY_ORDER).when(path("[*].b"), thenNot(IGNORING_ARRAY_ORDER))</code></li>
     * </ul>
     *
     * @see #then
     * @see #thenNot
     * @see #thenIgnore
     */
    public static PathsParam path(String path) {
        return new PathsParam(path);
    }

    public static PathsParam paths(String... paths) {
        return new PathsParam(paths);
    }

    public static PathsParam rootPath() {
        return path("");
    }

    /**
     * Applies specified options to the object. Use {@link #thenNot} to explicitly remove options from the path.
     * <br/>
     * When passing a path, options {@link Option#TREATING_NULL_AS_ABSENT} and {@link Option#IGNORING_VALUES} expect
     * paths to the field which supposed to be treated as absent or value-ignored.
     * Example: <code>when(path("a.b"), then(TREATING_NULL_AS_ABSENT))</code> for JSON of <code>{"a": 1, "b": null}</code>.
     * <br/>
     * For any other option specify the path to an object with ignorable child fields, or path to an array:
     * <code>when(path("a.array"), then(IGNORING_ARRAY_ORDER))</code> for <code>{"a": {"array": [1, 2, 3]}}</code>
     *
     */
    public static ApplicableForPath then(Option first, Option... next) {
        return new OptionsParam(true, first, next);
    }

    /**
     * Explicitly remove options from the objects.
     * @see #then
     */
    public static ApplicableForPath thenNot(Option first, Option... next) {
        return new OptionsParam(false, first, next);
    }

    /**
     * Marks that the object should be ignored.
     */
    public static ApplicableForPath thenIgnore() {
        return new IgnoredParam();
    }

    public static class PathsParam {
        private final List<String> paths;

        private PathsParam(String path) {
            this.paths = Collections.singletonList(path);
        }

        private PathsParam(String... paths) {
            this.paths = Arrays.asList(paths);
        }

        List<String> getPaths() {
            return paths;
        }

        Configuration apply(Configuration configuration, ApplicableForPath action) {
            return action.applyForPaths(configuration, this);
        }
    }

    public interface ApplicableForPath {
        Configuration applyForPaths(Configuration configuration, PathsParam pathsParam);
    }

    static class OptionsParam implements ApplicableForPath {
        private final EnumSet<Option> options;
        private final boolean included;

        private OptionsParam(boolean included, Option first, Option... next) {
            this.options = EnumSet.of(first, next);
            this.included = included;
        }

        @Override
        public Configuration applyForPaths(Configuration configuration, PathsParam pathsParam) {
            return configuration.addPathOption(new PathOption(pathsParam.getPaths(), options, included));
        }
    }

    static class IgnoredParam implements ApplicableForPath {
        private IgnoredParam() {
        }

        @Override
        public Configuration applyForPaths(Configuration configuration, PathsParam pathsParam) {
            return configuration.whenIgnoringPaths(pathsParam.paths);
        }
    }
}