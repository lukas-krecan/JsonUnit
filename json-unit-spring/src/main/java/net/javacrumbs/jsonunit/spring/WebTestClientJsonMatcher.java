package net.javacrumbs.jsonunit.spring;

import static net.javacrumbs.jsonunit.spring.Utils.getContentAsString;

import java.util.function.Consumer;
import java.util.function.Function;
import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.internal.matchers.InternalMatcher;
import org.jspecify.annotations.NonNull;
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
    private WebTestClientJsonMatcher(Configuration configuration, Function<Object, Object> jsonTransformer) {
        super(configuration, jsonTransformer);
    }

    public static WebTestClientJsonMatcher json() {
        return new WebTestClientJsonMatcher(Configuration.empty(), Function.identity());
    }

    @Override
    @NonNull
    Consumer<EntityExchangeResult<byte[]>> matcher(@NonNull Consumer<InternalMatcher> matcher) {
        return new JsonUnitWebTestClientMatcher(configuration, matcher, jsonTransformer);
    }

    @Override
    @NonNull
    WebTestClientJsonMatcher matchers(
            @NonNull Configuration configuration, @NonNull Function<Object, Object> jsonTransformer) {
        return new WebTestClientJsonMatcher(configuration, jsonTransformer);
    }

    private static class JsonUnitWebTestClientMatcher extends AbstractSpringMatcher
            implements Consumer<EntityExchangeResult<byte[]>> {
        private JsonUnitWebTestClientMatcher(
                @NonNull Configuration configuration,
                @NonNull Consumer<InternalMatcher> matcher,
                @NonNull Function<Object, Object> jsonTransformer) {
            super(configuration, matcher, jsonTransformer);
        }

        @Override
        public void accept(EntityExchangeResult<byte[]> result) {
            doMatch(getContentAsString(result));
        }
    }
}
