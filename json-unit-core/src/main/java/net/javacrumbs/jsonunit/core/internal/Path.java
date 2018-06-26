/**
 * Copyright 2009-2018 the original author or authors.
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Path {
    private final String path;
    private final String pathPrefix;

    private static final Pattern arrayPattern = Pattern.compile("(.*)\\[(-?\\d+)]");

    /**
     * We need to ignore "\." when splitting path.
     */
    private static final Pattern dotWithPreviousChar = Pattern.compile("[^\\\\]\\.");

    private Path(String path, String pathPrefix) {
        this.path = path;
        this.pathPrefix = pathPrefix;
    }

    public static Path create(String path) {
        return create(path, "");
    }

    public static Path create(String path, String pathPrefix) {
        return new Path(path, pathPrefix);
    }

    public Path copy(String newPath) {
        return new Path(newPath, pathPrefix);
    }

    public Path asPrefix() {
        return new Path("", getFullPath());
    }

    String getPath() {
        return path;
    }

    String getFullPath() {
        if (pathPrefix.isEmpty()) {
            return path;
        } else if (path.startsWith("[")) {
            return pathPrefix + path;
        } else if (!path.isEmpty()){
            return pathPrefix + "." + path;
        } else {
            return pathPrefix;
        }
    }

    /**
     * Construct path to a filed.
     */
    Path toField(String name) {
        if (isRoot()) {
            return copy(name);
        } else {
            return copy(path + "." + name);
        }
    }

    /**
     * Constructs path to an array element.
     */
    Path toElement(int i) {
        return copy(path + "[" + i + "]");
    }

    public Path to(String name) {
        if (name.startsWith("[")){
            return copy(path + name);
        } else {
            return toField(name);
        }
    }

    public String toString() {
        return getFullPath();
    }

    /**
     * Returns node with given path.
     */
    Node getNode(Node root) {
        if (isRoot()) {
            return root;
        }

        Node startNode = root;
        Matcher pathMatcher = dotWithPreviousChar.matcher(path);
        int pos = 0;
        while (pathMatcher.find()) {
            String step = path.substring(pos, pathMatcher.end() - 1);
            pos = pathMatcher.end();
            startNode = doStep(step, startNode);
        }
        startNode = doStep(path.substring(pos), startNode);
        return startNode;
    }

    private boolean isRoot() {
        return path.length() == 0;
    }

    private static Node doStep(String step, Node startNode) {
        step = step.replaceAll("\\\\.", ".");
        Matcher matcher = arrayPattern.matcher(step);
        if (!matcher.matches()) {
            startNode = startNode.get(step);
        } else {
            if (matcher.group(1).length() != 0) {
                startNode = startNode.get(matcher.group(1));
            }

            int index = Integer.valueOf(matcher.group(2));
            if (index < 0) {
                startNode = startNode.element(startNode.size() + index);
            } else {
                startNode = startNode.element(index);
            }
        }
        return startNode;
    }
}
