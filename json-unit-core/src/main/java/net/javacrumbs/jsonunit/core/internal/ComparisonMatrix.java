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
package net.javacrumbs.jsonunit.core.internal;

import static java.lang.Math.min;
import static java.util.Collections.unmodifiableList;
import static net.javacrumbs.jsonunit.core.Configuration.dummyDifferenceListener;
import static net.javacrumbs.jsonunit.core.Option.FAIL_FAST;
import static net.javacrumbs.jsonunit.core.internal.Diff.DEFAULT_DIFFERENCE_STRING;
import static net.javacrumbs.jsonunit.core.internal.JsonUnitLogger.NULL_LOGGER;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import net.javacrumbs.jsonunit.core.Configuration;
import org.jspecify.annotations.Nullable;

/**
 * Stores possible matches between actual and expected array elements and resolves them to a one-to-one matching.
 *
 * <p>Rows are actual elements, columns are expected elements. A value in {@link #equalElements} means that the row's
 * actual element is similar to the corresponding expected element under the current comparison options.
 */
class ComparisonMatrix {
    /**
     * equalElements[actualIndex] contains expected indexes that can be matched by actualIndex.
     */
    private final List<List<Integer>> equalElements;

    /**
     * First actual index that still has to be considered in this matrix branch.
     */
    private final int compareFrom;

    /**
     * matches[expectedIndex] contains the actual index selected for that expected element.
     */
    private final @Nullable Integer[] matches;

    /**
     * Actual indexes that could not be matched to any expected element.
     */
    private final List<Integer> extra;

    /**
     * Actual indexes already consumed by {@link #recordMatch(int, int)}.
     */
    private final BitSet alreadyMatched;

    // just for debugging
    private final List<Node> expectedElements;
    private final List<Node> actualElements;

    private ComparisonMatrix(
            List<List<Integer>> equalElements,
            int compareFrom,
            @Nullable Integer[] matches,
            List<Integer> extra,
            BitSet alreadyMatched,
            List<Node> expectedElements,
            List<Node> actualElements) {
        this.equalElements = equalElements;
        this.compareFrom = compareFrom;
        this.matches = matches;
        this.extra = extra;
        this.alreadyMatched = alreadyMatched;
        this.expectedElements = expectedElements;
        this.actualElements = actualElements;
    }

    ComparisonMatrix(List<Node> expectedElements, List<Node> actualElements, Path path, Configuration configuration) {
        this(
                generateEqualElements(expectedElements, actualElements, path, configuration),
                0,
                new Integer[expectedElements.size()],
                new ArrayList<>(),
                new BitSet(),
                expectedElements,
                actualElements);
    }

    private static List<List<Integer>> generateEqualElements(
            List<Node> expectedElements, List<Node> actualElements, Path path, Configuration configuration) {
        List<List<Integer>> equalElements = new ArrayList<>(actualElements.size());

        // Compare every actual element to every expected element before resolving any pairings.
        for (int i = 0; i < actualElements.size(); i++) {
            Node actual = actualElements.get(i);
            ArrayList<Integer> actualIsEqualTo = new ArrayList<>(expectedElements.size());

            for (int j = 0; j < expectedElements.size(); j++) {
                Node expected = expectedElements.get(j);
                boolean similar = isSimilar(path, configuration, expected, actual, i);
                if (similar) {
                    actualIsEqualTo.add(j);
                }
            }

            equalElements.add(unmodifiableList(actualIsEqualTo));
        }
        return equalElements;
    }

    private static boolean isSimilar(Path path, Configuration configuration, Node expected, Node actual, int i) {
        Diff diff = new Diff(
                expected,
                actual,
                Path.create("", path.toElement(i).getFullPath()),
                configuration.withDifferenceListener(dummyDifferenceListener()).withOptions(FAIL_FAST),
                NULL_LOGGER,
                NULL_LOGGER,
                DEFAULT_DIFFERENCE_STRING);

        return diff.similar();
    }

    ComparisonMatrix compare() {
        doSimpleMatching();

        for (int i = compareFrom; i < equalElements.size(); i++) {
            if (!alreadyMatched.get(i)) {
                List<Integer> matches = getEqualValues(i);
                if (matches.size() == 1) {
                    recordMatch(i, matches.get(0));
                } else if (!matches.isEmpty()) {
                    // Similarity is not necessarily transitive: for example [1, 2] == [2] == [2, 3], but [1, 2] may
                    // not equal [2, 3]. Try every candidate until a complete matching is found.
                    for (int match : matches) {
                        ComparisonMatrix copy = copy(i + 1);
                        copy.recordMatch(i, match);
                        copy = copy.compare();
                        if (copy.isMatching()) {
                            return copy;
                        }
                    }
                    // no combination matching, let's report the first difference
                    recordMatch(i, matches.get(0));
                } else {
                    addExtra(i);
                }
            }
        }
        return this;
    }

    /**
     * Collapses deterministic parts of the matrix before recursive matching.
     *
     * <p>The recursive algorithm is expensive for arrays with many repeated values, for example [1,1,1,1,1,1] vs
     * [8,1,1,1,1,1]. When several actual elements have exactly the same candidate expected elements, we can often
     * match some of them immediately without changing whether a complete matching exists.
     */
    private void doSimpleMatching() {
        for (int i = 0; i < equalElements.size(); i++) {
            if (!alreadyMatched.get(i)) {
                List<Integer> equalTo = equalElements.get(i);
                if (!equalTo.isEmpty()) {
                    List<Integer> equivalentElements = getEquivalentElements(equalTo);

                    // The group has exactly as many actual elements as expected candidates, so none of those candidates
                    // can be needed outside the group.
                    if (equalTo.size() == equivalentElements.size()) {
                        for (int j = 0; j < equivalentElements.size(); j++) {
                            recordMatch(equivalentElements.get(j), equalTo.get(j));
                        }
                    } else if (equivalentElements.size() > 1 && equalTo.size() > 1) {
                        List<Integer> equalToUsedOnlyInEquivalentElements =
                                getEqualToUsedOnlyInEquivalentElements(equalTo, equivalentElements);
                        // Only consume expected candidates that no other actual element can use. Shared candidates have
                        // to stay available for the recursive search.
                        for (int j = 0;
                                j < min(equivalentElements.size(), equalToUsedOnlyInEquivalentElements.size());
                                j++) {
                            recordMatch(equivalentElements.get(j), equalToUsedOnlyInEquivalentElements.get(j));
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns candidates from {@code equalTo} that are not used by actual elements outside {@code equivalentElements}.
     *
     * <p>Those candidates are safe to greedily assign to the equivalent group. Any candidate also used outside the group
     * must be preserved for the recursive search because it might be the only way to match another actual element.
     */
    private List<Integer> getEqualToUsedOnlyInEquivalentElements(
            List<Integer> equalTo, List<Integer> equivalentElements) {
        List<Integer> result = new ArrayList<>(equalTo);
        for (int i = 0; i < equalElements.size(); i++) {
            if (!alreadyMatched.get(i)) {
                if (!equivalentElements.contains(i)) {
                    result.removeAll(equalElements.get(i));
                }
            }
        }
        return result;
    }

    /**
     * Finds unmatched actual elements with the same candidate expected indexes as the current actual element.
     */
    private List<Integer> getEquivalentElements(List<Integer> equalTo) {
        List<Integer> equivalentElements = new ArrayList<>();
        for (int i = 0; i < equalElements.size(); i++) {
            if (!alreadyMatched.get(i)) {
                if (equalTo.equals(equalElements.get(i))) {
                    equivalentElements.add(i);
                }
            }
        }
        return equivalentElements;
    }

    private void addExtra(int index) {
        extra.add(index);
    }

    private List<Integer> getEqualValues(int actualIndex) {
        return equalElements.get(actualIndex);
    }

    private ComparisonMatrix copy(int compareFrom) {
        return new ComparisonMatrix(
                new ArrayList<>(equalElements),
                compareFrom,
                matches.clone(),
                new ArrayList<>(extra),
                (BitSet) alreadyMatched.clone(),
                expectedElements,
                actualElements);
    }

    private void recordMatch(int actualIndex, int expectedIndex) {
        matches[expectedIndex] = actualIndex;
        // Once an expected element has been consumed, remove it from every unresolved actual row.
        for (int i = 0; i < equalElements.size(); i++) {
            if (!alreadyMatched.get(i)) {
                equalElements.set(
                        i,
                        equalElements.get(i).stream()
                                .filter(n -> n != expectedIndex)
                                .toList());
            }
        }
        alreadyMatched.set(actualIndex);
    }

    private boolean isMatching() {
        return extra.isEmpty() && getMissing().isEmpty();
    }

    List<Integer> getMissing() {
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < matches.length; i++) {
            if (matches[i] == null) {
                result.add(i);
            }
        }
        return result;
    }

    List<Integer> getExtra() {
        return extra;
    }
}
