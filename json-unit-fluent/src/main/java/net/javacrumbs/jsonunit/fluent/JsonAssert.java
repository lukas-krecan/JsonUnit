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
package net.javacrumbs.jsonunit.fluent;

import org.codehaus.jackson.JsonNode;
import net.javacrumbs.jsonunit.core.internal.Options;


/**
 * Contains JSON related fluent assertions inspired by FEST or AssertJ. Typical usage is:
 * <p/>
 * <code>
 * assertThatJson("{\"test\":1}").isEqualTo("{\"test\":2}");
 * assertThatJson("{\"test\":1}").hasSameStructureAs("{\"test\":21}");
 * assertThatJson("{\"root\":{\"test\":1}}").node("root.test").isEqualTo("2");
 * </code>
 * <p/>
 * Please note that the method name is assertThatJson and not assertThat. The reason is that we need to accept String parameter
 * and do not want to override standard FEST or AssertJ assertThat(String) method.
 *
 * @deprecated use JsonFluentAssert instead
 */
@Deprecated
public class JsonAssert extends JsonFluentAssert {
    protected JsonAssert(JsonNode actual, String path, String description, String ignorePlaceholder) {
        super(actual, path, description, ignorePlaceholder, null, Options.empty());
    }

    public JsonAssert(JsonNode actual) {
        super(actual);
    }
}
