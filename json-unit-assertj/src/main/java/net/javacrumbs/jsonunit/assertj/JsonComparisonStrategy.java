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

import java.util.Set;
import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.Option;

class JsonComparisonStrategy {
    private final Configuration configuration;

    JsonComparisonStrategy(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public String toString() {
        Set<Option> options = configuration.getOptions();
        return "when comparing as JSON" + (!options.isEmpty() ? " with " + options : "");
    }
}
