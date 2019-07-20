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
package net.javacrumbs.jsonunit.spring;

import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.internal.Diff;
import net.javacrumbs.jsonunit.core.internal.JsonUtils;
import net.javacrumbs.jsonunit.core.internal.Node;
import org.springframework.test.web.servlet.MvcResult;

import java.util.function.BiConsumer;

import static net.javacrumbs.jsonunit.core.internal.Diff.create;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.nodeAbsent;
import static net.javacrumbs.jsonunit.core.internal.Node.NodeType.NULL;
import static net.javacrumbs.jsonunit.core.internal.Node.NodeType.STRING;

abstract class AbstractMatcher {
    private final String path;
    private final Configuration configuration;
    private final BiConsumer<Object, AbstractMatcher> matcher;

    AbstractMatcher(String path, Configuration configuration, BiConsumer<Object, AbstractMatcher> matcher) {
        this.path = path;
        this.configuration = configuration;
        this.matcher = matcher;
    }

    public void match(MvcResult result) throws Exception {
        Object actual = result.getResponse().getContentAsString();
        doMatch(actual);
    }

    Diff createDiff(Object expected, Object actual) {
        return create(expected, actual, "actual", path, configuration);
    }

    void isPresent(Object actual) {
        if (nodeAbsent(actual, path, configuration)) {
            failWithMessage("Node \"" + path + "\" is missing.");
        }
    }

    void failOnType(Node node, String type) {
        failWithMessage("Node \"" + path + "\" is different. Expected: <" + type + "> but was: <" + node + ">.");
    }


    Node getNode(Object actual) {
        return JsonUtils.getNode(actual, path);
    }

    void isString(Object actual) {
        isPresent(actual);
        Node node = getNode(actual);
        if (node.getNodeType() != STRING) {
            failOnType(node, "a string");
        }
    }

    void isNull(Object actual) {
        isPresent(actual);
        Node node = getNode(actual);
        if (node.getNodeType() != NULL) {
            failOnType(node, "a null");
        }
    }

    void isNotNull(Object actual) {
        isPresent(actual);
        Node node = getNode(actual);
        if (node.getNodeType() == NULL) {
            failOnType(node, "not null");
        }
    }

    void doMatch(Object actual) {
        matcher.accept(actual, this);
    }

    static void failWithMessage(String message) {
        throw new AssertionError(message);
    }
}
