package net.javacrumbs.jsonunit.spring;

import static net.javacrumbs.jsonunit.spring.Utils.getCharset;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.internal.Diff;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractGenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.test.json.JsonComparator;
import org.springframework.test.json.JsonComparison;
import org.springframework.util.FileCopyUtils;

/**
 * Implements Spring's JsonComparator. The integration API is pretty limited, so
 * you are better off using JsonUnit directly.
 */
public class JsonUnitJsonComparator implements JsonComparator {

    private final Configuration configuration;

    private JsonUnitJsonComparator(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Creates JsonUnit backed JsonComparator with given configuration.
     */
    public static JsonComparator comparator(Configuration configuration) {
        return new JsonUnitJsonComparator(configuration);
    }

    /**
     * Creates JsonUnit backed JsonComparator
     */
    public static JsonComparator comparator() {
        return new JsonUnitJsonComparator(Configuration.empty());
    }

    public static HttpMessageConverter<Object> jsonUnitMessageConverter() {
        return new DirectGenericHttpMessageConverter();
    }

    @Override
    public @NotNull JsonComparison compare(@NotNull String expectedJson, @NotNull String actualJson) {
        Diff diff = Diff.create(expectedJson, actualJson, "actual", "", configuration);
        if (diff.similar()) {
            return JsonComparison.match();
        } else {
            return JsonComparison.mismatch(diff.differences());
        }
    }

    private static class DirectGenericHttpMessageConverter extends AbstractGenericHttpMessageConverter<Object> {
        public DirectGenericHttpMessageConverter() {
            super(MediaType.APPLICATION_JSON, new MediaType("application", "*+json"));
        }

        @Override
        public @NotNull Object read(
                @NotNull Type type, @NotNull Class<?> contextClass, @NotNull HttpInputMessage inputMessage)
                throws IOException, HttpMessageNotReadableException {
            return readInternal(contextClass, inputMessage);
        }

        @Override
        protected @NotNull Object readInternal(@NotNull Class<?> clazz, @NotNull HttpInputMessage inputMessage)
                throws IOException, HttpMessageNotReadableException {
            return FileCopyUtils.copyToString(new InputStreamReader(inputMessage.getBody(), getCharset(inputMessage)));
        }

        @Override
        protected void writeInternal(@NotNull Object o, @NotNull Type type, @NotNull HttpOutputMessage outputMessage)
                throws HttpMessageNotWritableException {
            throw new UnsupportedOperationException();
        }

        @Override
        protected boolean supports(@NotNull Class<?> clazz) {
            return true;
        }
    }
    ;
}
