package net.javacrumbs.jsonunit.core.internal;

import static net.javacrumbs.jsonunit.core.internal.GenericMatcher.Result.continueProcessing;
import static net.javacrumbs.jsonunit.core.internal.GenericMatcher.Result.stopProcessing;

class IgnoreElementGenericMatcher implements GenericMatcher {
    @Override
    public Result matches(Context context) {
        if (shouldIgnoreElement(context.getExpectedNode())) {
            return stopProcessing();
        } else {
            return continueProcessing();
        }
    }

    static boolean shouldIgnoreElement(Node expectedNode) {
        return expectedNode.getNodeType() == Node.NodeType.STRING && "${json-unit.ignore-element}".equals(expectedNode.asText());
    }
}
