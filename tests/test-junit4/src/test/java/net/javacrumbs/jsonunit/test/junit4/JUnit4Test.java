package net.javacrumbs.jsonunit.test.junit4;

import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.Test;

public class JUnit4Test {

    @Test
    public void testComplexErrors() {
        assertThatThrownBy(
                        () -> assertJsonEquals(
                                """
                {
                   "test":[
                      1,
                      2,
                      {
                         "child":{
                            "value1":1,
                            "value2":true,
                            "value3":"test",
                            "value4":{
                               "leaf":5
                            }
                         }
                      }
                   ],
                   "root2":false,
                   "root3":1
                }""",
                                """
                {
                   "test":[
                      5,
                      false,
                      {
                         "child":{
                            "value1":5,
                            "value2":"true",
                            "value3":"test",
                            "value4":{
                               "leaf2":5
                            }
                         },
                         "child2":{

                         }
                      }
                   ],
                   "root4":"bar"
                }"""))
                .hasMessage(
                        """
                JSON documents are different:
                Different keys found in node "", missing: "root2","root3", extra: "root4", expected: <{"root2":false,"root3":1,"test":[1, 2, {"child":{"value1":1,"value2":true,"value3":"test","value4":{"leaf":5}}}]}> but was: <{"root4":"bar","test":[5, false, {"child":{"value1":5,"value2":"true","value3":"test","value4":{"leaf2":5}},"child2":{}}]}>
                Different value found in node "test[0]", expected: <1> but was: <5>.
                Different value found in node "test[1]", expected: <2> but was: <false>.
                Different keys found in node "test[2]", extra: "test[2].child2", expected: <{"child":{"value1":1,"value2":true,"value3":"test","value4":{"leaf":5}}}> but was: <{"child":{"value1":5,"value2":"true","value3":"test","value4":{"leaf2":5}},"child2":{}}>
                Different value found in node "test[2].child.value1", expected: <1> but was: <5>.
                Different value found in node "test[2].child.value2", expected: <true> but was: <"true">.
                Different keys found in node "test[2].child.value4", missing: "test[2].child.value4.leaf", extra: "test[2].child.value4.leaf2", expected: <{"leaf":5}> but was: <{"leaf2":5}>
                """);
    }
}
