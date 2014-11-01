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
package net.javacrumbs.jsonunit.test.jackson2;

import net.javacrumbs.jsonunit.test.base.JsonMatchersTest;
import net.javacrumbs.jsonunit.test.base.JsonUtils;
import org.junit.Test;

import java.io.IOException;

import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.junit.Assert.assertThat;

public class Jackson1JsonMatchersTest extends JsonMatchersTest {
    @Test
    public void testJsonNodeJackson() throws IOException {
        assertThat(JsonUtils.readByJackson1("{\"test\":1}"), jsonEquals("{\"test\":1}"));
    }
}
