package net.javacrumbs.jsonunit.core.internal;

public interface NodeFactory {
    /**
     * Returns true if this factory is preferred for given source.
     * @param source
     * @return
     */
    public boolean isPreferredFor(Object source);

    /**
     * Creates node from given source.
     * @param source
     * @param label
     * @return
     */
    public Node convertToNode(Object source, String label);
}
