package net.javacrumbs.jsonunit.spring;

import static net.javacrumbs.jsonunit.spring.Utils.getContentAsString;

import java.util.function.Consumer;
import java.util.function.Function;
import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.internal.Path;
import net.javacrumbs.jsonunit.core.internal.matchers.InternalMatcher;
import org.jetbrains.annotations.NotNull;
import org.springframework.test.web.reactive.server.EntityExchangeResult;

/**
 * Matcher compatible with Spring TestWebClient.
 * <p/>
 * Sample usage:
 * <p/>
 * <code>
 * client.get().uri(path).accept(MediaType.APPLICATION_JSON).exchange().expectBody()
 * .consumeWith(json().isEqualTo(CORRECT_JSON));
 * </code>
 */
public class WebTestClientJsonMatcher
        extends AbstractSpringMatchers<WebTestClientJsonMatcher, Consumer<EntityExchangeResult<byte[]>>> {
    private WebTestClientJsonMatcher(Path path, Configuration configuration, Function<Object, Object> jsonTransformer) {
        super(path, configuration, jsonTransformer);
    }

    public static WebTestClientJsonMatcher json() {
        return new WebTestClientJsonMatcher(Path.root(), Configuration.empty(), Function.identity());
    }

    @Override
    @NotNull
    Consumer<EntityExchangeResult<byte[]>> matcher(@NotNull Consumer<InternalMatcher> matcher) {
        return new JsonUnitWebTestClientMatcher(path, configuration, matcher, jsonTransformer);
    }

    @Override
    @NotNull
    WebTestClientJsonMatcher matchers(
            @NotNull Path path,
            @NotNull Configuration configuration,
            @NotNull Function<Object, Object> jsonTransformer) {
        return new WebTestClientJsonMatcher(path, configuration, jsonTransformer);
    }

    private static class JsonUnitWebTestClientMatcher extends AbstractSpringMatcher
            implements Consumer<EntityExchangeResult<byte[]>> {
        private JsonUnitWebTestClientMatcher(
                @NotNull Path path,
                @NotNull Configuration configuration,
                @NotNull Consumer<InternalMatcher> matcher,
                @NotNull Function<Object, Object> jsonTransformer) {
            super(path, configuration, matcher, jsonTransformer);
        }

        @Override
        public void accept(EntityExchangeResult<byte[]> result) {
            doMatch(getContentAsString(result));
        }
    }
}
