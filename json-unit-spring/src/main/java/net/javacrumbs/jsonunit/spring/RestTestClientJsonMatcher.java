package net.javacrumbs.jsonunit.spring;

import static net.javacrumbs.jsonunit.spring.JsonTransformer.identity;
import static net.javacrumbs.jsonunit.spring.RestTestClientUtils.getContentAsString;

import java.util.function.Consumer;
import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.internal.matchers.InternalMatcher;
import org.springframework.test.web.servlet.client.EntityExchangeResult;

/**
 * Matcher compatible with Spring RestTestClient.
 * <p>
 * Sample usage:
 * <p>
 * <code>
 * client.get().uri(path).accept(MediaType.APPLICATION_JSON).exchange().expectBody()
 * .consumeWith(json().isEqualTo(CORRECT_JSON));
 * </code>
 */
public class RestTestClientJsonMatcher
        extends AbstractSpringMatchers<RestTestClientJsonMatcher, Consumer<EntityExchangeResult<byte[]>>> {
    private RestTestClientJsonMatcher(Configuration configuration, JsonTransformer jsonTransformer) {
        super(configuration, jsonTransformer);
    }

    public static RestTestClientJsonMatcher json() {
        return new RestTestClientJsonMatcher(Configuration.empty(), identity());
    }

    @Override
    Consumer<EntityExchangeResult<byte[]>> matcher(Consumer<InternalMatcher> matcher) {
        return new JsonUnitWebTestClientMatcher(configuration, matcher, jsonTransformer);
    }

    @Override
    RestTestClientJsonMatcher matchers(Configuration configuration, JsonTransformer jsonTransformer) {
        return new RestTestClientJsonMatcher(configuration, jsonTransformer);
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
