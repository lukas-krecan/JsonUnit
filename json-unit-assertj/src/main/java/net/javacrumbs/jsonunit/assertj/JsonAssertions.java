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
package net.javacrumbs.jsonunit.assertj;

import net.javacrumbs.jsonunit.assertj.JsonAssert.ConfigurableJsonAssert;
import net.javacrumbs.jsonunit.assertj.internal.JsonRepresentation;
import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.internal.JsonUtils;
import org.assertj.core.api.AssertFactory;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactory;
import org.jspecify.annotations.Nullable;

public final class JsonAssertions {

    /**
     * Allows to move from standard AssertJ asserts to JsonUnit. For example
     * <code>
     *       assertThat(resp)
     *             .hasFieldOrPropertyWithValue("trackingId", "abcd-0001")  // &lt;- Assertj API
     *             .extracting("json").asInstanceOf(JSON)
     *             .isObject().containsEntry("foo", "bar"); // &lt;- JsonUnit API
     * </code>
     */
    public static final InstanceOfAssertFactory<Object, ConfigurableJsonAssert> JSON =
            new InstanceOfAssertFactory<>(Object.class, jsonUnitJson());

    static {
        Assertions.useRepresentation(new JsonRepresentation());
    }

    private JsonAssertions() {}

    public static ConfigurableJsonAssert assertThatJson(@Nullable Object actual) {
        return new ConfigurableJsonAssert(actual, Configuration.empty());
    }

    /**
     * Assert json properties with possibility to chain assertion callbacks like this
     *
     * <pre>{@code
     * assertThatJson("{\"test1\":2, \"test2\":1}",
     *             json -> json.inPath("test1").isEqualTo(2),
     *             json -> json.inPath("test2").isEqualTo(1)
     *         );
     * }</pre>
     */
    public static ConfigurableJsonAssert assertThatJson(Object actual, JsonAssertionCallback... callbacks) {
        ConfigurableJsonAssert a = assertThatJson(actual);
        for (JsonAssertionCallback callback : callbacks) {
            callback.doAssert(a);
        }
        return a;
    }

    /**
     * JSON to be used in expected part of the assertion,
     *
     *
     * @return Object suitable for comparison. Implementation type may change in the future.
     */
    public static Object json(Object input) {
        return new ExpectedNode(JsonUtils.convertToJson(input, "expected", true));
    }

    /**
     * Value passed here is not parsed as JSON but used as it is
     */
    public static Object value(Object input) {
        return new ExpectedNode(JsonUtils.wrapDeserializedObject(input));
    }

    /**
     * Returns AssertFactory allowing to move from standard AssertJ to JsonUnit. For example:
     * <code>
     *      assertThat(mvc.get().uri("/sample"))
     *                 .hasStatusOk()
     *                 .bodyJson()
     *                 .convertTo(jsonUnitJson())
     *                 .inPath("result.array") &lt;-- JsonUnit assert
     *                 .isArray()
     *                 .containsExactly(1, 2, 3);
     * </code>
     */
    public static AssertFactory<Object, ConfigurableJsonAssert> jsonUnitJson() {
        return new JsonUnitAssertFactory();
    }

    @FunctionalInterface
    public interface JsonAssertionCallback {
        void doAssert(ConfigurableJsonAssert assertion);
    }

    private static class JsonUnitAssertFactory implements AssertFactory<Object, ConfigurableJsonAssert> {
        @Override
        public ConfigurableJsonAssert createAssert(Object actual) {
            return new ConfigurableJsonAssert(actual, Configuration.empty());
        }
    }
}
