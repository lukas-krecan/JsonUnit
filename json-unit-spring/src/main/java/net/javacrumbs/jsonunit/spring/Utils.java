package net.javacrumbs.jsonunit.spring;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.EntityExchangeResult;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

class Utils {
    static String getContentAsString(EntityExchangeResult<byte[]> result) {
        Charset charset = getCharset(result);
        return new String(result.getResponseBody(), charset);
    }

    @NotNull
    private static Charset getCharset(EntityExchangeResult<byte[]> result) {
        try {
            MediaType contentType = result.getResponseHeaders().getContentType();
            if (contentType != null && contentType.getCharset() != null) {
                return contentType.getCharset();
            }
        } catch (InvalidMediaTypeException e) {
            //ignore
        }
        return StandardCharsets.UTF_8;
    }
}
