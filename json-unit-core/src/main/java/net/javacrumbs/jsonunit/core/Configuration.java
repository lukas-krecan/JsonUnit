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

import static java.util.Arrays.asList;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import net.javacrumbs.jsonunit.core.ConfigurationWhen.ApplicableForPath;
import net.javacrumbs.jsonunit.core.ConfigurationWhen.PathsParam;
import net.javacrumbs.jsonunit.core.internal.DefaultNumberComparator;
import net.javacrumbs.jsonunit.core.internal.PathOption;
import net.javacrumbs.jsonunit.core.listener.DifferenceListener;
import org.hamcrest.Matcher;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Comparison configuration. Immutable.
 */
public class Configuration {
    private static final DifferenceListener DUMMY_LISTENER = (difference, context) -> {};

    private static final String DEFAULT_IGNORE_PLACEHOLDER = "${json-unit.ignore}";
    private static final String ALTERNATIVE_IGNORE_PLACEHOLDER = "#{json-unit.ignore}";
    private static final DefaultNumberComparator DEFAULT_NUMBER_COMPARATOR = new DefaultNumberComparator();
    private static final Configuration EMPTY_CONFIGURATION = new Configuration(
            null,
            Options.empty(),
            DEFAULT_IGNORE_PLACEHOLDER,
            Matchers.empty(),
            Collections.emptySet(),
            DUMMY_LISTENER,
            Collections.emptyList(),
            DEFAULT_NUMBER_COMPARATOR);
    private final BigDecimal tolerance;
    private final Options options;
    private final String ignorePlaceholder;
    private final Matchers matchers;
    private final List<PathOption> pathOptions;
    private final Set<String> pathsToBeIgnored;
    private final DifferenceListener differenceListener;
    private final NumberComparator numberComparator;

    private Configuration(
            @Nullable BigDecimal tolerance,
            Options options,
            String ignorePlaceholder,
            Matchers matchers,
            Set<String> pathsToBeIgnored,
            DifferenceListener differenceListener,
            List<PathOption> pathOptions,
            NumberComparator numberComparator) {
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
    @NonNull
    public static Configuration empty() {
        return EMPTY_CONFIGURATION;
    }

    /**
     * Sets numerical comparison tolerance.
     *
     * @param tolerance
     * @return
     */
    @NonNull
    public Configuration withTolerance(@Nullable BigDecimal tolerance) {
        return new Configuration(
                tolerance,
                options,
                ignorePlaceholder,
                matchers,
                pathsToBeIgnored,
                differenceListener,
                pathOptions,
                numberComparator);
    }

    /**
     * Sets numerical comparison tolerance.
     *
     * @param tolerance
     * @return
     */
    @NonNull
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
    @NonNull
    public Configuration when(@NonNull Option first, @NonNull Option... next) {
        return withOptions(first, next);
    }

    /**
     * Adds comparison options.
     *
     * @param first
     * @param next
     * @return
     */
    @NonNull
    public Configuration withOptions(@NonNull Option first, @NonNull Option... next) {
        return new Configuration(
                tolerance,
                options.with(first, next),
                ignorePlaceholder,
                matchers,
                pathsToBeIgnored,
                differenceListener,
                pathOptions,
                numberComparator);
    }

    /**
     * Adds comparison options.
     */
    @NonNull
    public Configuration withOptions(@NonNull Collection<Option> optionsToAdd) {
        return new Configuration(
                tolerance,
                options.with(optionsToAdd),
                ignorePlaceholder,
                matchers,
                pathsToBeIgnored,
                differenceListener,
                pathOptions,
                numberComparator);
    }

    @NonNull
    public Configuration resetOptions() {
        return new Configuration(
                tolerance,
                Options.empty(),
                ignorePlaceholder,
                matchers,
                pathsToBeIgnored,
                differenceListener,
                pathOptions,
                numberComparator);
    }

    /**
     * Defines general comparison options. See {@link ConfigurationWhen#path} for some examples.
     *
     * @param object an object to apply actions, e.g. {@link ConfigurationWhen#path}, {@link ConfigurationWhen#rootPath}.
     * @param actions actions to be applied on the object.
     *
     * @see ConfigurationWhen#path
     */
    @NonNull
    public final Configuration when(@NonNull PathsParam object, @NonNull ApplicableForPath... actions) {
        Configuration configuration = this;
        for (ApplicableForPath action : actions) {
            configuration = object.apply(configuration, action);
        }
        return configuration;
    }

    @NonNull
    Configuration addPathOption(@NonNull PathOption pathOption) {
        List<PathOption> newOptions = new ArrayList<>(this.pathOptions);
        newOptions.add(pathOption);
        return withPathOptions(newOptions);
    }

    @NonNull
    public Configuration withPathOptions(@NonNull List<PathOption> pathOptions) {
        return new Configuration(
                tolerance,
                options,
                ignorePlaceholder,
                matchers,
                pathsToBeIgnored,
                differenceListener,
                List.copyOf(pathOptions),
                numberComparator);
    }

    @NonNull
    public Configuration whenIgnoringPaths(@NonNull Collection<String> pathsToBeIgnored) {
        return new Configuration(
                tolerance,
                options,
                ignorePlaceholder,
                matchers,
                Set.copyOf(pathsToBeIgnored),
                differenceListener,
                pathOptions,
                numberComparator);
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
    @NonNull
    public Configuration whenIgnoringPaths(@NonNull String... pathsToBeIgnored) {
        return whenIgnoringPaths(asList(pathsToBeIgnored));
    }

    /**
     * Sets ignore placeholder.
     *
     * @param ignorePlaceholder
     * @return
     */
    @NonNull
    public Configuration withIgnorePlaceholder(@NonNull String ignorePlaceholder) {
        return new Configuration(
                tolerance,
                options,
                ignorePlaceholder,
                matchers,
                pathsToBeIgnored,
                differenceListener,
                pathOptions,
                numberComparator);
    }

    /**
     * Adds a matcher to be used in ${json-unit.matches:matcherName} macro.
     *
     * @param matcherName
     * @param matcher
     * @return
     */
    @NonNull
    public Configuration withMatcher(@NonNull String matcherName, @NonNull Matcher<?> matcher) {
        return new Configuration(
                tolerance,
                options,
                ignorePlaceholder,
                matchers.with(matcherName, matcher),
                pathsToBeIgnored,
                differenceListener,
                pathOptions,
                numberComparator);
    }

    /**
     * Sets difference listener
     */
    @NonNull
    public Configuration withDifferenceListener(@NonNull DifferenceListener differenceListener) {
        return new Configuration(
                tolerance,
                options,
                ignorePlaceholder,
                matchers,
                pathsToBeIgnored,
                differenceListener,
                pathOptions,
                numberComparator);
    }

    /**
     * Sets Number comparator
     */
    @NonNull
    public Configuration withNumberComparator(@NonNull NumberComparator numberComparator) {
        return new Configuration(
                tolerance,
                options,
                ignorePlaceholder,
                matchers,
                pathsToBeIgnored,
                differenceListener,
                pathOptions,
                numberComparator);
    }

    @NonNull
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

    @NonNull
    public Set<Option> getOptions() {
        return options.values();
    }

    @NonNull
    public String getIgnorePlaceholder() {
        return ignorePlaceholder;
    }

    @NonNull
    public List<PathOption> getPathOptions() {
        return pathOptions;
    }

    @NonNull
    public Set<String> getPathsToBeIgnored() {
        return pathsToBeIgnored;
    }

    @NonNull
    public DifferenceListener getDifferenceListener() {
        return differenceListener;
    }

    @NonNull
    public NumberComparator getNumberComparator() {
        return numberComparator;
    }

    public boolean shouldIgnore(String expectedValue) {
        if (DEFAULT_IGNORE_PLACEHOLDER.equals(ignorePlaceholder)) {
            // special handling of default state. We want to support both # and $ before {json-unit.ignore} but do not
            // want to
            // override user specified value if any
            return DEFAULT_IGNORE_PLACEHOLDER.equals(expectedValue)
                    || ALTERNATIVE_IGNORE_PLACEHOLDER.equals(expectedValue);
        } else {
            return ignorePlaceholder.equals(expectedValue);
        }
    }
}
