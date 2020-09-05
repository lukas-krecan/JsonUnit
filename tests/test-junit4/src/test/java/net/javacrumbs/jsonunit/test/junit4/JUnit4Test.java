package net.javacrumbs.jsonunit.test.junit4;


import org.junit.Test;

import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class JUnit4Test {

    @Test
    public void testComplexErrors() {
        assertThatThrownBy(() -> assertJsonEquals("{\n" +
                "   \"test\":[\n" +
                "      1,\n" +
                "      2,\n" +
                "      {\n" +
                "         \"child\":{\n" +
                "            \"value1\":1,\n" +
                "            \"value2\":true,\n" +
                "            \"value3\":\"test\",\n" +
                "            \"value4\":{\n" +
                "               \"leaf\":5\n" +
                "            }\n" +
                "         }\n" +
                "      }\n" +
                "   ],\n" +
                "   \"root2\":false,\n" +
                "   \"root3\":1\n" +
                "}",
            "{\n" +
                "   \"test\":[\n" +
                "      5,\n" +
                "      false,\n" +
                "      {\n" +
                "         \"child\":{\n" +
                "            \"value1\":5,\n" +
                "            \"value2\":\"true\",\n" +
                "            \"value3\":\"test\",\n" +
                "            \"value4\":{\n" +
                "               \"leaf2\":5\n" +
                "            }\n" +
                "         },\n" +
                "         \"child2\":{\n" +
                "\n" +
                "         }\n" +
                "      }\n" +
                "   ],\n" +
                "   \"root4\":\"bar\"\n" +
                "}"
        ))
            .hasMessage("JSON documents are different:\n" +
                "Different keys found in node \"\", missing: \"root2\",\"root3\", extra: \"root4\", expected: <{\"root2\":false,\"root3\":1,\"test\":[1, 2, {\"child\":{\"value1\":1,\"value2\":true,\"value3\":\"test\",\"value4\":{\"leaf\":5}}}]}> but was: <{\"root4\":\"bar\",\"test\":[5, false, {\"child\":{\"value1\":5,\"value2\":\"true\",\"value3\":\"test\",\"value4\":{\"leaf2\":5}},\"child2\":{}}]}>\n" +
                "Different value found in node \"test[0]\", expected: <1> but was: <5>.\n" +
                "Different value found in node \"test[1]\", expected: <2> but was: <false>.\n" +
                "Different keys found in node \"test[2]\", extra: \"test[2].child2\", expected: <{\"child\":{\"value1\":1,\"value2\":true,\"value3\":\"test\",\"value4\":{\"leaf\":5}}}> but was: <{\"child\":{\"value1\":5,\"value2\":\"true\",\"value3\":\"test\",\"value4\":{\"leaf2\":5}},\"child2\":{}}>\n" +
                "Different value found in node \"test[2].child.value1\", expected: <1> but was: <5>.\n" +
                "Different value found in node \"test[2].child.value2\", expected: <true> but was: <\"true\">.\n" +
                "Different keys found in node \"test[2].child.value4\", missing: \"test[2].child.value4.leaf\", extra: \"test[2].child.value4.leaf2\", expected: <{\"leaf\":5}> but was: <{\"leaf2\":5}>\n");

    }

}
