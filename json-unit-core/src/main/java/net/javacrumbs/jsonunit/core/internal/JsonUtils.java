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

import static java.util.Collections.emptyList;
import static java.util.Map.Entry.comparingByKey;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.Option;
import org.jspecify.annotations.Nullable;

/**
 * Internal utility class to parse JSON values.
 */
public class JsonUtils {
    private static final Converter converter = Converter.createDefaultConverter();

    /**
     * Converts object to JSON.
     *
     *
     * @param label  label to be logged in case of error.
     *
     */
    public static Node convertToJson(@Nullable Object source, String label) {
        return convertToJson(source, label, false);
    }

    /**
     * Converts object to JSON.
     */
    public static Node convertToJson(@Nullable Object source, String label, boolean lenient) {
        if (source instanceof JsonSource jsonSource) {
            return converter.convertToNode(jsonSource.getJson(), label, lenient);
        } else {
            return converter.convertToNode(source, label, lenient);
        }
    }

    /**
     * Returns node with given path.
     */
    public static Node getNode(@Nullable Object root, String path) {
        return getNode(root, Path.create(path));
    }

    /**
     * Returns node with given path.
     */
    public static Node getNode(@Nullable Object root, Path path) {
        return path.getNode(convertToJson(root, "actual"));
    }

    public static boolean nodeAbsent(@Nullable Object json, String path, Configuration configuration) {
        return nodeAbsent(json, Path.create(path), configuration);
    }

    public static boolean nodeAbsent(@Nullable Object json, Path path, Configuration configuration) {
        return nodeAbsent(json, path, configuration.getOptions().contains(Option.TREATING_NULL_AS_ABSENT));
    }

    public static Object jsonSource(Object json, String pathPrefix) {
        return jsonSource(json, pathPrefix, emptyList());
    }

    public static Object jsonSource(Object json, String pathPrefix, List<String> matchingPaths) {
        return new DefaultJsonSource(json, pathPrefix, matchingPaths);
    }

    public static String getPathPrefix(@Nullable Object json) {
        if (json instanceof JsonSource jsonSource) {
            return jsonSource.getPathPrefix();
        } else {
            return "";
        }
    }

    /**
     * Returns true if the is absent.
     */
    static boolean nodeAbsent(@Nullable Object json, Path path, boolean treatNullAsAbsent) {
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
     *
     *
     */
    static String quoteIfNeeded(String source) {
        String trimmed = source.trim();

        if (isObject(trimmed)
                || isArray(trimmed)
                || isString(trimmed)
                || isBoolean(trimmed)
                || isNull(trimmed)
                || isNumber(trimmed)) {
            return source;
        } else {
            return "\"" + source + "\"";
        }
    }

    /**
     * Add quotes around the object iff it's not a JSON object.
     */
    static @Nullable Object quoteIfNeeded(@Nullable Object source) {
        if (source instanceof String sourceString) {
            return quoteIfNeeded(sourceString);
        } else {
            return source;
        }
    }

    /**
     * Wraps deserialized object - supports null, String, numbers, maps, lists, ...
     */
    public static Node wrapDeserializedObject(@Nullable Object source) {
        return GenericNodeBuilder.wrapDeserializedObject(source);
    }

    public static Object missingNode() {
        return Node.MISSING_NODE;
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

    static String prettyPrint(Map<String, Object> map) {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        Iterator<Entry<String, Object>> entries =
                map.entrySet().stream().sorted(comparingByKey()).iterator();
        while (entries.hasNext()) {
            Entry<String, Object> entry = entries.next();
            builder.append('"').append(entry.getKey()).append('"').append(":");
            appendQuoteString(entry.getValue(), builder);
            if (entries.hasNext()) {
                builder.append(",");
            }
        }
        builder.append("}");
        return builder.toString();
    }

    private static void appendQuoteString(Object value, StringBuilder builder) {
        if (value instanceof String) {
            builder.append("\"").append(value).append("\"");
        } else {
            builder.append(value);
        }
    }

    private static class DefaultJsonSource implements JsonSource {
        private final Object json;
        private final String pathPrefix;
        private final List<String> matchingPaths;

        private DefaultJsonSource(Object json, String pathPrefix, List<String> matchingPaths) {
            this.json = json;
            this.pathPrefix = pathPrefix;
            this.matchingPaths = matchingPaths;
        }

        @Override
        public Object getJson() {
            return json;
        }

        @Override
        public String getPathPrefix() {
            return pathPrefix;
        }

        @Override
        public List<String> getMatchingPaths() {
            return matchingPaths;
        }

        @Override
        public String toString() {
            return "JSON in path \"" + pathPrefix + "\"";
        }
    }
}
