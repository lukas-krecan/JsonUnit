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
package net.javacrumbs.jsonunit.test.base;

import com.google.gson.JsonParser;
import com.squareup.moshi.Moshi;
import org.json.JSONTokener;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.fail;

public class JsonTestUtils {

    public static Object readByJackson2(String value) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().readTree(value);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static Object readByJackson1(String value) {
        try {
            return new org.codehaus.jackson.map.ObjectMapper().readTree(value);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static Object readByGson(String value) {
        return new JsonParser().parse(value);
    }

    public static Object readByJsonOrg(String value) {
            return new JSONTokener(value).nextValue();
        }

    public static Object readByMoshi(String value) {
        Moshi moshi = new Moshi.Builder().build();
        try {
            return moshi.adapter(Map.class).fromJson(value);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static void failIfNoException() {
        fail("Exception expected");
    }
}
