package net.javacrumbs.jsonunit.spring;

import static net.javacrumbs.jsonunit.spring.JsonTransformer.identity;
import static net.javacrumbs.jsonunit.spring.WebTestClientUtils.getContentAsString;

import java.util.function.Consumer;
import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.internal.matchers.InternalMatcher;
import org.springframework.test.web.reactive.server.EntityExchangeResult;

/**
 * Matcher compatible with Spring TestWebClient.
 * <p>
 * Sample usage:
 * <p>
 * <code>
 * client.get().uri(path).accept(MediaType.APPLICATION_JSON).exchange().expectBody()
 * .consumeWith(json().isEqualTo(CORRECT_JSON));
 * </code>
 */
public class WebTestClientJsonMatcher
        extends AbstractSpringMatchers<WebTestClientJsonMatcher, Consumer<EntityExchangeResult<byte[]>>> {
    private WebTestClientJsonMatcher(Configuration configuration, JsonTransformer jsonTransformer) {
        super(configuration, jsonTransformer);
    }

    public static WebTestClientJsonMatcher json() {
        return new WebTestClientJsonMatcher(Configuration.empty(), identity());
    }

    @Override
    Consumer<EntityExchangeResult<byte[]>> matcher(Consumer<InternalMatcher> matcher) {
        return new JsonUnitWebTestClientMatcher(configuration, matcher, jsonTransformer);
    }

    @Override
    WebTestClientJsonMatcher matchers(Configuration configuration, JsonTransformer jsonTransformer) {
        return new WebTestClientJsonMatcher(configuration, jsonTransformer);
    }

    private static class JsonUnitWebTestClientMatcher extends AbstractSpringMatcher
            implements Consumer<EntityExchangeResult<byte[]>> {
        private JsonUnitWebTestClientMatcher(
                Configuration configuration, Consumer<InternalMatcher> matcher, JsonTransformer jsonTransformer) {
            super(configuration, matcher, jsonTransformer);
        }

        @Override
        public void accept(EntityExchangeResult<byte[]> result) {
            doMatch(getContentAsString(result));
        }
    }
}
