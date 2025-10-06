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

import static java.math.BigDecimal.valueOf;
import static java.util.Collections.singletonMap;
import static net.javacrumbs.jsonunit.core.Option.FAIL_FAST;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_ARRAY_ITEMS;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static net.javacrumbs.jsonunit.core.listener.Difference.Type.DIFFERENT;
import static net.javacrumbs.jsonunit.core.listener.Difference.Type.EXTRA;
import static net.javacrumbs.jsonunit.core.listener.Difference.Type.MISSING;
import static net.javacrumbs.jsonunit.core.util.ResourceUtils.resource;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.NumberComparator;
import net.javacrumbs.jsonunit.core.ParametrizedMatcher;
import net.javacrumbs.jsonunit.core.listener.Difference;
import net.javacrumbs.jsonunit.core.listener.DifferenceContext;
import net.javacrumbs.jsonunit.core.listener.DifferenceListener;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

public class DifferenceTest {
    private final RecordingDifferenceListener listener = new RecordingDifferenceListener();

    @Test
    void shouldSeeEmptyDiffNodes() {
        Diff diff = Diff.create("{}", "{}", "", "", commonConfig());
        diff.similar();
        assertThat(listener.getDifferenceList()).isEmpty();
    }

    @Test
    void shouldSeeRemovedNode() {
        Diff diff = Diff.create("{\"test\": \"1\"}", "{}", "", "", commonConfig());
        diff.similar();
        assertThat(listener.getDifferenceList()).hasSize(1);
        Difference difference = listener.getDifferenceList().get(0);
        assertThat(difference.getType()).isEqualTo(DifferenceImpl.Type.MISSING);
        assertThat(difference.getActualPath()).isEqualTo(null);
        assertThat(difference.getExpected()).isEqualTo("1");
        assertThat(difference.getActual()).isNull();
    }

    @Test
    void shouldSeeAddedNode() {
        Diff diff = Diff.create("{}", "{\"test\": \"1\"}", "", "", commonConfig());
        diff.similar();
        assertThat(listener.getDifferenceList()).hasSize(1);
        Difference difference = listener.getDifferenceList().get(0);
        assertThat(difference.getType()).isEqualTo(EXTRA);
        assertThat(difference.getActualPath()).isEqualTo("test");
        assertThat(difference.getActual()).isEqualTo("1");
        assertThat(difference.getExpected()).isNull();
    }

    @Test
    void shouldSeeEmptyForCheckAnyNode() {
        Diff diff = Diff.create("{\"test\": \"${json-unit.ignore}\"}", "{\"test\":\"1\"}", "", "", commonConfig());
        diff.similar();
        assertThat(listener.getDifferenceList()).isEmpty();
    }

    @Test
    void shouldSeeEmptyForCheckAnyBooleanNode() {
        Diff diff = Diff.create("{\"test\": \"${json-unit.any-boolean}\"}", "{\"test\": true}", "", "", commonConfig());
        diff.similar();
        assertThat(listener.getDifferenceList()).isEmpty();
    }

    @Test
    void shouldSeeEmptyForCheckAnyNumberNode() {
        Diff diff = Diff.create("{\"test\": \"${json-unit.any-number}\"}", "{\"test\": 11}", "", "", commonConfig());
        diff.similar();
        assertThat(listener.getDifferenceList()).isEmpty();
    }

    @Test
    void shouldSeeEmptyForCheckAnyStringNode() {
        Diff diff = Diff.create("{\"test\": \"${json-unit.any-string}\"}", "{\"test\": \"1\"}", "", "", commonConfig());
        diff.similar();
        assertThat(listener.getDifferenceList()).isEmpty();
    }

    @Test
    void shouldSeeChangedStringNode() {
        Diff diff = Diff.create("{\"test\": \"1\"}", "{\"test\": \"2\"}", "", "", commonConfig());
        diff.similar();
        assertThat(listener.getDifferenceList()).hasSize(1);
        Difference difference = listener.getDifferenceList().get(0);
        assertThat(difference.getType()).isEqualTo(DIFFERENT);
        assertThat(difference.getActualPath()).isEqualTo("test");
        assertThat(difference.getExpected()).isEqualTo("1");
        assertThat(difference.getActual()).isEqualTo("2");
    }

    @Test
    void shouldSeeChangedNumberNode() {
        Diff diff = Diff.create("{\"test\": 1}", "{\"test\": 2 }", "", "", commonConfig());
        diff.similar();
        assertThat(listener.getDifferenceList()).hasSize(1);
        Difference difference = listener.getDifferenceList().get(0);
        assertThat(difference.getType()).isEqualTo(DIFFERENT);
        assertThat(difference.getActualPath()).isEqualTo("test");
        assertThat(difference.getExpected()).isEqualTo(new BigDecimal(1));
        assertThat(difference.getActual()).isEqualTo(new BigDecimal(2));
    }

    @Test
    void shouldSeeChangedBooleanNode() {
        Diff diff = Diff.create("{\"test\": true}", "{\"test\": false}", "", "", commonConfig());
        diff.similar();
        assertThat(listener.getDifferenceList()).hasSize(1);
        Difference difference = listener.getDifferenceList().get(0);
        assertThat(difference.getType()).isEqualTo(DIFFERENT);
        assertThat(difference.getActualPath()).isEqualTo("test");
        assertThat(difference.getExpected()).isEqualTo(true);
        assertThat(difference.getActual()).isEqualTo(false);
    }

    @Test
    void shouldSeeChangedStructureNode() {
        Diff diff = Diff.create("{\"test\": \"1\"}", "{\"test\": false}", "", "", commonConfig());
        diff.similar();
        assertThat(listener.getDifferenceList()).hasSize(1);
        Difference difference = listener.getDifferenceList().get(0);
        assertThat(difference.getType()).isEqualTo(DIFFERENT);
        assertThat(difference.getActualPath()).isEqualTo("test");
        assertThat(difference.getExpected()).isEqualTo("1");
        assertThat(difference.getActual()).isEqualTo(false);
    }

    @Test
    void shouldSeeChangedArrayNode() {
        Diff diff = Diff.create("[1, 1]", "[1, 2]", "", "", commonConfig());
        diff.similar();
        assertThat(listener.getDifferenceList()).hasSize(1);
        Difference difference = listener.getDifferenceList().get(0);
        assertThat(difference.getType()).isEqualTo(DIFFERENT);
        assertThat(difference.getActualPath()).isEqualTo("[1]");
        assertThat(difference.getExpected()).isEqualTo(valueOf(1));
        assertThat(difference.getActual()).isEqualTo(valueOf(2));
    }

    @Test
    void shouldSeeRemovedArrayNode() {
        Diff diff = Diff.create("[1, 2]", "[1]", "", "", commonConfig());
        diff.similar();
        assertThat(listener.getDifferenceList()).hasSize(1);
        Difference difference = listener.getDifferenceList().get(0);
        assertThat(difference.getType()).isEqualTo(DifferenceImpl.Type.MISSING);
        assertThat(difference.getActualPath()).isNull();
        assertThat(difference.getExpectedPath()).isEqualTo("[1]");
        assertThat(difference.getExpected()).isEqualTo(valueOf(2));
        assertThat(difference.getActual()).isNull();
    }

    @Test
    void shouldSeeAddedArrayNode() {
        Diff diff = Diff.create("[1]", "[1, 2]", "", "", commonConfig());
        diff.similar();
        assertThat(listener.getDifferenceList()).hasSize(1);
        Difference difference = listener.getDifferenceList().get(0);
        assertThat(difference.getType()).isEqualTo(EXTRA);
        assertThat(difference.getActual()).isEqualTo(valueOf(2));
        assertThat(difference.getActualPath()).isEqualTo("[1]");
        assertThat(difference.getExpectedPath()).isNull();
        assertThat(difference.getExpected()).isNull();
    }

    @Test
    void shouldSeeObjectDiffNodes() {
        Diff diff = Diff.create(
                "{\"test\": { \"test1\": \"1\"}}", "{\"test\": { \"test1\": \"2\"} }", "", "", commonConfig());
        diff.similar();
        assertThat(listener.getDifferenceList()).hasSize(1);
        Difference difference = listener.getDifferenceList().get(0);
        assertThat(difference.getType()).isEqualTo(DIFFERENT);
        assertThat(difference.getActualPath()).isEqualTo("test.test1");
        assertThat(difference.getExpected()).isEqualTo("1");
        assertThat(difference.getActual()).isEqualTo("2");
    }

    @Test
    void shouldSeeNullNode() {
        Diff diff = Diff.create(null, null, "", "", commonConfig());
        diff.similar();
        assertThat(listener.getDifferenceList()).isEmpty();
    }

    @Test
    void shouldWorkWhenIgnoringArrayOrder() {
        Diff diff = Diff.create(
                "{\"test\": [[1,2],[2,3]]}",
                "{\"test\":[[4,2],[1,2]]}",
                "",
                "",
                commonConfig().when(IGNORING_ARRAY_ORDER));
        diff.similar();
        assertThat(listener.getDifferenceList()).hasSize(1);
        Difference difference = listener.getDifferenceList().get(0);
        assertThat(difference.getType()).isEqualTo(DIFFERENT);
        assertThat(difference.getActualPath()).isEqualTo("test[0][0]");
        assertThat(difference.getActual()).isEqualTo(valueOf(4));
        assertThat(difference.getExpectedPath()).isEqualTo("test[1][1]");
        assertThat(difference.getExpected()).isEqualTo(valueOf(3));
    }

    @Test
    void shouldSeeActualSource() {
        Diff diff = Diff.create("{\"test\": \"1\"}", "{}", "", "", commonConfig());
        diff.similar();
        assertThat(listener.getActualSource()).asString().isEqualTo("{}");
    }

    @Test
    void shouldSeeExpectedSource() {
        Diff diff = Diff.create("{\"test\": \"1\"}", "{}", "", "", commonConfig());
        diff.similar();
        assertThat(listener.getExpectedSource()).isEqualTo(singletonMap("test", "1"));
    }

    @Test
    void shouldMatchWithLineSeparatorCustomMatcher() {
        Configuration cfg = commonConfig().withMatcher("equalTo", new EqualsMatcher());
        Diff diff = Diff.create(
                "{\"key\": \"${json-unit.matches:equalTo}separated \\n line\"}",
                "{\"key\": \"separated \\n line\"}",
                "",
                "",
                cfg);
        assertThat(diff.similar()).isTrue();
        assertThat(listener.getDifferenceList()).isEmpty();
    }

    @Test
    void shouldBeAwareOfRootMissingElement() {
        Diff diff = Diff.create("foo", "{\"test\":-1}", "", "path.node", commonConfig());
        assertThat(diff.similar()).isFalse();

        assertThat(listener.getDifferenceList()).hasSize(1);
        Difference difference = listener.getDifferenceList().get(0);
        assertThat(difference.getType()).isEqualTo(MISSING);
        assertThat(difference.getExpectedPath()).isEqualTo("path.node");
        assertThat(difference.getActualPath()).isEqualTo(null);
        assertThat(difference.getExpected()).isEqualTo("foo");
        assertThat(difference.getActual()).isEqualTo(null);
    }

    @Test
    void shouldWorkWhenExtraArrayItemsAreNotAllowed() {
        Diff diff = Diff.create(
                "{\"test\": [2, 3]}",
                "{\"test\": [1, 2, 3]}",
                "",
                "",
                commonConfig().when(IGNORING_ARRAY_ORDER));
        assertThat(diff.similar()).isFalse();

        assertThat(listener.getDifferenceList()).hasSize(1);
        Difference difference = listener.getDifferenceList().get(0);
        assertThat(difference.getType()).isEqualTo(EXTRA);
        assertThat(difference.getExpectedPath()).isEqualTo(null);
        assertThat(difference.getActualPath()).isEqualTo("test[0]");
        assertThat(difference.getExpected()).isEqualTo(null);
        assertThat(difference.getActual()).isEqualTo(valueOf(1));
    }

    @Test
    void shouldWorkWhenExtraArrayItemsAreAllowed() {
        Diff diff = Diff.create(
                "{\"test\": [2, 3]}",
                "{\"test\": [1, 2, 3]}",
                "",
                "",
                commonConfig().when(IGNORING_ARRAY_ORDER, IGNORING_EXTRA_ARRAY_ITEMS));
        assertThat(diff.similar()).isTrue();
        assertThat(listener.getDifferenceList()).isEmpty();
    }

    @Test
    void shouldWorkWithDifferentKeys() {
        Diff diff = Diff.create("{\"a\": 1}", "{\"b\": 1}", "", "", commonConfig());
        assertThat(diff.similar()).isFalse();

        assertThat(listener.getDifferenceList()).hasSize(2);

        assertThat(listener.getDifferenceList().get(0).getType()).isEqualTo(MISSING);
        assertThat(listener.getDifferenceList().get(0).getExpectedPath()).isEqualTo("a");
        assertThat(listener.getDifferenceList().get(0).getActualPath()).isEqualTo(null);
        assertThat(listener.getDifferenceList().get(0).getExpected()).isEqualTo(valueOf(1));
        assertThat(listener.getDifferenceList().get(0).getActual()).isEqualTo(null);

        assertThat(listener.getDifferenceList().get(1).getType()).isEqualTo(EXTRA);
        assertThat(listener.getDifferenceList().get(1).getExpectedPath()).isEqualTo(null);
        assertThat(listener.getDifferenceList().get(1).getActualPath()).isEqualTo("b");
        assertThat(listener.getDifferenceList().get(1).getExpected()).isEqualTo(null);
        assertThat(listener.getDifferenceList().get(1).getActual()).isEqualTo(valueOf(1));
    }

    @Test
    @Timeout(1)
    void shouldRunDiffBeforeTimeout() {
        var actual = resource("big-json-with-common-keys-actual.json");
        var expected = resource("big-json-with-common-keys-expected.json");
        var cfg = commonConfig()
                .withNumberComparator(new NormalisedNumberComparator())
                .withOptions(IGNORING_ARRAY_ORDER, IGNORING_EXTRA_ARRAY_ITEMS, IGNORING_EXTRA_FIELDS, FAIL_FAST);
        Diff diff = Diff.create(expected, actual, "", "", cfg);
        diff.similar();
    }

    private Configuration commonConfig() {
        return Configuration.empty().withDifferenceListener(listener);
    }

    private static class RecordingDifferenceListener implements DifferenceListener {
        private final List<Difference> differenceList = new ArrayList<>();
        private @Nullable Object actualSource;
        private @Nullable Object expectedSource;

        @Override
        public void diff(Difference difference, DifferenceContext context) {
            differenceList.add(difference);
            actualSource = context.getActualSource();
            expectedSource = context.getExpectedSource();
        }

        List<Difference> getDifferenceList() {
            return differenceList;
        }

        @Nullable
        Object getActualSource() {
            return actualSource;
        }

        @Nullable
        Object getExpectedSource() {
            return expectedSource;
        }
    }

    private static class EqualsMatcher extends BaseMatcher<Object> implements ParametrizedMatcher {
        private @Nullable String parameter;

        @Override
        public void setParameter(@Nullable String parameter) {
            this.parameter = parameter;
        }

        @Override
        public boolean matches(Object o) {
            return o.toString().equals(parameter);
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("the same ").appendText(parameter);
        }
    }

    @SuppressWarnings("BigDecimalEquals")
    private static class NormalisedNumberComparator implements NumberComparator {
        @Override
        public boolean compare(BigDecimal expectedValue, BigDecimal actualValue, @Nullable BigDecimal tolerance) {
            var normalisedExpectedValue = expectedValue.stripTrailingZeros();
            var normalisedActualValue = actualValue.stripTrailingZeros();
            if (tolerance != null) {
                var diff =
                        normalisedExpectedValue.subtract(normalisedActualValue).abs();
                return diff.compareTo(tolerance) <= 0;
            } else {
                return normalisedExpectedValue.equals(normalisedActualValue);
            }
        }
    }
}
