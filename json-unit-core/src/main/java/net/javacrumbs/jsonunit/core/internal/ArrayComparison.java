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
package net.javacrumbs.jsonunit.core.internal;

import net.javacrumbs.jsonunit.core.Configuration;

import java.util.List;
import java.util.stream.Collectors;

class ArrayComparison {
    private final ComparisonMatrix comparisonMatrix;
    private final List<Node> actualElements;
    private final List<Node> expectedElements;

    ArrayComparison(List<Node> expectedElements, List<Node> actualElements, Path path, Configuration configuration) {
        comparisonMatrix = new ComparisonMatrix(expectedElements, actualElements, path, configuration);
        this.actualElements = actualElements;
        this.expectedElements = expectedElements;
    }

    ComparisonResult compareArraysIgnoringOrder() {
        return new ComparisonResult(comparisonMatrix.compare(), expectedElements, actualElements);
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

    static class ComparisonResult {
        private final List<NodeWithIndex> extraValues;
        private final List<NodeWithIndex> missingValues;

        private ComparisonResult(ComparisonMatrix result, List<Node> expectedElements, List<Node> actualElements) {
            extraValues = result.getExtra().stream().map(i -> new NodeWithIndex(actualElements.get(i), i)).collect(Collectors.toList());
            missingValues = result.getMissing().stream().map(i -> new NodeWithIndex(expectedElements.get(i), i)).collect(Collectors.toList());
        }

        public List<NodeWithIndex> getExtraValues() {
            return extraValues;
        }

        public List<NodeWithIndex> getMissingValues() {
            return missingValues;
        }
    }

}