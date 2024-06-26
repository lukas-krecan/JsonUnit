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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.NumberComparator;
import net.javacrumbs.jsonunit.core.Option;
import net.javacrumbs.jsonunit.core.ParametrizedMatcher;
import net.javacrumbs.jsonunit.core.listener.Difference;
import net.javacrumbs.jsonunit.core.listener.DifferenceContext;
import net.javacrumbs.jsonunit.core.listener.DifferenceListener;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

public class DifferenceTest {
    private final RecordingDifferenceListener listener = new RecordingDifferenceListener();

    @Test
    void shouldSeeEmptyDiffNodes() {
        Diff diff = Diff.create("{}", "{}", "", "", commonConfig());
        diff.similar();
        assertThat(listener.getDifferenceList(), hasSize(0));
    }

    @Test
    void shouldSeeRemovedNode() {
        Diff diff = Diff.create("{\"test\": \"1\"}", "{}", "", "", commonConfig());
        diff.similar();
        assertThat(listener.getDifferenceList(), hasSize(1));
        assertThat(listener.getDifferenceList().get(0).getType(), equalTo(DifferenceImpl.Type.MISSING));
        assertThat(listener.getDifferenceList().get(0).getActualPath(), equalTo("test"));
        assertThat(listener.getDifferenceList().get(0).getExpected(), equalTo("1"));
        assertThat(listener.getDifferenceList().get(0).getActual(), nullValue());
    }

    @Test
    void shouldSeeAddedNode() {
        Diff diff = Diff.create("{}", "{\"test\": \"1\"}", "", "", commonConfig());
        diff.similar();
        assertThat(listener.getDifferenceList(), hasSize(1));
        assertThat(listener.getDifferenceList().get(0).getType(), equalTo(DifferenceImpl.Type.EXTRA));
        assertThat(listener.getDifferenceList().get(0).getActualPath(), equalTo("test"));
        assertThat(listener.getDifferenceList().get(0).getActual(), equalTo("1"));
        assertThat(listener.getDifferenceList().get(0).getExpected(), nullValue());
    }

    @Test
    void shouldSeeEmptyForCheckAnyNode() {
        Diff diff = Diff.create("{\"test\": \"${json-unit.ignore}\"}", "{\"test\":\"1\"}", "", "", commonConfig());
        diff.similar();
        assertThat(listener.getDifferenceList(), hasSize(0));
    }

    @Test
    void shouldSeeEmptyForCheckAnyBooleanNode() {
        Diff diff = Diff.create("{\"test\": \"${json-unit.any-boolean}\"}", "{\"test\": true}", "", "", commonConfig());
        diff.similar();
        assertThat(listener.getDifferenceList(), hasSize(0));
    }

    @Test
    void shouldSeeEmptyForCheckAnyNumberNode() {
        Diff diff = Diff.create("{\"test\": \"${json-unit.any-number}\"}", "{\"test\": 11}", "", "", commonConfig());
        diff.similar();
        assertThat(listener.getDifferenceList(), hasSize(0));
    }

    @Test
    void shouldSeeEmptyForCheckAnyStringNode() {
        Diff diff = Diff.create("{\"test\": \"${json-unit.any-string}\"}", "{\"test\": \"1\"}", "", "", commonConfig());
        diff.similar();
        assertThat(listener.getDifferenceList(), hasSize(0));
    }

    @Test
    void shouldSeeChangedStringNode() {
        Diff diff = Diff.create("{\"test\": \"1\"}", "{\"test\": \"2\"}", "", "", commonConfig());
        diff.similar();
        assertThat(listener.getDifferenceList(), hasSize(1));
        assertThat(listener.getDifferenceList().get(0).getType(), equalTo(DifferenceImpl.Type.DIFFERENT));
        assertThat(listener.getDifferenceList().get(0).getActualPath(), equalTo("test"));
        assertThat(listener.getDifferenceList().get(0).getExpected(), equalTo("1"));
        assertThat(listener.getDifferenceList().get(0).getActual(), equalTo("2"));
    }

    @Test
    void shouldSeeChangedNumberNode() {
        Diff diff = Diff.create("{\"test\": 1}", "{\"test\": 2 }", "", "", commonConfig());
        diff.similar();
        assertThat(listener.getDifferenceList(), hasSize(1));
        assertThat(listener.getDifferenceList().get(0).getType(), equalTo(DifferenceImpl.Type.DIFFERENT));
        assertThat(listener.getDifferenceList().get(0).getActualPath(), equalTo("test"));
        assertThat(listener.getDifferenceList().get(0).getExpected(), equalTo(new BigDecimal(1)));
        assertThat(listener.getDifferenceList().get(0).getActual(), equalTo(new BigDecimal(2)));
    }

    @Test
    void shouldSeeChangedBooleanNode() {
        Diff diff = Diff.create("{\"test\": true}", "{\"test\": false}", "", "", commonConfig());
        diff.similar();
        assertThat(listener.getDifferenceList(), hasSize(1));
        assertThat(listener.getDifferenceList().get(0).getType(), equalTo(DifferenceImpl.Type.DIFFERENT));
        assertThat(listener.getDifferenceList().get(0).getActualPath(), equalTo("test"));
        assertThat(listener.getDifferenceList().get(0).getExpected(), equalTo(true));
        assertThat(listener.getDifferenceList().get(0).getActual(), equalTo(false));
    }

    @Test
    void shouldSeeChangedStructureNode() {
        Diff diff = Diff.create("{\"test\": \"1\"}", "{\"test\": false}", "", "", commonConfig());
        diff.similar();
        assertThat(listener.getDifferenceList(), hasSize(1));
        assertThat(listener.getDifferenceList().get(0).getType(), equalTo(DifferenceImpl.Type.DIFFERENT));
        assertThat(listener.getDifferenceList().get(0).getActualPath(), equalTo("test"));
        assertThat(listener.getDifferenceList().get(0).getExpected(), equalTo("1"));
        assertThat(listener.getDifferenceList().get(0).getActual(), equalTo(false));
    }

    @Test
    void shouldSeeChangedArrayNode() {
        Diff diff = Diff.create("[1, 1]", "[1, 2]", "", "", commonConfig());
        diff.similar();
        assertThat(listener.getDifferenceList(), hasSize(1));
        assertThat(listener.getDifferenceList().get(0).getType(), equalTo(DifferenceImpl.Type.DIFFERENT));
        assertThat(listener.getDifferenceList().get(0).getActualPath(), equalTo("[1]"));
        assertThat(listener.getDifferenceList().get(0).getExpected(), equalTo(valueOf(1)));
        assertThat(listener.getDifferenceList().get(0).getActual(), equalTo(valueOf(2)));
    }

    @Test
    void shouldSeeRemovedArrayNode() {
        Diff diff = Diff.create("[1, 2]", "[1]", "", "", commonConfig());
        diff.similar();
        assertThat(listener.getDifferenceList(), hasSize(1));
        assertThat(listener.getDifferenceList().get(0).getType(), equalTo(DifferenceImpl.Type.MISSING));
        assertThat(listener.getDifferenceList().get(0).getActualPath(), nullValue());
        assertThat(listener.getDifferenceList().get(0).getExpectedPath(), equalTo("[1]"));
        assertThat(listener.getDifferenceList().get(0).getExpected(), equalTo(valueOf(2)));
        assertThat(listener.getDifferenceList().get(0).getActual(), nullValue());
    }

    @Test
    void shouldSeeAddedArrayNode() {
        Diff diff = Diff.create("[1]", "[1, 2]", "", "", commonConfig());
        diff.similar();
        assertThat(listener.getDifferenceList(), hasSize(1));
        assertThat(listener.getDifferenceList().get(0).getType(), equalTo(DifferenceImpl.Type.EXTRA));
        assertThat(listener.getDifferenceList().get(0).getActual(), equalTo(valueOf(2)));
        assertThat(listener.getDifferenceList().get(0).getActualPath(), equalTo("[1]"));
        assertThat(listener.getDifferenceList().get(0).getExpectedPath(), nullValue());
        assertThat(listener.getDifferenceList().get(0).getExpected(), nullValue());
    }

    @Test
    void shouldSeeObjectDiffNodes() {
        Diff diff = Diff.create(
                "{\"test\": { \"test1\": \"1\"}}", "{\"test\": { \"test1\": \"2\"} }", "", "", commonConfig());
        diff.similar();
        assertThat(listener.getDifferenceList(), hasSize(1));
        assertThat(listener.getDifferenceList().get(0).getType(), equalTo(DifferenceImpl.Type.DIFFERENT));
        assertThat(listener.getDifferenceList().get(0).getActualPath(), equalTo("test.test1"));
        assertThat(listener.getDifferenceList().get(0).getExpected(), equalTo("1"));
        assertThat(listener.getDifferenceList().get(0).getActual(), equalTo("2"));
    }

    @Test
    void shouldSeeNullNode() {
        Diff diff = Diff.create(null, null, "", "", commonConfig());
        diff.similar();
        assertThat(listener.getDifferenceList(), hasSize(0));
    }

    @Test
    void shouldWorkWhenIgnoringArrayOrder() {
        Diff diff = Diff.create(
                "{\"test\": [[1,2],[2,3]]}",
                "{\"test\":[[4,2],[1,2]]}",
                "",
                "",
                commonConfig().when(Option.IGNORING_ARRAY_ORDER));
        diff.similar();
        assertThat(listener.getDifferenceList(), hasSize(1));
        assertThat(listener.getDifferenceList().get(0).getType(), equalTo(DifferenceImpl.Type.DIFFERENT));
        assertThat(listener.getDifferenceList().get(0).getActualPath(), equalTo("test[0][0]"));
        assertThat(listener.getDifferenceList().get(0).getActual(), equalTo(valueOf(4)));
        assertThat(listener.getDifferenceList().get(0).getExpectedPath(), equalTo("test[1][1]"));
        assertThat(listener.getDifferenceList().get(0).getExpected(), equalTo(valueOf(3)));
    }

    @Test
    void shouldSeeActualSource() {
        Diff diff = Diff.create("{\"test\": \"1\"}", "{}", "", "", commonConfig());
        diff.similar();
        assertThat(listener.getActualSource().toString(), equalTo("{}"));
    }

    @Test
    void shouldSeeExpectedSource() {
        Diff diff = Diff.create("{\"test\": \"1\"}", "{}", "", "", commonConfig());
        diff.similar();
        assertThat(listener.getExpectedSource(), equalTo(singletonMap("test", "1")));
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
        assertTrue(diff.similar());
        assertThat(listener.getDifferenceList(), hasSize(0));
    }

    @Test
    @Timeout(1)
    void shouldRunDiffBeforeTimeout() throws URISyntaxException, IOException {
        //noinspection DataFlowIssue
        var actual = Files.readString(
            Paths.get(this.getClass().getResource("/big-json-with-common-keys-actual.json").toURI())
        );
        //noinspection DataFlowIssue
        var expected = Files.readString(
            Paths.get(this.getClass().getResource("/big-json-with-common-keys-expected.json").toURI())
        );
        var cfg = commonConfig()
            .withNumberComparator(new NormalisedNumberComparator())
            .withOptions(Option.IGNORING_ARRAY_ORDER, Option.IGNORING_EXTRA_ARRAY_ITEMS, Option.IGNORING_EXTRA_FIELDS);
        Diff diff = Diff.create(
            expected,
            actual,
            "",
            "",
            cfg
        );
        diff.similar();
    }

    private Configuration commonConfig() {
        return Configuration.empty().withDifferenceListener(listener);
    }

    private static class RecordingDifferenceListener implements DifferenceListener {
        private final List<Difference> differenceList = new ArrayList<>();
        private Object actualSource;
        private Object expectedSource;

        @Override
        public void diff(Difference difference, DifferenceContext context) {
            differenceList.add(difference);
            actualSource = context.getActualSource();
            expectedSource = context.getExpectedSource();
        }

        List<Difference> getDifferenceList() {
            return differenceList;
        }

        Object getActualSource() {
            return actualSource;
        }

        Object getExpectedSource() {
            return expectedSource;
        }
    }

    private static class EqualsMatcher extends BaseMatcher<Object> implements ParametrizedMatcher {
        private String parameter;

        @Override
        public void setParameter(String parameter) {
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

    private static class NormalisedNumberComparator implements NumberComparator {
        @Override
        public boolean compare(BigDecimal expectedValue, BigDecimal actualValue, BigDecimal tolerance) {
            var normalisedExpectedValue = expectedValue.stripTrailingZeros();
            var normalisedActualValue = actualValue.stripTrailingZeros();
            if (tolerance != null) {
                var diff = normalisedExpectedValue.subtract(normalisedActualValue).abs();
                return diff.compareTo(tolerance) <= 0;
            } else {
                return normalisedExpectedValue.equals(normalisedActualValue);
            }
        }
    }
}
