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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import org.jspecify.annotations.Nullable;

/**
 * Resource reading utility
 */
class Utils {
    static String readAsString(Reader resourceReader) throws IOException {
        if (resourceReader instanceof JsonStringReader jsonStringReader) {
            return jsonStringReader.getString();
        }

        StringBuilder builder = new StringBuilder();
        char[] arr = new char[8 * 1024];
        int numCharsRead;
        while ((numCharsRead = resourceReader.read(arr, 0, arr.length)) != -1) {
            builder.append(arr, 0, numCharsRead);
        }
        resourceReader.close();
        return builder.toString();
    }

    static Reader toReader(String string) {
        return new JsonStringReader(string);
    }

    @SuppressWarnings("EmptyCatch")
    static void closeQuietly(@Nullable Reader resourceReader) {
        if (resourceReader != null) {
            try {
                resourceReader.close();
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * StringReader giving access to the enclosed String.
     */
    static class JsonStringReader extends StringReader {
        private final String string;

        public JsonStringReader(String s) {
            super(s);
            this.string = s;
        }

        public String getString() {
            return string;
        }
    }
}
