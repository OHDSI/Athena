package com.odysseusinc.athena.model.athena;


import com.odysseusinc.athena.model.security.AthenaUser;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "download_share")
public class DownloadShare {
    @EmbeddedId
    private DownloadShareId downloadShareId;

    @ManyToOne(optional = false, targetEntity = AthenaUser.class)
    @JoinColumn(name = "owner_id")
    private AthenaUser owner;

    public DownloadShare() {
        // default constructor for entity
    }

    public DownloadShare(Long bundleId, String email, AthenaUser owner) {
        this.downloadShareId = new DownloadShareId(bundleId, email);
        this.owner = owner;
    }

    public DownloadShareId getDownloadShareId() {
        return downloadShareId;
    }

    public AthenaUser getOwner() {
        return owner;
    }

    public Long getBundleId() {
        return this.downloadShareId.getBundleId();
    }

    public String getUserEmail() {
        return this.downloadShareId.getUserEmail();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DownloadShare that = (DownloadShare) o;

        if (!downloadShareId.equals(that.downloadShareId)) return false;
        return owner.getId().equals(that.owner.getId());
    }

    @Override
    public int hashCode() {
        int result = downloadShareId.hashCode();
        result = 31 * result + owner.getId().hashCode();
        return result;
    }
}
