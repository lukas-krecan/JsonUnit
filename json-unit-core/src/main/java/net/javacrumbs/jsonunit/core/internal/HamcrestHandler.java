package net.javacrumbs.jsonunit.core.internal;

import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.ParametrizedMatcher;
import org.hamcrest.Description;
import org.hamcrest.StringDescription;

import java.util.regex.Matcher;

import static net.javacrumbs.jsonunit.core.internal.Diff.quoteTextValue;


/**
 * Hamcrest dependent classes moved here so we can theoretically work without it.
 */
class HamcrestHandler {
    private final Configuration configuration;
    private final DifferenceReporter valueDifferenceReporter;
    private final DifferenceReporter structureDifferenceReporter;

    HamcrestHandler(Configuration configuration, DifferenceReporter valueDifferenceReporter, DifferenceReporter structureDifferenceReporter) {
        this.configuration = configuration;
        this.valueDifferenceReporter = valueDifferenceReporter;
        this.structureDifferenceReporter = structureDifferenceReporter;
    }

    void matchHamcrestMatcher(Context context, Node actualNode, Matcher patternMatcher, String matcherName) {
        org.hamcrest.Matcher<?> matcher = configuration.getMatcher(matcherName);
        if (matcher != null) {
            if (matcher instanceof ParametrizedMatcher) {
                ((ParametrizedMatcher) matcher).setParameter(patternMatcher.group(2));
            }
            Object value = actualNode.getValue();
            if (!matcher.matches(value)) {
                Description description = new StringDescription();
                matcher.describeMismatch(value, description);
                valueDifferenceReporter.differenceFound(context, "Matcher \"%s\" does not match value %s in node \"%s\". %s", matcherName, quoteTextValue(actualNode), context.getActualPath(), description);
            }
        } else {
            structureDifferenceReporter.differenceFound(context, "Matcher \"%s\" not found.", matcherName);
        }
    }

    @FunctionalInterface
    interface DifferenceReporter {
        void differenceFound(Context context, String message, Object... arguments);
    }
}
