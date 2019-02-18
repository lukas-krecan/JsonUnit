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

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.error.AssertionErrorCreator;
import org.opentest4j.MultipleFailuresError;

import java.util.List;

public class JsonSoftAssertions extends SoftAssertions {

    private final AssertionErrorCreator assertionErrorCreator = new AssertionErrorCreator();

    /**
     * Verifies that no soft assertions have failed.
     *
     * @throws MultipleFailuresError if possible or SoftAssertionError if any proxied assertion objects threw an {@link AssertionError}
     */
    public void assertAll() {
        List<Throwable> errors = errorsCollected();
        if (!errors.isEmpty()) throw assertionErrorCreator.multipleSoftAssertionsError(errors);
    }

    public JsonAssert assertThatJson(Object actual) {
        return proxy(JsonAssert.ConfigurableJsonAssert.class, Object.class, actual);
    }
}
