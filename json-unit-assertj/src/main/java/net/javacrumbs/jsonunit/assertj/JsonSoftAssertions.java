/**
 * Copyright 2009-2017 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.javacrumbs.jsonunit.assertj;

import org.assertj.core.api.AbstractSoftAssertions;
import org.assertj.core.api.SoftAssertionError;
import org.assertj.core.api.iterable.Extractor;

import java.util.Arrays;
import java.util.List;

import static java.lang.String.format;
import static org.assertj.core.groups.FieldsOrPropertiesExtractor.extract;

public class JsonSoftAssertions extends AbstractSoftAssertions {
    private Extractor<Throwable, String> errorDescriptionExtractor = throwable -> {
      Throwable cause = throwable.getCause();
      if (cause == null) {
        return throwable.getMessage();
      }
      // error has a cause, display the cause message and the first stack trace elements.
      StackTraceElement[] stackTraceFirstElements = Arrays.copyOf(cause.getStackTrace(), 5);
      String stackTraceDescription = "";
      for (StackTraceElement stackTraceElement : stackTraceFirstElements) {
        stackTraceDescription += format("\tat %s%n", stackTraceElement);
      }
      return format("%s%n" +
                    "cause message: %s%n" +
                    "cause first five stack trace elements:%n" +
                    "%s",
                    throwable.getMessage(),
                    cause.getMessage(),
                    stackTraceDescription);
    };

    /**
     * Verifies that no proxied assertion methods have failed.
     *
     * @throws SoftAssertionError if any proxied assertion objects threw
     */
    public void assertAll() {
      List<Throwable> errors = errorsCollected();
      if (!errors.isEmpty()) throw new SoftAssertionError(describeErrors(errors));
    }

    private List<String> describeErrors(List<Throwable> errors) {
       return extract(errors, errorDescriptionExtractor);
     }

    public JsonAssert assertThatJson(Object actual) {
        return proxy(JsonAssert.ConfigurableJsonAssert.class, Object.class, actual);
    }
}
