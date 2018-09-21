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

import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.Option;
import net.javacrumbs.jsonunit.core.internal.Path;
import net.javacrumbs.jsonunit.jsonpath.JsonPathAdapter;

import java.util.function.Function;

import static net.javacrumbs.jsonunit.core.internal.JsonUtils.getPathPrefix;

public class JsonAssert extends AbstractJsonAssert<JsonAssert> {

    JsonAssert(Path path, Configuration configuration, Object actual) {
        super(path, configuration, actual, JsonAssert.class);
    }

    JsonAssert(Object actual, Configuration configuration) {
        this(Path.create("", getPathPrefix(actual)), configuration, actual);
    }


    /**
     * JsonAssert that can be configured to prevent mistakes like
     *
     * <code>
     * assertThatJson(...).isEqualsTo(...).when(...);
     * </code>
     */
    public static class ConfigurableJsonAssert extends AbstractJsonAssert<ConfigurableJsonAssert> {
        ConfigurableJsonAssert(Path path, Configuration configuration, Object actual) {
            super(path, configuration, actual, ConfigurableJsonAssert.class);
        }

        ConfigurableJsonAssert(Object actual, Configuration configuration) {
            super(Path.create("", getPathPrefix(actual)), configuration, actual, ConfigurableJsonAssert.class);
        }

        /**
         * Adds comparison options.
         */
        public ConfigurableJsonAssert when(Option first, Option... other) {
            return withConfiguration(c -> c.when(first, other));
        }

        /**
         * Allows to configure like this
         *
         * <code>
         *     assertThatJson(...)
         *             .withConfiguration(c -> c.withMatcher("positive", greaterThan(valueOf(0)))
         *             ....
         * </code>
         */
        public ConfigurableJsonAssert withConfiguration(Function<Configuration, Configuration> configurationFunction) {
            return new ConfigurableJsonAssert(path, configurationFunction.apply(configuration), actual);
        }

        /**
         * Uses json-path to extract node(s) and use them in further comparison
         * (Can not be chained since it would lead to end ot he universe as we know it)
         */
        public JsonAssert inPath(String jsonPath) {
            return new JsonAssert(JsonPathAdapter.inPath(actual, jsonPath), configuration);
        }
    }

}
