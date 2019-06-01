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
package net.javacrumbs.jsonunit.test.johnzon;

import net.javacrumbs.jsonunit.test.base.AbstractJsonFluentAssertTest;
import net.javacrumbs.jsonunit.test.base.JsonTestUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;

import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;

public class JohnzonJsonFluentAssertTest extends AbstractJsonFluentAssertTest {
    @Override
    protected Object readValue(String value) {
        return JsonTestUtils.readByJohnzon(value);
    }

    @Test
    public void shouldNotChangeObject() {
        JsonObject json = Json.createObjectBuilder()
            .add("test", JsonValue.NULL)
            .build();
        assertThatJson(json).isEqualTo("{\"test\":null}");
    }

    @Override
    @Disabled
    public void shouldAllowUnquotedKeysAndCommentInExpectedValue() {
        //not supported
    }
}
