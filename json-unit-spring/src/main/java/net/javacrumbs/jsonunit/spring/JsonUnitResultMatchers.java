/**
 * Copyright 2009-2017 the original author or authors.
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
package net.javacrumbs.jsonunit.spring;

import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.Option;
import net.javacrumbs.jsonunit.core.internal.Diff;
import net.javacrumbs.jsonunit.core.internal.JsonUtils;
import net.javacrumbs.jsonunit.core.internal.Node;
import org.hamcrest.Matcher;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

import java.math.BigDecimal;

import static net.javacrumbs.jsonunit.core.internal.Diff.create;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.nodeAbsent;
import static net.javacrumbs.jsonunit.core.internal.Node.NodeType.ARRAY;
import static net.javacrumbs.jsonunit.core.internal.Node.NodeType.OBJECT;
import static net.javacrumbs.jsonunit.core.internal.Node.NodeType.STRING;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Matchers compatible with Spring MVC test framework.
 * <p/>
 * Sample usage:
 * <p/>
 * <code>
 * this.mockMvc.perform(get("/sample").accept(MediaType.APPLICATION_JSON)).andExpect(json().isEqualTo(CORRECT_JSON));
 * </code>
 */
public class JsonUnitResultMatchers {
    private final String path;
    private final Configuration configuration;

    private JsonUnitResultMatchers(String path, Configuration configuration) {
        this.path = path;
        this.configuration = configuration;
    }

    /**
     * Creates JsonUnitResultMatchers to be used for JSON assertions.
     *
     * @return
     */
    public static JsonUnitResultMatchers json() {
        return new JsonUnitResultMatchers("", Configuration.empty());
    }

    /**
     * Creates a matcher object that only compares given node.
     * The path is denoted by JSON path, for example.
     * <p/>
     * <code>
     * this.mockMvc.perform(get("/sample").accept(MediaType.APPLICATION_JSON)).andExpect(json().node("root.test[0]").isEqualTo("1"));
     * </code>
     *
     * @param path
     * @return object comparing only node given by path.
     */
    public JsonUnitResultMatchers node(String path) {
        return new JsonUnitResultMatchers(path, configuration);
    }

    /**
     * Compares JSON for equality. The expected object is converted to JSON
     * before comparison. Ignores order of sibling nodes and whitespaces.
     * <p/>
     * Please note that if you pass a String, it's parsed as JSON which can lead to an
     * unexpected behavior. If you pass in "1" it is parsed as a JSON containing
     * integer 1. If you compare it with a string it fails due to a different type.
     * If you want to pass in real string you have to quote it "\"1\"" or use
     * {@link #isStringEqualTo(String)}.
     * <p/>
     * If the string parameter is not a valid JSON, it is quoted automatically.
     *
     * @param expected
     * @return {@code this} object.
     * @see #isStringEqualTo(String)
     */
    public ResultMatcher isEqualTo(final Object expected) {
        return new AbstractResultMatcher(path, configuration) {
            public void doMatch(Object actual) {
                Diff diff = createDiff(expected, actual);
                if (!diff.similar()) {
                    failWithMessage(diff.differences());
                }
            }
        };
    }

    /**
     * Fails if the selected JSON is not a String or is not present or the value
     * is not equal to expected value.
     */
    public ResultMatcher isStringEqualTo(final String expected) {
        return new AbstractResultMatcher(path, configuration) {
            public void doMatch(Object actual) {
                isString(actual);
                Node node = getNode(actual);
                if (!node.asText().equals(expected)) {
                    failWithMessage("Node \"" + path + "\" is not equal to \"" + expected + "\".");
                }
            }
        };

    }

    /**
     * Fails if compared documents are equal. The expected object is converted to JSON
     * before comparison. Ignores order of sibling nodes and whitespaces.
     */
    public ResultMatcher isNotEqualTo(final String expected) {
        return new AbstractResultMatcher(path, configuration) {
            public void doMatch(Object actual) {
                Diff diff = createDiff(expected, actual);
                if (diff.similar()) {
                    failWithMessage("JSON is equal.");
                }
            }
        };
    }

    /**
     * Fails if the node exists.
     *
     * @return
     */
    public ResultMatcher isAbsent() {
        return new AbstractResultMatcher(path, configuration) {
            public void doMatch(Object actual) {
                if (!nodeAbsent(actual, path, configuration)) {
                    failWithMessage("Node \"" + path + "\" is present.");
                }
            }
        };
    }

    /**
     * Fails if the node is missing.
     */
    public ResultMatcher isPresent() {
        return new AbstractResultMatcher(path, configuration) {
            public void doMatch(Object actual) {
                isPresent(actual);
            }
        };
    }

    /**
     * Fails if the selected JSON is not an Array or is not present.
     *
     * @return
     */
    public ResultMatcher isArray() {
        return new AbstractResultMatcher(path, configuration) {
            public void doMatch(Object actual) {
                isPresent(actual);
                Node node = getNode(actual);
                if (node.getNodeType() != ARRAY) {
                    failOnType(node, "an array");
                }
            }
        };
    }

    /**
     * Fails if the selected JSON is not an Object or is not present.
     */
    public ResultMatcher isObject() {
        return new AbstractResultMatcher(path, configuration) {
            public void doMatch(Object actual) {
                isPresent(actual);
                Node node = getNode(actual);
                if (node.getNodeType() != OBJECT) {
                    failOnType(node, "an object");
                }
            }
        };
    }

    /**
     * Fails if the selected JSON is not a String or is not present.
     */
    public ResultMatcher isString() {
        return new AbstractResultMatcher(path, configuration) {
            public void doMatch(Object actual) {
                isString(actual);
            }
        };
    }

    /**
     * Sets the placeholder that can be used to ignore values.
     * The default value is ${json-unit.ignore}
     */
    public JsonUnitResultMatchers ignoring(String ignorePlaceholder) {
        return new JsonUnitResultMatchers(path, configuration.withIgnorePlaceholder(ignorePlaceholder));
    }

    /**
     * Sets the tolerance for floating number comparison. If set to null, requires exact match of the values.
     * For example, if set to 0.01, ignores all differences lower than 0.01, so 1 and 0.9999 are considered equal.
     */
    public JsonUnitResultMatchers withTolerance(double tolerance) {
        return withTolerance(BigDecimal.valueOf(tolerance));
    }

    /**
     * Sets the tolerance for floating number comparison. If set to null, requires exact match of the values.
     * For example, if set to 0.01, ignores all differences lower than 0.01, so 1 and 0.9999 are considered equal.
     */
    public JsonUnitResultMatchers withTolerance(BigDecimal tolerance) {
        return new JsonUnitResultMatchers(path, configuration.withTolerance(tolerance));
    }

    /**
     * Sets options changing comparison behavior. For more
     * details see {@link net.javacrumbs.jsonunit.core.Option}
     *
     * @see net.javacrumbs.jsonunit.core.Option
     */
    public JsonUnitResultMatchers when(Option firstOption, Option... otherOptions) {
        return new JsonUnitResultMatchers(path, configuration.withOptions(firstOption, otherOptions));
    }

    /**
     * Matches the node using Hamcrest matcher.
     * <p/>
     * <ul>
     * <li>Numbers are mapped to BigDecimal</li>
     * <li>Arrays are mapped to a Collection</li>
     * <li>Objects are mapped to a map so you can use json(Part)Equals or a Map matcher</li>
     * </ul>
     *
     * @param matcher
     * @return
     */
    public ResultMatcher matches(final Matcher<?> matcher) {
        return new AbstractResultMatcher(path, configuration) {
            public void doMatch(Object actual) {
                isPresent(actual);
                Node node = getNode(actual);
                assertThat("Node \"" + path + "\" does not match.", node.getValue(), (Matcher<? super Object>) matcher);
            }
        };

    }

    private static void failWithMessage(String message) {
        throw new AssertionError(message);
    }

    private static abstract class AbstractResultMatcher implements ResultMatcher {
        private final String path;
        private final Configuration configuration;

        protected AbstractResultMatcher(String path, Configuration configuration) {
            this.path = path;
            this.configuration = configuration;
        }

        public void match(MvcResult result) throws Exception {
            Object actual = result.getResponse().getContentAsString();
            doMatch(actual);
        }

        protected Diff createDiff(Object expected, Object actual) {
            return create(expected, actual, "actual", path, configuration);
        }

        protected void isPresent(Object actual) {
            if (nodeAbsent(actual, path, configuration)) {
                failWithMessage("Node \"" + path + "\" is missing.");
            }
        }

        protected void failOnType(Node node, String type) {
            failWithMessage("Node \"" + path + "\" is not " + type + ". The actual value is '" + node + "'.");
        }


        protected Node getNode(Object actual) {
            return JsonUtils.getNode(actual, path);
        }

        protected void isString(Object actual) {
            isPresent(actual);
            Node node = getNode(actual);
            if (node.getNodeType() != STRING) {
                failOnType(node, "a string");
            }
        }

        protected abstract void doMatch(Object actual);
    }


}
