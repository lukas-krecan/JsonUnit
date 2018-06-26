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
package net.javacrumbs.jsonunit.test.jsonorg;

import net.javacrumbs.jsonunit.test.base.AbstractAssertJTest;
import org.junit.Test;

import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;
import static net.javacrumbs.jsonunit.test.base.JsonTestUtils.readByJsonOrg;

public class JsonOrgAssertJTest extends AbstractAssertJTest {

    @Override
    protected Object readValue(String value) {
            return readByJsonOrg(value);
        }

    @Test
    @Override
    public void shouldAllowUnquotedKeysAndCommentInExpectedValue() {
        assertThatJson("{\"test\":1}").isEqualTo("{test:1}");
    }

    @Test
    @Override
    public void jsonPathShouldBeAbleToUseArrays() {
        // ignored, json org does not keep the order in JSON
    }
}
