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
package net.javacrumbs.jsonunit.core.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import static java.lang.String.format;

/**
 * Resource reading utility
 */
public class ResourceUtils {

    /**
     * Helper method to read a classpath resource.
     */
    public static Reader resource(String resourceName) {
        if (resourceName == null) {
            throw new NullPointerException("'null' passed instead of resource name");
        }

        final InputStream resourceStream = ClassLoader.getSystemResourceAsStream(resourceName);
        if (resourceStream == null) {
            throw new IllegalArgumentException(format("resource '%s' not found", resourceName));
        }

        return new BufferedReader(new InputStreamReader(resourceStream));
    }

    static void closeQuietly(final Reader resourceReader) {
        if (resourceReader != null) {
            try {
                resourceReader.close();
            } catch (IOException ignored) {
            }
        }
    }
}
