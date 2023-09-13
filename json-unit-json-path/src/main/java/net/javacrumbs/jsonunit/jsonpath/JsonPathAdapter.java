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

import com.jayway.jsonpath.EvaluationListener;
import com.jayway.jsonpath.PathNotFoundException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.jayway.jsonpath.Configuration.defaultConfiguration;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.jsonSource;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.missingNode;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.wrapDeserializedObject;
import static net.javacrumbs.jsonunit.jsonpath.InternalJsonPathUtils.fromBracketNotation;
import static net.javacrumbs.jsonunit.jsonpath.InternalJsonPathUtils.readValue;

/**
 * Adapts json-path to json-unit.
 */
public final class  JsonPathAdapter {
    private JsonPathAdapter() {

    }

    @NotNull
    public static Object inPath(@Nullable Object json, @NotNull String path) {
        String normalizedPath = fromBracketNotation(path);
        try {
            MatchRecordingListener recordingListener = new MatchRecordingListener();
            Object value = readValue(defaultConfiguration().addEvaluationListeners(recordingListener), json, path);
            return jsonSource(wrapDeserializedObject(value), normalizedPath, recordingListener.getMatchingPaths());
        } catch (PathNotFoundException e) {
            return jsonSource(missingNode(), normalizedPath);
        }
    }

    private static class MatchRecordingListener implements EvaluationListener {
        private final List<String> matchingPaths = new ArrayList<>();

        @Override
        public EvaluationContinuation resultFound(FoundResult foundResult) {
            matchingPaths.add(fromBracketNotation(foundResult.path()));
            return EvaluationContinuation.CONTINUE;
        }

        public List<String> getMatchingPaths() {
            return matchingPaths;
        }
    }
}
