package com.odysseusinc.athena.security.pac4j;

import com.odysseusinc.athena.model.security.AthenaProfile;
import com.odysseusinc.athena.model.security.AthenaToken;
import com.odysseusinc.athena.repositories.athena.AthenaTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.exception.CredentialsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class ApiTokenAuthenticator implements Authenticator<ApiTokenCredentials> {
    @Autowired
    private AthenaTokenRepository repository;

    @Override
    @Transactional
    public void validate(ApiTokenCredentials credentials, WebContext context) {
        AthenaToken token = repository.findByValue(credentials.getToken()).orElseThrow(() ->
                new CredentialsException("API token is not recognized")
        );

        AthenaProfile profile = new AthenaProfile();
        profile.setAthenaUser(token.getUser());
        credentials.setUserProfile(profile);
    }

}
