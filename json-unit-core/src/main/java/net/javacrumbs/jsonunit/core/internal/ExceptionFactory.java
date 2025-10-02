package net.javacrumbs.jsonunit.core.internal;

import static java.util.stream.Collectors.toList;
import static net.javacrumbs.jsonunit.core.internal.ExceptionUtils.formatDifferences;

import java.util.Collections;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.opentest4j.AssertionFailedError;
import org.opentest4j.MultipleFailuresError;

interface ExceptionFactory {
    AssertionError createException(@Nullable String message, Differences diffs);
}

class Opentest4jExceptionFactory implements ExceptionFactory {
    @Override
    public AssertionError createException(@Nullable String message, Differences diffs) {
        List<JsonDifference> differences = diffs.getDifferences();
        if (differences.size() == 1) {
            JsonDifference difference = differences.get(0);
            return new AssertionFailedError(
                    formatDifferences(message, Collections.singletonList(difference)),
                    difference.getExpected(),
                    difference.getActual());
        } else {
            return new JsonAssertError(message, diffs);
        }
    }

    private static class JsonAssertError extends MultipleFailuresError {
        private final @Nullable String message;
        private final Differences differences;

        JsonAssertError(@Nullable String message, Differences differences) {
            super(
                    message,
                    differences.getDifferences().stream()
                            .map(JsonAssertError::getError)
                            .collect(toList()));
            this.message = message;
            this.differences = differences;
        }

        private static AssertionFailedError getError(JsonDifference difference) {
            return new AssertionFailedError(
                    difference.getMessage(),
                    difference.getExpected() != null ? difference.getExpected().getValue() : null,
                    difference.getActual() != null ? difference.getActual().getValue() : null);
        }

        @Override
        public String getMessage() {
            return formatDifferences(message, differences);
        }
    }
}
