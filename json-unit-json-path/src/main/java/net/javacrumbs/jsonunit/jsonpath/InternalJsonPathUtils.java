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
package net.javacrumbs.jsonunit.jsonpath;


import com.jayway.jsonpath.Option;
import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.internal.JsonUtils;
import net.javacrumbs.jsonunit.core.internal.PathOption;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static com.jayway.jsonpath.JsonPath.using;
import static java.util.stream.Collectors.toList;

public class InternalJsonPathUtils {
    private InternalJsonPathUtils() {

    }

    public static Configuration resolveJsonPaths(Object json, Configuration configuration) {
        Collection<String> pathsToBeIgnored = resolveJsonPaths(json, configuration.getPathsToBeIgnored());
        List<PathOption> pathOptions = configuration.getPathOptions()
            .stream().map(po -> {
                List<String> newPoPaths = resolveJsonPaths(json, po.getPaths());
                return po.withPaths(newPoPaths);
            }).collect(toList());

        return configuration
            .whenIgnoringPaths(pathsToBeIgnored)
            .withPathOptions(pathOptions);
    }

    private static List<String> resolveJsonPaths(Object json, Collection<String> paths) {
        com.jayway.jsonpath.Configuration conf = com.jayway.jsonpath.Configuration.builder()
            .options(Option.AS_PATH_LIST, Option.SUPPRESS_EXCEPTIONS)
            .build();

        return paths.stream().flatMap(path -> {
            if (path.startsWith("$")) {
                List<String> resolvedPaths = readValue(conf, json, path);
                return resolvedPaths.stream().map(InternalJsonPathUtils::fromBracketNotation);
            } else {
                return Stream.of(path);
            }
        }).collect(toList());
    }

    static <T> T readValue(com.jayway.jsonpath.Configuration conf, Object json, String path) {
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
