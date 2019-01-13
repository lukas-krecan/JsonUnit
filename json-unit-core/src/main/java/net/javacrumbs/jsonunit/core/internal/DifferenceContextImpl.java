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
package net.javacrumbs.jsonunit.core.internal;

import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.listener.DifferenceContext;

class DifferenceContextImpl implements DifferenceContext {

    private final Configuration configuration;
    private final Node actualSource;
    private final Node expectedSource;

    private DifferenceContextImpl(Configuration configuration, Node actualSource, Node expectedSource) {
        this.configuration = configuration;
        this.actualSource = actualSource;
        this.expectedSource = expectedSource;
    }


    static DifferenceContextImpl differenceContext(Configuration configuration, Node actualSource, Node expectedSource) {
        return new DifferenceContextImpl(configuration, actualSource, expectedSource);
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public Object getActualSource() {
        return actualSource.getValue();
    }

    @Override
    public Object getExpectedSource() {
        return expectedSource.getValue();
    }
}
