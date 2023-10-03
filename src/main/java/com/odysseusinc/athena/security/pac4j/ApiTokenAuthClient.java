package com.odysseusinc.athena.security.pac4j;

import com.odysseusinc.athena.model.security.AthenaProfile;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.pac4j.core.client.DirectClient;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.creator.ProfileCreator;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.Optional;

import static com.odysseusinc.athena.security.pac4j.ApiTokenBearerHeaderExtractor.AUTH_PREFIX;

public class ApiTokenAuthClient extends DirectClient<ApiTokenCredentials, AthenaProfile> {
    private static final String REALM = "athena-api";

    public ApiTokenAuthClient(Authenticator<ApiTokenCredentials> authenticator, ProfileCreator<ApiTokenCredentials, AthenaProfile> profileCreator) {
        defaultAuthenticator(authenticator);
        defaultProfileCreator(profileCreator);
    }

    @Override
    protected void clientInit() {
        defaultCredentialsExtractor(new ApiTokenBearerHeaderExtractor());
    }

    @Override
    protected ApiTokenCredentials retrieveCredentials( WebContext context) {
        return Optional.ofNullable(super.retrieveCredentials(context)).orElseThrow(() -> {
            String header = MessageFormat.format("{0} realm=\"{1}\", qop=\"auth\", nonce=\"{2}\"", AUTH_PREFIX, REALM, Instant.now().toString());
            context.setResponseHeader(HttpConstants.AUTHENTICATE_HEADER, header);
            return HttpAction.unauthorized(context);
        });
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).toString();
    }
}
