/**
 * Copyright 2009-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.javacrumbs.jsonunit.core.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

abstract class PathMatcher {
    private static final PathMatcher EMPTY = new PathMatcher() {
        @Override
        boolean matches(String pathToMatch) {
            return false;
        }
    };

    abstract boolean matches(String pathToMatch);

    static PathMatcher create(Set<String> paths) {
        if (paths == null || paths.isEmpty()) {
            return EMPTY;
        }
        List<PathMatcher> matchers = new ArrayList<>(paths.size());
        for (String path : paths) {
            matchers.add(PathMatcher.create(path));
        }
        return new AggregatePathMatcher(matchers);
    }

    static PathMatcher create(String path) {
        if (path.contains("[*]")) {
            return new ArrayWildcardMatcher(path);
        } else {
            return new SimplePathMatcher(path);
        }
    }

    private static class SimplePathMatcher extends PathMatcher {
        private final String path;

        SimplePathMatcher(String path) {
            this.path = path;
        }

        @Override
        boolean matches(String pathToMatch) {
            return path.equals(pathToMatch);
        }
    }

    /**
     * Matches array[*].something to array[1].something
     */
    private static class ArrayWildcardMatcher extends PathMatcher {
        private final Pattern pattern;

        ArrayWildcardMatcher(String path) {
            StringBuilder regexp = new StringBuilder();
            int from = 0;
            int to;
            while ((to = path.indexOf("[*]", from)) >= 0) {
                regexp
                    .append("\\Q")
                    .append(path.substring(from, to))
                    .append("\\E")
                    .append("\\[\\d+\\]");
                from = to + 3; // length of the placeholder [*]
            }
            // End of the pattern if any
            if (from < path.length()) {
                regexp
                    .append("\\Q")
                    .append(path.substring(from, path.length()))
                    .append("\\E");
            }
            pattern = Pattern.compile(regexp.toString());
        }

        @Override
        boolean matches(String pathToMatch) {
            return pattern.matcher(pathToMatch).matches();
        }
    }

    private static class AggregatePathMatcher extends PathMatcher {

        private final Collection<PathMatcher> pathMatchers;

        private AggregatePathMatcher(Collection<PathMatcher> pathMatchers) {
            this.pathMatchers = pathMatchers;
        }

        @Override
        boolean matches(String path) {
            for (PathMatcher matcher : pathMatchers) {
                if (matcher.matches(path)) {
                    return true;
                }
            }
            return false;
        }
    }
}
