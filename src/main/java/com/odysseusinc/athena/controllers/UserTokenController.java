package com.odysseusinc.athena.controllers;

import com.odysseusinc.athena.security.hmac.ApiClients;
import com.odysseusinc.athena.security.hmac.HmacVerifyingFilter;
import com.odysseusinc.athena.service.security.UserTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Optional;

/* non-javadoc
curl 'http://localhost:3010/api/v1/notifications' \
-H 'X-Athena-Client-Id: pd' \
-H "X-Athena-Nonce: 2023-10-03T20:45:59.013184736Z" \
-H "X-Athena-Auth: Bearer XWwOYcUreca5S0Aq6W3aSbFLnb8xNKe4UGdoP/ofYYZpQr+XKZk3KBQIvtHVkcqm0Ns8eHA3muCaJ8G8MXtdqw==" \
-H "X-Athena-Hmac: MD0CHQCAS/IK7xrAoY/1meKrKz2GGmhe54SAixYbzzP0AhwlRRxJECfaMNFjDV/kA1mWkxVh5SNDHwk7v9TZ"
*/

/**
 * The token needs to be obrained by opening http://localhost:3010/api/v1/user-token in browser where SAML is logged in.
 * An example of token usage can be found in the non-javadoc comment above. In that example, headers should be filled as follows:
 *
 * <p>"X-Athena-Client-Id" header contains the identification of remote client (as configured in application properties)
 * <p>"X-Athena-Auth: Bearer" header must contain the user token
 * <p>"X-Athena-Nonce" header must contain current UTC timestamp. Athena allows 1 minute tolerance on it.
 * <p>"X-Athena-Hmac" header contains HMAC.
 * @see HmacVerifyingFilter#verify(HttpServletRequest, String, Optional) for details on the fields used for HMAC calculation
 * @see ApiClients for details on the cryptography in use for HMAC calculation
 */
@Slf4j
@RestController
public class UserTokenController {

    @Autowired
    private UserTokenService tokenService;

    @GetMapping("/api/v1/user-token")
    public ResponseEntity<String> get(Principal principal) {
        try {
            return ResponseEntity.ok(tokenService.getToken(principal));
        } catch (Exception e) {
            log.info(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @DeleteMapping("/api/v1/user-token")
    public void delete(Principal principal) {
        try {
            tokenService.deleteToken(principal);
        } catch (Exception e) {
            log.info(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
