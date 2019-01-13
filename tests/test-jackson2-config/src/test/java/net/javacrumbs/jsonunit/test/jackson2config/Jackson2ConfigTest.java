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
package net.javacrumbs.jsonunit.test.jackson2config;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

class Jackson2ConfigTest {

    @Test
    void testSerializeTime() {
        assertThatJson(new Bean()).isEqualTo("{time:'2019-01-11T18:12:00Z'}");
    }

    @Test
    void testSerializeTime2() {
        assertThatJson(new Bean()).isEqualTo(new Bean());
    }

    public static class Bean {
        private final Instant time = Instant.parse("2019-01-11T18:12:00Z");

        public Instant getTime() {
            return time;
        }
    }
}
