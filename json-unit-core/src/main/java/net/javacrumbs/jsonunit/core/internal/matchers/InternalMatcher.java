/**
 * Copyright 2009-2019 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.javacrumbs.jsonunit.core.internal.matchers;

import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.Option;
import net.javacrumbs.jsonunit.core.internal.Diff;
import net.javacrumbs.jsonunit.core.internal.Path;
import net.javacrumbs.jsonunit.core.listener.DifferenceListener;
import org.hamcrest.Matcher;

import java.math.BigDecimal;

import static net.javacrumbs.jsonunit.core.Option.COMPARING_ONLY_STRUCTURE;


/**
 * Internal class, please do not use outside the library
 */
public final class InternalMatcher extends SimpleInternalMatcher {

    public InternalMatcher(Object actual, Path path, String description, Configuration configuration) {
        super(actual, path, description, configuration);
    }

    private InternalMatcher(Object actual, String pathPrefix) {
        this(actual, Path.create("", pathPrefix), "", Configuration.empty());
    }

    public InternalMatcher whenIgnoringPaths(String... pathsToBeIgnored) {
        return new InternalMatcher(actual, path, description, getConfiguration().whenIgnoringPaths(pathsToBeIgnored));
    }

    /**
     * Sets the description of this object.
     */
    public InternalMatcher describedAs(String description) {
        return new InternalMatcher(actual, path, description, getConfiguration());
    }

    /**
     * Sets the placeholder that can be used to ignore values.
     * The default value is ${json-unit.ignore}
     */
    public InternalMatcher withIgnorePlaceholder(String ignorePlaceholder) {
        return new InternalMatcher(actual, path, description, getConfiguration().withIgnorePlaceholder(ignorePlaceholder));
    }

    /**
     * Sets the tolerance for floating number comparison. If set to null, requires exact match of the values.
     * For example, if set to 0.01, ignores all differences lower than 0.01, so 1 and 0.9999 are considered equal.
     */
    public InternalMatcher withTolerance(double tolerance) {
        return withTolerance(BigDecimal.valueOf(tolerance));
    }

    /**
     * Sets the tolerance for floating number comparison. If set to null, requires exact match of the values.
     * For example, if set to 0.01, ignores all differences lower than 0.01, so 1 and 0.9999 are considered equal.
     */
    public InternalMatcher withTolerance(BigDecimal tolerance) {
        return new InternalMatcher(actual, path, description, getConfiguration().withTolerance(tolerance));
    }


    /**
     * Adds a internalMatcher to be used in ${json-unit.matches:matcherName} macro.
     */
    public InternalMatcher withMatcher(String matcherName, Matcher<?> matcher) {
        return new InternalMatcher(actual, path, description, getConfiguration().withMatcher(matcherName, matcher));
    }

    public InternalMatcher withDifferenceListener(DifferenceListener differenceListener) {
        return new InternalMatcher(actual, path, description, getConfiguration().withDifferenceListener(differenceListener));
    }

    /**
     * Sets options changing comparison behavior. This method has to be called
     * <b>before</b> assertion.
     * For more info see {@link net.javacrumbs.jsonunit.core.Option}
     *
     * @param firstOption
     * @param otherOptions
     * @see net.javacrumbs.jsonunit.core.Option
     */
    public InternalMatcher withOptions(Option firstOption, Option... otherOptions) {
        return new InternalMatcher(actual, path, description, getConfiguration().withOptions(firstOption, otherOptions));
    }

    /**
     * Compares JSON structure. Ignores values, only compares shape of the document and key names.
     * Is too lenient, ignores types, prefer IGNORING_VALUES option instead.
     *
     * @param expected
     * @return {@code this} object.
     */
    public void hasSameStructureAs(Object expected) {
        Diff diff = createDiff(expected, getConfiguration().withOptions(COMPARING_ONLY_STRUCTURE));
        diff.failIfDifferent();
    }

    @Override
    public InternalMatcher node(String newPath) {
        return new InternalMatcher(actual, path.copy(newPath), description, getConfiguration());
    }

    @Override
    public Configuration getConfiguration() {
        return (Configuration) super.getConfiguration();
    }
}
