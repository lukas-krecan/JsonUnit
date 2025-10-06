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
package net.javacrumbs.jsonunit.providers;

import tools.jackson.databind.ObjectMapper;

/**
 * Interface for customizing Jackson 3 ObjectMapper. @see <a href="https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html">SPI intro</a>
 */
public interface Jackson3ObjectMapperProvider {
    /**
     * Provides ObjectMapper
     * @param lenient Lenient parsing is used for parsing the expected JSON value
     * @return customized ObjectMapper.
     */
    ObjectMapper getObjectMapper(boolean lenient);
}
