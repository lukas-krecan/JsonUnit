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
package net.javacrumbs.jsonunit.test.moshi;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.test.base.JsonTestUtils.readByMoshi;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import net.javacrumbs.jsonunit.test.base.AbstractAssertJTest;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

public class MoshiAssertJTest extends AbstractAssertJTest {

    @Override
    protected @Nullable Object readValue(String value) {
        return readByMoshi(value);
    }

    @Test
    @Override
    protected void shouldAssertInteger() {
        assertThatThrownBy(() -> assertThatJson("{\"a\":1}").node("a").isIntegralNumber())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @Override
    protected void shouldAssert1e0() {
        // Ignored, does not work
    }

    @Test
    @Override
    protected void shouldAssertIntegerFailure() {
        assertThatThrownBy(() -> assertThatJson("{\"a\":1.0}").node("a").isIntegralNumber())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @Override
    protected void shouldDiffCloseNumbers() {
        // Does not work
    }

    @Test
    @Override
    protected void shouldUseCustomNumberComparator() {
        // Does not work
    }

    @Test
    @Override
    protected void shouldEqualNumberInObject() {
        // ignored, no support of object serialization neither
    }

    @Test
    @Override
    protected void assertContainsEntryNumberFailure() {
        // Ignored, does not work
    }
}
