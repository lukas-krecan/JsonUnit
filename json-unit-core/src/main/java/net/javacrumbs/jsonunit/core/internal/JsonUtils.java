/**
 * Copyright 2009-2013 the original author or authors.
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


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Internal utility class to parse JSON values.
 */
public class JsonUtils {
    private static final ObjectMapper mapper = new ObjectMapper();

    private static final Pattern arrayPattern = Pattern.compile("(\\w*)\\[(\\d+)\\]");


    /**
     * Parses value from String.
     *
     * @param value
     * @param label label to be logged in case of error.
     * @return
     */
    public static JsonNode readValue(String value, String label) {
        return readValue(new StringReader(value), label);
    }

    /**
     * Parses value from Reader.
     *
     * @param value
     * @param label label to be logged in case of error.
     * @return
     */
    public static JsonNode readValue(Reader value, String label) {
        try {
            return mapper.readTree(value);
        } catch (IOException e) {
            throw new IllegalArgumentException("Can not parse " + label + " value.", e);
        }
    }

    /**
     * Converts object to JSON.
     *
     * @param source
     * @param label  label to be logged in case of error.
     * @return
     */
    public static JsonNode convertToJson(Object source, String label) {
        if (source == null) {
            return NullNode.instance;
        } else if (source instanceof JsonNode) {
            return (JsonNode) source;
        } else if (source instanceof String) {
            return readValue((String) source, label);
        } else if (source instanceof Reader) {
            return readValue((Reader) source, label);
        } else {
            return mapper.convertValue(source, JsonNode.class);
        }
    }

    /**
     * Converts object to JSON, quotes the String value if it is needed (not a valid JSON object)
     *
     * @param source
     * @param label  label to be logged in case of error.
     * @return
     */
    public static JsonNode convertToJsonQuoteIfNeeded(Object source, String label) {
        if (source instanceof String) {
            return convertToJson(quoteIfNeeded((String) source), label);
        } else {
            return convertToJson(source, label);
        }
    }

    /**
     * Returns node with given path.
     *
     * @param root
     * @param path
     * @return
     */
    static JsonNode getNode(JsonNode root, String path) {
        if (path.length() == 0) {
            return root;
        }

        JsonNode startNode = root;
        StringTokenizer stringTokenizer = new StringTokenizer(path, ".");
        while (stringTokenizer.hasMoreElements()) {
            String step = stringTokenizer.nextToken();
            Matcher matcher = arrayPattern.matcher(step);
            if (!matcher.matches()) {
                startNode = startNode.path(step);
            } else {
                if (matcher.group(1).length() != 0) {
                    startNode = startNode.path(matcher.group(1));
                }
                startNode = startNode.path(Integer.valueOf(matcher.group(2)));
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
    public static JsonNode getNode(Object root, String path) {
        return getNode(convertToJson(root, "actual"), path);
    }

    /**
     * Returns true if the node exists.
     *
     * @param root
     * @param path
     * @return
     */
    public static boolean nodeExists(Object json, String path) {
        return !getNode(json, path).isMissingNode();
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
    public static Object quoteIfNeeded(Object source) {
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
