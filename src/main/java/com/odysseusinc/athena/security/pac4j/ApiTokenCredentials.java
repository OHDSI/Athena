package com.odysseusinc.athena.security.pac4j;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.util.CommonHelper;

import java.util.Objects;

@RequiredArgsConstructor
public class ApiTokenCredentials extends Credentials {
    @Getter
    private final String token;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ApiTokenCredentials that = (ApiTokenCredentials) o;
        return Objects.equals(token, that.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(token);
    }

    @Override
    public String toString() {
        return CommonHelper.toNiceString(this.getClass(), "token", this.token);
    }
}
