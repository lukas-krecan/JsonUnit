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

import static net.javacrumbs.jsonunit.core.internal.ClassUtils.isClassPresent;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Converts object to Node using {@link NodeFactory}.
 */
record Converter(List<NodeFactory> factories) {
    static final String LIBRARIES_PROPERTY_NAME = "json-unit.libraries";

    private static final boolean jackson2Present = isClassPresent("com.fasterxml.jackson.databind.ObjectMapper")
            && isClassPresent("com.fasterxml.jackson.core.JsonGenerator");

    private static final boolean gsonPresent = isClassPresent("com.google.gson.Gson");

    private static final boolean jsonOrgPresent = isClassPresent("org.json.JSONObject");

    private static final boolean moshiPresent = isClassPresent("com.squareup.moshi.Moshi");

    private static final boolean johnzonPresent = isClassPresent("org.apache.johnzon.mapper.Mapper");

    Converter {
        if (factories.isEmpty()) {
            throw new IllegalStateException("List of factories can not be empty");
        }
    }

    /**
     * Creates converter based on the libraries on the classpath.
     */
    static Converter createDefaultConverter() {
        List<NodeFactory> factories;
        String property = System.getProperty(LIBRARIES_PROPERTY_NAME);

        if (property != null && !property.trim().isEmpty()) {
            factories = createFactoriesSpecifiedInProperty(property);
        } else {
            factories = createDefaultFactories();
        }

        if (factories.isEmpty()) {
            throw new IllegalStateException(
                    "Please add either json.org, Jackson 1.x, Jackson 2.x, Johnzon or Gson to the classpath");
        }
        return new Converter(factories);
    }

    private static List<NodeFactory> createFactoriesSpecifiedInProperty(String property) {
        List<NodeFactory> factories = new ArrayList<>();
        for (String factoryName : property.toLowerCase().split(",")) {
            factoryName = factoryName.trim();
            switch (factoryName) {
                case "moshi" -> factories.add(new MoshiNodeFactory());
                case "json.org" -> factories.add(new JsonOrgNodeFactory());
                case "jackson2" -> factories.add(new Jackson2NodeFactory());
                case "gson" -> factories.add(new GsonNodeFactory());
                case "johnzon" -> factories.add(new JohnzonNodeFactory());
                default -> throw new IllegalArgumentException("'" + factoryName + "' library name not recognized.");
            }
        }
        return factories;
    }

    private static List<NodeFactory> createDefaultFactories() {
        List<NodeFactory> factories = new ArrayList<>();
        if (moshiPresent) {
            factories.add(new MoshiNodeFactory());
        }

        if (johnzonPresent) {
            factories.add(new JohnzonNodeFactory());
        }

        if (jsonOrgPresent) {
            factories.add(new JsonOrgNodeFactory());
        }

        if (gsonPresent) {
            factories.add(new GsonNodeFactory());
        }

        if (jackson2Present) {
            factories.add(new Jackson2NodeFactory());
        }
        return factories;
    }

    @NotNull
    Node convertToNode(@Nullable Object source, String label, boolean lenient) {
        for (int i = 0; i < factories.size(); i++) {
            NodeFactory factory = factories.get(i);
            if (isLastFactory(i) || factory.isPreferredFor(source)) {
                return factory.convertToNode(source, label, lenient);
            }
        }
        throw new IllegalStateException("Should not happen");
    }

    @NotNull
    Node valueToNode(Object source) {
        for (int i = 0; i < factories.size(); i++) {
            NodeFactory factory = factories.get(i);
            if (isLastFactory(i) || factory.isPreferredFor(source)) {
                return factory.valueToNode(source);
            }
        }
        throw new IllegalStateException("Should not happen");
    }

    private boolean isLastFactory(int i) {
        return factories.size() - 1 == i;
    }
}
