package net.javacrumbs.jsonunit.differences;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDifferences implements Differences {

	private String differenceType;
	private List<String> messages = new ArrayList<String>();

	protected AbstractDifferences(String differenceType) {
		this.differenceType = differenceType;
	}

	public String getDifferenceType() {
		return differenceType;
	}

	public void add(String message, Object... args) {
		add(String.format(message, args));
	}

	public void add(String message) {
		messages.add(message);
	}

	public boolean isEmpty() {
		return messages.isEmpty();
	}

	public void appendDifferences(StringBuilder builder) {
		if ( ! messages.isEmpty()) {
			builder.append("JSON documents have different " + getDifferenceType() + ":\n");
			for (String message : messages) {
				builder.append(message).append("\n");
			}
	    }
	}

}
