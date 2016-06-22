/**
 * Copyright 2009-2015 the original author or authors.
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
package net.javacrumbs.jsonunit.core;

import net.javacrumbs.jsonunit.core.internal.Options;
import org.hamcrest.Matcher;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Comparison configuration. Immutable.
 */
public class Configuration {
    private static final Map<String, NodeMatcher> EMPTY_INLINE_MATCHERS = Collections.emptyMap();
    private static final Configuration EMPTY_CONFIGURATION = new Configuration(null, Options.empty(), "${json-unit.ignore}", EMPTY_INLINE_MATCHERS);
    private final BigDecimal tolerance;
    private final Options options;
    private final String ignorePlaceholder;

    private final Map<String, NodeMatcher> inlineMatchers;

    @Deprecated
    public Configuration(BigDecimal tolerance, Options options, String ignorePlaceholder) {
        this(tolerance, options, ignorePlaceholder, EMPTY_INLINE_MATCHERS);
    }

    private Configuration(BigDecimal tolerance, Options options, String ignorePlaceholder, Map<String, NodeMatcher> inlineMatchers) {
        this.tolerance = tolerance;
        this.options = options;
        this.ignorePlaceholder = ignorePlaceholder;
        this.inlineMatchers = inlineMatchers;
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
        return new Configuration(tolerance, options, ignorePlaceholder, inlineMatchers);
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
        return new Configuration(tolerance, options.with(first, next), ignorePlaceholder, inlineMatchers);
    }

    /**
     * Sets comparison options.
     *
     * @param options
     * @return
     */
    public Configuration withOptions(Options options) {
        return new Configuration(tolerance, options, ignorePlaceholder, inlineMatchers);
    }

    /**
     * Sets ignore placeholder.
     *
     * @param ignorePlaceholder
     * @return
     */
    public Configuration withIgnorePlaceholder(String ignorePlaceholder) {
        return new Configuration(tolerance, options, ignorePlaceholder, inlineMatchers);
    }

    /**
     * Adds inline matcher.
     *
     * @param ignorePlaceholder
     * @return
     */
    public Configuration withInlineMatcher(String name, Matcher<?> matcher) {
        return withInlineMatcher(name, new HamcrestNodeMatcher(matcher));
    }

    /**
     * Adds inline matcher.
     *
     * @param ignorePlaceholder
     * @return
     */
    public Configuration withInlineMatcher(String name, NodeMatcher matcher) {
        Map<String, NodeMatcher> newInlineMatchers = new HashMap<String, NodeMatcher>(inlineMatchers);
        newInlineMatchers.put(name, matcher);
        return new Configuration(tolerance, options, ignorePlaceholder, Collections.unmodifiableMap(newInlineMatchers));
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

    public NodeMatcher getInlineMatcher(String name) {
        return inlineMatchers.get(name);
    }
}
