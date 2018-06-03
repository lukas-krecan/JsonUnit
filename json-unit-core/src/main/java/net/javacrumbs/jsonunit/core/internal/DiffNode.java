package net.javacrumbs.jsonunit.core.internal;

public abstract class DiffNode {

    private Path path;
    private Node actual;
    private Node expected;

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public Node getActual() {
        return actual;
    }

    public void setActual(Node actual) {
        this.actual = actual;
    }

    public Node getExpected() {
        return expected;
    }

    public void setExpected(Node expected) {
        this.expected = expected;
    }

    private DiffNode(Path path, Node actual, Node expected) {
        this.path = path;
        this.actual = actual;
        this.expected = expected;
    }

    public abstract String toString();

    public static class Added extends DiffNode {

        private Added(Path path, Node actual, Node expected) {
            super(path, actual, expected);
        }

        @Override
        public String toString() {
            return String.format("ADDED %s", this.getPath().getFullPath());
        }
    }

    public static class Removed extends DiffNode {

        private Removed(Path path, Node actual, Node expected) {
            super(path, actual, expected);
        }

        @Override
        public String toString() {
            return String.format("REMOVED %s %s", this.getPath().getFullPath());
        }
    }

    public static class Changed extends DiffNode {

        private Changed(Path path, Node actual, Node expected) {
            super(path, actual, expected);
        }

        @Override
        public String toString() {
            return String.format("CHANGED %s", this.getPath().getFullPath());
        }
    }

    static DiffNode removed(Path path, Node expected) {
        return new Removed(path, null, expected);
    }

    static DiffNode added(Path path, Node actual) {
        return new Added(path, actual, null);
    }

    static DiffNode changed(Path path, Node actual, Node expected) {
        return new Changed(path, actual, expected);
    }
}
