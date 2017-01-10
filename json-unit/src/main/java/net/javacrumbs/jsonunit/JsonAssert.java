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
package net.javacrumbs.jsonunit;

import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.Option;
import net.javacrumbs.jsonunit.core.internal.Diff;
import net.javacrumbs.jsonunit.core.internal.Options;

import java.math.BigDecimal;

import static net.javacrumbs.jsonunit.core.Option.COMPARING_ONLY_STRUCTURE;
import static net.javacrumbs.jsonunit.core.Option.TREATING_NULL_AS_ABSENT;
import static net.javacrumbs.jsonunit.core.internal.Diff.create;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.nodeAbsent;

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
    private static final String ACTUAL = "actual";
    private static final String ROOT = "";
    private static Configuration configuration = Configuration.empty();

    private JsonAssert() {
        //nothing
    }

    /**
     * Compares to JSON documents. Throws {@link AssertionError} if they are different.
     */
    public static void assertJsonEquals(Object expected, Object actual) {
        assertJsonEquals(expected, actual, configuration);
    }

    /**
     * Compares to JSON documents. Throws {@link AssertionError} if they are different.
     */
    public static void assertJsonEquals(Object expected, Object actual, Configuration configuration) {
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
    public static void assertJsonPartEquals(Object expected, Object fullJson, String path, Configuration configuration) {
        Diff diff = create(expected, fullJson, FULL_JSON, path, configuration);
        if (!diff.similar()) {
            doFail(diff.toString());
        }
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
    public static void assertJsonPartNotEquals(Object expected, Object fullJson, String path, Configuration configuration) {
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
     * Compares structures of two JSON documents. Is too lenient, ignores types, prefer IGNORING_VALUES option instead.
     * Throws {@link AssertionError} if they are different.
     */
    public static void assertJsonStructureEquals(Object expected, Object actual) {
        Diff diff = create(expected, actual, ACTUAL, ROOT, configuration.withOptions(COMPARING_ONLY_STRUCTURE));
        if (!diff.similar()) {
            doFail(diff.differences());
        }
    }

    /**
     * Compares structure of part of the JSON. Path has this format "root.array[0].value".
     * Is too lenient, ignores types, prefer IGNORING_VALUES option instead.
     */
    public static void assertJsonPartStructureEquals(Object expected, Object fullJson, String path) {
        Diff diff = create(expected, fullJson, FULL_JSON, path, configuration.withOptions(COMPARING_ONLY_STRUCTURE));
        if (!diff.similar()) {
            doFail(diff.differences());
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
    public static void setTolerance(BigDecimal numericComparisonTolerance) {
        configuration = configuration.withTolerance(numericComparisonTolerance);
    }

    /**
     * Sets the tolerance for floating number comparison. If set to null, requires exact match of the values.
     * For example, if set to 0.01, ignores all differences lower than 0.01, so 1 and 0.9999 are considered equal.
     */
    public static void setTolerance(double numberComparisonTolerance) {
        configuration = configuration.withTolerance(numberComparisonTolerance);
    }

    public static BigDecimal getTolerance() {
        return configuration.getTolerance();
    }

    /**
     * When set to true, treats null nodes in actual value as absent. In other words
     * if you expect {"test":{"a":1}} this {"test":{"a":1, "b": null}} will pass the test.
     *
     * @deprecated use setOptions(Option.TREATING_NULL_AS_ABSENT)
     */
    @Deprecated
    public static void setTreatNullAsAbsent(boolean treatNullAsAbsent) {
        if (treatNullAsAbsent) {
            configuration = configuration.withOptions(TREATING_NULL_AS_ABSENT);
        } else {
            configuration = configuration.withOptions(configuration.getOptions().without(TREATING_NULL_AS_ABSENT));
        }
    }

    /**
     * @deprecated use getOptions().contains(Option.TREATING_NULL_AS_ABSENT)
     */
    @Deprecated
    public static boolean getTreatNullAsAbsent() {
        return configuration.getOptions().contains(TREATING_NULL_AS_ABSENT);
    }

    /**
     * Sets options changing comparison behavior. For more
     * details see {@link net.javacrumbs.jsonunit.core.Option}
     *
     * @see net.javacrumbs.jsonunit.core.Option
     */
    public static void setOptions(Option firstOption, Option... rest) {
        configuration = configuration.withOptions(Options.empty().with(firstOption, rest));
    }

    /**
     * Cleans all options.
     */
    public static void resetOptions() {
        configuration = configuration.withOptions(Options.empty());
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
}
