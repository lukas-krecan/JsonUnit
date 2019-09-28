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
package net.javacrumbs.jsonunit.jsonpath;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.ParseContext;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.internal.ParseContextImpl;
import net.javacrumbs.jsonunit.core.internal.JsonUtils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.jayway.jsonpath.JsonPath.using;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.jsonSource;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.missingNode;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.wrapDeserializedObject;

/**
 * Adapts json-path to json-unit.
 */
public final class JsonPathAdapter {
    private JsonPathAdapter() {

    }

    public static Object inPath(Object json, String path) {
        String normalizedPath = fromBracketNotation(path);
        try {
            return jsonSource(wrapDeserializedObject(readValue(Configuration.defaultConfiguration(), json, path)), normalizedPath);
        } catch (PathNotFoundException e) {
            return jsonSource(missingNode(), normalizedPath);
        }
    }

    public static Collection<String> resolveJsonPaths(Object json, Collection<String> paths) {
        Configuration conf = Configuration.builder()
            .options(Option.AS_PATH_LIST, Option.SUPPRESS_EXCEPTIONS)
            .build();

        return paths.stream().flatMap(path -> {
            if (path.startsWith("$")) {
                List<String> resolvedPaths = readValue(conf, json, path);
                return resolvedPaths.stream().map(JsonPathAdapter::fromBracketNotation);
            } else {
                return Stream.of(path);
            }
        }).collect(Collectors.toList());
    }

    private static <T> T readValue(Configuration conf, Object json, String path) {
        if (json instanceof String) {
            return using(conf).parse((String) json).read(path);
        } else {
            return using(conf).parse(JsonUtils.convertToJson(json, "actual").getValue()).read(path);
        }
    }

    static String fromBracketNotation(String path) {
        return path
            .replace("['", ".")
            .replace("']", "");
    }
}
