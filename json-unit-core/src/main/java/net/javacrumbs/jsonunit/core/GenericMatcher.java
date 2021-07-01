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
package net.javacrumbs.jsonunit.core;

import java.util.Optional;

public interface GenericMatcher {
    /**
     * Matches expected and actual value. If they do match ar the matcher is does not support the expected value,
     * it returns an empty Optional. If it want's to report mismatch, return non-empty optional.
     */
    //TODO: Accept Object in expected??
    Optional<Mismatch> matches(String expected, Object actual, String actualPath);

    final class Mismatch {
        private final String difference;

        public Mismatch(String difference) {
            this.difference = difference;
        }

        public String getDifference() {
            return difference;
        }
    }
}


