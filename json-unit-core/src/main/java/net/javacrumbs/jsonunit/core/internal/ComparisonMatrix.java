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
import java.util.List;

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

    // just for debugging
    private final List<Node> expectedElements;
    private final List<Node> actualElements;

    private ComparisonMatrix(List<List<Integer>> equalElements, int compareFrom, Integer[] matches, List<Integer> extra, List<Node> expectedElements, List<Node> actualElements) {
        this.equalElements = equalElements;
        this.compareFrom = compareFrom;
        this.matches = matches;
        this.extra = extra;
        this.expectedElements = expectedElements;
        this.actualElements = actualElements;
    }

    ComparisonMatrix(List<Node> expectedElements, List<Node> actualElements, Path path, Configuration configuration) {
        this(generateEqualElements(expectedElements, actualElements, path, configuration), 0, new Integer[expectedElements.size()], new ArrayList<>(), expectedElements, actualElements);
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

            equalElements.add(actualIsEqualTo);
        }
        //System.out.println(actualElements + " x " + expectedElements + " -> " + equalElements);
        return equalElements;
    }

    ComparisonMatrix compare() {
        int actualElementsSize = equalElements.size();
        for (int i = compareFrom; i < actualElementsSize; i++) {
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
        return this;
    }

    private void addExtra(int index) {
        extra.add(index);
    }

    private List<Integer> getEqualValues(int actualIndex) {
        return equalElements.get(actualIndex);
    }

    ComparisonMatrix copy(int compareFrom) {
        return new ComparisonMatrix(deepCopy(equalElements), compareFrom, matches.clone(), new ArrayList<>(extra), expectedElements, actualElements);
    }

    private List<List<Integer>> deepCopy(List<List<Integer>> list) {
        return list.stream().map(ArrayList::new).collect(toList());
    }

    private void recordMatch(int actualIndex, int expectedIndex) {
        matches[expectedIndex] = actualIndex;
        // remove all matches of expectedIndex
        equalElements.forEach(l -> l.removeIf(i -> i.equals(expectedIndex)));
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
