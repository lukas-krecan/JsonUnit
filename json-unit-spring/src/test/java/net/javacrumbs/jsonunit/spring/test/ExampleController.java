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
package net.javacrumbs.jsonunit.spring.test;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

import static java.util.Collections.singletonMap;

@RestController
public class ExampleController {

    @GetMapping(value = "/sample")
    public Object get() {
        return singletonMap("result", new Result());
    }


    @GetMapping(value = "/sampleProduces", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object getWithProduces() {
        return singletonMap("result", new Result());
    }

    private static class Result {
        public String getString() {
            return "stringValue";
        }
        public int[] getArray() {
            return new int[]{1, 2, 3};
        }
        public BigDecimal getDecimal() {
            return new BigDecimal("1.00001");
        }
        public boolean getBoolean() {
            return true;
        }
        public Object getNull() {
            return null;
        }

        public String getUtf8() {
            return "€";
        }
    }
}
