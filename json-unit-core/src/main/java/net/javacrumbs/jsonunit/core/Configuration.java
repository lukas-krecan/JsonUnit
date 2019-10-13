/**
 * Copyright 2009-2019 the original author or authors.
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

import net.javacrumbs.jsonunit.core.ConfigurationWhen.ApplicableForPath;
import net.javacrumbs.jsonunit.core.ConfigurationWhen.PathsParam;
import net.javacrumbs.jsonunit.core.internal.Options;
import net.javacrumbs.jsonunit.core.internal.PathOption;
import net.javacrumbs.jsonunit.core.listener.DifferenceListener;
import org.hamcrest.Matcher;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;

/**
 * Comparison configuration. Immutable.
 */
public class Configuration {
    private static final DifferenceListener DUMMY_LISTENER = (difference, context) -> {};

    private static final String DEFAULT_IGNORE_PLACEHOLDER = "${json-unit.ignore}";
    private static final String ALTERNATIVE_IGNORE_PLACEHOLDER = "#{json-unit.ignore}";
    private static final Configuration EMPTY_CONFIGURATION = new Configuration(null, Options.empty(), DEFAULT_IGNORE_PLACEHOLDER, Matchers.empty(), Collections.emptySet(), DUMMY_LISTENER, Collections.emptyList());
    private final BigDecimal tolerance;
    private final Options options;
    private final String ignorePlaceholder;
    private final Matchers matchers;
    private final List<PathOption> pathOptions;
    private final Set<String> pathsToBeIgnored;
    private final DifferenceListener differenceListener;

    @Deprecated
    public Configuration(BigDecimal tolerance, Options options, String ignorePlaceholder) {
        this(tolerance, options, ignorePlaceholder, Matchers.empty(), Collections.emptySet(), DUMMY_LISTENER, Collections.emptyList());
    }

    private Configuration(BigDecimal tolerance, Options options, String ignorePlaceholder, Matchers matchers, Set<String> pathsToBeIgnored, DifferenceListener differenceListener, List<PathOption> pathOptions) {
        this.tolerance = tolerance;
        this.options = options;
        this.ignorePlaceholder = ignorePlaceholder;
        this.matchers = matchers;
        this.pathsToBeIgnored = pathsToBeIgnored;
        this.pathOptions = pathOptions;
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
        return new Configuration(tolerance, options, ignorePlaceholder, matchers, pathsToBeIgnored, differenceListener, pathOptions);
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
        return new Configuration(tolerance, options.with(first, next), ignorePlaceholder, matchers, pathsToBeIgnored, differenceListener, pathOptions);
    }

    /**
     * Sets comparison options.
     *
     * @param options
     * @return
     */
    public Configuration withOptions(Options options) {
        return new Configuration(tolerance, options, ignorePlaceholder, matchers, pathsToBeIgnored, differenceListener, pathOptions);
    }

    /**
     * Defines general comparison options. See {@link ConfigurationWhen#path} for some examples.
     *
     * @param object an object to apply actions, e.g. {@link ConfigurationWhen#path}, {@link ConfigurationWhen#rootPath}.
     * @param actions actions to be applied on the object.
     *
     * @see ConfigurationWhen#path
     */
    public final Configuration when(PathsParam object, ApplicableForPath... actions) {
        Configuration configuration = this;
        for (ApplicableForPath action : actions) {
            configuration = object.apply(configuration, action);
        }
        return configuration;
    }

    Configuration addPathOption(PathOption pathOption) {
        List<PathOption> newOptions = new ArrayList<>(this.pathOptions);
        newOptions.add(pathOption);
        return withPathOptions(newOptions);
    }

    public Configuration withPathOptions(List<PathOption> pathOptions) {
        return new Configuration(tolerance, options, ignorePlaceholder, matchers, pathsToBeIgnored, differenceListener, Collections.unmodifiableList(new ArrayList<>(pathOptions)));
    }

    public Configuration whenIgnoringPaths(Collection<String> pathsToBeIgnored) {
        return new Configuration(tolerance, options, ignorePlaceholder, matchers, Collections.unmodifiableSet(new HashSet<>(pathsToBeIgnored)), differenceListener, pathOptions);
    }

    /**
     * Makes JsonUnit ignore the specified paths in the actual value. If the path matches,
     * it's completely ignored. It may be missing, null or have any value
     *
     * @param pathsToBeIgnored
     * @return
     *
     * @see ConfigurationWhen#thenIgnore
     */
    public Configuration whenIgnoringPaths(String... pathsToBeIgnored) {
        return whenIgnoringPaths(asList(pathsToBeIgnored));
    }

    /**
     * Sets ignore placeholder.
     *
     * @param ignorePlaceholder
     * @return
     */
    public Configuration withIgnorePlaceholder(String ignorePlaceholder) {
        return new Configuration(tolerance, options, ignorePlaceholder, matchers, pathsToBeIgnored, differenceListener, pathOptions);
    }

    /**
     * Adds a matcher to be used in ${json-unit.matches:matcherName} macro.
     *
     * @param matcherName
     * @param matcher
     * @return
     */
    public Configuration withMatcher(String matcherName, Matcher<?> matcher) {
        return new Configuration(tolerance, options, ignorePlaceholder, matchers.with(matcherName, matcher), pathsToBeIgnored, differenceListener, pathOptions);
    }

    /**
     * Sets difference listener
     */
    public Configuration withDifferenceListener(DifferenceListener differenceListener) {
        return new Configuration(tolerance, options, ignorePlaceholder, matchers, pathsToBeIgnored, differenceListener, pathOptions);
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

    public List<PathOption> getPathOptions() {
        return pathOptions;
    }

    public Set<String> getPathsToBeIgnored() {
        return pathsToBeIgnored;
    }

    public DifferenceListener getDifferenceListener() {
        return differenceListener;
    }

    public boolean shouldIgnore(String expectedValue) {
        if (DEFAULT_IGNORE_PLACEHOLDER.equals(ignorePlaceholder)) {
            // special handling of default state. We want to support both # and $ before {json-unit.ignore} but do not want to
            // override user specified value if any
            return DEFAULT_IGNORE_PLACEHOLDER.equals(expectedValue) || ALTERNATIVE_IGNORE_PLACEHOLDER.equals(expectedValue);
        } else {
            return ignorePlaceholder.equals(expectedValue);
        }
    }
}
