package net.javacrumbs.jsonunit.core;

import net.javacrumbs.jsonunit.core.Option;
import net.javacrumbs.jsonunit.core.listener.DifferenceListener;
import org.hamcrest.Matcher;

import java.math.BigDecimal;
import java.util.Set;

public interface ConfigurationSource {
    Set<String> getPathsToBeIgnored();

    DifferenceListener getDifferenceListener();

    boolean hasOption(String path, Option option);

    BigDecimal getTolerance();

    Matcher<?> getMatcher(String matcherName);

    boolean shouldIgnore(String asText);
}
