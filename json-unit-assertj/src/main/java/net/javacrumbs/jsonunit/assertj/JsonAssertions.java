/**
 * Copyright 2009-2017 the original author or authors.
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
package net.javacrumbs.jsonunit.assertj;

import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.internal.JsonUtils;
import net.javacrumbs.jsonunit.core.internal.Path;

import static net.javacrumbs.jsonunit.core.internal.JsonUtils.getPathPrefix;

public class JsonAssertions {
    public static JsonAssert assertThatJson(Object actual) {
      return new JsonAssert(Path.create("", getPathPrefix(actual)), Configuration.empty(), actual);
    }

    /**
     * JSON to be used in expected part of the assertion,
     * @param input
     * @return Object suitable for comparison. Implementation type may change in the future.
     */
    public static Object json(Object input) {
         return JsonUtils.convertToJson(input, "", true);
     }
}
