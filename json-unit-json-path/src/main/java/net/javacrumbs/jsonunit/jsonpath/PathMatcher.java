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

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.InvalidJsonException;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.internal.path.PathCompiler;
import com.jayway.jsonpath.spi.json.JsonProvider;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.jayway.jsonpath.JsonPath.using;

public class PathMatcher {

    public boolean matches(String path, String jsonPath) {
        Configuration configuration = Configuration
            .defaultConfiguration()
            .addOptions(Option.AS_PATH_LIST , Option.SUPPRESS_EXCEPTIONS )
            .jsonProvider(new PathJsonProvider());

        List<String> resolvedPaths = using(configuration).parse(path).read(jsonPath);
        return !resolvedPaths.isEmpty();
    }

    private static class PathSegment {
        private final PathSegment nextSegment;
        private final String value;

        private PathSegment(PathSegment nextSegment, String value) {
            this.nextSegment = nextSegment;
            this.value = value;
        }

        private boolean isMap() {
            return nextSegment != null && !nextSegment.isArrayIndex();
        }

        private boolean isArray() {
            return nextSegment != null && nextSegment.isArrayIndex();
        }

        private boolean isArrayIndex() {
            return !value.startsWith("'");
        }

        public String getValue() {
            if (isArrayIndex()) {
                return value;
            } else {
                return value.substring(1, value.length() - 1);
            }

        }
    }


    private static class PathJsonProvider implements JsonProvider {
        private static final Pattern pathPattern = Pattern.compile("\\[(.*?)]");

        private List<String> segments(String path) {
            List<String> result = new ArrayList<>();
            String normalizedPath = normalizePath(path);

            Matcher matcher = pathPattern.matcher(normalizedPath);
            while (matcher.find()) {
                String value = matcher.group(1);
                result.add(value);
            }
            return result;
        }

        private String normalizePath(String path) {
            String normalizedPath = PathCompiler.compile(path).toString();
            if (normalizedPath.startsWith("$")) {
                return normalizedPath.substring(1);
            } else {
                return normalizedPath;
            }
        }

        @Override
        public Object parse(String path) throws InvalidJsonException {
            List<String> segments = segments(path);
            PathSegment nextSegment = null;
            for (int i = segments.size() - 1; i >= 0; i--) {
                nextSegment = new PathSegment(nextSegment, segments.get(i));
            }
            return new PathSegment(nextSegment, "$");
        }

        @Override
        public Object parse(InputStream jsonStream, String charset) throws InvalidJsonException {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toJson(Object obj) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object createArray() {
            return new ArrayList<>();
        }

        @Override
        public Object createMap() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isArray(Object obj) {
            return segment(obj).isArray();
        }

        @Override
        public int length(Object obj) {
            return array(obj).size();
        }

        @Override
        public Iterable<?> toIterable(Object obj) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Collection<String> getPropertyKeys(Object obj) {
            return Collections.singleton(segment(obj).nextSegment.getValue());
        }

        @Override
        public Object getArrayIndex(Object obj, int idx) {
            if (obj instanceof PathSegment) {
                PathSegment nextSegment = segment(obj).nextSegment;
                if (nextSegment != null && nextSegment.value.equals(Integer.toString(idx))) {
                    return nextSegment;
                } else {
                    return JsonProvider.UNDEFINED;
                }
            } else {
                return array(obj).get(idx);
            }
        }

        @Override
        public Object getArrayIndex(Object obj, int idx, boolean unwrap) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setArrayIndex(Object array, int idx, Object newValue) {
            array(array).add(idx, newValue);
        }

        @SuppressWarnings("unchecked")
        private List<Object> array(Object array) {
            return (List<Object>) array;
        }

        @Override
        public Object getMapValue(Object obj, String key) {
            PathSegment segment = segment(obj);
            if (segment.nextSegment.getValue().equals(key)) {
                return segment.nextSegment;
            } else {
                return JsonProvider.UNDEFINED;
            }
        }

        @Override
        public void setProperty(Object obj, Object key, Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void removeProperty(Object obj, Object key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isMap(Object obj) {
            return segment(obj).isMap();
        }

        private PathSegment segment(Object obj) {
            return (PathSegment) obj;
        }

        @Override
        public Object unwrap(Object obj) {
            throw new UnsupportedOperationException();
        }
    }
}
