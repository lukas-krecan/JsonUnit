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
package net.javacrumbs.jsonunit.assertj;

import net.javacrumbs.jsonunit.assertj.JsonAssert.ConfigurableJsonAssert;
import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.internal.JsonUtils;

public final class JsonAssertions {
    private JsonAssertions() {
        
    }

    public static ConfigurableJsonAssert assertThatJson(Object actual) {
        return new ConfigurableJsonAssert(actual, Configuration.empty());
    }

    /**
     * JSON to be used in expected part of the assertion,
     *
     * @param input
     * @return Object suitable for comparison. Implementation type may change in the future.
     */
    public static Object json(Object input) {
        return new ExpectedNode(JsonUtils.convertToJson(input, "", true));
    }


    /**
     * Value passed here is not parsed as JSON but used as it is
     */
    public static Object value(Object input) {
        return new ExpectedNode(JsonUtils.wrapDeserializedObject(input));
    }
}
