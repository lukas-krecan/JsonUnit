/**
 * Copyright 2009-2018 the original author or authors.
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
package net.javacrumbs.jsonunit.core;

/**
 * Implement this interface to use with parametrized matchers. Sample usage
 *
 * <pre>
 *     <code>
 *
 *     Matcher&lt;?&gt; divisionMatcher = new DivisionMatcher();
 *     assertJsonEquals("{\"test\": \"${json-unit.matches:isDivisibleBy}3\"}", "{\"test\":5}", JsonAssert.withMatcher("isDivisibleBy", divisionMatcher));
 *
 *     private static class DivisionMatcher extends BaseMatcher&lt;Object&gt; implements ParametrizedMatcher {
 *       private BigDecimal param;
 *
 *       public boolean matches(Object item) {
 *           return ((BigDecimal)item).remainder(param).compareTo(ZERO) == 0;
 *       }
 *
 *       public void describeTo(Description description) {
 *           description.appendValue(param);
 *       }
 *
 *       public void describeMismatch(Object item, Description description) {
 *           description.appendText("It is not divisible by ").appendValue(param);
 *       }
 *
 *       public void setParameter(String parameter) {
 *           this.param = new BigDecimal(parameter);
 *       }
 *   }
 *
 *
 *   </code>
 * </pre>
 */
public interface ParametrizedMatcher {
    void setParameter(String parameter);
}
