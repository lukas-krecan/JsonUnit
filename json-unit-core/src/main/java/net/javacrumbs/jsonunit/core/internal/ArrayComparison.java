/**
 * Copyright 2009-2017 the original author or authors.
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
import net.javacrumbs.jsonunit.core.Option;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.javacrumbs.jsonunit.core.Configuration.dummyDifferenceListener;
import static net.javacrumbs.jsonunit.core.internal.JsonUnitLogger.NULL_LOGGER;

class ArrayComparison {
    private final int compareFrom;
    private final List<Node> actualElements;
    private final List<NodeWithIndex> extraValues;
    private final List<NodeWithIndex> missingValues;
    private final Path path;
    private final Configuration configuration;
    private final NodeWithIndex[] matchedValues;

    private ArrayComparison(int compareFrom, List<Node> actualElements, List<NodeWithIndex> extraValues, List<NodeWithIndex> missingValues, NodeWithIndex[] matchedValues, Path path, Configuration configuration) {
        this.compareFrom = compareFrom;
        this.actualElements = actualElements;
        this.extraValues = extraValues;
        this.missingValues = missingValues;
        this.path = path;
        this.configuration = configuration;
        this.matchedValues = matchedValues;
    }

    ArrayComparison(List<Node> expectedElements, List<Node> actualElements, Path path, Configuration configuration) {
        this(0, actualElements, new ArrayList<>(), copy(addIndex(expectedElements)), new NodeWithIndex[actualElements.size()], path, configuration);
    }

    private static List<NodeWithIndex> addIndex(List<Node> expectedElements) {
        List<NodeWithIndex> result = new ArrayList<>(expectedElements.size());
        for (int i = 0; i < expectedElements.size(); i++) {
            result.add(new NodeWithIndex(expectedElements.get(i), i));
        }
        return result;
    }


    ArrayComparison copy(int compareFrom) {
        return new ArrayComparison(compareFrom, actualElements, copy(extraValues), copy(missingValues), copy(matchedValues), path, configuration);
    }

    private NodeWithIndex[] copy(NodeWithIndex[] values) {
        return values.clone();
    }

    private static ArrayList<NodeWithIndex> copy(List<NodeWithIndex> values) {
        return new ArrayList<>(values);
    }

    ArrayComparison compareArraysIgnoringOrder() {
        for (int i = compareFrom; i < actualElements.size(); i++) {
            Node actual = actualElements.get(i);
            List<Integer> matches = indexOf(missingValues, actual);
            if (matches.size() == 1) {
                markMatch(i, matches.get(0));
            } else if (matches.size() > 0) {
                // we have more matches, since comparison does not have to be transitive ([1, 2] == [2] == [2, 3]), we have to check all the possibilities
                for (int match : matches) {
                    ArrayComparison copy = copy(i + 1);
                    copy.removeMissing(match);
                    copy.compareArraysIgnoringOrder();
                    if (copy.isMatching()) {
                        return copy;
                    }
                }
                // no combination matching, let's report the first difference
                markMatch(i, matches.get(0));
            } else {
                addExtra(new NodeWithIndex(actual, i));
            }
        }
        return this;
    }

    private void markMatch(int actualIndex, int matchIndex) {
        matchedValues[actualIndex] = missingValues.get(matchIndex);
        removeMissing(matchIndex);
    }

    private void removeMissing(int index) {
        missingValues.remove(index);
    }

    private void addExtra(NodeWithIndex actual) {
        extraValues.add(actual);
    }

    /**
     * Finds element in the expected elements.
     */
    private List<Integer> indexOf(List<NodeWithIndex> expectedElements, Node actual) {
        List<Integer> result = new ArrayList<>();
        int i = 0;
        for (NodeWithIndex expected : expectedElements) {
            Diff diff = new Diff(expected.getNode(), actual, Path.create("", path.toElement(i).getFullPath()), configuration.withDifferenceListener(dummyDifferenceListener()), NULL_LOGGER, NULL_LOGGER);
            if (diff.similar()) {
                result.add(i);
            }
            i++;
        }
        return result;
    }

    private boolean isMatching() {
        return missingValues.isEmpty() && (extraValues.isEmpty() || !configuration.getOptions().contains(Option.IGNORING_EXTRA_ARRAY_ITEMS));
    }

    List<NodeWithIndex> getMissingValues() {
        return missingValues;
    }

    List<NodeWithIndex> getExtraValues() {
        return extraValues;
    }

    List<NodeWithIndex> getMatchedValues() {
        return Arrays.asList(matchedValues);
    }

    boolean isInCorrectOrder() {
        for (int i = 0; i < matchedValues.length; i++) {
            if (matchedValues[i] == null || matchedValues[i].getIndex() != i) {
                return false;
            }
        }
        return true;
    }

    static class NodeWithIndex {
        private final Node node;

        private final int index;

        NodeWithIndex(Node node, int index) {
            this.node = node;
            this.index = index;
        }

        public Node getNode() {
            return node;
        }

        public int getIndex() {
            return index;
        }

        @Override
        public String toString() {
            return node.toString();
        }
    }

}