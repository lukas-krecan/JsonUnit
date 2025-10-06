package net.javacrumbs.jsonunit.spring;

import java.nio.charset.Charset;
import java.util.function.Consumer;
import java.util.function.Function;
import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.internal.matchers.InternalMatcher;
import org.jspecify.annotations.Nullable;
import org.springframework.test.web.servlet.client.EntityExchangeResult;

/**
 * Matcher compatible with Spring RestTestClient.
 * <p/>
 * Sample usage:
 * <p/>
 * <code>
 * client.get().uri(path).accept(MediaType.APPLICATION_JSON).exchange().expectBody()
 * .consumeWith(json().isEqualTo(CORRECT_JSON));
 * </code>
 */
public class RestTestClientJsonMatcher
        extends AbstractSpringMatchers<RestTestClientJsonMatcher, Consumer<EntityExchangeResult<byte[]>>> {
    private RestTestClientJsonMatcher(Configuration configuration, Function<Object, Object> jsonTransformer) {
        super(configuration, jsonTransformer);
    }

    public static RestTestClientJsonMatcher json() {
        return new RestTestClientJsonMatcher(Configuration.empty(), Function.identity());
    }

    @Override
    Consumer<EntityExchangeResult<byte[]>> matcher(Consumer<InternalMatcher> matcher) {
        return new JsonUnitWebTestClientMatcher(configuration, matcher, jsonTransformer);
    }

    @Override
    RestTestClientJsonMatcher matchers(Configuration configuration, Function<Object, Object> jsonTransformer) {
        return new RestTestClientJsonMatcher(configuration, jsonTransformer);
    }

    private static class JsonUnitWebTestClientMatcher extends AbstractSpringMatcher
            implements Consumer<EntityExchangeResult<byte[]>> {
        private JsonUnitWebTestClientMatcher(
                Configuration configuration,
                Consumer<InternalMatcher> matcher,
                Function<Object, Object> jsonTransformer) {
            super(configuration, matcher, jsonTransformer);
        }

        @Override
        public void accept(EntityExchangeResult<byte[]> result) {
            doMatch(getContentAsString(result));
        }

        private static @Nullable String getContentAsString(EntityExchangeResult<byte[]> result) {
            Charset charset = getCharset(result);
            byte[] responseBody = result.getResponseBody();
            return responseBody != null ? new String(responseBody, charset) : null;
        }

        private static Charset getCharset(EntityExchangeResult<byte[]> result) {
            return Utils.getCharset(result.getResponseHeaders().getContentType());
        }
    }
}
