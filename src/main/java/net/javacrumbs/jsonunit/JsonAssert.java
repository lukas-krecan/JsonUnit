package net.javacrumbs.jsonunit;

import java.io.IOException;

import junit.framework.AssertionFailedError;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

/**
 * Assertions for comparing JSON.
 * @author Lukas Krecan
 *
 */
//TODO: Make compatible with assertThat
//TODO: isValidJSon
//TODO: Different strategies for different number types
//TODO: Binary
public class JsonAssert {
	
	private static final ObjectMapper MAPPER = new ObjectMapper();
	
	private JsonAssert(){
		//nothing
	}
	
	public static void  assertJsonEquals(String expected, String actual) {
		ObjectNode expectedNode = readValue(expected, "expected");
		ObjectNode actualNode = readValue(actual, "actual");
		Diff diff = new Diff(expectedNode, actualNode);
		if (!diff.similar()) {
			doFail(diff.toString());
		}
	}

	
	private static ObjectNode readValue(String value, String label) {
		try {
			return MAPPER.readValue(value, ObjectNode.class);
		} catch (IOException e) {
			throw new IllegalArgumentException("Can not parse "+label+" value.", e);
		}
	}
	/**
	 * Fails a test with the given message.
	 */
	private static void doFail(String message) {
		if (message == null) {
			throw new AssertionFailedError();
		}
		throw new AssertionFailedError(message);
	}
	
	
}
