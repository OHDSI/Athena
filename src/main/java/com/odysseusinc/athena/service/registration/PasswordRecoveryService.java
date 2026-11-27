package com.odysseusinc.athena.service.registration;

import com.google.common.hash.Hashing;
import java.nio.charset.StandardCharsets;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(transactionManager = "accountJdbcTransactionManager")
public class PasswordRecoveryService {
    private final JdbcClient jdbc;
    private final BCryptPasswordEncoder passwords = new BCryptPasswordEncoder();

    public PasswordRecoveryService(@Qualifier("dataSourceAthenaDB") DataSource source) {
        this.jdbc = JdbcClient.create(source);
    }

    public boolean reset(String email, String token, String password) {
        String hash = digest(token);
        var id = jdbc.sql("""
                SELECT athena_user_id FROM users_data WHERE reset_token_hash = :hash
                    AND lower(email) = lower(:email) AND reset_expires_at > clock_timestamp() FOR UPDATE
                """).param("hash", hash).param("email", email).query(Long.class).optional();
        if (id.isEmpty()) {
            return false;
        }
        return jdbc.sql("""
                WITH changed AS (
                    UPDATE users_data SET password = :password, last_password_reset = now(),
                        email_confirmed_at = coalesce(email_confirmed_at, now()),
                        confirmation_token_hash = NULL, confirmation_expires_at = NULL,
                        confirmation_delivery_id = NULL, confirmation_delivery_due_at = NULL,
                        reset_token_hash = NULL, reset_expires_at = NULL,
                        reset_delivery_id = NULL, reset_delivery_due_at = NULL
                    WHERE athena_user_id = :id AND reset_expires_at > clock_timestamp()
                    RETURNING athena_user_id
                )
                UPDATE users u SET browser_session_version = u.browser_session_version + 1
                FROM changed WHERE u.id = changed.athena_user_id
                """).param("password", passwords.encode(password)).param("id", id.get()).update() == 1;
    }

    static String digest(String token) {
        return Hashing.sha256().hashString(token, StandardCharsets.UTF_8).toString();
    }
}
