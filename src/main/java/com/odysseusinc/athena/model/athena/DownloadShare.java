package com.odysseusinc.athena.model.athena;


import com.odysseusinc.athena.model.security.AthenaUser;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "download_share")
public class DownloadShare {
    @Id
    @SequenceGenerator(name = "download_share_pk_sequence", sequenceName = "download_share_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "download_share_pk_sequence")
    private Long id;

    @NotNull
    @ManyToOne(optional = false, targetEntity = DownloadBundle.class)
    @JoinColumn(name = "bundle_id")
    private DownloadBundle bundle;

    @NotBlank
    @Column(name = "user_email")
    private String userEmail;

    @NotNull
    @Column(name = "owner_id")
    private Long ownerId;

    @NotNull
    @Column(name = "owner_name")
    private String ownerName;

    public DownloadShare() {
        // default constructor for entity
    }

    public DownloadShare(DownloadBundle bundle, String email, AthenaUser owner) {
        this.bundle = bundle;
        this.userEmail = email;
        this.ownerId = owner.getId();
        this.ownerName = owner.getUsername();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public DownloadBundle getBundle() {
        return bundle;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public Long getBundleId() {
        return bundle != null ? bundle.getId() : null;
    }

    public void setBundle(DownloadBundle bundle) {
        this.bundle = bundle;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public void setOwner(AthenaUser user) {
        this.ownerName = user.getUsername();
        this.ownerId = user.getId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DownloadShare that = (DownloadShare) o;

        if (getBundleId() != null ? !getBundleId().equals(that.getBundleId()) : that.getBundleId() != null) return false;
        if (userEmail != null ? !userEmail.equals(that.userEmail) : that.userEmail != null) return false;
        return ownerId != null ? ownerId.equals(that.ownerId) : that.ownerId == null;
    }

    @Override
    public int hashCode() {
        int result = bundle != null && bundle.getId() != null ? bundle.getId().hashCode() : 0;
        result = 31 * result + (userEmail != null ? userEmail.hashCode() : 0);
        result = 31 * result + (ownerId != null ? ownerId.hashCode() : 0);
        return result;
    }
}
