package net.javacrumbs.jsonunit.core.internal;

import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.Option;
import net.javacrumbs.jsonunit.core.listener.Difference;
import net.javacrumbs.jsonunit.core.listener.DifferenceContext;
import net.javacrumbs.jsonunit.core.listener.DifferenceListener;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static java.math.BigDecimal.valueOf;
import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;

public class DifferenceTest {
    private final RecordingDifferenceListener listener = new RecordingDifferenceListener();

    @Test
    public void shouldSeeEmptyDiffNodes() {
        Diff diff = Diff.create("{}", "{}", "", "", commonConfig());
        diff.similar();
        assertThat(listener.getDifferenceList(), hasSize(0));
    }

    @Test
    public void shouldSeeRemovedNode() {
        Diff diff = Diff.create("{\"test\": \"1\"}", "{}", "", "", commonConfig());
        diff.similar();
        assertThat(listener.getDifferenceList(), hasSize(1));
        assertThat(listener.getDifferenceList().get(0).getType(), equalTo(DifferenceImpl.Type.MISSING));
        assertThat(listener.getDifferenceList().get(0).getActualPath(), equalTo("test"));
        assertThat((String) listener.getDifferenceList().get(0).getExpected(), equalTo("1"));
        assertThat(listener.getDifferenceList().get(0).getActual(), nullValue());
    }

    @Test
    public void shouldSeeAddedNode() {
        Diff diff = Diff.create("{}", "{\"test\": \"1\"}", "", "", commonConfig());
        diff.similar();
        assertThat(listener.getDifferenceList(), hasSize(1));
        assertThat(listener.getDifferenceList().get(0).getType(), equalTo(DifferenceImpl.Type.EXTRA));
        assertThat(listener.getDifferenceList().get(0).getActualPath(), equalTo("test"));
        assertThat((String) listener.getDifferenceList().get(0).getActual(), equalTo("1"));
        assertThat(listener.getDifferenceList().get(0).getExpected(), nullValue());
    }

    @Test
    public void shouldSeeEmptyForCheckAnyNode() {
        Diff diff = Diff.create("{\"test\": \"${json-unit.ignore}\"}", "{\"test\":\"1\"}", "", "", commonConfig());
        diff.similar();
        assertThat(listener.getDifferenceList(), hasSize(0));
    }

    @Test
    public void shouldSeeEmptyForCheckAnyBooleanNode() {
        Diff diff = Diff.create("{\"test\": \"${json-unit.any-boolean}\"}", "{\"test\": true}", "", "", commonConfig());
        diff.similar();
        assertThat(listener.getDifferenceList(), hasSize(0));
    }

    @Test
    public void shouldSeeEmptyForCheckAnyNumberNode() {
        Diff diff = Diff.create("{\"test\": \"${json-unit.any-number}\"}", "{\"test\": 11}", "", "", commonConfig());
        diff.similar();
        assertThat(listener.getDifferenceList(), hasSize(0));
    }

    @Test
    public void shouldSeeEmptyForCheckAnyStringNode() {
        Diff diff = Diff.create("{\"test\": \"${json-unit.any-string}\"}", "{\"test\": \"1\"}", "", "", commonConfig());
        diff.similar();
        assertThat(listener.getDifferenceList(), hasSize(0));
    }

    @Test
    public void shouldSeeChangedStringNode() {
        Diff diff = Diff.create("{\"test\": \"1\"}", "{\"test\": \"2\"}", "", "", commonConfig());
        diff.similar();
        assertThat(listener.getDifferenceList(), hasSize(1));
        assertThat(listener.getDifferenceList().get(0).getType(), equalTo(DifferenceImpl.Type.DIFFERENT));
        assertThat(listener.getDifferenceList().get(0).getActualPath(), equalTo("test"));
        assertThat((String) listener.getDifferenceList().get(0).getExpected(), equalTo("1"));
        assertThat((String) listener.getDifferenceList().get(0).getActual(), equalTo("2"));
    }

    @Test
    public void shouldSeeChangedNumberNode() {
        Diff diff = Diff.create("{\"test\": 1}", "{\"test\": 2 }", "", "", commonConfig());
        diff.similar();
        assertThat(listener.getDifferenceList(), hasSize(1));
        assertThat(listener.getDifferenceList().get(0).getType(), equalTo(DifferenceImpl.Type.DIFFERENT));
        assertThat(listener.getDifferenceList().get(0).getActualPath(), equalTo("test"));
        assertThat((BigDecimal) listener.getDifferenceList().get(0).getExpected(), equalTo(new BigDecimal(1)));
        assertThat((BigDecimal) listener.getDifferenceList().get(0).getActual(), equalTo(new BigDecimal(2)));
    }

    @Test
    public void shouldSeeChangedBooleanNode() {
        Diff diff = Diff.create("{\"test\": true}", "{\"test\": false}", "", "", commonConfig());
        diff.similar();
        assertThat(listener.getDifferenceList(), hasSize(1));
        assertThat(listener.getDifferenceList().get(0).getType(), equalTo(DifferenceImpl.Type.DIFFERENT));
        assertThat(listener.getDifferenceList().get(0).getActualPath(), equalTo("test"));
        assertThat((Boolean) listener.getDifferenceList().get(0).getExpected(), equalTo(true));
        assertThat((Boolean) listener.getDifferenceList().get(0).getActual(), equalTo(false));
    }

    @Test
    public void shouldSeeChangedStructureNode() {
        Diff diff = Diff.create("{\"test\": \"1\"}", "{\"test\": false}", "", "", commonConfig());
        diff.similar();
        assertThat(listener.getDifferenceList(), hasSize(1));
        assertThat(listener.getDifferenceList().get(0).getType(), equalTo(DifferenceImpl.Type.DIFFERENT));
        assertThat(listener.getDifferenceList().get(0).getActualPath(), equalTo("test"));
        assertThat((String) listener.getDifferenceList().get(0).getExpected(), equalTo("1"));
        assertThat((Boolean) listener.getDifferenceList().get(0).getActual(), equalTo(false));
    }

    @Test
    public void shouldSeeChangedArrayNode() {
        Diff diff = Diff.create("[1, 1]", "[1, 2]", "", "", commonConfig());
        diff.similar();
        assertThat(listener.getDifferenceList(), hasSize(1));
        assertThat(listener.getDifferenceList().get(0).getType(), equalTo(DifferenceImpl.Type.DIFFERENT));
        assertThat(listener.getDifferenceList().get(0).getActualPath(), equalTo("[1]"));
        assertThat((BigDecimal) listener.getDifferenceList().get(0).getExpected(), equalTo(valueOf(1)));
        assertThat((BigDecimal) listener.getDifferenceList().get(0).getActual(), equalTo(valueOf(2)));
    }

    @Test
    public void shouldSeeRemovedArrayNode() {
        Diff diff = Diff.create("[1, 2]", "[1]", "", "", commonConfig());
        diff.similar();
        assertThat(listener.getDifferenceList(), hasSize(1));
        assertThat(listener.getDifferenceList().get(0).getType(), equalTo(DifferenceImpl.Type.MISSING));
        assertThat(listener.getDifferenceList().get(0).getActualPath(), nullValue());
        assertThat(listener.getDifferenceList().get(0).getExpectedPath(), equalTo("[1]"));
        assertThat((BigDecimal) listener.getDifferenceList().get(0).getExpected(), equalTo(valueOf(2)));
        assertThat(listener.getDifferenceList().get(0).getActual(), nullValue());
    }

    @Test
    public void shouldSeeAddedArrayNode() {
        Diff diff = Diff.create("[1]", "[1, 2]", "", "", commonConfig());
        diff.similar();
        assertThat(listener.getDifferenceList(), hasSize(1));
        assertThat(listener.getDifferenceList().get(0).getType(), equalTo(DifferenceImpl.Type.EXTRA));
        assertThat((BigDecimal) listener.getDifferenceList().get(0).getActual(), equalTo(valueOf(2)));
        assertThat(listener.getDifferenceList().get(0).getActualPath(), equalTo("[1]"));
        assertThat(listener.getDifferenceList().get(0).getExpectedPath(), nullValue());
        assertThat(listener.getDifferenceList().get(0).getExpected(), nullValue());
    }

    @Test
    public void shouldSeeObjectDiffNodes() {
        Diff diff = Diff.create("{\"test\": { \"test1\": \"1\"}}", "{\"test\": { \"test1\": \"2\"} }", "", "", commonConfig());
        diff.similar();
        assertThat(listener.getDifferenceList(), hasSize(1));
        assertThat(listener.getDifferenceList().get(0).getType(), equalTo(DifferenceImpl.Type.DIFFERENT));
        assertThat(listener.getDifferenceList().get(0).getActualPath(), equalTo("test.test1"));
        assertThat((String) listener.getDifferenceList().get(0).getExpected(), equalTo("1"));
        assertThat((String) listener.getDifferenceList().get(0).getActual(), equalTo("2"));
    }

    @Test
    public void shouldSeeNullNode() {
        Diff diff = Diff.create(null, null, "", "", commonConfig());
        diff.similar();
        assertThat(listener.getDifferenceList(), hasSize(0));
    }

    @Test
    public void shouldWorkWhenIgnoringArrayOrder() {
        Diff diff = Diff.create("{\"test\": [[1,2],[2,3]]}", "{\"test\":[[4,2],[1,2]]}", "", "", commonConfig().when(Option.IGNORING_ARRAY_ORDER));
        diff.similar();
        assertThat(listener.getDifferenceList(), hasSize(1));
        assertThat(listener.getDifferenceList().get(0).getType(), equalTo(DifferenceImpl.Type.DIFFERENT));
        assertThat(listener.getDifferenceList().get(0).getActualPath(), equalTo("test[0][0]"));
        assertThat((BigDecimal) listener.getDifferenceList().get(0).getActual(), equalTo(valueOf(4)));
        assertThat(listener.getDifferenceList().get(0).getExpectedPath(), equalTo("test[1][1]"));
        assertThat((BigDecimal) listener.getDifferenceList().get(0).getExpected(), equalTo(valueOf(3)));
    }

    @Test
    public void shouldSeeActualSource() {
        Diff diff = Diff.create("{\"test\": \"1\"}", "{}", "", "", commonConfig());
        diff.similar();
        assertThat(listener.getActualSource().toString(), equalTo("{}"));
    }

    @Test
    public void shouldSeeExpectedSource() {
        Diff diff = Diff.create("{\"test\": \"1\"}", "{}", "", "", commonConfig());
        diff.similar();
        assertThat(listener.getExpectedSource(), Matchers.<Object>equalTo(singletonMap("test", "1")));
    }


    private Configuration commonConfig() {
        return Configuration.empty().withDifferenceListener(listener);
    }

    private static class RecordingDifferenceListener implements DifferenceListener {
        private final List<Difference> differenceList = new ArrayList<Difference>();
        private Object actualSource;
        private Object expectedSource;

        @Override
        public void diff(Difference difference, DifferenceContext context) {
            differenceList.add(difference);
            actualSource = context.getActualSource();
            expectedSource = context.getExpectedSource();
        }

        public List<Difference> getDifferenceList() {
            return differenceList;
        }

        public Object getActualSource() {
            return actualSource;
        }

        public Object getExpectedSource() {
            return expectedSource;
        }
    }
}
