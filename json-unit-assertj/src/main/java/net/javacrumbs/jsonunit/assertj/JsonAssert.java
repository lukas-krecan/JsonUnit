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
package net.javacrumbs.jsonunit.assertj;

import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.Option;
import net.javacrumbs.jsonunit.core.internal.Diff;
import net.javacrumbs.jsonunit.core.internal.JsonUtils;
import net.javacrumbs.jsonunit.core.internal.Node;
import net.javacrumbs.jsonunit.core.internal.Path;
import net.javacrumbs.jsonunit.jsonpath.JsonPathAdapter;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractCharSequenceAssert;
import org.assertj.core.api.BigDecimalAssert;
import org.assertj.core.api.BooleanAssert;
import org.assertj.core.api.ListAssert;
import org.assertj.core.api.MapAssert;
import org.assertj.core.api.StringAssert;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static net.javacrumbs.jsonunit.core.internal.Diff.quoteTextValue;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.getNode;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.getPathPrefix;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.nodeAbsent;
import static net.javacrumbs.jsonunit.core.internal.Node.NodeType.ARRAY;
import static net.javacrumbs.jsonunit.core.internal.Node.NodeType.BOOLEAN;
import static net.javacrumbs.jsonunit.core.internal.Node.NodeType.NULL;
import static net.javacrumbs.jsonunit.core.internal.Node.NodeType.NUMBER;
import static net.javacrumbs.jsonunit.core.internal.Node.NodeType.OBJECT;
import static net.javacrumbs.jsonunit.core.internal.Node.NodeType.STRING;

public class JsonAssert extends AbstractAssert<JsonAssert, Object> {
    final Path path;
    final Configuration configuration;

    JsonAssert(Path path, Configuration configuration, Object o) {
        super(JsonUtils.convertToJson(o, "actual"), JsonAssert.class);
        this.path = path;
        this.configuration = configuration;
        usingComparator(new JsonComparator(configuration, path, false));
    }

    JsonAssert(Object actual, Configuration configuration) {
        this(Path.create("", getPathPrefix(actual)), configuration, actual);
    }

    /**
     * Navigates to inner node using dotted notation.
     * Comparison to the node can be made using nodeAssert Consumer.
     * Second call navigates from the root position in the JSON exactly as first one.
     */
    public JsonAssert node(String node, Consumer<JsonAssert> nodeAssert) {
        nodeAssert.accept(new JsonAssert(path.to(node), configuration, getNode(actual, node)));
        return this;
    }

    /**
     * Navigates to inner node using JSONPath.
     * Comparison to the node can be made using nodeAssert Consumer.
     * Second call navigates from the root position in the JSON exactly as first one.
     */
    public JsonAssert inPath(String jsonPath, Consumer<JsonAssert> nodeAssert) {
        nodeAssert.accept(new JsonAssert(JsonPathAdapter.inPath(actual, jsonPath), configuration));
        return this;
    }

    /**
     * Compares JSONs.
     */
    @Override
    public JsonAssert isEqualTo(Object expected) {
        Diff diff = Diff.create(expected, actual, "fullJson", path.asPrefix(), configuration);
        if (!diff.similar()) {
            failWithMessage(diff.toString());
        }
        return this;
    }

    /**
     * Asserts that given node is present and is of type object.
     *
     * @return MapAssert where the object is serialized as Map
     */
    public MapAssert<String, Object> isObject() {
        Node node = assertType(OBJECT);
        return new JsonMapAssert((Map<String, Object>) node.getValue(), path.asPrefix(), configuration)
            .as("Different value found in node \"%s\"", path);
    }

    /**
     * Asserts that given node is present and is of type number.
     *
     * @return
     */
    public BigDecimalAssert isNumber() {
        Node node = assertType(NUMBER);
        return new BigDecimalAssert(node.decimalValue()).as("Different value found in node \"%s\"", path);
    }

    /**
     * Asserts that given node is present and is of type array.
     *
     * @return
     */
    public ListAssert<Object> isArray() {
        Node node = assertType(ARRAY);
        return new JsonListAssert((List<?>)node.getValue(), path.asPrefix(), configuration)
            .as("Different value found in node \"%s\"", path);
    }

    /**
     * Asserts that given node is present and is of type boolean.
     *
     * @return
     */
    public BooleanAssert isBoolean() {
        Node node = assertType(BOOLEAN);
        return new BooleanAssert((Boolean) node.getValue()).as("Different value found in node \"%s\"", path);
    }

    /**
     * Asserts that given node is present and is of type string.
     *
     * @return
     */
    public StringAssert isString() {
        Node node = assertType(STRING);
        return new StringAssert((String) node.getValue()).as("Different value found in node \"%s\"", path);
    }

    @Override
    public AbstractCharSequenceAssert<?, String> asString() {
        return isString();
    }

    /**
     * Asserts that given node is present and is null.
     * @return
     */
    @Override
    public void isNull() {
        assertType(NULL);
    }

    /**
     * Asserts that given node is present.
     *
     * @return
     */
    public JsonAssert isPresent() {
        isPresent("node to be present");
        return this;
    }

    /**
     * Asserts that given node is absent.
     *
     * @return
     */
    public void isAbsent() {
        if (!nodeAbsent(actual, path.asPrefix(), configuration)) {
            failOnDifference("node to be absent", quoteTextValue(actual));
        }
    }

    /**
     * Asserts that given node is present and is not null.
     * @return
     */
    @Override
    public JsonAssert isNotNull() {
        isPresent("not null");
        Node node = getNode(actual, "");
        if (node.getNodeType() == NULL) {
            failOnType(node, "not null");
        }
        return this;
    }

    private Node assertType(Node.NodeType type) {
        isPresent(type.getDescription());
        Node node = getNode(actual, "");
        if (node.getNodeType() != type) {
            failOnType(node, type.getDescription());
        }
        return node;
    }

    private void isPresent(String expectedValue) {
        if (nodeAbsent(actual, "", configuration)) {
            failOnDifference(expectedValue, "missing");
        }
    }

    private void failOnDifference(Object expected, Object actual) {
        failWithMessage(String.format("Different value found in node \"%s\", expected: <%s> but was: <%s>.", path, expected, actual));
    }

    private void failOnType(Node node, String expectedTypeDescription) {
        failWithMessage("Node \"" + path + "\" has invalid type, expected: <" + expectedTypeDescription + "> but was: <" + quoteTextValue(node.getValue()) + ">.");
    }

    /**
     * JsonAssert that can be configured to prevent mistakes like
     *
     * <code>
     * assertThatJson(...).isEqualsTo(...).when(...);
     * </code>
     */
    public static class ConfigurableJsonAssert extends JsonAssert {
        ConfigurableJsonAssert(Path path, Configuration configuration, Object o) {
            super(path, configuration, o);
        }

        ConfigurableJsonAssert(Object actual, Configuration configuration) {
            super(Path.create("", getPathPrefix(actual)), configuration, actual);
        }

        /**
         * Adds comparison options.
         */
        public ConfigurableJsonAssert when(Option first, Option... other) {
            return withConfiguration(c -> c.when(first, other));
        }

        /**
         * Allows to configure like this
         *
         * <code>
         *     assertThatJson(...)
         *             .withConfiguration(c -> c.withMatcher("positive", greaterThan(valueOf(0)))
         *             ....
         * </code>
         */
        public ConfigurableJsonAssert withConfiguration(Function<Configuration, Configuration> configurationFunction) {
            return new ConfigurableJsonAssert(path, configurationFunction.apply(configuration), actual);
        }
    }
}
