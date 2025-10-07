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
package net.javacrumbs.jsonunit.spring;

import java.math.BigDecimal;
import java.util.function.Consumer;
import java.util.function.Function;
import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.ConfigurationWhen;
import net.javacrumbs.jsonunit.core.Option;
import net.javacrumbs.jsonunit.core.internal.matchers.InternalMatcher;
import net.javacrumbs.jsonunit.core.listener.DifferenceListener;
import net.javacrumbs.jsonunit.jsonpath.JsonPathAdapter;
import org.hamcrest.Matcher;
import org.jspecify.annotations.Nullable;

/**
 * Common superclass for request and response matcher
 * @param <ME> Type of the factory class
 * @param <MATCHER> Type of the matcher
 */
abstract class AbstractSpringMatchers<ME, MATCHER> {
    final Configuration configuration;
    final Function<Object, Object> jsonTransformer;

    AbstractSpringMatchers(Configuration configuration, Function<Object, Object> jsonTransformer) {
        this.configuration = configuration;
        this.jsonTransformer = jsonTransformer;
    }

    abstract MATCHER matcher(Consumer<InternalMatcher> matcher);

    abstract ME matchers(Configuration configuration, Function<Object, Object> jsonTransformer);

    protected ME matchers(Configuration configuration) {
        return matchers(configuration, jsonTransformer);
    }

    /**
     * Creates a matcher object that only compares given node.
     * The path is denoted by JSON path, for example.
     * <p>
     * <code>
     * this.mockMvc.perform(get("/sample").accept(MediaType.APPLICATION_JSON)).andExpect(json().node("root.test[0]").isEqualTo("1"));
     * </code>
     *
     * @return object comparing only node given by path.
     */
    public ME node(String path) {
        return inPath(path);
    }

    /**
     * Uses JsonPath to extract values from the actual value.
     */
    public ME inPath(String path) {
        return matchers(configuration, json -> JsonPathAdapter.inPath(jsonTransformer.apply(json), path));
    }

    /**
     * Sets the placeholder that can be used to ignore values.
     * The default value is ${json-unit.ignore}
     */
    public ME ignoring(String ignorePlaceholder) {
        return matchers(configuration.withIgnorePlaceholder(ignorePlaceholder));
    }

    /**
     * Sets the tolerance for floating number comparison. If set to null, requires exact match of the values.
     * For example, if set to 0.01, ignores all differences lower than 0.01, so 1 and 0.9999 are considered equal.
     */
    public ME withTolerance(double tolerance) {
        return withTolerance(BigDecimal.valueOf(tolerance));
    }

    /**
     * Adds a matcher to be used in ${json-unit.matches:matcherName} macro.
     */
    public ME withMatcher(String matcherName, Matcher<?> matcher) {
        return matchers(configuration.withMatcher(matcherName, matcher));
    }

    /**
     * Sets the tolerance for floating number comparison. If set to null, requires exact match of the values.
     * For example, if set to 0.01, ignores all differences lower than 0.01, so 1 and 0.9999 are considered equal.
     */
    public ME withTolerance(@Nullable BigDecimal tolerance) {
        return matchers(configuration.withTolerance(tolerance));
    }

    public ME withDifferenceListener(DifferenceListener differenceListener) {
        return matchers(configuration.withDifferenceListener(differenceListener));
    }

    /**
     * Sets options changing comparison behavior. For more
     * details see {@link net.javacrumbs.jsonunit.core.Option}
     *
     * @see net.javacrumbs.jsonunit.core.Option
     */
    public ME when(Option firstOption, Option... otherOptions) {
        return matchers(configuration.withOptions(firstOption, otherOptions));
    }

    /**
     * Adds path specific options.
     *
     * @see Configuration#when(ConfigurationWhen.PathsParam, ConfigurationWhen.ApplicableForPath...)
     */
    public ME when(ConfigurationWhen.PathsParam object, ConfigurationWhen.ApplicableForPath... actions) {
        return matchers(configuration.when(object, actions));
    }

    /**
     * Compares JSON for equality. The expected object is converted to JSON
     * before comparison. Ignores order of sibling nodes and whitespaces.
     * <p>
     * Please note that if you pass a String, it's parsed as JSON which can lead to an
     * unexpected behavior. If you pass in "1" it is parsed as a JSON containing
     * integer 1. If you compare it with a string it fails due to a different type.
     * If you want to pass in real string you have to quote it "\"1\"" or use
     * {@link #isStringEqualTo(String)}.
     * <p>
     * If the string parameter is not a valid JSON, it is quoted automatically.
     *
     *
     * @return {@code this} object.
     * @see #isStringEqualTo(String)
     */
    public MATCHER isEqualTo(@Nullable Object expected) {
        return matcher(ctx -> ctx.isEqualTo(expected));
    }

    /**
     * Fails if the selected JSON is not a String or is not present or the value
     * is not equal to expected value.
     */
    public MATCHER isStringEqualTo(@Nullable final String expected) {
        return matcher(ctx -> ctx.isStringEqualTo(expected));
    }

    /**
     * Fails if compared documents are equal. The expected object is converted to JSON
     * before comparison. Ignores order of sibling nodes and whitespaces.
     */
    public MATCHER isNotEqualTo(@Nullable Object expected) {
        return matcher(ctx -> ctx.isNotEqualTo(expected));
    }

    /**
     * Fails if the node exists.
     */
    public MATCHER isAbsent() {
        return matcher(InternalMatcher::isAbsent);
    }

    /**
     * Fails if the node is missing.
     */
    public MATCHER isPresent() {
        return matcher(InternalMatcher::isPresent);
    }

    /**
     * Fails if the selected JSON is not an Array or is not present.
     */
    public MATCHER isArray() {
        return matcher(InternalMatcher::isArray);
    }

    /**
     * Fails if the selected JSON is not an Object or is not present.
     */
    public MATCHER isObject() {
        return matcher(InternalMatcher::isObject);
    }

    /**
     * Fails if the selected JSON is not a String or is not present.
     */
    public MATCHER isString() {
        return matcher(InternalMatcher::isString);
    }

    /**
     * Fails if selected JSON is not null.
     */
    public MATCHER isNull() {
        return matcher(InternalMatcher::isNull);
    }

    /**
     * Fails if selected JSON is  null.
     */
    public MATCHER isNotNull() {
        return matcher(InternalMatcher::isNotNull);
    }

    /**
     * Matches the node using Hamcrest matcher.
     * <p>
     * <ul>
     * <li>Numbers are mapped to BigDecimal</li>
     * <li>Arrays are mapped to a Collection</li>
     * <li>Objects are mapped to a map so you can use json(Part)Equals or a Map matcher</li>
     * </ul>
     *
     *
     *
     */
    public MATCHER matches(final Matcher<?> matcher) {
        return matcher(ctx -> ctx.matches(matcher));
    }

    /**
     * Fails if selected JSON is not true.
     */
    public MATCHER isTrue() {
        return isEqualTo(true);
    }

    /**
     * Fails if selected JSON is not false.
     */
    public MATCHER isFalse() {
        return isEqualTo(false);
    }
}
