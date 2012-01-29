/**
 * Copyright 2009-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.javacrumbs.jsonunit;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

/**
 * Assertions for comparing JSON. The comparison ignores whitespaces and order of nodes. 
 * @author Lukas Krecan
 *
 */
 public class JsonAssert {
	
	private static final ObjectMapper MAPPER = new ObjectMapper();
	
	private JsonAssert(){
		//nothing
	}
	
	/**
	 * Compares to JSON documents. Throws {@link AssertionError} if they are different.
	 * @param expected
	 * @param actual
	 */
	public static void  assertJsonEquals(String expected, String actual) {
		assertJsonEquals(new StringReader(expected), new StringReader(actual));
	}
	
	/**
	 * Compares to JSON documents. Throws {@link AssertionError} if they are different.
	 * @param expected
	 * @param actual
	 */
	public static void  assertJsonEquals(Reader expected, Reader actual) {
		ObjectNode expectedNode = readValue(expected, "expected");
		ObjectNode actualNode = readValue(actual, "actual");
		assertJsonEquals(expectedNode, actualNode);
	}

	/**
	 * Compares to JSON documents. Throws {@link AssertionError} if they are different.
	 * @param expectedNode
	 * @param actualNode
	 */
	public static void assertJsonEquals(ObjectNode expectedNode, ObjectNode actualNode) {
		Diff diff = new Diff(expectedNode, actualNode);
		if (!diff.similar()) {
			doFail(diff.toString());
		}
	}

	
	private static ObjectNode readValue(Reader value, String label) {
		try {
			return MAPPER.readValue(value, ObjectNode.class);
		} catch (IOException e) {
			throw new IllegalArgumentException("Can not parse "+label+" value.", e);
		}
	}
	/**
	 * Fails a test with the given message.
	 */
	private static void doFail(String diffMessage) {
		throw new AssertionError(diffMessage);
	}
	
	
}
