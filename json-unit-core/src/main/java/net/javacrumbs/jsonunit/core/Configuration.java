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
package net.javacrumbs.jsonunit.core;

import net.javacrumbs.jsonunit.core.internal.Options;
import net.javacrumbs.jsonunit.core.listener.Difference;
import net.javacrumbs.jsonunit.core.listener.DifferenceContext;
import net.javacrumbs.jsonunit.core.listener.DifferenceListener;
import org.hamcrest.Matcher;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;

/**
 * Comparison configuration. Immutable.
 */
public class Configuration {
    private static final DifferenceListener DUMMY_LISTENER = new DifferenceListener() {
        @Override
        public void diff(Difference difference, DifferenceContext context) {

        }
    };

    private static final Configuration EMPTY_CONFIGURATION = new Configuration(null, Options.empty(), "${json-unit.ignore}", Matchers.empty(), Collections.emptySet(), DUMMY_LISTENER);
    private final BigDecimal tolerance;
    private final Options options;
    private final String ignorePlaceholder;
    private final Matchers matchers;
    private final Set<String> pathsToBeIgnored;
    private final DifferenceListener differenceListener;

    @Deprecated
    public Configuration(BigDecimal tolerance, Options options, String ignorePlaceholder) {
        this(tolerance, options, ignorePlaceholder, Matchers.empty(), Collections.emptySet(), DUMMY_LISTENER);
    }

    private Configuration(BigDecimal tolerance, Options options, String ignorePlaceholder, Matchers matchers, Collection<String> pathsToBeIgnored, DifferenceListener differenceListener) {
        this.tolerance = tolerance;
        this.options = options;
        this.ignorePlaceholder = ignorePlaceholder;
        this.matchers = matchers;
        this.pathsToBeIgnored = Collections.unmodifiableSet(new HashSet<>(pathsToBeIgnored));
        this.differenceListener = differenceListener;
    }

    /**
     * Returns an empty configuration.
     *
     * @return
     */
    public static Configuration empty() {
        return EMPTY_CONFIGURATION;
    }

    /**
     * Sets numerical comparison tolerance.
     *
     * @param tolerance
     * @return
     */
    public Configuration withTolerance(BigDecimal tolerance) {
        return new Configuration(tolerance, options, ignorePlaceholder, matchers, pathsToBeIgnored, differenceListener);
    }

    /**
     * Sets numerical comparison tolerance.
     *
     * @param tolerance
     * @return
     */
    public Configuration withTolerance(double tolerance) {
        return withTolerance(BigDecimal.valueOf(tolerance));
    }

    /**
     * Adds comparison options.
     *
     * @param first
     * @param next
     * @return
     */
    public Configuration when(Option first, Option... next) {
        return withOptions(first, next);
    }

    /**
     * Adds comparison options.
     *
     * @param first
     * @param next
     * @return
     */
    public Configuration withOptions(Option first, Option... next) {
        return new Configuration(tolerance, options.with(first, next), ignorePlaceholder, matchers, pathsToBeIgnored, differenceListener);
    }

    /**
     * Sets comparison options.
     *
     * @param options
     * @return
     */
    public Configuration withOptions(Options options) {
        return new Configuration(tolerance, options, ignorePlaceholder, matchers, pathsToBeIgnored, differenceListener);
    }

    public Configuration whenIgnoringPaths(String... pathsToBeIgnored) {
        return new Configuration(tolerance, options, ignorePlaceholder, matchers, asList(pathsToBeIgnored), differenceListener);
    }

    /**
     * Sets ignore placeholder.
     *
     * @param ignorePlaceholder
     * @return
     */
    public Configuration withIgnorePlaceholder(String ignorePlaceholder) {
        return new Configuration(tolerance, options, ignorePlaceholder, matchers, pathsToBeIgnored, differenceListener);
    }

    /**
     * Adds a matcher to be used in ${json-unit.matches:matcherName} macro.
     *
     * @param matcherName
     * @param matcher
     * @return
     */
    public Configuration withMatcher(String matcherName, Matcher<?> matcher) {
        return new Configuration(tolerance, options, ignorePlaceholder, matchers.with(matcherName, matcher), pathsToBeIgnored, differenceListener);
    }

    /**
     * Sets difference listener
     */
    public Configuration withDifferenceListener(DifferenceListener differenceListener) {
        return new Configuration(tolerance, options, ignorePlaceholder, matchers, pathsToBeIgnored, differenceListener);
    }

    public static DifferenceListener dummyDifferenceListener() {
        return DUMMY_LISTENER;
    }

    public Matcher<?> getMatcher(String matcherName) {
        return matchers.getMatcher(matcherName);
    }

    public BigDecimal getTolerance() {
        return tolerance;
    }

    public Options getOptions() {
        return options;
    }

    public String getIgnorePlaceholder() {
        return ignorePlaceholder;
    }

    public Set<String> getPathsToBeIgnored() {
        return pathsToBeIgnored;
    }

    public DifferenceListener getDifferenceListener() {
        return differenceListener;
    }
}
