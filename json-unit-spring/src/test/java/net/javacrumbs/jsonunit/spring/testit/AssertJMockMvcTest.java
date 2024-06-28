package net.javacrumbs.jsonunit.spring.testit;

import static java.util.Collections.singleton;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.jsonUnitAssert;
import static net.javacrumbs.jsonunit.spring.JsonUnitJsonComparator.comparator;
import static net.javacrumbs.jsonunit.spring.JsonUnitJsonComparator.jsonUnitMessageConverter;
import static net.javacrumbs.jsonunit.spring.testit.demo.ExampleController.CORRECT_JSON;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.spring.testit.demo.ExampleController;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
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
    void shouldUseConvertToDirect() {
        MockMvcTester mvc = MockMvcTester.of(new ExampleController())
                .withHttpMessageConverters(singleton(jsonUnitMessageConverter()));
        assertThat(mvc.get().uri("/sample"))
                .hasStatusOk()
                .bodyJson()
                .convertTo(jsonUnitAssert())
                .inPath("result.array")
                .isArray()
                .containsExactly(1, 2, 3);
    }

    @Test
    void shouldUseConvertToJAckson() {
        MockMvcTester mvc = MockMvcTester.of(new ExampleController())
                .withHttpMessageConverters(singleton(new MappingJackson2HttpMessageConverter()));
        assertThat(mvc.get().uri("/sample"))
                .hasStatusOk()
                .bodyJson()
                .convertTo(jsonUnitAssert())
                .inPath("result.array")
                .isArray()
                .containsExactly(1, 2, 3);
    }
}
