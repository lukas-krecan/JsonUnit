/**
 * Copyright 2009-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.javacrumbs.jsonunit.springkotlin.test

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.util.Collections

@RestController
class ExampleController {
    @GetMapping(value = ["/sample"])
    fun get(): Any {
        return Collections.singletonMap("result", Result())
    }

    @GetMapping(value = ["/sampleProduces"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun withProduces() = Collections.singletonMap("result", Result())

    class Result {
        val string: String = "stringValue"

        val array: IntArray = intArrayOf(1, 2, 3)

        val decimal: BigDecimal = BigDecimal("1.00001")

        val boolean: Boolean = true

        val `null`: Any? = null

        val utf8: String = "€"
    }
}
