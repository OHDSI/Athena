package com.odysseusinc.athena.service.registration;

import com.odysseusinc.athena.api.v1.controller.dto.arachne.AddressDTO;
import com.odysseusinc.athena.api.v1.controller.dto.arachne.UserRegistrationDTO;
import java.nio.charset.StandardCharsets;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(transactionManager = "accountJdbcTransactionManager")
public class RegistrationService {

    public static class Rejected extends RuntimeException {
        private final String field;

        Rejected(String field, String message) {
            super(message);
            this.field = field;
        }

        public String field() {
            return field;
        }
    }

    private final JdbcClient jdbc;
    private final BCryptPasswordEncoder passwords = new BCryptPasswordEncoder();
    private final AccountEmailQueue queue;
    private final String defaultRoles;

    public RegistrationService(@Qualifier("dataSourceAthenaDB") DataSource dataSource, AccountEmailQueue queue,
                               @Value("${athena.security.defaultRoles}") String defaultRoles) {
        this.jdbc = JdbcClient.create(dataSource);
        this.queue = queue;
        this.defaultRoles = defaultRoles;
    }

    public void register(UserRegistrationDTO form) {
        if (form.getPassword().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new Rejected("password", "Password exceeds the bcrypt limit of 72 UTF-8 bytes.");
        }

        // The same form can renew an expired challenge using the original password.
        // A retry never replaces a credential or edits an existing user's profile.
        var expired = jdbc.sql("""
                SELECT password FROM users_data WHERE lower(email) = lower(:email)
                    AND confirmation_expires_at <= CURRENT_TIMESTAMP FOR UPDATE
                """).param("email", form.getEmail()).query(String.class).optional();
        if (expired.isPresent() && passwords.matches(form.getPassword(), expired.get())) {
            queue.enqueueConfirmation(form.getEmail());
            return;
        }

        var address = form.getAddress();
        long country = jdbc.sql("SELECT id FROM countries WHERE alfa2 = upper(:code)")
                .param("code", address.getCountry().getIsoCode()).query(Long.class).optional()
                .orElseThrow(() -> new Rejected("address.country", "Select a valid country."));
        Long province = provinceId(address, country);
        if (!jdbc.sql("SELECT EXISTS (SELECT 1 FROM professional_types WHERE id = :id)")
                .param("id", form.getProfessionalTypeId()).query(Boolean.class).single()) {
            throw new Rejected("professionalTypeId", "Select a valid professional type.");
        }

        long user = jdbc.sql("""
                INSERT INTO users (id, login, origin, email, firstname, middlename, lastname, organization)
                SELECT nextval('users_seq'), :email, 'Athena', :email, :first, :middle, :last, :org
                WHERE NOT EXISTS (SELECT 1 FROM users WHERE lower(login) = lower(:email) OR lower(email) = lower(:email))
                ON CONFLICT (lower(login), origin) DO NOTHING RETURNING id
                """).param("email", form.getEmail()).param("first", form.getFirstname())
                .param("middle", form.getMiddlename()).param("last", form.getLastname())
                .param("org", form.getOrganization()).query(Long.class).optional()
                .orElseThrow(RegistrationService::emailTaken);

        int inserted = jdbc.sql("""
                INSERT INTO users_data (athena_user_id, email, password, department, professional_type_id,
                    country_id, state_province_id, address1, address2, city, zip_code, mobile, phone, contact_email)
                VALUES (:user, :email, :hash, :department, :profession, :country, :province, :address1,
                    :address2, :city, :zip, :mobile, :phone, :contact)
                ON CONFLICT (lower(email)) DO NOTHING
                """).param("user", user).param("email", form.getEmail()).param("hash", passwords.encode(form.getPassword()))
                .param("department", form.getDepartment()).param("profession", form.getProfessionalTypeId())
                .param("country", country).param("province", province).param("address1", address.getAddress1())
                .param("address2", address.getAddress2()).param("city", address.getCity()).param("zip", address.getZipCode())
                .param("mobile", address.getMobile()).param("phone", address.getPhone()).param("contact", address.getContactEmail())
                .update();
        if (inserted == 0) {
            throw emailTaken();
        }
        jdbc.sql("""
                INSERT INTO users_roles (user_id, role_id)
                SELECT :user, id FROM roles WHERE name = ANY(string_to_array(:roles, ','))
                """).param("user", user).param("roles", defaultRoles).update();
        queue.enqueueConfirmation(form.getEmail());
    }

    private Long provinceId(AddressDTO address, long country) {
        if (address.getStateProvince() == null) {
            return null;
        }
        return jdbc.sql("SELECT id FROM states_provinces WHERE country_id = :country AND iso_code = upper(:code)")
                .param("country", country).param("code", address.getStateProvince().getIsoCode())
                .query(Long.class).optional()
                .orElseThrow(() -> new Rejected("address.stateProvince", "Select a province in the selected country."));
    }

    private static Rejected emailTaken() {
        return new Rejected("email", "This email address is already registered.");
    }

    public boolean confirm(String token) {
        return jdbc.sql("""
                UPDATE users_data SET email_confirmed_at = CURRENT_TIMESTAMP,
                    confirmation_token_hash = NULL, confirmation_expires_at = NULL,
                    confirmation_delivery_id = NULL, confirmation_delivery_due_at = NULL
                WHERE confirmation_token_hash = :hash AND confirmation_expires_at > CURRENT_TIMESTAMP
                """).param("hash", PasswordRecoveryService.digest(token)).update() == 1;
    }
}
