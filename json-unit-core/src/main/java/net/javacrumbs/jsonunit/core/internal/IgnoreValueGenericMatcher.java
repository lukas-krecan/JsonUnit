package net.javacrumbs.jsonunit.core.internal;

import static net.javacrumbs.jsonunit.core.internal.GenericMatcher.Result.continueProcessing;
import static net.javacrumbs.jsonunit.core.internal.GenericMatcher.Result.stopProcessing;

class IgnoreValueGenericMatcher implements GenericMatcher {
    private final String ignorePlaceholder;

    private static final String DEFAULT_IGNORE_PLACEHOLDER = "${json-unit.ignore}";
    private static final String ALTERNATIVE_IGNORE_PLACEHOLDER = "#{json-unit.ignore}";

    IgnoreValueGenericMatcher(String ignorePlaceholder) {
        this.ignorePlaceholder = ignorePlaceholder;
    }

    @Override
    public Result matches(Context context) {
        Node expectedNode = context.getExpectedNode();
        if (expectedNode.getNodeType() == Node.NodeType.STRING && shouldIgnore(expectedNode.asText())) {
            return stopProcessing();
        } else {
            return continueProcessing();
        }
    }

    private boolean shouldIgnore(String expectedValue) {
        if (DEFAULT_IGNORE_PLACEHOLDER.equals(ignorePlaceholder)) {
            // special handling of default state. We want to support both # and $ before {json-unit.ignore} but do not want to
            // override user specified value if any
            return DEFAULT_IGNORE_PLACEHOLDER.equals(expectedValue) || ALTERNATIVE_IGNORE_PLACEHOLDER.equals(expectedValue);
        } else {
            return ignorePlaceholder.equals(expectedValue);
        }
    }
}
