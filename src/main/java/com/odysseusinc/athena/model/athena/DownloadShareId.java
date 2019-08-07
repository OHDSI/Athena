package com.odysseusinc.athena.model.athena;

import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Embeddable
public class DownloadShareId implements Serializable {
    @NotNull
    @Column(name = "bundle_id")
    private Long bundleId;

    @NotBlank
    @Column(name = "user_email")
    private String userEmail;

    public DownloadShareId() {
        // default constructor for entity
    }

    public DownloadShareId(Long bundleId, String userEmail) {
        this.bundleId = bundleId;
        this.userEmail = userEmail;
    }

    public Long getBundleId() {
        return bundleId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DownloadShareId that = (DownloadShareId) o;

        if (!bundleId.equals(that.bundleId)) return false;
        return userEmail.equals(that.userEmail);
    }

    @Override
    public int hashCode() {
        int result = bundleId.hashCode();
        result = 31 * result + userEmail.hashCode();
        return result;
    }
}
