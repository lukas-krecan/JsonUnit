package net.javacrumbs.jsonunit.differences;

public interface Differences {

	String getDifferenceType();
	void add(String message, Object... args);
	void add(String message);
	boolean isEmpty();
	void appendDifferences(StringBuilder builder);
}
