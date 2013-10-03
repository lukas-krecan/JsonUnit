/**
 * Copyright 2009-2013 the original author or authors.
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

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

class JsonUtils {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    static JsonNode readValue(String value, String label) {
        return readValue(new StringReader(value), label);
    }

    static JsonNode readValue(Reader value, String label) {
   		try {
   			return MAPPER.readTree(value);
   		} catch (IOException e) {
   			throw new IllegalArgumentException("Can not parse "+label+" value.", e);
   		}
   	}
}
