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

import java.util.ArrayList;
import java.util.List;

import static net.javacrumbs.jsonunit.core.internal.ClassUtils.isClassPresent;

/**
 * Converts object to Node using {@link net.javacrumbs.jsonunit.core.internal.NodeFactory}.
 */
class Converter {
    static final String LIBRARIES_PROPERTY_NAME = "json-unit.libraries";

    private final List<NodeFactory> factories;

    private static final boolean jackson1Present =
        isClassPresent("org.codehaus.jackson.map.ObjectMapper") &&
            isClassPresent("org.codehaus.jackson.JsonGenerator");

    private static final boolean jackson2Present =
        isClassPresent("com.fasterxml.jackson.databind.ObjectMapper") &&
            isClassPresent("com.fasterxml.jackson.core.JsonGenerator");

    private static final boolean gsonPresent =
        isClassPresent("com.google.gson.Gson");

    private static final boolean jsonOrgPresent =
        isClassPresent("org.json.JSONObject");

    private static final boolean moshiPresent =
        isClassPresent("com.squareup.moshi.Moshi");

    Converter(List<NodeFactory> factories) {
        if (factories.isEmpty()) {
            throw new IllegalStateException("List of factories can not be empty");
        }
        this.factories = factories;
    }

    /**
     * Creates converter based on the libraries on the classpath.
     */
    static Converter createDefaultConverter() {
        List<NodeFactory> factories;
        String property = System.getProperty(LIBRARIES_PROPERTY_NAME);

        if (property != null && property.trim().length() > 0) {
            factories = createFactoriesSpecifiedInProperty(property);
        } else {
            factories = createDefaultFactories();
        }

        if (factories.isEmpty()) {
            throw new IllegalStateException("Please add either json.org, Jackson 1.x, Jackson 2.x or Gson to the classpath");
        }
        return new Converter(factories);
    }

    private static List<NodeFactory> createFactoriesSpecifiedInProperty(String property) {
        List<NodeFactory> factories = new ArrayList<NodeFactory>();
        for (String factoryName : property.toLowerCase().split(",")) {
            factoryName = factoryName.trim();
            if ("moshi".equals(factoryName)) {
                factories.add(new MoshiNodeFactory());
            } else if ("json.org".equals(factoryName)) {
                factories.add(new JsonOrgNodeFactory());
            } else if ("jackson1".equals(factoryName)) {
                factories.add(new Jackson1NodeFactory());
            } else if ("jackson2".equals(factoryName)) {
                factories.add(new Jackson2NodeFactory());
            } else if ("gson".equals(factoryName)) {
                factories.add(new GsonNodeFactory());
            } else {
                throw new IllegalArgumentException("'" +factoryName + "' library name not recognized.");
            }
        }
        return factories;
    }

    private static List<NodeFactory> createDefaultFactories() {
        List<NodeFactory> factories = new ArrayList<NodeFactory>();
        if (moshiPresent) {
            factories.add(new MoshiNodeFactory());
        }

        if (jsonOrgPresent) {
            factories.add(new JsonOrgNodeFactory());
        }

        if (jackson1Present) {
            factories.add(new Jackson1NodeFactory());
        }

        if (gsonPresent) {
            factories.add(new GsonNodeFactory());
        }

        if (jackson2Present) {
            factories.add(new Jackson2NodeFactory());
        }
        return factories;
    }

    Node convertToNode(Object source, String label, boolean lenient) {
        for (int i = 0; i < factories.size(); i++) {
            NodeFactory factory = factories.get(i);
            if (isLastFactory(i) || factory.isPreferredFor(source)) {
                return factory.convertToNode(source, label, lenient);
            }
        }
        throw new IllegalStateException("Should not happen");
    }

    private boolean isLastFactory(int i) {
        return factories.size() - 1 == i;
    }

    List<NodeFactory> getFactories() {
        return factories;
    }
}
