package net.javacrumbs.jsonunit.core.internal;


import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.internal.ArrayComparison.NodeWithIndex;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static net.javacrumbs.jsonunit.core.internal.JsonUtils.valueToNode;
import static org.assertj.core.api.Assertions.assertThat;

public class ArrayComparisonTest {

    @Test
    public void shouldMatchSameArrays() {
        List<Node> expected = nodes(1, 2, 3);
        List<Node> actual = nodes(1, 2, 3);

        ArrayComparison comparison = compare(expected, actual);

        assertThat(comparison.getExtraValues()).isEmpty();
        assertThat(comparison.getMissingValues()).isEmpty();
        assertThat(comparison.getMatchedValues()).extracting(NodeWithIndex::getIndex).containsExactly(0, 1, 2);
        assertThat(comparison.isInCorrectOrder()).isTrue();
    }


    @Test
    public void shouldMatchDifferentOrder() {
        List<Node> expected = nodes(1, 2, 3);
        List<Node> actual = nodes(3, 1, 2);

        ArrayComparison comparison = compare(expected, actual);

        assertThat(comparison.getExtraValues()).isEmpty();
        assertThat(comparison.getMissingValues()).isEmpty();
        assertThat(comparison.getMatchedValues()).extracting(NodeWithIndex::getIndex).containsExactly(2, 0, 1);
        assertThat(comparison.isInCorrectOrder()).isFalse();
    }

    @Test
    public void shouldMatchShorterArray() {
        List<Node> expected = nodes(1, 2, 3, 4);
        List<Node> actual = nodes(4, 3, 2);

        ArrayComparison comparison = compare(expected, actual);

        assertThat(comparison.getExtraValues()).isEmpty();
        assertThat(comparison.getMissingValues()).usingRecursiveFieldByFieldElementComparator().containsExactly(nodeWithIndex(1, 0));
        assertThat(comparison.getMatchedValues()).extracting(NodeWithIndex::getIndex).containsExactly(3, 2, 1);
        assertThat(comparison.isInCorrectOrder()).isFalse();
    }

    @Test
    public void shouldMatchLongerArray() {
        List<Node> expected = nodes(1, 2, 3);
        List<Node> actual = nodes(4, 3, 2, 1);

        ArrayComparison comparison = compare(expected, actual);

        assertThat(comparison.getExtraValues()).usingRecursiveFieldByFieldElementComparator().containsExactly(nodeWithIndex(4, 0));
        assertThat(comparison.getMissingValues()).isEmpty();
        assertThat(comparison.getMatchedValues()).usingRecursiveFieldByFieldElementComparator().containsExactly(null, nodeWithIndex(3, 2), nodeWithIndex(2, 1), nodeWithIndex(1, 0));
        assertThat(comparison.isInCorrectOrder()).isFalse();
    }

    @Test
    public void shouldMatchWithMultipleMatches() {
        List<Node> expected = nodes(1, 1, 2, 2);
        List<Node> actual = nodes(2, 2, 1, 2);

        ArrayComparison comparison = compare(expected, actual);

        assertThat(comparison.getExtraValues()).usingRecursiveFieldByFieldElementComparator().containsExactly(nodeWithIndex(2, 3));
        assertThat(comparison.getMissingValues()).usingRecursiveFieldByFieldElementComparator().containsExactly(nodeWithIndex(1, 1));
        assertThat(comparison.getMatchedValues()).usingRecursiveFieldByFieldElementComparator().containsExactly(nodeWithIndex(2, 2), nodeWithIndex(2, 3), nodeWithIndex(1, 0), null);
        assertThat(comparison.isInCorrectOrder()).isFalse();
    }

    private NodeWithIndex nodeWithIndex(int value, int index) {
        return new NodeWithIndex(valueToNode(value), index);
    }

    private ArrayComparison compare(List<Node> expected, List<Node> actual) {
        ArrayComparison comparison = new ArrayComparison(expected, actual, Path.create(""), Configuration.empty());
        comparison.compareArraysIgnoringOrder();
        return comparison;
    }


    private static List<Node> nodes(Integer... values) {
        return Arrays.stream(values).map(JsonUtils::valueToNode).collect(Collectors.toList());
    }

}