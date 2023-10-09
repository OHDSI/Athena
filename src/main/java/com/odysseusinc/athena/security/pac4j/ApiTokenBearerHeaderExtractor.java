package com.odysseusinc.athena.security.pac4j;

import com.odysseusinc.athena.security.hmac.HmacVerifyingFilter;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.extractor.CredentialsExtractor;
import org.pac4j.core.exception.BadCredentialsException;

import java.util.Optional;
import java.util.stream.Stream;

import static com.odysseusinc.athena.security.hmac.HmacVerifyingFilter.HEADER_HMAC;

public class ApiTokenBearerHeaderExtractor implements CredentialsExtractor<ApiTokenCredentials> {
    public static final String AUTH_PREFIX = "Bearer";
    private static final String AUTH_HEADER = "X-Athena-Auth";

    @Override
    public ApiTokenCredentials extract(WebContext context) {
        return Optional.ofNullable(((Boolean) context.getRequestAttribute(HmacVerifyingFilter.ATTRIBUTE_HMAC_VALID))).map(hmacValid -> {
            if (hmacValid) {
                return Stream.of(
                        context.getRequestHeader(AUTH_HEADER),
                        context.getRequestHeader(AUTH_HEADER.toLowerCase())
                ).findFirst().map(header -> {
                    if (header.startsWith(AUTH_PREFIX)) {
                        return new ApiTokenCredentials(header.substring(AUTH_PREFIX.length()).trim());
                    } else {
                        throw new BadCredentialsException("The prefix on header [" + AUTH_HEADER + "] must be [" + AUTH_PREFIX + "]");
                    }
                }).orElse(null);
            } else {
                // To
                throw new BadCredentialsException("Valid hmac is required in [" + HEADER_HMAC + "] header");
            }
        }).orElse(null);

    }
}
