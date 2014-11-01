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
package net.javacrumbs.jsonunit.core.internal;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts object to Node using {@link net.javacrumbs.jsonunit.core.internal.NodeFactory}.
 */
class Converter {

    private final List<NodeFactory> factories;

    private static final boolean jackson1Present =
            isClassPresent("org.codehaus.jackson.map.ObjectMapper") &&
                    isClassPresent("org.codehaus.jackson.JsonGenerator");

    private static final boolean jackson2Present =
            isClassPresent("com.fasterxml.jackson.databind.ObjectMapper") &&
                    isClassPresent("com.fasterxml.jackson.core.JsonGenerator");

    private static final boolean gsonPresent =
            isClassPresent("com.google.gson.Gson");

    Converter(List<NodeFactory> factories) {
        if (factories.isEmpty()) {
            throw new IllegalStateException("List of factories can not be empty");
        }
        this.factories = factories;
    }

    /**
     * Creates converter based on the libraries on the classpath.
     *
     * @return
     */
    public static Converter createDefaultConverter() {
        List<NodeFactory> factories = new ArrayList<NodeFactory>();

        if (jackson1Present) {
            factories.add(new Jackson1NodeFactory());
        }

        if (gsonPresent) {
            factories.add(new GsonNodeFactory());
        }

        if (jackson2Present) {
            factories.add(new Jackson2NodeFactory());
        }
        if (factories.isEmpty()) {
            throw new IllegalStateException("Please add either Jackson 1.x, Jackson 2.x or Gson to the classpath");
        }
        return new Converter(factories);
    }

    public Node convertToNode(Object source, String label) {
        for (int i = 0; i < factories.size(); i++) {
            NodeFactory factory = factories.get(i);
            if (factory.isPreferredFor(source) || isLastFactory(i)) {
                return factory.convertToNode(source, label);
            }
        }
        throw new IllegalStateException("Should not happen");
    }

    private boolean isLastFactory(int i) {
        return factories.size() - 1 == i;
    }


    /**
     * Checks if given class is present.
     *
     * @param className
     * @return
     */
    static boolean isClassPresent(String className) {
        try {
            Converter.class.getClassLoader().loadClass(className);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }
}
