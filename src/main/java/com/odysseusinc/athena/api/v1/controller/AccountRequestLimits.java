package com.odysseusinc.athena.api.v1.controller;

import com.odysseusinc.athena.api.v1.controller.dto.arachne.ArachnePortalResponse;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
class AccountRequestLimits {
    private final JdbcClient jdbc;
    private final int windowSeconds;
    private final int loginAddress;
    private final int loginEmail;
    private final int reminderAddress;
    private final int reminderEmail;
    private final int resetAddress;
    private final int registerAddress;
    private final int registerEmail;

    AccountRequestLimits(@Qualifier("dataSourceAthenaDB") DataSource source,
                         @Value("${athena.auth.limits.window-seconds:900}") int windowSeconds,
                         @Value("${athena.auth.limits.login-per-ip:100}") int loginAddress,
                         @Value("${athena.auth.limits.login-per-email:10}") int loginEmail,
                         @Value("${athena.auth.limits.reminders-per-ip:30}") int reminderAddress,
                         @Value("${athena.auth.limits.reminders-per-email:5}") int reminderEmail,
                         @Value("${athena.auth.limits.resets-per-ip:60}") int resetAddress,
                         @Value("${athena.auth.limits.registrations-per-ip:30}") int registerAddress,
                         @Value("${athena.auth.limits.registrations-per-email:5}") int registerEmail) {
        Assert.isTrue(windowSeconds > 0 && loginAddress > 0 && loginEmail > 0 && reminderAddress > 0
                && reminderEmail > 0 && resetAddress > 0 && registerAddress > 0 && registerEmail > 0, "Account request limits must be positive.");
        jdbc = JdbcClient.create(source);
        this.windowSeconds = windowSeconds;
        this.loginAddress = loginAddress;
        this.loginEmail = loginEmail;
        this.reminderAddress = reminderAddress;
        this.reminderEmail = reminderEmail;
        this.resetAddress = resetAddress;
        this.registerAddress = registerAddress;
        this.registerEmail = registerEmail;
    }

    int loginDelay(String address, String email) {
        int delay = take("login-ip", address, loginAddress);
        if (delay > 0 || email == null || email.isBlank() || email.length() > 255) {
            return delay;
        }
        return take("login-email", email.strip(), loginEmail);
    }

    int registrationDelay(String address, String email) {
        int delay = take("register-ip", address, registerAddress);
        if (delay > 0 || email == null || email.isBlank() || email.length() > 128) return delay;
        return take("register-email", email.strip(), registerEmail);
    }

    boolean allowReminder(String address, String email) {
        return take("reminder-ip", address, reminderAddress) == 0
                && take("reminder-email", email.strip(), reminderEmail) == 0;
    }

    int resetDelay(String address) {
        return take("reset-ip", address, resetAddress);
    }

    // The upsert serializes admissions for one subject across all Athena replicas.
    // Use the database's lower() here and in the credential lookup so email aliases share a quota.
    private int take(String scope, String subject, int maximum) {
        return jdbc.sql("""
                INSERT INTO account_request_limits AS b (scope, subject_hash, attempts, expires_at)
                VALUES (:scope, encode(sha256(convert_to(lower(:subject), 'UTF8')), 'hex'), 1,
                        statement_timestamp() + :seconds * interval '1 second')
                ON CONFLICT (scope, subject_hash) DO UPDATE SET
                    attempts = CASE WHEN b.expires_at <= statement_timestamp() THEN 1
                                    ELSE least(b.attempts + 1, CAST(:maximum AS bigint) + 1) END,
                    expires_at = CASE WHEN b.expires_at <= statement_timestamp() THEN excluded.expires_at
                                      ELSE b.expires_at END
                RETURNING CASE WHEN attempts <= :maximum THEN 0
                               ELSE greatest(1, ceil(extract(epoch FROM expires_at - statement_timestamp())))::int END
                """).param("scope", scope).param("subject", subject).param("maximum", maximum)
                .param("seconds", windowSeconds).query(Integer.class).single();
    }

    @Scheduled(initialDelay = 1, fixedDelay = 1, timeUnit = TimeUnit.HOURS)
    void pruneExpired() {
        jdbc.sql("DELETE FROM account_request_limits WHERE expires_at <= statement_timestamp()").update();
    }

    static <T> ResponseEntity<ArachnePortalResponse<T>> retryLater(int seconds) {
        var error = new ArachnePortalResponse<T>(ArachnePortalResponse.ErrorCode.VALIDATION_ERROR);
        error.setErrorMessage("Too many attempts. Please try again later.");
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header(HttpHeaders.RETRY_AFTER, Integer.toString(seconds))
                .cacheControl(CacheControl.noStore()).body(error);
    }
}
