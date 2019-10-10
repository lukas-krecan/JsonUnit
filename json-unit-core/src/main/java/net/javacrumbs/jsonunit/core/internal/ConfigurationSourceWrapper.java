/**
 * Copyright 2009-2019 the original author or authors.
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
package net.javacrumbs.jsonunit.core.internal;

import net.javacrumbs.jsonunit.core.ConfigurationSource;
import net.javacrumbs.jsonunit.core.Option;
import net.javacrumbs.jsonunit.core.listener.DifferenceListener;
import org.hamcrest.Matcher;

import java.math.BigDecimal;
import java.util.Set;

class ConfigurationSourceWrapper implements ConfigurationSource {
    @Override
    public Set<String> getPathsToBeIgnored() {
        return wrapped.getPathsToBeIgnored();
    }

    @Override
    public DifferenceListener getDifferenceListener() {
        return wrapped.getDifferenceListener();
    }

    @Override
    public boolean hasOption(String path, Option option) {
        return wrapped.hasOption(path, option);
    }

    @Override
    public BigDecimal getTolerance() {
        return wrapped.getTolerance();
    }

    @Override
    public Matcher<?> getMatcher(String matcherName) {
        return wrapped.getMatcher(matcherName);
    }

    @Override
    public boolean shouldIgnore(String asText) {
        return wrapped.shouldIgnore(asText);
    }

    private final ConfigurationSource wrapped;

    public ConfigurationSourceWrapper(ConfigurationSource wrapped) {
        this.wrapped = wrapped;
    }
}
