/**
 * Copyright 2009-2017 the original author or authors.
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
import net.javacrumbs.jsonunit.core.Option;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Internal utility class to parse JSON values.
 */
public class JsonUtils {

    private static final Pattern arrayPattern = Pattern.compile("(.*)\\[(-?\\d+)\\]");

    /**
     * We need to ignore "\." when splitting path.
     */
    public static final Pattern dotWithPreviousChar = Pattern.compile("[^\\\\]\\.");

    private static final Converter converter = Converter.createDefaultConverter();


    /**
     * Converts object to JSON.
     *
     * @param source
     * @param label  label to be logged in case of error.
     * @return
     */
    public static Node convertToJson(Object source, String label) {
        return convertToJson(source, label, false);
    }

    /**
     * Converts object to JSON.
     *
     * @param source
     * @param label  label to be logged in case of error.
     * @param lenient lenient parser used for expected values. Allows unquoted keys.
     * @return
     */
    public static Node convertToJson(Object source, String label, boolean lenient) {
        return converter.convertToNode(source, label, lenient);
    }


    /**
     * Returns node with given path.
     *
     * @param root
     * @param path
     * @return
     */
    static Node getNode(Node root, String path) {
        if (path.length() == 0) {
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

    /**
     * Returns node with given path.
     *
     * @param root
     * @param path
     * @return
     */
    public static Node getNode(Object root, String path) {
        return getNode(convertToJson(root, "actual"), path);
    }

    /**
     * Returns true if the node exists.
     */
    @Deprecated
    public static boolean nodeExists(Object json, String path) {
        return !getNode(json, path).isMissingNode();
    }

    public static boolean nodeAbsent(Object json, String path, Configuration configuration) {
        return nodeAbsent(json, path, configuration.getOptions().contains(Option.TREATING_NULL_AS_ABSENT));
    }

    /**
     * Returns true if the is absent.
     */
    static boolean nodeAbsent(Object json, String path, boolean treatNullAsAbsent) {
        Node node = getNode(json, path);
        if (node.isNull()) {
            return treatNullAsAbsent;
        } else {
            return node.isMissingNode();
        }
    }


    /**
     * Add quotes around the object iff it's not a JSON object.
     *
     * @param source
     * @return
     */
    static String quoteIfNeeded(String source) {
        String trimmed = source.trim();

        if (isObject(trimmed) || isArray(trimmed) || isString(trimmed)
            || isBoolean(trimmed) || isNull(trimmed)
            || isNumber(trimmed)) {
            return source;
        } else {
            return "\"" + source + "\"";
        }
    }

    /**
     * Add quotes around the object iff it's not a JSON object.
     *
     * @param source
     * @return
     */
    static Object quoteIfNeeded(Object source) {
        if (source instanceof String) {
            return quoteIfNeeded((String) source);
        } else {
            return source;
        }
    }

    private static boolean isNull(String trimmed) {
        return trimmed.equals("null");
    }

    private static boolean isBoolean(String trimmed) {
        return trimmed.equals("true") || trimmed.equals("false");
    }

    private static boolean isString(String trimmed) {
        return trimmed.startsWith("\"");
    }

    private static boolean isArray(String trimmed) {
        return trimmed.startsWith("[");
    }

    private static boolean isObject(String trimmed) {
        return trimmed.startsWith("{");
    }

    private static boolean isNumber(String source) {
        try {
            Double.parseDouble(source);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}
