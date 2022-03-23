package net.javacrumbs.jsonunit.core.internal;

import java.util.regex.Pattern;

import static net.javacrumbs.jsonunit.core.internal.GenericMatcher.Result.continueProcessing;
import static net.javacrumbs.jsonunit.core.internal.GenericMatcher.Result.stopProcessing;

public class AnyGenericMatcher implements GenericMatcher {
    // DO NOT remove redundant character escapes, they are needed for Android https://github.com/lukas-krecan/JsonUnit/pull/227
    private static final Pattern ANY_NUMBER_PLACEHOLDER = Pattern.compile("[$#]\\{json-unit.any-number\\}");
    private static final Pattern ANY_BOOLEAN_PLACEHOLDER = Pattern.compile("[$#]\\{json-unit.any-boolean\\}");
    private static final Pattern ANY_STRING_PLACEHOLDER = Pattern.compile("[$#]\\{json-unit.any-string\\}");


    @Override
    public Result matches(Context context) {
        // Any number
        if (checkAny(Node.NodeType.NUMBER, ANY_NUMBER_PLACEHOLDER, "a number", context)) {
            return stopProcessing();
        }

        // Any boolean
        if (checkAny(Node.NodeType.BOOLEAN, ANY_BOOLEAN_PLACEHOLDER, "a boolean", context)) {
            stopProcessing();
        }

        // Any string
        if (checkAny(Node.NodeType.STRING, ANY_STRING_PLACEHOLDER, "a string", context)) {
            stopProcessing();
        }

        return continueProcessing();
    }

    private boolean checkAny(Node.NodeType type, Pattern placeholder, String name, Context context) {
        Node expectedNode = context.getExpectedNode();
        Node actualNode = context.getActualNode();

        if (expectedNode.getNodeType() == Node.NodeType.STRING && placeholder.matcher(expectedNode.asText()).matches()) {
            if (actualNode.getNodeType() == type) {
                return true;
            } else {
                reportValueDifference(context, "Different value found in node \"%s\", " + differenceString() + ".", context.getActualPath(), name, quoteTextValue(actualNode));
                return true;
            }
        }
        return false;
    }

}
