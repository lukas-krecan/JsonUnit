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

import net.javacrumbs.jsonunit.core.internal.Diff;
import org.codehaus.jackson.JsonNode;

import static net.javacrumbs.jsonunit.core.internal.JsonUtils.convertToJson;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.convertToJsonQuoteIfNeeded;

/**
 * Assertions for comparing JSON. The comparison ignores white-spaces and order of nodes.
 * @author Lukas Krecan
 *
 */
 public class JsonAssert {

    private static final String EXPECTED = "expected";
    public static final String FULL_JSON = "fullJson";
    public static final String ACTUAL = "actual";
    private static String ignorePlaceholder = "${json-unit.ignore}";

	private JsonAssert(){
		//nothing
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
   	 * Compares to JSON documents. Throws {@link AssertionError} if they are different.
   	 * @param expected
   	 * @param actual
   	 */
   	public static void assertJsonEquals(Object expected, Object actual) {
   		assertJsonPartEquals(convertExpectedToJson(expected), convertActualToJson(actual), "");
   	}

    /**
	 * Compares part of the JSON. Path has this format "root.array[0].value".
	 * @param expected
	 * @param fullJson
	 * @param path
	 */
	public static void assertJsonPartEquals(JsonNode expected, JsonNode fullJson, String path) {
		Diff diff = new Diff(expected, fullJson, path, ignorePlaceholder);
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
	public static void assertJsonPartEquals(Object expected, Object fullJson, String path) {
		assertJsonPartEquals(convertExpectedToJson(expected), convertToJson(fullJson, FULL_JSON), path);
	}

	/**
	 * Compares structures of two JSON documents.
	 * Throws {@link AssertionError} if they are different.
	 * @param expected
	 * @param actual
	 */
	public static void  assertJsonStructureEquals(Object expected, Object actual) {
		assertJsonStructureEquals(convertExpectedToJson(expected), convertActualToJson(actual));
	}

	/**
	 * Compares structures of two JSON documents.
	 * Throws {@link AssertionError} if they are different.
	 * @param expectedNode
	 * @param actualNode
	 */
	public static void assertJsonStructureEquals(JsonNode expectedNode, JsonNode actualNode) {
		assertJsonPartStructureEquals(expectedNode, actualNode, "");
	}

	/**
	 * Compares structure of part of the JSON. Path has this format "root.array[0].value".
	 * @param expected
	 * @param fullJson
	 * @param path
	 */
	public static void assertJsonPartStructureEquals(JsonNode expected, JsonNode fullJson, String path) {
		Diff diff = new Diff(expected, fullJson, path, ignorePlaceholder);
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
	public static void assertJsonPartStructureEquals(Object expected, Object fullJson, String path) {
		assertJsonPartStructureEquals(convertExpectedToJson(expected), convertToJson(fullJson, FULL_JSON), path);
	}

	/**
	 * Fails a test with the given message.
	 */
	private static void doFail(String diffMessage) {
		throw new AssertionError(diffMessage);
	}


    private static JsonNode convertExpectedToJson(Object expected) {
        return convertToJsonQuoteIfNeeded(expected, EXPECTED);
    }

    private static JsonNode convertActualToJson(Object actual) {
        return convertToJson(actual, ACTUAL);
    }

    /**
     * Set's string that will be ignored in comparison. Default value is "${json-unit.ignore}"
     * @param ignorePlaceholder
     */
    public static void setIgnorePlaceholder(String ignorePlaceholder) {
        JsonAssert.ignorePlaceholder = ignorePlaceholder;
    }

    public static String getIgnorePlaceholder() {
        return ignorePlaceholder;
    }
}
