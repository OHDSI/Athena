package com.odysseusinc.athena.service.registration;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.odysseusinc.athena.service.mail.AccountMail;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class AccountEmailQueue {
    private static final Logger log = LoggerFactory.getLogger(AccountEmailQueue.class);
    private final JdbcClient jdbc;
    private final AccountMail mail;
    private final HashFunction signingKey;
    private final URI accountUrl;

    private enum Kind {
        RESET("reset", "password-reset", "reset-password"),
        CONFIRM("confirmation", "email-confirmation", "confirm-email");

        final String column;
        final String purpose;
        final String path;

        Kind(String column, String purpose, String path) {
            this.column = column;
            this.purpose = purpose;
            this.path = path;
        }
    }

    public AccountEmailQueue(@Qualifier("dataSourceAthenaDB") DataSource source, AccountMail mail,
                             @Value("${athena.url}") String athenaUrl, @Value("${salt}") String salt) {
        jdbc = JdbcClient.create(source);
        this.mail = mail;
        signingKey = Hashing.hmacSha256(salt.getBytes(StandardCharsets.UTF_8));
        accountUrl = UriComponentsBuilder.fromUriString(athenaUrl).pathSegment("auth").build().toUri();
        if (accountUrl.getHost() == null || !("https".equals(accountUrl.getScheme()) || "http".equals(accountUrl.getScheme()))) {
            throw new IllegalStateException("Set athena.url to the public Athena URL for account emails.");
        }
    }

    // Derive the bearer token from a random job ID and a purpose-specific HMAC input.
    // A worker can reconstruct it after restart without storing a usable reset link.
    private String token(Kind kind, UUID deliveryId) {
        return signingKey.hashString("athena/" + kind.purpose + "/v1/" + deliveryId, StandardCharsets.UTF_8).toString();
    }

    public void enqueueReset(String email) {
        UUID id = UUID.randomUUID();
        jdbc.sql("""
                UPDATE users_data SET reset_token_hash = :hash,
                    reset_expires_at = statement_timestamp() + interval '30 minutes',
                    reset_delivery_id = :id, reset_delivery_due_at = statement_timestamp()
                WHERE lower(email) = lower(:email)
                    AND (reset_token_hash IS NULL OR reset_expires_at <= statement_timestamp())
                """).param("hash", PasswordRecoveryService.digest(token(Kind.RESET, id))).param("id", id)
                .param("email", email.strip()).update();
    }

    void enqueueConfirmation(String email) {
        UUID id = UUID.randomUUID();
        int updated = jdbc.sql("""
                UPDATE users_data SET confirmation_token_hash = :hash,
                    confirmation_expires_at = statement_timestamp() + interval '24 hours',
                    confirmation_delivery_id = :id, confirmation_delivery_due_at = statement_timestamp()
                WHERE lower(email) = lower(:email) AND email_confirmed_at IS NULL
                """).param("hash", PasswordRecoveryService.digest(token(Kind.CONFIRM, id))).param("id", id)
                .param("email", email).update();
        if (updated != 1) throw new IllegalStateException("Pending registration could not be queued.");
    }

    private record Delivery(long userId, UUID deliveryId, String email, String firstName, String hash, boolean active) {}

    @Scheduled(scheduler = "resetEmailScheduler", fixedDelayString = "${athena.recovery.mail.poll-delay:PT1S}",
            initialDelayString = "${athena.recovery.mail.poll-delay:PT1S}")
    public void deliverPending() {
        for (int i = 0; i < 10; i++) {
            boolean confirmation = deliverNext(Kind.CONFIRM);
            boolean reset = deliverNext(Kind.RESET);
            if (!confirmation && !reset) return;
        }
    }

    private boolean deliverNext(Kind kind) {
        // Only the enum above supplies SQL column prefixes.
        // Commit the lease before SMTP. Other replicas can claim other users immediately.
        var next = jdbc.sql("""
                    WITH next AS (
                        SELECT athena_user_id FROM users_data WHERE %1$s_delivery_due_at <= statement_timestamp()
                        ORDER BY %1$s_delivery_due_at, athena_user_id LIMIT 1 FOR UPDATE SKIP LOCKED
                    )
                    UPDATE users_data d SET %1$s_delivery_due_at = statement_timestamp() + interval '2 minutes'
                    FROM next, users u WHERE d.athena_user_id = next.athena_user_id AND u.id = d.athena_user_id
                    RETURNING d.athena_user_id AS user_id, d.%1$s_delivery_id AS delivery_id, d.email,
                        u.firstname AS first_name, d.%1$s_token_hash AS hash,
                        coalesce(d.%1$s_expires_at > clock_timestamp(), false) AS active
                    """.formatted(kind.column)).query(Delivery.class).optional();
        next.ifPresent(job -> deliver(kind, job));
        return next.isPresent();
    }

    private void deliver(Kind kind, Delivery job) {
        String token = token(kind, job.deliveryId());
        if (job.active() && PasswordRecoveryService.digest(token).equals(job.hash())) {
            var page = UriComponentsBuilder.fromUri(accountUrl).pathSegment(kind.path);
            try {
                var recipient = new AccountMail.Recipient(job.email(), job.firstName());
                if (kind == Kind.CONFIRM) {
                    URI link = page.fragment("token={token}").encode().buildAndExpand(token).toUri();
                    mail.sendConfirmation(recipient, link);
                } else {
                    URI link = page.fragment("email={email}&token={token}").encode().buildAndExpand(job.email(), token).toUri();
                    mail.sendPasswordReset(recipient, link);
                }
            } catch (RuntimeException e) {
                // The lease makes this job eligible for retry, including after a process crash.
                log.warn("{} email delivery failed ({}). Delivery will retry until expiry.", kind, e.getClass().getSimpleName());
                return;
            }
        }
        // A late acknowledgement must never clear a newer link's pending delivery.
        jdbc.sql("""
                UPDATE users_data SET %1$s_delivery_id = NULL, %1$s_delivery_due_at = NULL
                WHERE athena_user_id = :user AND %1$s_delivery_id = :id
                """.formatted(kind.column)).param("user", job.userId()).param("id", job.deliveryId()).update();
    }
}
