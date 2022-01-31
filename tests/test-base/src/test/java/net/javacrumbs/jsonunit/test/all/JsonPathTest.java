package net.javacrumbs.jsonunit.test.all;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

public class JsonPathTest {
    private static final String json = "{\n" +
        "    \"store\": {\n" +
        "        \"book\": [\n" +
        "            {\n" +
        "                \"category\": \"reference\",\n" +
        "                \"author\": \"Nigel Rees\",\n" +
        "                \"title\": \"Sayings of the Century\",\n" +
        "                \"price\": 8.95\n" +
        "            },\n" +
        "            {\n" +
        "                \"category\": \"fiction\",\n" +
        "                \"author\": \"Evelyn Waugh\",\n" +
        "                \"title\": \"Sword of Honour\",\n" +
        "                \"price\": 12.99\n" +
        "            },\n" +
        "            {\n" +
        "                \"category\": \"fiction\",\n" +
        "                \"author\": \"Herman Melville\",\n" +
        "                \"title\": \"Moby Dick\",\n" +
        "                \"isbn\": \"0-553-21311-3\",\n" +
        "                \"price\": 8.99\n" +
        "            },\n" +
        "            {\n" +
        "                \"category\": \"fiction\",\n" +
        "                \"author\": \"J. R. R. Tolkien\",\n" +
        "                \"title\": \"The Lord of the Rings\",\n" +
        "                \"isbn\": \"0-395-19395-8\",\n" +
        "                \"price\": 22.99\n" +
        "            }\n" +
        "        ],\n" +
        "        \"bicycle\": {\n" +
        "            \"color\": \"red\",\n" +
        "            \"price\": 19.95\n" +
        "        }\n" +
        "    },\n" +
        "    \"expensive\": 10\n" +
        "}";

    @Test
    void testJsonPath() {
        assertThat(bookLength()).describedAs("First run").isEqualTo(4);
        assertThat(bookLength()).describedAs("Second run").isEqualTo(4);
        assertThat(bookLength()).describedAs("Third run").isEqualTo(4);
    }

    private int bookLength() {
        return JsonPath.read(json, "$..book.length()");
    }
}
