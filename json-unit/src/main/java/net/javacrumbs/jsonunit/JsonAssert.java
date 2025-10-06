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
package net.javacrumbs.jsonunit;

import static net.javacrumbs.jsonunit.core.internal.Diff.create;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.nodeAbsent;

import java.math.BigDecimal;
import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.ConfigurationWhen.ApplicableForPath;
import net.javacrumbs.jsonunit.core.ConfigurationWhen.PathsParam;
import net.javacrumbs.jsonunit.core.Option;
import net.javacrumbs.jsonunit.core.internal.Diff;
import net.javacrumbs.jsonunit.core.listener.DifferenceListener;
import org.hamcrest.Matcher;
import org.jspecify.annotations.Nullable;

/**
 * Assertions for comparing JSON. The comparison ignores white-spaces and order of nodes.
 *
 * All the methods accept Objects as parameters. The supported types are:
 * <ol>
 * <li>Jackson JsonNode</li>
 * <li>Numbers, booleans and any other type parseable by Jackson's ObjectMapper.convertValue</li>
 * <li>String is parsed as JSON. For expected values the string is quoted if it contains obviously invalid JSON.</li>
 * <li>{@link java.io.Reader} similarly to String</li>
 * <li>null as null Node</li>
 * </ol>
 *
 * @author Lukas Krecan
 */
public class JsonAssert {
    private static final String FULL_JSON = "fullJson";

    private static final String ROOT = "";
    private static Configuration configuration = Configuration.empty();

    private JsonAssert() {
        // nothing
    }

    /**
     * Compares to JSON documents. Throws {@link AssertionError} if they are different.
     */
    public static void assertJsonEquals(@Nullable Object expected, @Nullable Object actual) {
        assertJsonEquals(expected, actual, configuration);
    }

    /**
     * Compares to JSON documents. Throws {@link AssertionError} if they are different.
     */
    public static void assertJsonEquals(
            @Nullable Object expected, @Nullable Object actual, Configuration configuration) {
        assertJsonPartEquals(expected, actual, ROOT, configuration);
    }

    /**
     * Compares part of the JSON. Path has this format "root.array[0].value".
     */
    public static void assertJsonPartEquals(Object expected, Object fullJson, String path) {
        assertJsonPartEquals(expected, fullJson, path, configuration);
    }

    /**
     * Compares part of the JSON. Path has this format "root.array[0].value".
     */
    public static void assertJsonPartEquals(
            @Nullable Object expected, @Nullable Object fullJson, String path, Configuration configuration) {
        Diff diff = create(expected, fullJson, FULL_JSON, path, configuration);
        diff.failIfDifferent();
    }

    /**
     * Compares JSONs and fails if they are equal.
     */
    public static void assertJsonNotEquals(Object expected, Object fullJson) {
        assertJsonNotEquals(expected, fullJson, configuration);
    }

    /**
     * Compares JSONs and fails if they are equal.
     */
    public static void assertJsonNotEquals(Object expected, Object fullJson, Configuration configuration) {
        assertJsonPartNotEquals(expected, fullJson, ROOT, configuration);
    }

    /**
     * Compares part of the JSON and fails if they are equal.
     * Path has this format "root.array[0].value".
     */
    public static void assertJsonPartNotEquals(Object expected, Object fullJson, String path) {
        assertJsonPartNotEquals(expected, fullJson, path, configuration);
    }

    /**
     * Compares part of the JSON and fails if they are equal.
     * Path has this format "root.array[0].value".
     */
    public static void assertJsonPartNotEquals(
            Object expected, Object fullJson, String path, Configuration configuration) {
        Diff diff = create(expected, fullJson, FULL_JSON, path, configuration);
        if (diff.similar()) {
            if (ROOT.equals(path)) {
                doFail("Expected different values but the values were equal.");
            } else {
                doFail(String.format("Expected different values in node \"%s\" but the values were equal.", path));
            }
        }
    }

    /**
     * Fails if node in given path exists.
     */
    public static void assertJsonNodeAbsent(Object actual, String path) {
        if (!nodeAbsent(actual, path, configuration)) {
            doFail("Node \"" + path + "\" is present.");
        }
    }

    /**
     * Fails if node in given does not exist.
     */
    public static void assertJsonNodePresent(Object actual, String path) {
        if (nodeAbsent(actual, path, configuration)) {
            doFail("Node \"" + path + "\" is missing.");
        }
    }

    /**
     * Fails a test with the given message.
     */
    private static void doFail(String diffMessage) {
        throw new AssertionError(diffMessage);
    }

    /**
     * Set's string that will be ignored in comparison. Default value is "${json-unit.ignore}"
     */
    public static void setIgnorePlaceholder(String ignorePlaceholder) {
        configuration = configuration.withIgnorePlaceholder(ignorePlaceholder);
    }

    public static String getIgnorePlaceholder() {
        return configuration.getIgnorePlaceholder();
    }

    /**
     * Sets the tolerance for floating number comparison. If set to null, requires exact match of the values.
     * For example, if set to 0.01, ignores all differences lower than 0.01, so 1 and 0.9999 are considered equal.
     */
    public static void setTolerance(@Nullable BigDecimal numericComparisonTolerance) {
        configuration = configuration.withTolerance(numericComparisonTolerance);
    }

    /**
     * Sets the tolerance for floating number comparison. If set to null, requires exact match of the values.
     * For example, if set to 0.01, ignores all differences lower than 0.01, so 1 and 0.9999 are considered equal.
     */
    public static void setTolerance(double numberComparisonTolerance) {
        configuration = configuration.withTolerance(numberComparisonTolerance);
    }

    public static @Nullable BigDecimal getTolerance() {
        return configuration.getTolerance();
    }

    /**
     * Sets listener to customize diff format
     */
    public static void setDifferenceListener(DifferenceListener listener) {
        configuration = configuration.withDifferenceListener(listener);
    }

    public static DifferenceListener getDifferenceListener() {
        return configuration.getDifferenceListener();
    }

    /**
     * Sets options changing comparison behavior. For more
     * details see {@link net.javacrumbs.jsonunit.core.Option}
     *
     * @see net.javacrumbs.jsonunit.core.Option
     */
    public static void setOptions(Option firstOption, Option... rest) {
        configuration = configuration.withOptions(firstOption, rest);
    }

    /**
     * Cleans all options.
     */
    public static void resetOptions() {
        configuration = configuration.resetOptions();
    }

    static Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Creates empty configuration and sets numerical comparison tolerance.
     */
    public static Configuration withTolerance(double tolerance) {
        return Configuration.empty().withTolerance(tolerance);
    }

    /**
     * Creates empty configuration and sets numerical comparison tolerance.
     */
    public static Configuration withTolerance(BigDecimal tolerance) {
        return Configuration.empty().withTolerance(tolerance);
    }

    /**
     * Creates empty configuration and sets options.
     */
    public static Configuration when(Option first, Option... next) {
        return Configuration.empty().withOptions(first, next);
    }

    /**
     * Adds a matcher to be used in ${json-unit.matches:matcherName} macro.
     */
    public static Configuration withMatcher(String matcherName, Matcher<?> matcher) {
        return Configuration.empty().withMatcher(matcherName, matcher);
    }

    /**
     * Sets paths to be ignored.
     */
    public static Configuration whenIgnoringPaths(String... paths) {
        return Configuration.empty().whenIgnoringPaths(paths);
    }

    /**
     * Creates an empty configuration with specific path options.
     *
     * @see Configuration#when(PathsParam, ApplicableForPath...)
     */
    public static Configuration when(PathsParam subject, ApplicableForPath... actions) {
        return Configuration.empty().when(subject, actions);
    }

    /**
     * Sets DifferenceListener.
     */
    public static Configuration withDifferenceListener(DifferenceListener differenceListener) {
        return Configuration.empty().withDifferenceListener(differenceListener);
    }
}
