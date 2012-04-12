package net.javacrumbs.jsonunit;

import static org.junit.Assert.*;

import java.io.IOException;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

public class DiffTest {
	private static final ObjectMapper MAPPER = new ObjectMapper();
	@Test
	public void testGetStartNodeRoot() throws JsonProcessingException, IOException {
		JsonNode startNode = Diff.getStartNode(MAPPER.readTree("{\"test\":{\"value\":1}}"), "");
		assertEquals("{\"test\":{\"value\":1}}", MAPPER.writeValueAsString(startNode));
	}
	@Test
	public void testGetStartNodeSimple() throws JsonProcessingException, IOException {
		JsonNode startNode = Diff.getStartNode(MAPPER.readTree("{\"test\":{\"value\":1}}"), "test");
		assertEquals("{\"value\":1}", MAPPER.writeValueAsString(startNode));
	}
	@Test
	public void testGetStartNodeTwoSteps() throws JsonProcessingException, IOException {
		JsonNode startNode = Diff.getStartNode(MAPPER.readTree("{\"test\":{\"value\":1}}"), "test.value");
		assertEquals("1", MAPPER.writeValueAsString(startNode));
	}
	@Test
	public void testGetStartNodeArrays() throws JsonProcessingException, IOException {
		JsonNode startNode = Diff.getStartNode(MAPPER.readTree("{\"test\":{\"values\":[1,2]}}"), "test.values[1]");
		assertEquals("2", MAPPER.writeValueAsString(startNode));
	}
	@Test
	public void testGetStartNodeArrays2() throws JsonProcessingException, IOException {
		JsonNode startNode = Diff.getStartNode(MAPPER.readTree("{\"test\":[{\"values\":[1,2]}, {\"values\":[3,4]}]}"), "test[1].values[1]");
		assertEquals("4", MAPPER.writeValueAsString(startNode));
	}
	@Test
	public void testGetStartNodeNonexisting() throws JsonProcessingException, IOException {
		JsonNode startNode = Diff.getStartNode(MAPPER.readTree("{\"test\":{\"value\":1}}"), "test.bogus");
		assertEquals(true, startNode.isMissingNode());
	}

}
