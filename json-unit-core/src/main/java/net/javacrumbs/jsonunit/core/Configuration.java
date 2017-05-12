/**
 * Copyright 2009-2017 the original author or authors.
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
import org.hamcrest.Matcher;

import java.math.BigDecimal;

/**
 * Comparison configuration. Immutable.
 */
public class Configuration {
    private static final Configuration EMPTY_CONFIGURATION = new Configuration(null, Options.empty(), "${json-unit.ignore}", Matchers.empty());
    private final BigDecimal tolerance;
    private final Options options;
    private final String ignorePlaceholder;
    private final Matchers matchers;

    @Deprecated
    public Configuration(BigDecimal tolerance, Options options, String ignorePlaceholder) {
        this(tolerance, options, ignorePlaceholder, Matchers.empty());
    }

    private Configuration(BigDecimal tolerance, Options options, String ignorePlaceholder, Matchers matchers) {
        this.tolerance = tolerance;
        this.options = options;
        this.ignorePlaceholder = ignorePlaceholder;
        this.matchers = matchers;
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
        return new Configuration(tolerance, options, ignorePlaceholder, matchers);
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
        return new Configuration(tolerance, options.with(first, next), ignorePlaceholder, matchers);
    }

    /**
     * Sets comparison options.
     *
     * @param options
     * @return
     */
    public Configuration withOptions(Options options) {
        return new Configuration(tolerance, options, ignorePlaceholder, matchers);
    }

    /**
     * Sets ignore placeholder.
     *
     * @param ignorePlaceholder
     * @return
     */
    public Configuration withIgnorePlaceholder(String ignorePlaceholder) {
        return new Configuration(tolerance, options, ignorePlaceholder, matchers);
    }

    /**
     * Adds a matcher to be used in ${json-unit.matches:matcherName} macro.
     *
     * @param matcherName
     * @param matcher
     * @return
     */
    public Configuration withMatcher(String matcherName, Matcher<?> matcher) {
        return new Configuration(tolerance, options, ignorePlaceholder, matchers.with(matcherName, matcher));
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

}
