/**
 * Copyright 2009-2013 the original author or authors.
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
package net.javacrumbs.jsonunit.fest;

import net.javacrumbs.jsonunit.core.internal.Diff;
import org.codehaus.jackson.JsonNode;
import org.fest.assertions.api.AbstractAssert;

import static net.javacrumbs.jsonunit.core.internal.JsonUtils.readValue;

public class JsonAssert extends AbstractAssert<JsonAssert, JsonNode> {
    private static String ignorePlaceholder = "${json-unit.ignore}";

    protected JsonAssert(JsonNode actual) {
        super(actual, JsonAssert.class);
    }

    public static JsonAssert assertThat(String json) {
        return new JsonAssert(readValue(json, "actual"));
    }

    @Override
    public JsonAssert isEqualTo(JsonNode expected) {
        isNotNull();

        Diff diff = new Diff(expected, actual, "", ignorePlaceholder);
        if (!diff.similar()) {
            doFail(diff.toString());
        }
        return this;
    }

    public JsonAssert isEqualTo(String expected) {
       return isEqualTo(readValue(expected, "expected"));
    }

    /**
     * Fails a test with the given message.
     */
    private static void doFail(String diffMessage) {
        throw new AssertionError(diffMessage);
    }
}
