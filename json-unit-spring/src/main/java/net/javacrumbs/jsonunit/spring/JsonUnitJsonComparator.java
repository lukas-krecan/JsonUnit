package net.javacrumbs.jsonunit.spring;

import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.internal.Diff;
import org.jspecify.annotations.Nullable;
import org.springframework.test.json.JsonComparator;
import org.springframework.test.json.JsonComparison;

/**
 * Implements Spring's JsonComparator. The integration API is pretty limited, so
 * you are better off using JsonUnit directly.
 */
public class JsonUnitJsonComparator implements JsonComparator {

    private final Configuration configuration;

    private JsonUnitJsonComparator(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Creates JsonUnit backed JsonComparator with given configuration.
     */
    public static JsonComparator comparator(Configuration configuration) {
        return new JsonUnitJsonComparator(configuration);
    }

    /**
     * Creates JsonUnit backed JsonComparator
     */
    public static JsonComparator comparator() {
        return new JsonUnitJsonComparator(Configuration.empty());
    }

    @Override
    public JsonComparison compare(@Nullable String expectedJson, @Nullable String actualJson) {
        Diff diff = Diff.create(expectedJson, actualJson, "actual", "", configuration);
        if (diff.similar()) {
            return JsonComparison.match();
        } else {
            return JsonComparison.mismatch(diff.differences());
        }
    }
}
