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
import java.util.function.BiConsumer;
import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.Option;
import net.javacrumbs.jsonunit.core.internal.Path;
import net.javacrumbs.jsonunit.core.internal.matchers.InternalMatcher;
import net.javacrumbs.jsonunit.core.listener.DifferenceListener;
import org.hamcrest.Matcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Common superclass for request and response matcher
 * @param <ME> Type of the factory class
 * @param <MATCHER> Type of the matcher
 */
abstract class AbstractSpringMatchers<ME, MATCHER> {
    final Path path;
    final Configuration configuration;

    AbstractSpringMatchers(@NotNull Path path, @NotNull Configuration configuration) {
        this.path = path;
        this.configuration = configuration;
    }

    @NotNull
    abstract MATCHER matcher(@NotNull BiConsumer<Object, InternalMatcher> matcher);

    @NotNull
    abstract ME matchers(@NotNull Path path, @NotNull Configuration configuration);

    /**
     * Creates a matcher object that only compares given node.
     * The path is denoted by JSON path, for example.
     * <p/>
     * <code>
     * this.mockMvc.perform(get("/sample").accept(MediaType.APPLICATION_JSON)).andExpect(json().node("root.test[0]").isEqualTo("1"));
     * </code>
     *
     * @param newPath
     * @return object comparing only node given by path.
     */
    @NotNull
    public ME node(String newPath) {
        return matchers(path.copy(newPath), configuration);
    }

    /**
     * Sets the placeholder that can be used to ignore values.
     * The default value is ${json-unit.ignore}
     */
    @NotNull
    public ME ignoring(@NotNull String ignorePlaceholder) {
        return matchers(path, configuration.withIgnorePlaceholder(ignorePlaceholder));
    }

    /**
     * Sets the tolerance for floating number comparison. If set to null, requires exact match of the values.
     * For example, if set to 0.01, ignores all differences lower than 0.01, so 1 and 0.9999 are considered equal.
     */
    @NotNull
    public ME withTolerance(double tolerance) {
        return withTolerance(BigDecimal.valueOf(tolerance));
    }

    /**
     * Adds a matcher to be used in ${json-unit.matches:matcherName} macro.
     */
    @NotNull
    public ME withMatcher(@NotNull String matcherName, @NotNull Matcher<?> matcher) {
        return matchers(path, configuration.withMatcher(matcherName, matcher));
    }

    /**
     * Sets the tolerance for floating number comparison. If set to null, requires exact match of the values.
     * For example, if set to 0.01, ignores all differences lower than 0.01, so 1 and 0.9999 are considered equal.
     */
    @NotNull
    public ME withTolerance(@Nullable BigDecimal tolerance) {
        return matchers(path, configuration.withTolerance(tolerance));
    }

    @NotNull
    public ME withDifferenceListener(@NotNull DifferenceListener differenceListener) {
        return matchers(path, configuration.withDifferenceListener(differenceListener));
    }

    /**
     * Sets options changing comparison behavior. For more
     * details see {@link net.javacrumbs.jsonunit.core.Option}
     *
     * @see net.javacrumbs.jsonunit.core.Option
     */
    @NotNull
    public ME when(@NotNull Option firstOption, @NotNull Option... otherOptions) {
        return matchers(path, configuration.withOptions(firstOption, otherOptions));
    }

    /**
     * Compares JSON for equality. The expected object is converted to JSON
     * before comparison. Ignores order of sibling nodes and whitespaces.
     * <p/>
     * Please note that if you pass a String, it's parsed as JSON which can lead to an
     * unexpected behavior. If you pass in "1" it is parsed as a JSON containing
     * integer 1. If you compare it with a string it fails due to a different type.
     * If you want to pass in real string you have to quote it "\"1\"" or use
     * {@link #isStringEqualTo(String)}.
     * <p/>
     * If the string parameter is not a valid JSON, it is quoted automatically.
     *
     * @param expected
     * @return {@code this} object.
     * @see #isStringEqualTo(String)
     */
    @NotNull
    public MATCHER isEqualTo(@Nullable Object expected) {
        return matcher((actual, ctx) -> ctx.isEqualTo(expected));
    }

    /**
     * Fails if the selected JSON is not a String or is not present or the value
     * is not equal to expected value.
     */
    @NotNull
    public MATCHER isStringEqualTo(@Nullable final String expected) {
        return matcher((actual, ctx) -> ctx.isStringEqualTo(expected));
    }

    /**
     * Fails if compared documents are equal. The expected object is converted to JSON
     * before comparison. Ignores order of sibling nodes and whitespaces.
     */
    @NotNull
    public MATCHER isNotEqualTo(@Nullable Object expected) {
        return matcher((actual, ctx) -> ctx.isNotEqualTo(expected));
    }

    /**
     * Fails if the node exists.
     */
    @NotNull
    public MATCHER isAbsent() {
        return matcher((actual, ctx) -> ctx.isAbsent());
    }

    /**
     * Fails if the node is missing.
     */
    @NotNull
    public MATCHER isPresent() {
        return matcher((actual, ctx) -> ctx.isPresent());
    }

    /**
     * Fails if the selected JSON is not an Array or is not present.
     */
    @NotNull
    public MATCHER isArray() {
        return matcher((actual, ctx) -> ctx.isArray());
    }

    /**
     * Fails if the selected JSON is not an Object or is not present.
     */
    @NotNull
    public MATCHER isObject() {
        return matcher((actual, ctx) -> ctx.isObject());
    }

    /**
     * Fails if the selected JSON is not a String or is not present.
     */
    @NotNull
    public MATCHER isString() {
        return matcher((actual, ctx) -> ctx.isString());
    }

    /**
     * Fails if selected JSON is not null.
     */
    @NotNull
    public MATCHER isNull() {
        return matcher((actual, ctx) -> ctx.isNull());
    }

    /**
     * Fails if selected JSON is  null.
     */
    @NotNull
    public MATCHER isNotNull() {
        return matcher((actual, ctx) -> ctx.isNotNull());
    }

    /**
     * Matches the node using Hamcrest matcher.
     * <p/>
     * <ul>
     * <li>Numbers are mapped to BigDecimal</li>
     * <li>Arrays are mapped to a Collection</li>
     * <li>Objects are mapped to a map so you can use json(Part)Equals or a Map matcher</li>
     * </ul>
     *
     * @param matcher
     * @return
     */
    @NotNull
    public MATCHER matches(@NotNull final Matcher<?> matcher) {
        return matcher((actual, ctx) -> ctx.matches(matcher));
    }

    /**
     * Fails if selected JSON is not true.
     */
    @NotNull
    public MATCHER isTrue() {
        return isEqualTo(true);
    }

    /**
     * Fails if selected JSON is not false.
     */
    @NotNull
    public MATCHER isFalse() {
        return isEqualTo(false);
    }
}
