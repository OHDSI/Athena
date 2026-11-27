package com.odysseusinc.athena.api.v1.controller;

import com.odysseusinc.athena.api.v1.controller.dto.arachne.ArachnePortalResponse;
import com.odysseusinc.athena.security.jwt.JwtTokenService;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static com.odysseusinc.athena.api.v1.controller.dto.arachne.ArachnePortalResponse.ErrorCode.NO_ERROR;
import static com.odysseusinc.athena.api.v1.controller.dto.arachne.ArachnePortalResponse.ErrorCode.UNAUTHORIZED;

@RestController
public class LocalLoginController {

    public static class Credentials {
        public String email;
        public String password;
    }

    private final AccountRequestLimits limits;
    private final JdbcClient jdbc;
    private final JwtTokenService tokens;
    private final BCryptPasswordEncoder passwords = new BCryptPasswordEncoder();
    // Perform a bcrypt comparison for unknown emails as well.
    private final String dummyHash = passwords.encode(UUID.randomUUID().toString());

    public LocalLoginController(@Qualifier("dataSourceAthenaDB") DataSource source, JwtTokenService tokens,
                                AccountRequestLimits limits) {
        this.limits = limits;
        jdbc = JdbcClient.create(source);
        this.tokens = tokens;
    }

    @PostMapping("/api/v1/users/login")
    public ResponseEntity<ArachnePortalResponse<Map<String, String>>> login(@RequestBody Credentials form,
                                                                           HttpServletRequest request) {
        int delay = limits.loginDelay(request.getRemoteAddr(), form.email);
        if (delay > 0) {
            return AccountRequestLimits.retryLater(delay);
        }
        if (form.email == null || form.email.isBlank() || form.email.length() > 255 || form.password == null
                || form.password.isEmpty() || form.password.getBytes(StandardCharsets.UTF_8).length > 72) {
            return denied();
        }
        record Account(String login, String origin, String password, long browserSessionVersion, boolean eligible) {}
        var account = jdbc.sql("""
                SELECT u.login, u.origin, d.password, u.browser_session_version,
                       (d.email_confirmed_at IS NOT NULL OR d.arachne_user_id IS NOT NULL) AS eligible
                FROM users_data d JOIN users u ON u.id = d.athena_user_id
                WHERE lower(d.email) = lower(:email)
                """).param("email", form.email.strip()).query(Account.class).optional();

        if (!passwords.matches(form.password, account.map(Account::password).orElse(dummyHash))
                || account.isEmpty() || !account.get().eligible()) {
            return denied();
        }
        var user = account.get();
        String token = tokens.issue(user.login(), user.origin(), user.browserSessionVersion());
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(new ArachnePortalResponse<>(NO_ERROR, Map.of("token", token)));
    }

    private static ResponseEntity<ArachnePortalResponse<Map<String, String>>> denied() {
        var error = new ArachnePortalResponse<Map<String, String>>(UNAUTHORIZED);
        error.setErrorMessage("Unable to sign in. Check your email and password and confirm any new registration.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).cacheControl(CacheControl.noStore()).body(error);
    }
}
