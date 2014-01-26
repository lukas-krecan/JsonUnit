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

/**
 * Internal utility class to parse JSON values.
 */
public class JsonUtils {
    private static final ObjectMapper MAPPER = new ObjectMapper();

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
            return MAPPER.readTree(value);
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
            return MAPPER.convertValue(source, JsonNode.class);
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
     * Add quotes around the object iff it's not a JSON object.
     *
     * @param source
     * @return
     */
    public static String quoteIfNeeded(String source) {
        String trimmed = source.trim();

        if (isObject(trimmed) || isArray(trimmed) || isString(trimmed)
                || isBoolean(trimmed) || isNull(trimmed)
                || isNumber(source)) {
            return source;
        } else {
            return "\"" + source + "\"";
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
