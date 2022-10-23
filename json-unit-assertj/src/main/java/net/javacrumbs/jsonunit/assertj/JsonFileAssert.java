package net.javacrumbs.jsonunit.assertj;

import net.javacrumbs.jsonunit.assertj.JsonAssert.ConfigurableJsonAssert;
import org.assertj.core.api.AbstractFileAssert;
import org.assertj.core.internal.Files;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;

import static java.nio.file.Files.readAllBytes;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.internal.Files.instance;

/**
 * Wrapper on {@link ConfigurableJsonAssert} to retrieve JSON from a file.
 */
public class JsonFileAssert extends AbstractFileAssert<JsonFileAssert> {
    private final Files files = instance();

    /**
     * Assert json properties in a file from its path.
     *
     * <pre>{@code
     * assertThatJsonFile("path/to/file.json").isFile()
     *     .hasContent().inPath("test1").isEqualTo(2);
     * }</pre>
     */
    @NotNull
    public static JsonFileAssert assertThatJsonFile(@NotNull String path) {
        return new JsonFileAssert(new File(path));
    }

    /**
     * Assert json properties in a file.
     *
     * <pre>{@code
     * assertThatJsonFile(jsonFile).isFile()
     *     .hasContent().inPath("test1").isEqualTo(2);
     * }</pre>
     */
    @NotNull
    public static JsonFileAssert assertThatJsonFile(@NotNull File file) {
        return new JsonFileAssert(file);
    }

    private JsonFileAssert(File actual) {
        super(actual, JsonFileAssert.class);
    }

    /**
     * Convert file content to assertable JSON.
     *
     * @return ConfigurableJsonAssert to perform assertions on json content
     * @throws IllegalArgumentException when file content is not valid JSON
     */
    public ConfigurableJsonAssert hasContent() {
        return hasContent(Charset.defaultCharset());
    }

    /**
     * Convert file content to assertable JSON with provided charset.
     *
     * @param charset The charset of the JSON file
     * @return ConfigurableJsonAssert to perform assertions on json content
     * @throws IllegalArgumentException when file content is not valid JSON
     */
    public ConfigurableJsonAssert hasContent(Charset charset) {
        files.assertCanRead(info, actual);
        return assertThatJson(readFile(charset));
    }

    private String readFile(Charset charset) {
        try {
            return new String(readAllBytes(actual.toPath()), charset);
        } catch (IOException e) {
            throw new UncheckedIOException(String.format("Failed to read %s content with %s charset", actual, charset), e);
        }
    }
}
