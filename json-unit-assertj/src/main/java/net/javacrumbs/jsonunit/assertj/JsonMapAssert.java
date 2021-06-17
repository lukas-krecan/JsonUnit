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
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.internal.Maps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
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

/*
 * Can not extend AbstractMapAssert due to https://github.com/lukas-krecan/JsonUnit/issues/379
 */
public final class JsonMapAssert extends AbstractAssert<JsonMapAssert, Map<String, Object>> {
    private final Configuration configuration;
    private final Path path;

    private final Maps maps = Maps.instance();

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

    /**
     * Verifies that all the actual map entries satisfy the given {@code entryRequirements} .
     * <p>
     * Example:
     * <pre><code class='java'> Map&lt;TolkienCharacter, Ring&gt; elvesRingBearers = new HashMap&lt;&gt;();
     * elvesRingBearers.put(galadriel, nenya);
     * elvesRingBearers.put(gandalf, narya);
     * elvesRingBearers.put(elrond, vilya);
     *
     * // this assertion succeeds:
     * assertThat(elvesRingBearers).allSatisfy((character, ring) -&gt; {
     *   assertThat(character.getRace()).isIn(ELF, MAIA);
     *   assertThat(ring).isIn(nenya, narya, vilya);
     * });
     *
     * // this assertion fails as Gandalf is a maia and not an elf:
     * assertThat(elvesRingBearers).allSatisfy((character, ring) -&gt; {
     *   assertThat(character.getRace()).isEqualTo(ELF);
     *   assertThat(ring).isIn(nenya, narya, vilya);
     * });</code></pre>
     * <p>
     * If the actual map is empty, this assertion succeeds as there is nothing to check.
     *
     * @param entryRequirements the given requirements that each entry must satisfy.
     * @return {@code this} assertion object.
     * @throws NullPointerException if the given entryRequirements {@link BiConsumer} is {@code null}.
     * @throws AssertionError       if the actual map is {@code null}.
     * @throws AssertionError       if one or more entries don't satisfy the given requirements.
     */
    public JsonMapAssert allSatisfy(BiConsumer<String, Object> entryRequirements) {
        maps.assertAllSatisfy(info, actual, entryRequirements);
        return this;
    }

    /**
     * Verifies that at least one map entry satisfies the given {@code entryRequirements} .
     * <p>
     * Example:
     * <pre><code class='java'> Map&lt;TolkienCharacter, Ring&gt; elvesRingBearers = new HashMap&lt;&gt;();
     * elvesRingBearers.put(galadriel, nenya);
     * elvesRingBearers.put(gandalf, narya);
     * elvesRingBearers.put(elrond, vilya);
     *
     * // this assertion succeeds as gandalf is a maia wearing narya:
     * assertThat(elvesRingBearers).anySatisfy((character, ring) -&gt; {
     *   assertThat(character.getRace()).isEqualTo(MAIA);
     *   assertThat(ring).isEqualTo(narya);
     * });
     *
     * // this assertion fails, gandalf is a maia but he does not wear the One Ring:
     * assertThat(elvesRingBearers).anySatisfy((character, ring) -&gt; {
     *   assertThat(character.getRace()).isIn(MAIA, HOBBIT);
     *   assertThat(ring).isEqualTo(oneRing);
     * });</code></pre>
     *
     * @param entryRequirements the given requirements that at least one entry must satisfy.
     * @return {@code this} assertion object.
     * @throws NullPointerException if the given entryRequirements {@link BiConsumer} is {@code null}.
     * @throws AssertionError       if the actual map is {@code null}.
     * @throws AssertionError       if no entries satisfy the given requirements.
     */
    public JsonMapAssert anySatisfy(BiConsumer<String, Object> entryRequirements) {
        maps.assertAnySatisfy(info, actual, entryRequirements);
        return this;
    }

    /**
     * Verifies that no map entry satisfies the given {@code entryRequirements} .
     * <p>
     * Example:
     * <pre><code class='java'> Map&lt;TolkienCharacter, Ring&gt; elvesRingBearers = new HashMap&lt;&gt;();
     * elvesRingBearers.put(galadriel, nenya);
     * elvesRingBearers.put(gandalf, narya);
     * elvesRingBearers.put(elrond, vilya);
     *
     * // this assertion succeeds:
     * assertThat(elvesRingBearers).noneSatisfy((character, ring) -&gt; {
     *   assertThat(character.getRace()).isIn(HOBBIT, DWARF);M
     *   assertThat(ring).isIn(nenya, narya, vilya);
     * });
     *
     * // this assertion fails as Gandalf is a maia.
     * assertThat(elvesRingBearers).noneSatisfy((character, ring) -&gt; {
     *   assertThat(character.getRace()).isEqualTo(MAIA);
     *   assertThat(ring).isIn(nenya, narya, vilya);
     * });</code></pre>
     * <p>
     * If the actual map is empty, this assertion succeeds as there is nothing to check.
     *
     * @param entryRequirements the given requirements that each entry must not satisfy.
     * @return {@code this} assertion object.
     * @throws NullPointerException if the given entryRequirements {@link BiConsumer} is {@code null}.
     * @throws AssertionError       if the actual map is {@code null}.
     * @throws AssertionError       if one or more entries satisfies the given requirements.
     */
    public JsonMapAssert noneSatisfy(BiConsumer<String, Object> entryRequirements) {
        maps.assertNoneSatisfy(info, actual, entryRequirements);
        return this;
    }

    /**
     * Verifies that the {@link Map} is {@code null} or empty.
     * <p>
     * Example:
     * <pre><code class='java'> // assertions will pass
     * Map&lt;Integer, String&gt; map = null;
     * assertThat(map).isNullOrEmpty();
     * assertThat(new HashMap()).isNullOrEmpty();
     *
     * // assertion will fail
     * Map&lt;String, String&gt; keyToValue = new HashMap();
     * keyToValue.put(&quot;key&quot;, &quot;value&quot;);
     * assertThat(keyToValue).isNullOrEmpty()</code></pre>
     *
     * @throws AssertionError if the {@link Map} is not {@code null} or not empty.
     */
    public void isNullOrEmpty() {
        maps.assertNullOrEmpty(info, actual);
    }

    /**
     * Verifies that the {@link Map} is empty.
     * <p>
     * Example:
     * <pre><code class='java'> // assertion will pass
     * assertThat(new HashMap()).isEmpty();
     *
     * // assertion will fail
     * Map&lt;String, String&gt; map = new HashMap();
     * map.put(&quot;key&quot;, &quot;value&quot;);
     * assertThat(map).isEmpty();</code></pre>
     *
     * @throws AssertionError if the {@link Map} of values is not empty.
     */
    public void isEmpty() {
        maps.assertEmpty(info, actual);
    }

    /**
     * Verifies that the {@link Map} is not empty.
     * <p>
     * Example:
     * <pre><code class='java'> Map&lt;String, String&gt; map = new HashMap();
     * map.put(&quot;key&quot;, &quot;value&quot;);
     *
     * // assertion will pass
     * assertThat(map).isNotEmpty();
     *
     * // assertion will fail
     * assertThat(new HashMap()).isNotEmpty();</code></pre>
     *
     * @return {@code this} assertion object.
     * @throws AssertionError if the {@link Map} is empty.
     */
    public JsonMapAssert isNotEmpty() {
        maps.assertNotEmpty(info, actual);
        return this;
    }

    /**
     * Verifies that the number of values in the {@link Map} is equal to the given one.
     * <p>
     * Example:
     * <pre><code class='java'>
     * Map&lt;String, String&gt; map = new HashMap();
     * map.put(&quot;key&quot;, &quot;value&quot;);
     *
     * // assertion will pass
     * assertThat(map).hasSize(1);
     *
     * // assertions will fail
     * assertThat(map).hasSize(0);
     * assertThat(map).hasSize(2);</code></pre>
     *
     * @param expected the expected number of values in the {@link Map}.
     * @return {@code this} assertion object.
     * @throws AssertionError if the number of values of the {@link Map} is not equal to the given one.
     */
    public JsonMapAssert hasSize(int expected) {
        maps.assertHasSize(info, actual, expected);
        return this;
    }

    /**
     * Verifies that the number of values in the {@link Map} is greater than the boundary.
     * <p>
     * Example:
     * <pre><code class='java'> Map&lt;String, String&gt; map = new HashMap();
     * map.put(&quot;key&quot;, &quot;value&quot;);
     * map.put(&quot;key2&quot;, &quot;value2&quot;);
     *
     * // assertion will pass
     * assertThat(map).hasSizeGreaterThan(1);
     *
     * // assertions will fail
     * assertThat(map).hasSizeGreaterThan(3);</code></pre>
     *
     * @param boundary the given value to compare the size of {@code actual} to.
     * @return {@code this} assertion object.
     * @throws AssertionError if the number of values of the {@link Map} is not greater than the boundary.
     */
    public JsonMapAssert hasSizeGreaterThan(int boundary) {
        maps.assertHasSizeGreaterThan(info, actual, boundary);
        return this;
    }

    /**
     * Verifies that the number of values in the {@link Map} is greater than or equal to the boundary.
     * <p>
     * Example:
     * <pre><code class='java'> Map&lt;String, String&gt; map = new HashMap();
     * map.put(&quot;key&quot;, &quot;value&quot;);
     * map.put(&quot;key2&quot;, &quot;value2&quot;);
     *
     * // assertions will pass
     * assertThat(map).hasSizeGreaterThanOrEqualTo(1)
     *                .hasSizeGreaterThanOrEqualTo(2);
     *
     * // assertions will fail
     * assertThat(map).hasSizeGreaterThanOrEqualTo(3);
     * assertThat(map).hasSizeGreaterThanOrEqualTo(5);</code></pre>
     *
     * @param boundary the given value to compare the size of {@code actual} to.
     * @return {@code this} assertion object.
     * @throws AssertionError if the number of values of the {@link Map} is not greater than or equal to the boundary.
     */
    public JsonMapAssert hasSizeGreaterThanOrEqualTo(int boundary) {
        maps.assertHasSizeGreaterThanOrEqualTo(info, actual, boundary);
        return this;
    }

    /**
     * Verifies that the number of values in the {@link Map} is less than the boundary.
     * <p>
     * Example:
     * <pre><code class='java'> Map&lt;String, String&gt; map = new HashMap();
     * map.put(&quot;key&quot;, &quot;value&quot;);
     * map.put(&quot;key2&quot;, &quot;value2&quot;);
     *
     * // assertion will pass
     * assertThat(map).hasSizeLessThan(3);
     *
     * // assertions will fail
     * assertThat(map).hasSizeLessThan(1);
     * assertThat(map).hasSizeLessThan(2);</code></pre>
     *
     * @param boundary the given value to compare the size of {@code actual} to.
     * @return {@code this} assertion object.
     * @throws AssertionError if the number of values of the {@link Map} is not less than the boundary.
     */
    public JsonMapAssert hasSizeLessThan(int boundary) {
        maps.assertHasSizeLessThan(info, actual, boundary);
        return this;
    }

    /**
     * Verifies that the number of values in the {@link Map} is less than or equal to the boundary.
     * <p>
     * Example:
     * <pre><code class='java'> Map&lt;String, String&gt; map = new HashMap();
     * map.put(&quot;key&quot;, &quot;value&quot;);
     * map.put(&quot;key2&quot;, &quot;value2&quot;);
     *
     * // assertions will pass
     * assertThat(map).hasSizeLessThanOrEqualTo(2)
     *                .hasSizeLessThanOrEqualTo(3);
     *
     * // assertions will fail
     * assertThat(map).hasSizeLessThanOrEqualTo(0);
     * assertThat(map).hasSizeLessThanOrEqualTo(1);</code></pre>
     *
     * @param boundary the given value to compare the size of {@code actual} to.
     * @return {@code this} assertion object.
     * @throws AssertionError if the number of values of the {@link Map} is not less than the boundary.
     */
    public JsonMapAssert hasSizeLessThanOrEqualTo(int boundary) {
        maps.assertHasSizeLessThanOrEqualTo(info, actual, boundary);
        return this;
    }

    /**
     * Verifies that the number of values in the {@link Map} is between the given boundaries (inclusive).
     * <p>
     * Example:
     * <pre><code class='java'> Map&lt;String, String&gt; map = new HashMap();
     * map.put(&quot;key&quot;, &quot;value&quot;);
     * map.put(&quot;key2&quot;, &quot;value2&quot;);
     *
     * // assertions will pass
     * assertThat(map).hasSizeBetween(1, 3)
     *                .hasSizeBetween(2, 2);
     *
     * // assertions will fail
     * assertThat(map).hasSizeBetween(3, 4);</code></pre>
     *
     * @param lowerBoundary  the lower boundary compared to which actual size should be greater than or equal to.
     * @param higherBoundary the higher boundary compared to which actual size should be less than or equal to.
     * @return {@code this} assertion object.
     * @throws AssertionError if the number of values of the {@link Map} is not between the boundaries.
     */
    public JsonMapAssert hasSizeBetween(int lowerBoundary, int higherBoundary) {
        maps.assertHasSizeBetween(info, actual, lowerBoundary, higherBoundary);
        return this;
    }

    /**
     * Verifies that the actual map has the same size as the given array.
     * <p>
     * Parameter is declared as Object to accept both Object[] and primitive arrays (e.g. int[]).
     * <p>
     * Example:
     * <pre><code class='java'> int[] oneTwoThree = {1, 2, 3};
     *
     * Map&lt;Ring, TolkienCharacter&gt; elvesRingBearers = new HashMap&lt;&gt;();
     * elvesRingBearers.put(nenya, galadriel);
     * elvesRingBearers.put(narya, gandalf);
     * elvesRingBearers.put(vilya, elrond);
     *
     * // assertion will pass
     * assertThat(elvesRingBearers).hasSameSizeAs(oneTwoThree);
     *
     * // assertions will fail
     * assertThat(elvesRingBearers).hasSameSizeAs(new int[] {1});
     * assertThat(keyToValue).hasSameSizeAs(new char[] {'a', 'b', 'c', 'd'});</code></pre>
     *
     * @param other the array to compare size with actual group.
     * @return {@code this} assertion object.
     * @throws AssertionError if the actual group is {@code null}.
     * @throws AssertionError if the array parameter is {@code null} or is not a true array.
     * @throws AssertionError if actual group and given array don't have the same size.
     */
    public JsonMapAssert hasSameSizeAs(Object other) {
        maps.assertHasSameSizeAs(info, actual, other);
        return this;
    }

    /**
     * Verifies that the actual map has the same size as the given {@link Iterable}.
     * <p>
     * Examples:
     * <pre><code class='java'> Map&lt;Ring, TolkienCharacter&gt; elvesRingBearers = new HashMap&lt;&gt;();
     * elvesRingBearers.put(nenya, galadriel);
     * elvesRingBearers.put(narya, gandalf);
     * elvesRingBearers.put(vilya, elrond);
     *
     * // assertion will pass
     * assertThat(elvesRingBearers).hasSameSizeAs(Array.asList(vilya, nenya, narya));
     *
     * // assertions will fail
     * assertThat(elvesRingBearers).hasSameSizeAs(Array.asList(1));
     * assertThat(keyToValue).hasSameSizeAs(Array.asList('a', 'b', 'c', 'd'));</code></pre>
     *
     * @param other the {@code Iterable} to compare size with actual group.
     * @return {@code this} assertion object.
     * @throws AssertionError if the actual map is {@code null}.
     * @throws AssertionError if the other {@code Iterable} is {@code null}.
     * @throws AssertionError if the actual map and the given {@code Iterable} don't have the same size
     */
    public JsonMapAssert hasSameSizeAs(Iterable<?> other) {
        maps.assertHasSameSizeAs(info, actual, other);
        return this;
    }

    /**
     * Verifies that the actual map has the same size as the given {@link Map}.
     * <p>
     * Examples:
     * <pre><code class='java'> import static com.google.common.collect.ImmutableMap.of;
     *
     * Map&lt;Ring, TolkienCharacter&gt; ringBearers = ImmutableMap.of(nenya, galadriel,
     *                                                           narya, gandalf,
     *                                                           vilya, elrond,
     *                                                           oneRing, frodo);
     *
     * // assertion succeeds:
     * assertThat(ringBearers).hasSameSizeAs(ImmutableMap.of(oneRing, frodo,
     *                                                       narya, gandalf,
     *                                                       nenya, galadriel,
     *                                                       vilya, elrond));
     *
     * // assertions fails:
     * assertThat(ringBearers).hasSameSizeAs(Collections.emptyMap());
     * assertThat(ringBearers).hasSameSizeAs(ImmutableMap.of(nenya, galadriel,
     *                                                       narya, gandalf,
     *                                                       vilya, elrond));</code></pre>
     *
     * @param other the {@code Map} to compare size with actual map
     * @return {@code this} assertion object
     * @throws NullPointerException if the other {@code Map} is {@code null}
     * @throws AssertionError       if the actual map is {@code null}
     * @throws AssertionError       if the actual map and the given {@code Map} don't have the same size
     */
    public JsonMapAssert hasSameSizeAs(Map<?, ?> other) {
        maps.assertHasSameSizeAs(info, actual, other);
        return this;
    }

    /**
     * Verifies that the actual map contains the given values.
     * <p>
     * Examples:
     * <pre><code class='java'> Map&lt;Ring, TolkienCharacter&gt; ringBearers = new HashMap&lt;&gt;();
     * ringBearers.put(nenya, galadriel);
     * ringBearers.put(narya, gandalf);
     * ringBearers.put(vilya, elrond);
     * ringBearers.put(oneRing, frodo);
     *
     * // assertion will pass
     * assertThat(ringBearers).containsValues(frodo, galadriel);
     *
     * // assertions will fail
     * assertThat(ringBearers).containsValues(sauron, aragorn);
     * assertThat(ringBearers).containsValues(sauron, frodo);</code></pre>
     *
     * @param values the values to look for in the actual map.
     * @return {@code this} assertions object
     * @throws AssertionError if the actual map is {@code null}.
     * @throws AssertionError if the actual map does not contain the given values.
     */
    @NotNull
    public JsonMapAssert containsValue(@Nullable Object expected) {
        if (expected instanceof Node) {
            if (!contains(expected)) {
                throwAssertionError(shouldContainValue(actual, expected));
            }
        } else {
            maps.assertContainsValue(info, actual, expected);
        }
        return this;
    }

    /**
     * Verifies that the actual map does not contain the given value.
     * <p>
     * Examples:
     * <pre><code class='java'> Map&lt;Ring, TolkienCharacter&gt; ringBearers = new HashMap&lt;&gt;();
     * ringBearers.put(nenya, galadriel);
     * ringBearers.put(narya, gandalf);
     * ringBearers.put(vilya, elrond);
     * ringBearers.put(oneRing, frodo);
     *
     * // assertion will pass
     * assertThat(ringBearers).doesNotContainValue(aragorn);
     *
     * // assertion will fail
     * assertThat(ringBearers).doesNotContainValue(frodo);</code></pre>
     *
     * @param value the value that should not be in actual map.
     * @return {@code this} assertions object
     * @throws AssertionError if the actual map is {@code null}.
     * @throws AssertionError if the actual map contains the given value.
     */
    @NotNull
    public JsonMapAssert doesNotContainValue(@Nullable Object expected) {
        if (expected instanceof Node) {
            if (contains(expected)) {
                throwAssertionError(shouldNotContainValue(actual, expected));
            }
        } else {
            maps.assertDoesNotContainValue(info, actual, expected);
        }
        return this;
    }

    @NotNull
    @Deprecated
    public JsonMapAssert isEqualToIgnoringGivenFields(@Nullable Object other, @NotNull String... propertiesOrFieldsToIgnore) {
        return compare(other, configuration.whenIgnoringPaths(propertiesOrFieldsToIgnore));
    }

    /**
     * Same as {@link #containsOnly(Map.Entry[])} but handles the conversion of {@link Map#entrySet()} to array.
     * <p>
     * Verifies that the actual map contains only the given entries and nothing else, in any order.
     * <p>
     * Examples:
     * <pre><code class='java'> // newLinkedHashMap builds a Map with iteration order corresponding to the insertion order
     * Map&lt;Ring, TolkienCharacter&gt; ringBearers = newLinkedHashMap(entry(oneRing, frodo),
     *                                                            entry(nenya, galadriel),
     *                                                            entry(narya, gandalf));
     *
     * // assertion will pass
     * assertThat(ringBearers).containsExactlyInAnyOrderEntriesOf(newLinkedHashMap(entry(oneRing, frodo),
     *                                                                             entry(nenya, galadriel),
     *                                                                             entry(narya, gandalf)));
     *
     * // assertion will pass although actual and expected order differ
     * assertThat(ringBearers).containsExactlyInAnyOrderEntriesOf(newLinkedHashMap(entry(nenya, galadriel),
     *                                                                             entry(narya, gandalf),
     *                                                                             entry(oneRing, frodo)));
     * // assertion will fail as actual does not contain all expected entries
     * assertThat(ringBearers).containsExactlyInAnyOrderEntriesOf(newLinkedHashMap(entry(oneRing, frodo),
     *                                                                             entry(nenya, galadriel),
     *                                                                             entry(vilya, elrond)));
     * // assertion will fail as actual and expected have different sizes
     * assertThat(ringBearers).containsExactlyInAnyOrderEntriesOf(newLinkedHashMap(entry(oneRing, frodo),
     *                                                                             entry(nenya, galadriel),
     *                                                                             entry(narya, gandalf),
     *                                                                             entry(narya, gandalf)));</code></pre>
     *
     * @param map the given {@link Map} with the expected entries to be found in actual.
     * @return {@code this} assertions object
     * @throws NullPointerException     if the given map is {@code null}.
     * @throws AssertionError           if the actual map is {@code null}.
     * @throws IllegalArgumentException if the given map is empty.
     * @throws AssertionError           if the actual map does not contain the entries of the given map, i.e the actual map contains
     *                                  some or none of the entries of the given map, or the actual map contains more entries than the entries of
     *                                  the given map.
     */
    public JsonMapAssert containsExactlyInAnyOrderEntriesOf(Map<String, Object> map) {
        return containsOnly(toEntries(map));
    }

    /**
     * Verifies that the actual map contains the given entry.
     * <p>
     * Examples:
     * <pre><code class='java'> Map&lt;Ring, TolkienCharacter&gt; ringBearers = new HashMap&lt;&gt;();
     * ringBearers.put(nenya, galadriel);
     * ringBearers.put(narya, gandalf);
     * ringBearers.put(vilya, elrond);
     * ringBearers.put(oneRing, frodo);
     *
     * // assertions will pass
     * assertThat(ringBearers).containsEntry(oneRing, frodo).containsEntry(nenya, galadriel);
     *
     * // assertion will fail
     * assertThat(ringBearers).containsEntry(oneRing, sauron);</code></pre>
     *
     * @param key   the given key to check.
     * @param value the given value to check.
     * @return {@code this} assertion object.
     * @throws NullPointerException     if the given argument is {@code null}.
     * @throws IllegalArgumentException if the given argument is an empty array.
     * @throws NullPointerException     if any of the entries in the given array is {@code null}.
     * @throws AssertionError           if the actual map is {@code null}.
     * @throws AssertionError           if the actual map does not contain the given entries.
     */
    public JsonMapAssert containsEntry(String key, Object value) {
        return contains(array(entry(key, value)));
    }

    /**
     * Verifies that the actual map contains at least one of the given entries.
     * <p>
     * Examples:
     * <pre><code class='java'> Map&lt;Ring, TolkienCharacter&gt; ringBearers = new HashMap&lt;&gt;();
     * ringBearers.put(nenya, galadriel);
     * ringBearers.put(narya, gandalf);
     * ringBearers.put(vilya, elrond);
     * ringBearers.put(oneRing, frodo);
     *
     * // assertions will pass
     * assertThat(ringBearers).containsAnyOf(entry(oneRing, frodo), entry(oneRing, sauron));
     * assertThat(emptyMap).containsAnyOf();
     *
     * // assertion will fail
     * assertThat(ringBearers).containsAnyOf(entry(oneRing, gandalf), entry(oneRing, aragorn));</code></pre>
     *
     * @param entries the given entries.
     * @return {@code this} assertion object.
     * @throws NullPointerException     if the given argument is {@code null}.
     * @throws IllegalArgumentException if the given argument is an empty array.
     * @throws NullPointerException     if any of the entries in the given array is {@code null}.
     * @throws AssertionError           if the actual map is {@code null}.
     * @throws AssertionError           if the actual map does not contain any of the given entries.
     */
    @SafeVarargs
    public final JsonMapAssert containsAnyOf(Entry<? extends String, ?>... entries) {
        boolean anyMatch = stream(entries).anyMatch(this::doesContainEntry);
        if (!anyMatch) {
            throwAssertionError(shouldContainAnyOf(actual, entries));
        }
        return this;
    }

    /**
     * Verifies that the actual map contains all entries of the given map, in any order.
     * <p>
     * Examples:
     * <pre><code class='java'> Map&lt;Ring, TolkienCharacter&gt; ringBearers = new HashMap&lt;&gt;();
     * ringBearers.put(nenya, galadriel);
     * ringBearers.put(narya, gandalf);
     * ringBearers.put(vilya, elrond);
     * ringBearers.put(oneRing, frodo);
     *
     * Map&lt;Ring, TolkienCharacter&gt; elvesRingBearers = new HashMap&lt;&gt;();
     * elvesRingBearers.put(nenya, galadriel);
     * elvesRingBearers.put(narya, gandalf);
     * elvesRingBearers.put(vilya, elrond);
     *
     * // assertions succeed
     * assertThat(ringBearers).containsAllEntriesOf(elvesRingBearers);
     * assertThat(ringBearers).containsAllEntriesOf(emptyMap);
     *
     * // assertion fails
     * assertThat(elvesRingBearers).containsAllEntriesOf(ringBearers);</code></pre>
     *
     * @param other the map with the given entries.
     * @return {@code this} assertion object.
     * @throws NullPointerException if the given argument is {@code null}.
     * @throws NullPointerException if any of the entries in the given map is {@code null}.
     * @throws AssertionError       if the actual map is {@code null}.
     * @throws AssertionError       if the actual map does not contain the given entries.
     */
    public JsonMapAssert containsAllEntriesOf(Map<? extends String, ?> other) {
        return contains(toEntries(other));
    }

    /**
     * Verifies that the actual map contains only the given entries and nothing else, in any order.
     * <p>
     * The verification tries to honor the key comparison semantic of the underlying map implementation.
     * The map under test has to be cloned to identify unexpected elements, but depending on the map implementation
     * this may not always be possible. In case it is not possible, a regular map is used and the key comparison strategy
     * may not be the same as the map under test.
     * <p>
     * Examples :
     * <pre><code class='java'> Map&lt;Ring, TolkienCharacter&gt; ringBearers = new HashMap&lt;&gt;();
     * ringBearers.put(nenya, galadriel);
     * ringBearers.put(narya, gandalf);
     * ringBearers.put(vilya, elrond);
     * ringBearers.put(oneRing, frodo);
     *
     * // assertions will pass
     * assertThat(ringBearers).containsOnly(entry(oneRing, frodo),
     *                                      entry(nenya, galadriel),
     *                                      entry(narya, gandalf),
     *                                      entry(vilya, elrond));
     *
     * assertThat(Collections.emptyMap()).containsOnly();
     *
     * // assertion will fail
     * assertThat(ringBearers).containsOnly(entry(oneRing, frodo), entry(nenya, galadriel));</code></pre>
     *
     * @param entries the entries that should be in the actual map.
     * @return {@code this} assertions object
     * @throws AssertionError           if the actual map is {@code null}.
     * @throws NullPointerException     if the given argument is {@code null}.
     * @throws IllegalArgumentException if the given argument is an empty array.
     * @throws AssertionError           if the actual map does not contain the given entries, i.e. the actual map contains some or
     *                                  none of the given entries, or the actual map contains more entries than the given ones.
     */
    @SafeVarargs
    public final JsonMapAssert containsOnly(Entry<? extends String, ?>... expected) {
        Map<? extends String, ?> expectedAsMap = stream(expected).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        return isEqualTo(wrapDeserializedObject(expectedAsMap));
    }

    @NotNull
    private List<Entry<? extends String, ?>> entriesNotFoundInMap(Entry<? extends String, ?>[] expected) {
        return stream(expected).filter(entry -> !doesContainEntry(entry)).collect(toList());
    }

    /**
     * Verifies that the actual map contains the given entries, in any order.
     * <p>
     * This assertion succeeds if both actual map and given entries are empty.
     * <p>
     * Examples:
     * <pre><code class='java'> Map&lt;Ring, TolkienCharacter&gt; ringBearers = new HashMap&lt;&gt;();
     * ringBearers.put(nenya, galadriel);
     * ringBearers.put(narya, gandalf);
     * ringBearers.put(vilya, elrond);
     * ringBearers.put(oneRing, frodo);
     *
     * // assertions will pass
     * assertThat(ringBearers).contains(entry(oneRing, frodo), entry(nenya, galadriel));
     * assertThat(emptyMap).contains();
     *
     * // assertions will fail
     * assertThat(ringBearers).contains(entry(oneRing, sauron));
     * assertThat(ringBearers).contains(entry(oneRing, sauron), entry(oneRing, aragorn));
     * assertThat(ringBearers).contains(entry(narya, gandalf), entry(oneRing, sauron));</code></pre>
     *
     * @param entries the given entries.
     * @return {@code this} assertion object.
     * @throws NullPointerException if the given argument is {@code null}.
     * @throws NullPointerException if any of the entries in the given array is {@code null}.
     * @throws AssertionError       if the actual map is {@code null}.
     * @throws AssertionError       if the actual map does not contain the given entries.
     */
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

    /**
     * Verifies that the actual map contains the given values.
     * <p>
     * Examples:
     * <pre><code class='java'> Map&lt;Ring, TolkienCharacter&gt; ringBearers = new HashMap&lt;&gt;();
     * ringBearers.put(nenya, galadriel);
     * ringBearers.put(narya, gandalf);
     * ringBearers.put(vilya, elrond);
     * ringBearers.put(oneRing, frodo);
     *
     * // assertion will pass
     * assertThat(ringBearers).containsValues(frodo, galadriel);
     *
     * // assertions will fail
     * assertThat(ringBearers).containsValues(sauron, aragorn);
     * assertThat(ringBearers).containsValues(sauron, frodo);</code></pre>
     *
     * @param values the values to look for in the actual map.
     * @return {@code this} assertions object
     * @throws AssertionError if the actual map is {@code null}.
     * @throws AssertionError if the actual map does not contain the given values.
     */
    public JsonMapAssert containsValues(Object... values) {
        stream(values).forEach(this::containsValue);
        return this;
    }

    /**
     * Verifies that the actual map contains the given key.
     * <p>
     * Examples:
     * <pre><code class='java'> Map&lt;Ring, TolkienCharacter&gt; ringBearers = new HashMap&lt;&gt;();
     * ringBearers.put(nenya, galadriel);
     * ringBearers.put(narya, gandalf);
     * ringBearers.put(vilya, elrond);
     *
     * // assertion will pass
     * assertThat(ringBearers).containsKey(vilya);
     *
     * // assertion will fail
     * assertThat(ringBearers).containsKey(oneRing);</code></pre>
     *
     * @param key the given key
     * @return {@code this} assertions object
     * @throws AssertionError if the actual map is {@code null}.
     * @throws AssertionError if the actual map does not contain the given key.
     */
    public JsonMapAssert containsKey(String key) {
        return containsKeys(key);
    }

    /**
     * Verifies that the actual map contains the given keys.
     * <p>
     * Examples:
     * <pre><code class='java'> Map&lt;Ring, TolkienCharacter&gt; ringBearers = new HashMap&lt;&gt;();
     * ringBearers.put(nenya, galadriel);
     * ringBearers.put(narya, gandalf);
     * ringBearers.put(oneRing, frodo);
     *
     * // assertions will pass
     * assertThat(ringBearers).containsKeys(nenya, oneRing);
     *
     * // assertions will fail
     * assertThat(ringBearers).containsKeys(vilya);
     * assertThat(ringBearers).containsKeys(vilya, oneRing);</code></pre>
     *
     * @param keys the given keys
     * @return {@code this} assertions object
     * @throws AssertionError           if the actual map is {@code null}.
     * @throws AssertionError           if the actual map does not contain the given key.
     * @throws IllegalArgumentException if the given argument is an empty array.
     */
    public final JsonMapAssert containsKeys(String... keys) {
        maps.assertContainsKeys(info, actual, keys);
        return this;
    }

    /**
     * Verifies that the actual map does not contain the given key.
     * <p>
     * Examples:
     * <pre><code class='java'> Map&lt;Ring, TolkienCharacter&gt; elvesRingBearers = new HashMap&lt;&gt;();
     * elvesRingBearers.put(nenya, galadriel);
     * elvesRingBearers.put(narya, gandalf);
     * elvesRingBearers.put(vilya, elrond);
     *
     * // assertion will pass
     * assertThat(elvesRingBearers).doesNotContainKey(oneRing);
     *
     * // assertion will fail
     * assertThat(elvesRingBearers).doesNotContainKey(vilya);</code></pre>
     *
     * @param key the given key
     * @return {@code this} assertions object
     * @throws AssertionError if the actual map is {@code null}.
     * @throws AssertionError if the actual map contains the given key.
     */
    public JsonMapAssert doesNotContainKey(String key) {
        return doesNotContainKeys(key);
    }

    /**
     * Verifies that the actual map does not contain any of the given keys.
     * <p>
     * Examples:
     * <pre><code class='java'> Map&lt;Ring, TolkienCharacter&gt; elvesRingBearers = new HashMap&lt;&gt;();
     * elvesRingBearers.put(nenya, galadriel);
     * elvesRingBearers.put(narya, gandalf);
     * elvesRingBearers.put(vilya, elrond);
     *
     * // assertion will pass
     * assertThat(elvesRingBearers).doesNotContainKeys(oneRing, someManRing);
     *
     * // assertions will fail
     * assertThat(elvesRingBearers).doesNotContainKeys(vilya, nenya);
     * assertThat(elvesRingBearers).doesNotContainKeys(vilya, oneRing);</code></pre>
     *
     * @param keys the given keys
     * @return {@code this} assertions object
     * @throws AssertionError if the actual map is {@code null}.
     * @throws AssertionError if the actual map contains the given key.
     */
    public final JsonMapAssert doesNotContainKeys(String... keys) {
        maps.assertDoesNotContainKeys(info, actual, keys);
        return this;
    }

    /**
     * Verifies that the actual map contains only the given keys and nothing else, in any order.
     * <p>
     * The verification tries to honor the key comparison semantic of the underlying map implementation.
     * The map under test has to be cloned to identify unexpected elements, but depending on the map implementation
     * this may not always be possible. In case it is not possible, a regular map is used and the key comparison strategy
     * may not be the same as the map under test.
     * <p>
     * Examples :
     * <pre><code class='java'> Map&lt;Ring, TolkienCharacter&gt; ringBearers = new HashMap&lt;&gt;();
     * ringBearers.put(nenya, galadriel);
     * ringBearers.put(narya, gandalf);
     * ringBearers.put(vilya, elrond);
     * ringBearers.put(oneRing, frodo);
     *
     * // assertion will pass
     * assertThat(ringBearers).containsOnlyKeys(oneRing, nenya, narya, vilya);
     *
     * // assertion will fail
     * assertThat(ringBearers).containsOnlyKeys(oneRing, nenya);</code></pre>
     *
     * @param keys the given keys that should be in the actual map.
     * @return {@code this} assertions object
     * @throws AssertionError           if the actual map is {@code null}.
     * @throws AssertionError           if the actual map does not contain the given keys, i.e. the actual map contains some or none
     *                                  of the given keys, or the actual map contains more entries than the given ones.
     * @throws IllegalArgumentException if the given argument is an empty array.
     */
    public final JsonMapAssert containsOnlyKeys(String... keys) {
        maps.assertContainsOnlyKeys(info, actual, keys);
        return this;
    }

    /**
     * Verifies that the actual map contains only the given keys and nothing else, in any order.
     * <p>
     * The verification tries to honor the key comparison semantic of the underlying map implementation.
     * The map under test has to be cloned to identify unexpected elements, but depending on the map implementation
     * this may not always be possible. In case it is not possible, a regular map is used and the key comparison strategy
     * may not be the same as the map under test.
     * <p>
     * Examples :
     * <pre><code class='java'> Map&lt;Ring, TolkienCharacter&gt; ringBearers = new HashMap&lt;&gt;();
     * ringBearers.put(nenya, galadriel);
     * ringBearers.put(narya, gandalf);
     * ringBearers.put(vilya, elrond);
     * ringBearers.put(oneRing, frodo);
     *
     * // assertion will pass
     * assertThat(ringBearers).containsOnlyKeys(Arrays.asList(oneRing, nenya, narya, vilya));
     *
     * // assertions will fail
     * assertThat(ringBearers).containsOnlyKeys(Arrays.asList(oneRing, nenya));
     * assertThat(ringBearers).containsOnlyKeys(Arrays.asList(oneRing, nenya, narya, vilya, nibelungRing));</code></pre>
     *
     * @param keys the given keys that should be in the actual map.
     * @return {@code this} assertions object
     * @throws AssertionError           if the actual map is {@code null} or empty.
     * @throws AssertionError           if the actual map does not contain the given keys, i.e. the actual map contains some or none
     *                                  of the given keys, or the actual map's keys contains keys not in the given ones.
     * @throws IllegalArgumentException if the given argument is an empty array.
     */
    public JsonMapAssert containsOnlyKeys(Iterable<String> keys) {
        maps.assertContainsOnlyKeys(info, actual, keys);
        return this;
    }


    @SuppressWarnings("unchecked")
    private Entry<? extends String, ?>[] toEntries(Map<? extends String, ?> map) {
        return map.entrySet().toArray(new Entry[0]);
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
