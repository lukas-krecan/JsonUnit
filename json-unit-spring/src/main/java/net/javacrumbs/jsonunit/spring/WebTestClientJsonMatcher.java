package net.javacrumbs.jsonunit.spring;

import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.internal.Path;
import net.javacrumbs.jsonunit.core.internal.matchers.InternalMatcher;
import org.jetbrains.annotations.NotNull;
import org.springframework.test.web.reactive.server.EntityExchangeResult;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static net.javacrumbs.jsonunit.spring.Utils.getContentAsString;

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
public class WebTestClientJsonMatcher extends AbstractSpringMatchers<WebTestClientJsonMatcher, Consumer<EntityExchangeResult<byte[]>>> {
    private WebTestClientJsonMatcher(Path path, Configuration configuration) {
        super(path, configuration);
    }

    public static WebTestClientJsonMatcher json() {
        return new WebTestClientJsonMatcher(Path.root(), Configuration.empty());
    }

    @Override
    @NotNull
    Consumer<EntityExchangeResult<byte[]>> matcher(@NotNull BiConsumer<Object, InternalMatcher> matcher) {
        return new JsonUnitWebTestClientMatcher(path, configuration, matcher);
    }

    @Override
    @NotNull
    WebTestClientJsonMatcher matchers(@NotNull Path path, @NotNull Configuration configuration) {
        return new WebTestClientJsonMatcher(path, configuration);
    }

    private static class JsonUnitWebTestClientMatcher extends AbstractSpringMatcher implements Consumer<EntityExchangeResult<byte[]>> {
        private JsonUnitWebTestClientMatcher(@NotNull Path path, @NotNull Configuration configuration, @NotNull BiConsumer<Object, InternalMatcher> matcher) {
            super(path, configuration, matcher);
        }

        @Override
        public void accept(EntityExchangeResult<byte[]> result) {
            doMatch(getContentAsString(result));
        }
    }
}
