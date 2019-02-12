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

import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.internal.Diff;
import net.javacrumbs.jsonunit.core.internal.Node;
import net.javacrumbs.jsonunit.core.internal.Path;

import java.util.Comparator;

import static net.javacrumbs.jsonunit.core.internal.JsonUtils.wrapDeserializedObject;

class JsonComparator implements Comparator<Object> {
    private final Configuration configuration;
    private final Path path;
    private final boolean actualParsed;

    JsonComparator(Configuration configuration, Path path, boolean actualParsed) {
        this.configuration = configuration;
        this.path = path;
        this.actualParsed = actualParsed;
    }

    @Override
    public int compare(Object actual, Object expected) {
        // this comparator is not transitive, `expected` is usually a Node and `actual` is usually a Map, or primitive
        if (
            (actualParsed && !(actual instanceof Node) && (expected instanceof Node) && !(expected instanceof ExpectedNode)) ||
            (actual instanceof ExpectedNode)
        ) {
            Object tmp = actual;
            actual = expected;
            expected = tmp;
        }

        // if actual is already parsed, do not parse it again.
        Object actual2 = actualParsed ? wrapDeserializedObject(actual) : actual;

        Diff diff = Diff.create(expected, actual2, "", path, configuration);
        if (diff.similar()) {
            return 0;
        } else {
            return -1;
        }
    }
}
