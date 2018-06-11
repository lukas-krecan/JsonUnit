package net.javacrumbs.jsonunit.core.internal;

import net.javacrumbs.jsonunit.core.listener.Difference;

class DifferenceImpl implements Difference {
    private final Context context;
    private final Type type;

    DifferenceImpl(Context context, Type type) {
        this.context = context;
        this.type = type;
    }

    @Override
    public String getActualPath() {
        return context.getActualPath() != null ? context.getActualPath().getFullPath() : null;
    }

    @Override
    public String getExpectedPath() {
        return context.getExpectedPath() != null ? context.getExpectedPath().getFullPath() : null;
    }

    @Override
    public Object getActual() {
        Node actualNode = context.getActualNode();
        return actualNode != null && !actualNode.isMissingNode() ? actualNode.getValue() : null;
    }

    @Override
    public Object getExpected() {
        Node expectedNode = context.getExpectedNode();
        return expectedNode != null && !expectedNode.isMissingNode() ? expectedNode.getValue() : null;
    }

    @Override
    public String toString() {
        return getType() + " Expected " + getExpected() + " in " + getExpectedPath() + " got " + getActual() + " in " + getActualPath();
    }

    private static class MissingDifference extends DifferenceImpl {
        MissingDifference(Context context) {
            super(context, Type.MISSING);
        }

        @Override
        public String toString() {
            return getType() + " " + getExpected() + " in " + getExpectedPath();
        }
    }

    private static class ExtraDifference extends DifferenceImpl {
        ExtraDifference(Context context) {
            super(context, Type.EXTRA);
        }

        @Override
        public String toString() {
            return getType() + " " + getActual() + " in " + getActualPath();
        }
    }

    @Override
    public Type getType() {
        return type;
    }

    static Difference missing(Context context) {
        return new MissingDifference(context);
    }

    static Difference extra(Context context) {
        return new ExtraDifference(context);
    }

    static Difference different(Context context) {
        return new DifferenceImpl(context, Type.DIFFERENT);
    }
}
