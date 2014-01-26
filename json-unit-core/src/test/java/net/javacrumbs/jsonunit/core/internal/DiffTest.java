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
package net.javacrumbs.jsonunit.core.internal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class DiffTest {
	private static final ObjectMapper MAPPER = new ObjectMapper();
	@Test
	public void testGetStartNodeRoot() throws IOException {
		JsonNode startNode = Diff.getStartNode(MAPPER.readTree("{\"test\":{\"value\":1}}"), "");
		assertEquals("{\"test\":{\"value\":1}}", MAPPER.writeValueAsString(startNode));
	}
	@Test
	public void testGetStartNodeSimple() throws IOException {
		JsonNode startNode = Diff.getStartNode(MAPPER.readTree("{\"test\":{\"value\":1}}"), "test");
		assertEquals("{\"value\":1}", MAPPER.writeValueAsString(startNode));
	}
	@Test
	public void testGetStartNodeTwoSteps() throws IOException {
		JsonNode startNode = Diff.getStartNode(MAPPER.readTree("{\"test\":{\"value\":1}}"), "test.value");
		assertEquals("1", MAPPER.writeValueAsString(startNode));
	}
	@Test
	public void testGetStartNodeArrays() throws IOException {
		JsonNode startNode = Diff.getStartNode(MAPPER.readTree("{\"test\":{\"values\":[1,2]}}"), "test.values[1]");
		assertEquals("2", MAPPER.writeValueAsString(startNode));
	}
	@Test
	public void testGetStartNodeArrays2() throws IOException {
		JsonNode startNode = Diff.getStartNode(MAPPER.readTree("{\"test\":[{\"values\":[1,2]}, {\"values\":[3,4]}]}"), "test[1].values[1]");
		assertEquals("4", MAPPER.writeValueAsString(startNode));
	}
	@Test
	public void testGetStartNodeNonexisting() throws IOException {
		JsonNode startNode = Diff.getStartNode(MAPPER.readTree("{\"test\":{\"value\":1}}"), "test.bogus");
		assertEquals(true, startNode.isMissingNode());
	}

}
