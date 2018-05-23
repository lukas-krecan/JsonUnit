package net.javacrumbs.jsonunit.core.internal;

import net.javacrumbs.jsonunit.core.Configuration;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.nullValue;

public class DiffNodeTest {

    @Test
    public void shouldSeeEmptyDiffNodes() {
        Diff diff = Diff.create("{}", "{}", "", "", Configuration.empty(), new ArrayList<Filter>());
        diff.similar();
        assertThat(diff.getDiffNodes(), hasSize(0));
    }

    @Test
    public void shouldSeeRemovedNode() {
        Diff diff = Diff.create("{\"test\": \"1\"}", "{}", "", "", Configuration.empty(), new ArrayList<Filter>());
        diff.similar();
        assertThat(diff.getDiffNodes(), hasSize(1));
        assertThat(diff.getDiffNodes().get(0), instanceOf(DiffNode.Removed.class));
        assertThat(diff.getDiffNodes().get(0).getPath().getFullPath(), equalTo("test"));
        assertThat((String) diff.getDiffNodes().get(0).getExpected().getValue(), equalTo("1"));
        assertThat(diff.getDiffNodes().get(0).getActual(), nullValue());
    }

    @Test
    public void shouldSeeAddedNode() {
        Diff diff = Diff.create("{}", "{\"test\": \"1\"}", "", "", Configuration.empty(), new ArrayList<Filter>());
        diff.similar();
        assertThat(diff.getDiffNodes(), hasSize(1));
        assertThat(diff.getDiffNodes().get(0), instanceOf(DiffNode.Added.class));
        assertThat(diff.getDiffNodes().get(0).getPath().getFullPath(), equalTo("test"));
        assertThat((String) diff.getDiffNodes().get(0).getActual().getValue(), equalTo("1"));
        assertThat(diff.getDiffNodes().get(0).getExpected(), nullValue());
    }

    @Test
    public void shouldSeeEmptyForCheckAnyNode() {
        Diff diff = Diff.create("{\"test\": \"${json-unit.ignore}\"}", "{\"test\":\"1\"}", "", "", Configuration.empty(), new ArrayList<Filter>());
        diff.similar();
        assertThat(diff.getDiffNodes(), hasSize(0));
    }

    @Test
    public void shouldSeeEmptyForCheckAnyBooleanNode() {
        Diff diff = Diff.create("{\"test\": \"${json-unit.any-boolean}\"}", "{\"test\": true}", "", "", Configuration.empty(), new ArrayList<Filter>());
        diff.similar();
        assertThat(diff.getDiffNodes(), hasSize(0));
    }

    @Test
    public void shouldSeeEmptyForCheckAnyNumberNode() {
        Diff diff = Diff.create("{\"test\": \"${json-unit.any-number}\"}", "{\"test\": 11}", "", "", Configuration.empty(), new ArrayList<Filter>());
        diff.similar();
        assertThat(diff.getDiffNodes(), hasSize(0));
    }

    @Test
    public void shouldSeeEmptyForCheckAnyStringNode() {
        Diff diff = Diff.create("{\"test\": \"${json-unit.any-string}\"}", "{\"test\": \"1\"}", "", "", Configuration.empty(), new ArrayList<Filter>());
        diff.similar();
        assertThat(diff.getDiffNodes(), hasSize(0));
    }

    @Test
    public void shouldSeeChangedStringNode() {
        Diff diff = Diff.create("{\"test\": \"1\"}", "{\"test\": \"2\"}", "", "", Configuration.empty(), new ArrayList<Filter>());
        diff.similar();
        assertThat(diff.getDiffNodes(), hasSize(1));
        assertThat(diff.getDiffNodes().get(0), instanceOf(DiffNode.Changed.class));
        assertThat(diff.getDiffNodes().get(0).getPath().getFullPath(), equalTo("test"));
        assertThat((String) diff.getDiffNodes().get(0).getExpected().getValue(), equalTo("1"));
        assertThat((String) diff.getDiffNodes().get(0).getActual().getValue(), equalTo("2"));
    }

    @Test
    public void shouldSeeChangedNumberNode() {
        Diff diff = Diff.create("{\"test\": 1}", "{\"test\": 2 }", "", "", Configuration.empty(), new ArrayList<Filter>());
        diff.similar();
        assertThat(diff.getDiffNodes(), hasSize(1));
        assertThat(diff.getDiffNodes().get(0), instanceOf(DiffNode.Changed.class));
        assertThat(diff.getDiffNodes().get(0).getPath().getFullPath(), equalTo("test"));
        assertThat((BigDecimal) diff.getDiffNodes().get(0).getExpected().getValue(), equalTo(new BigDecimal(1)));
        assertThat((BigDecimal) diff.getDiffNodes().get(0).getActual().getValue(), equalTo(new BigDecimal(2)));
    }

    @Test
    public void shouldSeeChangedBooleanNode() {
        Diff diff = Diff.create("{\"test\": true}", "{\"test\": false}", "", "", Configuration.empty(), new ArrayList<Filter>());
        diff.similar();
        assertThat(diff.getDiffNodes(), hasSize(1));
        assertThat(diff.getDiffNodes().get(0), instanceOf(DiffNode.Changed.class));
        assertThat(diff.getDiffNodes().get(0).getPath().getFullPath(), equalTo("test"));
        assertThat((Boolean) diff.getDiffNodes().get(0).getExpected().getValue(), equalTo(true));
        assertThat((Boolean) diff.getDiffNodes().get(0).getActual().getValue(), equalTo(false));
    }

    @Test
    public void shouldSeeChangedStructureNode() {
        Diff diff = Diff.create("{\"test\": \"1\"}", "{\"test\": false}", "", "", Configuration.empty(), new ArrayList<Filter>());
        diff.similar();
        assertThat(diff.getDiffNodes(), hasSize(1));
        assertThat(diff.getDiffNodes().get(0), instanceOf(DiffNode.Changed.class));
        assertThat(diff.getDiffNodes().get(0).getPath().getFullPath(), equalTo("test"));
        assertThat((String) diff.getDiffNodes().get(0).getExpected().getValue(), equalTo("1"));
        assertThat((Boolean) diff.getDiffNodes().get(0).getActual().getValue(), equalTo(false));
    }

    @Test
    public void shouldSeeChangedArrayNode() {
        Diff diff = Diff.create("[1, 1]", "[1, 2]", "", "", Configuration.empty(), new ArrayList<Filter>());
        diff.similar();
        assertThat(diff.getDiffNodes(), hasSize(1));
        assertThat(diff.getDiffNodes().get(0), instanceOf(DiffNode.Changed.class));
        assertThat(diff.getDiffNodes().get(0).getPath().getFullPath(), equalTo("[1]"));
        assertThat((BigDecimal) diff.getDiffNodes().get(0).getExpected().getValue(), equalTo(BigDecimal.valueOf(1)));
        assertThat((BigDecimal) diff.getDiffNodes().get(0).getActual().getValue(), equalTo(BigDecimal.valueOf(2)));
    }

    @Test
    @Ignore
    public void shouldSeeRemovedArrayNode() {
        Diff diff = Diff.create("[1, 2]", "[1]", "", "", Configuration.empty(), new ArrayList<Filter>());
        diff.similar();
        assertThat(diff.getDiffNodes(), hasSize(1));
        assertThat(diff.getDiffNodes().get(0), instanceOf(DiffNode.Removed.class));
        assertThat(diff.getDiffNodes().get(0).getPath().getFullPath(), equalTo("[2]"));
        assertThat((BigDecimal) diff.getDiffNodes().get(0).getExpected(), equalTo(BigDecimal.valueOf(1)));
        assertThat(diff.getDiffNodes().get(0).getActual().getValue(), nullValue());
    }

    @Test
    @Ignore
    public void shouldSeeAddedArrayNode() {
        Diff diff = Diff.create("[1]", "[1, 2]", "", "", Configuration.empty(), new ArrayList<Filter>());
        diff.similar();
        assertThat(diff.getDiffNodes(), hasSize(1));
        assertThat(diff.getDiffNodes().get(0), instanceOf(DiffNode.Added.class));
        assertThat(diff.getDiffNodes().get(0).getPath().getFullPath(), equalTo("[2]"));
        assertThat(diff.getDiffNodes().get(0).getExpected(), nullValue());
        assertThat((BigDecimal) diff.getDiffNodes().get(0).getActual().getValue(), equalTo(BigDecimal.valueOf(2)));
    }

    @Test
    public void shouldSeeObjectDiffNodes() {
        Diff diff = Diff.create("{\"test\": { \"test1\": \"1\"}}", "{\"test\": { \"test1\": \"2\"} }", "", "", Configuration.empty(), new ArrayList<Filter>());
        diff.similar();
        assertThat(diff.getDiffNodes(), hasSize(1));
        assertThat(diff.getDiffNodes().get(0), instanceOf(DiffNode.Changed.class));
        assertThat(diff.getDiffNodes().get(0).getPath().getFullPath(), equalTo("test.test1"));
        assertThat((String) diff.getDiffNodes().get(0).getExpected().getValue(), equalTo("1"));
        assertThat((String) diff.getDiffNodes().get(0).getActual().getValue(), equalTo("2"));

    }


    @Test
    public void shouldSeeNullNode() {
        Diff diff = Diff.create(null, null, "", "", Configuration.empty(), new ArrayList<Filter>());
        diff.similar();
        assertThat(diff.getDiffNodes(), hasSize(0));
    }
}
