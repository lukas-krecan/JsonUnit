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

import static com.jayway.jsonpath.JsonPath.using;

import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.PathNotFoundException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.internal.JsonUtils;
import net.javacrumbs.jsonunit.core.internal.PathOption;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class InternalJsonPathUtils {
    private InternalJsonPathUtils() {}

    public static Configuration resolveJsonPaths(@Nullable Object json, Configuration configuration) {
        return resolveJsonPaths(null, json, configuration);
    }

    public static Configuration resolveJsonPaths(
            @Nullable Object expected, @Nullable Object actual, Configuration configuration) {
        Collection<String> pathsToBeIgnored =
                resolveJsonPaths(Arrays.asList(expected, actual), configuration.getPathsToBeIgnored());
        List<PathOption> pathOptions = configuration.getPathOptions().stream()
                .map(po -> {
                    List<String> newPoPaths = resolveJsonPaths(Arrays.asList(expected, actual), po.getPaths());
                    return po.withPaths(newPoPaths);
                })
                .toList();

        return configuration.whenIgnoringPaths(pathsToBeIgnored).withPathOptions(pathOptions);
    }

    private static List<String> resolveJsonPaths(List<@Nullable Object> jsons, Collection<String> paths) {
        com.jayway.jsonpath.Configuration conf = com.jayway.jsonpath.Configuration.builder()
                .options(Option.AS_PATH_LIST, Option.SUPPRESS_EXCEPTIONS)
                .build();

        return paths.stream()
                .flatMap(path -> {
                    if (path.startsWith("$")) {
                        Stream<String> originalPath = Stream.of(path);
                        Stream<String> resolvedPaths = jsons.stream()
                                .filter(Objects::nonNull)
                                .flatMap(json ->
                                        InternalJsonPathUtils.<List<String>>readValue(conf, json, path).stream())
                                .map(InternalJsonPathUtils::fromBracketNotation);
                        return Stream.concat(originalPath, resolvedPaths);
                    } else {
                        return Stream.of(path);
                    }
                })
                .distinct()
                .toList();
    }

    @SuppressWarnings("TypeParameterUnusedInFormals")
    static <T> T readValue(com.jayway.jsonpath.Configuration conf, @Nullable Object json, String path) {
        if (json instanceof String string) {
            return using(conf).parse(string).read(path);
        } else if (json == null) {
            throw new PathNotFoundException("Path not found in <null>");
        } else {
            return using(conf)
                    .parse(JsonUtils.convertToJson(json, "actual").getValue())
                    .read(path);
        }
    }

    static String fromBracketNotation(String path) {
        return path.replace("['", ".").replace("']", "");
    }
}
