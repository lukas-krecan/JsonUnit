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

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

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
	 * Compares two JSON documents. Throws {@link AssertionError} if they are different.
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
		JsonNode expectedNode = readValue(expected, "expected");
		JsonNode actualNode = readValue(actual, "actual");
		assertJsonEquals(expectedNode, actualNode);
	}

	/**
	 * Compares to JSON documents. Throws {@link AssertionError} if they are different.
	 * @param expectedNode
	 * @param actualNode
	 */
	public static void assertJsonEquals(JsonNode expectedNode, JsonNode actualNode) {
		assertJsonPartEquals(expectedNode, actualNode, "");
	}

	/**
	 * Compares part of the JSON. Path has this format "root.array[0].value".
	 * @param expected
	 * @param fullJson
	 * @param path
	 */
	public static void assertJsonPartEquals(JsonNode expected, JsonNode fullJson, String path) {
		Diff diff = new Diff(expected, fullJson, path);
		if (!diff.similar()) {
			doFail(diff.toString());
		}
	}

	/**
	 * Compares part of the JSON. Path has this format "root.array[0].value".
	 * @param expected
	 * @param fullJson
	 * @param path
	 */
	public static void assertJsonPartEquals(Reader expected, Reader fullJson, String path) {
		JsonNode expectedNode = readValue(expected, "expected");
		JsonNode fullJsonNode = readValue(fullJson, "fullJson");
		assertJsonPartEquals(expectedNode, fullJsonNode, path);
	}

	/**
	 * Compares part of the JSON. Path has this format "root.array[0].value".
	 * @param expected
	 * @param fullJson
	 * @param path
	 */
	public static void assertJsonPartEquals(String expected, String fullJson, String path) {
		assertJsonPartEquals(new StringReader(expected), new StringReader(fullJson), path);
	}


	/**
	 * Compares structures of two JSON documents.
	 * Throws {@link AssertionError} if they are different.
	 * @param expected
	 * @param actual
	 */
	public static void  assertJsonStructureEquals(String expected, String actual) {
		assertJsonStructureEquals(new StringReader(expected), new StringReader(actual));
	}

	/**
	 * Compares structures of two JSON documents.
	 * Throws {@link AssertionError} if they are different.
	 * @param expected
	 * @param actual
	 */
	public static void  assertJsonStructureEquals(Reader expected, Reader actual) {
		JsonNode expectedNode = readValue(expected, "expected");
		JsonNode actualNode = readValue(actual, "actual");
		assertJsonStructureEquals(expectedNode, actualNode);
	}

	/**
	 * Compares structures of two JSON documents.
	 * Throws {@link AssertionError} if they are different.
	 * @param expectedNode
	 * @param actualNode
	 */
	public static void assertJsonStructureEquals(JsonNode expectedNode, JsonNode actualNode) {
		assertJsonStructurePartEquals(expectedNode, actualNode, "");
	}

	/**
	 * Compares structure of part of the JSON. Path has this format "root.array[0].value".
	 * @param expected
	 * @param fullJson
	 * @param path
	 */
	public static void assertJsonStructurePartEquals(JsonNode expected, JsonNode fullJson, String path) {
		Diff diff = new Diff(expected, fullJson, path);
		if (!diff.similarStructure()) {
			doFail(diff.structureDifferences());
		}
	}

	/**
	 * Compares structure of part of the JSON. Path has this format "root.array[0].value".
	 * @param expected
	 * @param fullJson
	 * @param path
	 */
	public static void assertJsonStructurePartEquals(Reader expected, Reader fullJson, String path) {
		JsonNode expectedNode = readValue(expected, "expected");
		JsonNode fullJsonNode = readValue(fullJson, "fullJson");
		assertJsonStructurePartEquals(expectedNode, fullJsonNode, path);
	}

	/**
	 * Compares structure of part of the JSON. Path has this format "root.array[0].value".
	 * @param expected
	 * @param fullJson
	 * @param path
	 */
	public static void assertJsonStructurePartEquals(String expected, String fullJson, String path) {
		assertJsonStructurePartEquals(new StringReader(expected), new StringReader(fullJson), path);
	}


	private static JsonNode readValue(Reader value, String label) {
		try {
			return MAPPER.readTree(value);
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
