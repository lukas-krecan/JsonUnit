/**
 * Copyright 2009-2017 the original author or authors.
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
package net.javacrumbs.jsonunit;

import net.javacrumbs.jsonunit.core.Option;
import net.javacrumbs.jsonunit.core.internal.Options;
import org.hamcrest.Matcher;

import java.math.BigDecimal;

/**
 * JsonMatcher interface.
 *
 * @param <T>
 */
public interface ConfigurableJsonMatcher<T> extends Matcher<T> {

    /**
     * Set's numeric comparison tolerance. If none set, value form {@link net.javacrumbs.jsonunit.JsonAssert#getTolerance()}  is used.
     */
    ConfigurableJsonMatcher<T> withTolerance(BigDecimal tolerance);

    /**
     * Set's numeric comparison tolerance. If none set, value form {@link net.javacrumbs.jsonunit.JsonAssert#getTolerance()}  is used.
     */
    ConfigurableJsonMatcher<T> withTolerance(double tolerance);

    /**
     * Add options. Note that options are ADDED to those set by
     * by net.javacrumbs.jsonunit.JsonAssert.setOptions()
     */
    ConfigurableJsonMatcher<T> when(Option first, Option... next);

    /**
     * Sets options.
     */
    ConfigurableJsonMatcher<T> withOptions(Options options);
}
