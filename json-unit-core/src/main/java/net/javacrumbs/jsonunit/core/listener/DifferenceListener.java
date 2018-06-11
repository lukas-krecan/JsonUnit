package net.javacrumbs.jsonunit.core.listener;


/**
 * Can listen on differences between documents.
 */
public interface DifferenceListener {
    void diff(Difference difference, DifferenceContext context);
}
