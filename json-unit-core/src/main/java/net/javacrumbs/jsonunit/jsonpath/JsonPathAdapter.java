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

import static com.jayway.jsonpath.Configuration.defaultConfiguration;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.getPathPrefix;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.jsonSource;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.missingNode;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.wrapDeserializedObject;
import static net.javacrumbs.jsonunit.jsonpath.InternalJsonPathUtils.fromBracketNotation;
import static net.javacrumbs.jsonunit.jsonpath.InternalJsonPathUtils.readValue;

import com.jayway.jsonpath.EvaluationListener;
import com.jayway.jsonpath.PathNotFoundException;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * Adapts json-path to json-unit.
 */
public final class JsonPathAdapter {
    private JsonPathAdapter() {}

    public static Object inPath(@Nullable Object json, String path) {
        try {
            MatchRecordingListener recordingListener = new MatchRecordingListener();
            Object value = readValue(defaultConfiguration().addEvaluationListeners(recordingListener), json, path);
            return jsonSource(
                    wrapDeserializedObject(value), concatJsonPaths(json, path), recordingListener.getMatchingPaths());
        } catch (PathNotFoundException e) {
            return jsonSource(missingNode(), concatJsonPaths(json, path));
        }
    }

    private static String concatJsonPaths(@Nullable Object json, String path) {
        String newPathSegment = fromBracketNotation(path);
        String pathPrefix = getPathPrefix(json);
        if (pathPrefix.isEmpty()) {
            return newPathSegment;
        }
        if (newPathSegment.startsWith("$.")) {
            return pathPrefix + newPathSegment.substring(1);
        }
        if (newPathSegment.startsWith("[")) {
            return pathPrefix + newPathSegment;
        }
        return pathPrefix + "." + newPathSegment;
    }

    private static class MatchRecordingListener implements EvaluationListener {
        private final List<String> matchingPaths = new ArrayList<>();

        @Override
        public EvaluationContinuation resultFound(FoundResult foundResult) {
            matchingPaths.add(fromBracketNotation(foundResult.path()));
            return EvaluationContinuation.CONTINUE;
        }

        List<String> getMatchingPaths() {
            return matchingPaths;
        }
    }
}
