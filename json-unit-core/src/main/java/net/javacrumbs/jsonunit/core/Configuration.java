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
import net.javacrumbs.jsonunit.core.internal.DefaultNumberComparator;
import net.javacrumbs.jsonunit.core.internal.PathOption;
import net.javacrumbs.jsonunit.core.listener.DifferenceListener;
import org.hamcrest.Matcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
    private static final DefaultNumberComparator DEFAULT_NUMBER_COMPARATOR = new DefaultNumberComparator();
    private static final Configuration EMPTY_CONFIGURATION = new Configuration(null, Options.empty(), DEFAULT_IGNORE_PLACEHOLDER, Matchers.empty(), Collections.emptySet(), DUMMY_LISTENER, Collections.emptyList(), DEFAULT_NUMBER_COMPARATOR);
    private final BigDecimal tolerance;
    private final Options options;
    private final String ignorePlaceholder;
    private final Matchers matchers;
    private final List<PathOption> pathOptions;
    private final Set<String> pathsToBeIgnored;
    private final DifferenceListener differenceListener;
    private final NumberComparator numberComparator;

    private Configuration(@Nullable BigDecimal tolerance, Options options, String ignorePlaceholder, Matchers matchers, Set<String> pathsToBeIgnored, DifferenceListener differenceListener, List<PathOption> pathOptions, NumberComparator numberComparator) {
        this.tolerance = tolerance;
        this.options = options;
        this.ignorePlaceholder = ignorePlaceholder;
        this.matchers = matchers;
        this.pathsToBeIgnored = pathsToBeIgnored;
        this.pathOptions = pathOptions;
        this.differenceListener = differenceListener;
        this.numberComparator = numberComparator;
    }

    /**
     * Returns an empty configuration.
     */
    @NotNull
    public static Configuration empty() {
        return EMPTY_CONFIGURATION;
    }

    /**
     * Sets numerical comparison tolerance.
     *
     * @param tolerance
     * @return
     */
    @NotNull
    public Configuration withTolerance(@Nullable BigDecimal tolerance) {
        return new Configuration(tolerance, options, ignorePlaceholder, matchers, pathsToBeIgnored, differenceListener, pathOptions, numberComparator);
    }

    /**
     * Sets numerical comparison tolerance.
     *
     * @param tolerance
     * @return
     */
    @NotNull
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
    @NotNull
    public Configuration when(@NotNull Option first, @NotNull Option... next) {
        return withOptions(first, next);
    }

    /**
     * Adds comparison options.
     *
     * @param first
     * @param next
     * @return
     */
    @NotNull
    public Configuration withOptions(@NotNull Option first, @NotNull Option... next) {
        return new Configuration(tolerance, options.with(first, next), ignorePlaceholder, matchers, pathsToBeIgnored, differenceListener, pathOptions, numberComparator);
    }

    /**
     * Adds comparison options.
     */
    @NotNull
    public Configuration withOptions(@NotNull Collection<Option> optionsToAdd) {
        return new Configuration(tolerance, options.with(optionsToAdd), ignorePlaceholder, matchers, pathsToBeIgnored, differenceListener, pathOptions, numberComparator);
    }

    @NotNull
    public Configuration resetOptions() {
        return new Configuration(tolerance, Options.empty(), ignorePlaceholder, matchers, pathsToBeIgnored, differenceListener, pathOptions, numberComparator);
    }

    /**
     * Defines general comparison options. See {@link ConfigurationWhen#path} for some examples.
     *
     * @param object an object to apply actions, e.g. {@link ConfigurationWhen#path}, {@link ConfigurationWhen#rootPath}.
     * @param actions actions to be applied on the object.
     *
     * @see ConfigurationWhen#path
     */
    @NotNull
    public final Configuration when(@NotNull PathsParam object, @NotNull ApplicableForPath... actions) {
        Configuration configuration = this;
        for (ApplicableForPath action : actions) {
            configuration = object.apply(configuration, action);
        }
        return configuration;
    }

    @NotNull
    Configuration addPathOption(@NotNull PathOption pathOption) {
        List<PathOption> newOptions = new ArrayList<>(this.pathOptions);
        newOptions.add(pathOption);
        return withPathOptions(newOptions);
    }

    @NotNull
    public Configuration withPathOptions(@NotNull List<PathOption> pathOptions) {
        return new Configuration(tolerance, options, ignorePlaceholder, matchers, pathsToBeIgnored, differenceListener, List.copyOf(pathOptions), numberComparator);
    }

    @NotNull
    public Configuration whenIgnoringPaths(@NotNull Collection<String> pathsToBeIgnored) {
        return new Configuration(tolerance, options, ignorePlaceholder, matchers, Set.copyOf(pathsToBeIgnored), differenceListener, pathOptions, numberComparator);
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
    @NotNull
    public Configuration whenIgnoringPaths(@NotNull String... pathsToBeIgnored) {
        return whenIgnoringPaths(asList(pathsToBeIgnored));
    }

    /**
     * Sets ignore placeholder.
     *
     * @param ignorePlaceholder
     * @return
     */
    @NotNull
    public Configuration withIgnorePlaceholder(@NotNull String ignorePlaceholder) {
        return new Configuration(tolerance, options, ignorePlaceholder, matchers, pathsToBeIgnored, differenceListener, pathOptions, numberComparator);
    }

    /**
     * Adds a matcher to be used in ${json-unit.matches:matcherName} macro.
     *
     * @param matcherName
     * @param matcher
     * @return
     */
    @NotNull
    public Configuration withMatcher(@NotNull String matcherName, @NotNull Matcher<?> matcher) {
        return new Configuration(tolerance, options, ignorePlaceholder, matchers.with(matcherName, matcher), pathsToBeIgnored, differenceListener, pathOptions, numberComparator);
    }

    /**
     * Sets difference listener
     */
    @NotNull
    public Configuration withDifferenceListener(@NotNull DifferenceListener differenceListener) {
        return new Configuration(tolerance, options, ignorePlaceholder, matchers, pathsToBeIgnored, differenceListener, pathOptions, numberComparator);
    }

    /**
     * Sets Number comparator
     */
    @NotNull
    public Configuration withNumberComparator(@NotNull NumberComparator numberComparator) {
        return new Configuration(tolerance, options, ignorePlaceholder, matchers, pathsToBeIgnored, differenceListener, pathOptions, numberComparator);
    }

    @NotNull
    public static DifferenceListener dummyDifferenceListener() {
        return DUMMY_LISTENER;
    }

    @Nullable
    public Matcher<?> getMatcher(String matcherName) {
        return matchers.getMatcher(matcherName);
    }

    @Nullable
    public BigDecimal getTolerance() {
        return tolerance;
    }

    @NotNull
    public Set<Option> getOptions() {
        return options.values();
    }

    @NotNull
    public String getIgnorePlaceholder() {
        return ignorePlaceholder;
    }

    @NotNull
    public List<PathOption> getPathOptions() {
        return pathOptions;
    }

    @NotNull
    public Set<String> getPathsToBeIgnored() {
        return pathsToBeIgnored;
    }

    @NotNull
    public DifferenceListener getDifferenceListener() {
        return differenceListener;
    }

    @NotNull
    public NumberComparator getNumberComparator() {
        return numberComparator;
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
