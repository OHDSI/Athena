package com.odysseusinc.athena.service.security;

import com.odysseusinc.athena.model.security.AthenaToken;
import com.odysseusinc.athena.model.security.AthenaUser;
import com.odysseusinc.athena.repositories.athena.AthenaTokenRepository;
import com.odysseusinc.athena.service.impl.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class UserTokenService {

    @Autowired
    private AthenaTokenRepository repository;

    @Autowired
    private UserService userService;

    @Transactional
    public String getToken(Principal principal) {
        AthenaUser user = userService.getUser(principal);
        return repository.findByUser(user).orElseGet(() -> create(user)).getValue();
    }

    @Transactional
    public void deleteToken(Principal principal) {
        AthenaUser user = userService.getUser(principal);
        repository.findByUser(user).ifPresent(repository::delete);
    }

    private AthenaToken create(AthenaUser user) {
        AthenaToken token = new AthenaToken();
        token.setUser(user);
        token.setValue(generateValue());
        repository.save(token);
        return token;
    }

    private String generateValue() {
        byte[] value = new byte[64];
        SecureRandom random = new SecureRandom();
        random.nextBytes(value);
        return Base64.getEncoder().encodeToString(value);
    }
}
