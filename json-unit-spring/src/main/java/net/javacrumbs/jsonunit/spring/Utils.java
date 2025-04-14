package net.javacrumbs.jsonunit.spring;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpMessage;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.EntityExchangeResult;

class Utils {
    static String getContentAsString(EntityExchangeResult<byte[]> result) {
        Charset charset = getCharset(result);
        return new String(result.getResponseBody(), charset);
    }

    @NonNull
    private static Charset getCharset(EntityExchangeResult<byte[]> result) {
        return getCharset(result.getResponseHeaders().getContentType());
    }

    @NonNull
    static Charset getCharset(HttpMessage message) {
        return getCharset(message.getHeaders().getContentType());
    }

    @NonNull
    private static Charset getCharset(@Nullable MediaType contentType) {
        if (contentType != null && contentType.getCharset() != null) {
            return contentType.getCharset();
        } else {
            return StandardCharsets.UTF_8;
        }
    }
}
