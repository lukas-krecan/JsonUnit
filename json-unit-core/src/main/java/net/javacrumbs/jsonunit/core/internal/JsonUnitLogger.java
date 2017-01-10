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
package net.javacrumbs.jsonunit.core.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * There are some people that do not like dependency on SLF4J. Let's not force them to use it.
 */
interface JsonUnitLogger {

    boolean isEnabled();

    void log(String message, Object... params);

    final class SLF4JLogger implements JsonUnitLogger {
        private final Logger logger;

        SLF4JLogger(String name) {
            logger = LoggerFactory.getLogger(name);
        }

        public boolean isEnabled() {
            return logger.isDebugEnabled();
        }

        public void log(String message, Object... params) {
            logger.debug(message, params);
        }
    }

    final class NullLogger implements JsonUnitLogger {
        public boolean isEnabled() {
            return false;
        }

        public void log(String message, Object... params) {}
    }

}
