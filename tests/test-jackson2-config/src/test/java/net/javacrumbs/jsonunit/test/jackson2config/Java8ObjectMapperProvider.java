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
package net.javacrumbs.jsonunit.test.jackson2config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.javacrumbs.jsonunit.providers.Jackson2ObjectMapperProvider;

public class Java8ObjectMapperProvider implements Jackson2ObjectMapperProvider {
    private final ObjectMapper mapper;

    private final ObjectMapper lenientMapper;


    public Java8ObjectMapperProvider() {
        mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        // Test if we can deserialize lowercase keys only
        mapper.setNodeFactory(new LowercaseNodeFactory(true));

        lenientMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        lenientMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        lenientMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        lenientMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);

        // Test if we can deserialize lowercase keys only
        lenientMapper.setNodeFactory(new LowercaseNodeFactory(true));
    }

    @Override
    public ObjectMapper getObjectMapper(boolean lenient) {
        return lenient ? lenientMapper : mapper;
    }

    private static class LowercaseNodeFactory extends JsonNodeFactory {
        LowercaseNodeFactory(boolean bigDecimalExact) {
            super(bigDecimalExact);
        }

        @Override
        public ObjectNode objectNode() {
            return new LowercaseObjectNode(this);
        }
    }

    private static class LowercaseObjectNode extends ObjectNode {
        LowercaseObjectNode(JsonNodeFactory nc) {
            super(nc);
        }

        @Override
        protected ObjectNode _put(String fieldName, JsonNode value) {
            return super._put(fieldName.toLowerCase(), value);
        }

        @Override
        public JsonNode replace(String fieldName, JsonNode value) {
            return super.replace(fieldName.toLowerCase(), value);
        }

        //TODO: override other methods
    }
}
