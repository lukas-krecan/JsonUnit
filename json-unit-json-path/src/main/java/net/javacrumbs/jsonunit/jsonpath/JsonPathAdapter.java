/**
 * Copyright 2009-2018 the original author or authors.
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

import com.jayway.jsonpath.JsonPath;
import net.javacrumbs.jsonunit.core.internal.JsonUtils;

import static net.javacrumbs.jsonunit.core.internal.JsonUtils.jsonSource;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.wrapDeserializedObject;

/**
 * Adapts json-path to json-unit.
 */
public final class JsonPathAdapter {
    private JsonPathAdapter() {

    }

    public static Object inPath(Object json, String path) {
        if (json instanceof String) {
            return jsonSource(wrapDeserializedObject(JsonPath.read((String) json, path)), path);
        } else {
            return jsonSource(wrapDeserializedObject(JsonPath.read(JsonUtils.convertToJson(json, "actual").getValue(), path)), path);
        }
    }
}
