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

import static net.javacrumbs.jsonunit.core.internal.JsonUtils.wrapDeserializedObject;

class Context {

    private final Node expectedNode;
    private final Node actualNode;
    private final Path expectedPath;
    private final Path actualPath;
    private final Configuration configuration;

    Context(Node expectedNode, Node actualNode, Path expectedPath, Path actualPath, Configuration configuration) {
        this.expectedNode = expectedNode;
        this.actualNode = actualNode;
        this.expectedPath = expectedPath;
        this.actualPath = actualPath;
        this.configuration = configuration;
    }

    Node getExpectedNode() {
        return expectedNode;
    }

    Node getActualNode() {
        return actualNode;
    }

    Path getExpectedPath() {
        return expectedPath;
    }

    Path getActualPath() {
        return actualPath;
    }

    Context inField(String key) {
        return new Context(expectedNode.get(key), actualNode.get(key), expectedPath.toField(key), actualPath.toField(key), configuration);
    }

    Context toElement(int i) {
        return new Context(expectedNode.element(i), actualNode.element(i), expectedPath.toElement(i), actualPath.toElement(i), configuration);
    }

    Context missingElement(int i) {
        return new Context(expectedNode.element(i), null, expectedPath.toElement(i), null, configuration);
    }

    Context extraElement(int i) {
        return new Context(null, actualNode.element(i), null, actualPath.toElement(i), configuration);
    }

    Context length(Object expectedLength) {
        return new Context(wrapDeserializedObject(expectedLength), wrapDeserializedObject(actualNode.size()), expectedPath.length(), actualPath.length(), configuration);
    }

    public Configuration getConfiguration() {
        return configuration;
    }
}
