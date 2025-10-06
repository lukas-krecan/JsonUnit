package net.javacrumbs.jsonunit.spring.testit;

import static java.util.Collections.singleton;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.jsonUnitJson;
import static net.javacrumbs.jsonunit.spring.JsonUnitJsonComparator.comparator;
import static net.javacrumbs.jsonunit.spring.testit.demo.ExampleController.CORRECT_JSON;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.Option;
import net.javacrumbs.jsonunit.spring.testit.demo.ExampleController;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

public class AssertJMockMvcTest {

    @Test
    void shouldWorkWithMockMvcTester() {
        MockMvcTester mvc = MockMvcTester.of(new ExampleController());
        assertThat(mvc.get().uri("/sample"))
                .hasStatusOk()
                .bodyJson()
                .isEqualTo(CORRECT_JSON, comparator(Configuration.empty()));
    }

    @Test
    void shouldUseConvertToJackson() {
        MockMvcTester mvc = MockMvcTester.of(new ExampleController())
                .withHttpMessageConverters(singleton(new JacksonJsonHttpMessageConverter()));
        assertThat(mvc.get().uri("/sample"))
                .hasStatusOk()
                .bodyJson()
                .convertTo(jsonUnitJson())
                .when(Option.IGNORING_ARRAY_ORDER)
                .inPath("result.array")
                .isArray()
                .containsExactly(1, 2, 3);
    }
}
