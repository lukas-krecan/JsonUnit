package net.javacrumbs.jsonunit.core.internal;

public class AnyGenericMatcher implements GenericMatcher {
    @Override
    public Result matches(Context context) {
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
