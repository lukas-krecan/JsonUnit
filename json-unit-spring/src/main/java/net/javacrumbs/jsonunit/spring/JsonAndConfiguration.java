package net.javacrumbs.jsonunit.spring;

import net.javacrumbs.jsonunit.core.Configuration;
import org.jspecify.annotations.Nullable;

record JsonAndConfiguration(@Nullable Object json, Configuration configuration) {
}

@FunctionalInterface
interface JsonAndConfigurationTransformer {
    JsonAndConfiguration transform(JsonAndConfiguration input);

    default JsonAndConfiguration transform(@Nullable Object json, Configuration configuration) {
        return transform(new JsonAndConfiguration(json, configuration));
    }
}
