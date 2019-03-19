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

import net.javacrumbs.jsonunit.core.Configuration;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import static java.lang.Math.min;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;
import static net.javacrumbs.jsonunit.core.Configuration.dummyDifferenceListener;
import static net.javacrumbs.jsonunit.core.internal.Diff.DEFAULT_DIFFERENCE_STRING;
import static net.javacrumbs.jsonunit.core.internal.JsonUnitLogger.NULL_LOGGER;

/**
 * Stores comparison result when comparing two arrays.
 */
class ComparisonMatrix {
    private final List<List<Integer>> equalElements; //equalElements[actualIndex] = [expectedElementIndex1, expectedElementIndex2, ...]
    private final int compareFrom;
    private final Integer[] matches; //matches[expectedElementIndex] = actualElementIndex
    private final List<Integer> extra;
    private final BitSet alreadyMatched;

    // just for debugging
    private final List<Node> expectedElements;
    private final List<Node> actualElements;

    private ComparisonMatrix(List<List<Integer>> equalElements, int compareFrom, Integer[] matches, List<Integer> extra, BitSet alreadyMatched, List<Node> expectedElements, List<Node> actualElements) {
        this.equalElements = equalElements;
        this.compareFrom = compareFrom;
        this.matches = matches;
        this.extra = extra;
        this.alreadyMatched = alreadyMatched;
        this.expectedElements = expectedElements;
        this.actualElements = actualElements;
    }

    ComparisonMatrix(List<Node> expectedElements, List<Node> actualElements, Path path, Configuration configuration) {
        this(generateEqualElements(expectedElements, actualElements, path, configuration), 0, new Integer[expectedElements.size()], new ArrayList<>(), new BitSet(), expectedElements, actualElements);
    }

    private static List<List<Integer>> generateEqualElements(List<Node> expectedElements, List<Node> actualElements, Path path, Configuration configuration) {
        List<List<Integer>> equalElements = new ArrayList<>(actualElements.size());

        // Compare all elements
        for (int i = 0; i < actualElements.size(); i++) {
            Node actual = actualElements.get(i);
            ArrayList<Integer> actualIsEqualTo = new ArrayList<>(expectedElements.size());

            for (int j = 0; j < expectedElements.size(); j++) {
                Node expected = expectedElements.get(j);
                Diff diff = new Diff(expected, actual, Path.create("", path.toElement(i).getFullPath()), configuration.withDifferenceListener(dummyDifferenceListener()), NULL_LOGGER, NULL_LOGGER, DEFAULT_DIFFERENCE_STRING);
                if (diff.similar()) {
                    actualIsEqualTo.add(j);
                }
            }

            equalElements.add(unmodifiableList(actualIsEqualTo));
        }
        //System.out.println(actualElements + " x " + expectedElements + " -> " + equalElements);
        return equalElements;
    }

    ComparisonMatrix compare() {
        doSimpleMatching();

        for (int i = compareFrom; i < equalElements.size(); i++) {
            if (!alreadyMatched.get(i)) {
                List<Integer> matches = getEqualValues(i);
                if (matches.size() == 1) {
                    recordMatch(i, matches.get(0));
                } else if (matches.size() > 0) {
                    // we have more matches, since comparison does not have to be transitive ([1, 2] == [2] == [2, 3]), we have to check all the possibilities
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
     * Algorithm above is not effective when we are comparing arrays with lot of matching values like [1,1,1,1,1,1] vs [8,1,1,1,1,1].
     * We can make it faster if we collapse simple matching values.
     */
    private void doSimpleMatching() {
        for (int i = 0; i < equalElements.size(); i++) {
            if (!alreadyMatched.get(i)) {
                List<Integer> equalTo = equalElements.get(i);
                if (equalTo.size() > 0) {
                    List<Integer> equivalentElements = getEquivalentElements(equalTo);

                    // We have the same set matching as is equivalent, we can remove them all
                    if (equalTo.size() == equivalentElements.size()) {
                        for (int j = 0; j < equivalentElements.size(); j++) {
                            recordMatch(equivalentElements.get(j), equalTo.get(j));
                        }
                    } else if (equivalentElements.size() > 1 && equalTo.size() > 1) {
                        List<Integer> equalToUsedOnlyInEquivalentElements = getEqualToUsedOnlyInEquivalentElements(equalTo, equivalentElements);
                        for (int j = 0; j < min(equivalentElements.size(), equalToUsedOnlyInEquivalentElements.size()); j++) {
                            recordMatch(equivalentElements.get(j), equalTo.get(j));
                        }
                    }
                }
            }
        }
    }

    /**
     * If there are more equivalent elements, we can match those that are not used anywhere else
     * we iterate over actual elements that are not in equivalentElements and from equalTo remove those
     * that are used outside equivalent elements
     */
    private List<Integer> getEqualToUsedOnlyInEquivalentElements(List<Integer> equalTo, List<Integer> equivalentElements) {
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

    private List<Integer> getEquivalentElements(List<Integer> equalTo) {
        List<Integer> equivalentElments = new ArrayList<>();
        for (int i = 0; i < equalElements.size(); i++) {
            if (!alreadyMatched.get(i)) {
                if (equalTo.equals(equalElements.get(i))) {
                    equivalentElments.add(i);
                }
            }
        }
        return equivalentElments;
    }


    private void addExtra(int index) {
        extra.add(index);
    }

    private List<Integer> getEqualValues(int actualIndex) {
        return equalElements.get(actualIndex);
    }

    ComparisonMatrix copy(int compareFrom) {
        return new ComparisonMatrix(new ArrayList<>(equalElements), compareFrom, matches.clone(), new ArrayList<>(extra), (BitSet) alreadyMatched.clone(), expectedElements, actualElements);
    }

    private void recordMatch(int actualIndex, int expectedIndex) {
        matches[expectedIndex] = actualIndex;
        // remove all matches of expectedIndex
        for (int i = 0; i < equalElements.size(); i++) {
            if (!alreadyMatched.get(i)) {
                equalElements.set(i, equalElements.get(i).stream().filter(n -> n != expectedIndex).collect(toList()));
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
