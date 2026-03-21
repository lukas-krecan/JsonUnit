package net.javacrumbs.jsonunit.spring;

import org.jspecify.annotations.Nullable;

@FunctionalInterface
interface JsonTransformer {

    @Nullable
    Object transform(@Nullable Object input);

    static JsonTransformer identity() {
        return input -> input;
    }
}
