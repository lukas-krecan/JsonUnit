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
public class JsonUnitWebTestClientMatchers extends AbstractSpringMatchers<JsonUnitWebTestClientMatchers, Consumer<EntityExchangeResult<byte[]>>>  {
    private JsonUnitWebTestClientMatchers(Path path, Configuration configuration) {
        super(path, configuration);
    }

    public static JsonUnitWebTestClientMatchers json() {
        return new JsonUnitWebTestClientMatchers(Path.root(), Configuration.empty());
    }

    @Override
    @NotNull
    Consumer<EntityExchangeResult<byte[]>> matcher(@NotNull BiConsumer<Object, InternalMatcher> matcher) {
        return new JsonUnitWebTestClientMatcher(path, configuration, matcher);
    }

    @Override
    @NotNull
    JsonUnitWebTestClientMatchers matchers(@NotNull Path path, @NotNull Configuration configuration) {
        return new JsonUnitWebTestClientMatchers(path, configuration);
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
