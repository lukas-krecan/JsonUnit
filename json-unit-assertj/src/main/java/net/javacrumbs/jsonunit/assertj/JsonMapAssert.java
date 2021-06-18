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
package net.javacrumbs.jsonunit.assertj;

import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.internal.Diff;
import net.javacrumbs.jsonunit.core.internal.Node;
import net.javacrumbs.jsonunit.core.internal.Path;
import org.assertj.core.api.AbstractMapAssert;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;
import static java.util.Objects.deepEquals;
import static java.util.stream.Collectors.toList;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.wrapDeserializedObject;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.error.ShouldContain.shouldContain;
import static org.assertj.core.error.ShouldContainAnyOf.shouldContainAnyOf;
import static org.assertj.core.error.ShouldContainValue.shouldContainValue;
import static org.assertj.core.error.ShouldNotContainValue.shouldNotContainValue;
import static org.assertj.core.util.Arrays.array;

public class JsonMapAssert extends AbstractMapAssert<JsonMapAssert, Map<String, Object>, String, Object> {
    private final Configuration configuration;
    private final Path path;

    JsonMapAssert(Map<String, Object> actual, Path path, Configuration configuration) {
        super(actual, JsonMapAssert.class);
        this.path = path;
        this.configuration = configuration;
        usingComparator(new JsonComparator(configuration, path.asPrefix(), true));
    }

    @Override
    @NotNull
    public JsonMapAssert isEqualTo(@Nullable Object expected) {
        return compare(expected, configuration);
    }

    @Override
    @NotNull
    public JsonMapAssert containsValue(@Nullable Object expected) {
        if (expected instanceof Node) {
            if (!contains(expected)) {
                throwAssertionError(shouldContainValue(actual, expected));
            }
            return this;
        } else {
            return super.containsValue(expected);
        }
    }

    @Override
    @NotNull
    public JsonMapAssert doesNotContainValue(@Nullable Object expected) {
        if (expected instanceof Node) {
            if (contains(expected)) {
                throwAssertionError(shouldNotContainValue(actual, expected));
            }
            return this;
        } else {
            return super.doesNotContainValue(expected);
        }
    }

    @Override
    @NotNull
    @Deprecated
    public JsonMapAssert isEqualToIgnoringGivenFields(@Nullable Object other, @NotNull String... propertiesOrFieldsToIgnore) {
        return compare(other, configuration.whenIgnoringPaths(propertiesOrFieldsToIgnore));
    }

    @Override
    @NotNull
    @Deprecated
    public JsonMapAssert isEqualToComparingOnlyGivenFields(@Nullable Object other, @NotNull String... propertiesOrFieldsUsedInComparison) {
        throw unsupportedOperation();
    }

    @Override
    @NotNull
    @Deprecated
    public JsonMapAssert isEqualToIgnoringNullFields(@Nullable Object other) {
        throw unsupportedOperation();
    }

    @Override
    @NotNull
    @Deprecated
    public JsonMapAssert isEqualToComparingFieldByField(@Nullable Object other) {
        throw unsupportedOperation();
    }

    @Override
    @NotNull
    @Deprecated
    public JsonMapAssert isEqualToComparingFieldByFieldRecursively(@Nullable Object other) {
        throw unsupportedOperation();
    }

    @Override
    public JsonMapAssert containsEntry(String key, Object value) {
        return contains(array(entry(key, value)));
    }

    @SafeVarargs
    @Override
    public final JsonMapAssert containsAnyOf(Entry<? extends String, ?>... entries) {
        boolean anyMatch = stream(entries).anyMatch(this::doesContainEntry);
        if (!anyMatch) {
            throwAssertionError(shouldContainAnyOf(actual, entries));
        }
        return this;
    }

    @Override
    public JsonMapAssert containsAllEntriesOf(Map<? extends String, ?> other) {
        return contains(toEntries(other));
    }

    /**
     * This method does not support JsonUnit features. Prefer {@link #containsOnly(Entry[])}
     */
    @SafeVarargs
    @Override
    @Deprecated
    public final JsonMapAssert containsExactly(Entry<? extends String, ?>... entries) {
        return super.containsExactly(entries);
    }

    /**
     * This method does not support JsonUnit features. Prefer {@link #containsOnly(Entry[])}
     */
    @Override
    @Deprecated
    public JsonMapAssert containsExactlyEntriesOf(Map<? extends String, ?> map) {
        return super.containsExactlyEntriesOf(map);
    }

    @SafeVarargs
    @Override
    public final JsonMapAssert containsOnly(Entry<? extends String, ?>... expected) {
        Map<? extends String, ?> expectedAsMap = stream(expected).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        return isEqualTo(wrapDeserializedObject(expectedAsMap));
    }

    @NotNull
    private List<Entry<? extends String, ?>> entriesNotFoundInMap(Entry<? extends String, ?>[] expected) {
        return stream(expected).filter(entry -> !doesContainEntry(entry)).collect(toList());
    }

    @Override
    @SafeVarargs
    public final JsonMapAssert contains(Entry<? extends String, ?>... expected) {
        List<Entry<? extends String, ?>> notFound = entriesNotFoundInMap(expected);
        if (!notFound.isEmpty()) {
            throwAssertionError(shouldContain(actual, expected, notFound));
        }
        return this;
    }

    private boolean doesContainEntry(Entry<? extends String, ?> entry) {
        String key = entry.getKey();
        if (!actual.containsKey(key)) {
            return false;
        }
        Object actualValue = actual.get(key);
        if (entry.getValue() instanceof Node) {
            Node value = (Node) entry.getValue();
            return isSimilar(actualValue, value);
        } else {
            return deepEquals(actualValue, entry.getValue());
        }
    }

    @Override
    public JsonMapAssert containsValues(Object... values) {
        stream(values).forEach(this::containsValue);
        return this;
    }

    @SuppressWarnings("unchecked")
    private Entry<? extends String, ?>[] toEntries(Map<? extends String, ?> map) {
        return map.entrySet().toArray(new Entry[0]);
    }

    /**
     * Does not work. Use {@link #containsKey(Object)} instead.
     * https://github.com/lukas-krecan/JsonUnit/issues/324
     */
    @Override
    @Deprecated
    public JsonMapAssert hasFieldOrProperty(String name) {
        return super.hasFieldOrProperty(name);
    }

    /**
     * Does not work. Use {@link #contains(Entry[])} instead.
     * https://github.com/lukas-krecan/JsonUnit/issues/324
     */
    @Override
    @Deprecated
    public JsonMapAssert hasFieldOrPropertyWithValue(String name, Object value) {
        return super.hasFieldOrPropertyWithValue(name, value);
    }

    /**
     * Does not work. https://github.com/lukas-krecan/JsonUnit/issues/324
     */
    @Override
    @Deprecated
    public JsonMapAssert hasAllNullFieldsOrProperties() {
        return super.hasAllNullFieldsOrProperties();
    }

    /**
     * Does not work. https://github.com/lukas-krecan/JsonUnit/issues/324
     */
    @Override
    @Deprecated
    public JsonMapAssert hasAllNullFieldsOrPropertiesExcept(String... propertiesOrFieldsToIgnore) {
        return super.hasAllNullFieldsOrPropertiesExcept(propertiesOrFieldsToIgnore);
    }

    /**
     * Does not work. https://github.com/lukas-krecan/JsonUnit/issues/324
     */
    @Deprecated
    @Override
    public JsonMapAssert hasNoNullFieldsOrProperties() {
        return super.hasNoNullFieldsOrProperties();
    }

    /**
     * Does not work. https://github.com/lukas-krecan/JsonUnit/issues/324
     */
    @Override
    @Deprecated
    public JsonMapAssert hasNoNullFieldsOrPropertiesExcept(String... propertiesOrFieldsToIgnore) {
        return super.hasNoNullFieldsOrPropertiesExcept(propertiesOrFieldsToIgnore);
    }

    @NotNull
    private UnsupportedOperationException unsupportedOperation() {
        return new UnsupportedOperationException("Operation not supported for JSON documents");
    }

    @NotNull
    private JsonMapAssert compare(@Nullable Object other, @NotNull Configuration configuration) {
        describedAs(null);
        Diff diff = Diff.create(other, actual, "fullJson", path, configuration);
        diff.failIfDifferent();
        return this;
    }

    private boolean contains(Object expected) {
        return actual.entrySet().stream().anyMatch(entry -> isSimilar(entry.getValue(), expected));
    }

    private boolean isSimilar(Object actual, Object expected) {
        return Diff.create(expected, actual, "fullJson", path.asPrefix(), configuration).similar();
    }
}
