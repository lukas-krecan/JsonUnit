/**
 * Copyright 2009-2018 the original author or authors.
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

import org.hamcrest.Matcher;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * Immutable map of matchers.
 */
class Matchers {
    private final Map<String, Matcher<?>> matcherMap;

    private static final Matchers EMPTY = new Matchers(Collections.emptyMap());

    private Matchers(Map<String, Matcher<?>> matcherMap) {
        this.matcherMap = matcherMap;
    }


    static Matchers empty() {
        return EMPTY;
    }

    public Matchers with(String matcherName, Matcher<?> matcher) {
        Map<String, Matcher<?>> newMatcherMap = new HashMap<>(matcherMap);
        newMatcherMap.put(matcherName, matcher);
        return new Matchers(newMatcherMap);
    }

    public Matcher<?> getMatcher(String matcherName) {
        return matcherMap.get(matcherName);
    }
}
