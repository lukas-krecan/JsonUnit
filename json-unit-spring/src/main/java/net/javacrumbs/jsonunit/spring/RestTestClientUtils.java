package net.javacrumbs.jsonunit.spring;

import java.nio.charset.Charset;
import org.jspecify.annotations.Nullable;
import org.springframework.test.web.servlet.client.EntityExchangeResult;

class RestTestClientUtils {
    static @Nullable String getContentAsString(EntityExchangeResult<byte[]> result) {
        Charset charset = getCharset(result);
        byte[] responseBody = result.getResponseBody();
        if (responseBody != null) {
            return new String(responseBody, charset);
        } else {
            return null;
        }
    }

    private static Charset getCharset(EntityExchangeResult<byte[]> result) {
        return CharsetUtils.getCharset(result.getResponseHeaders().getContentType());
    }
}
