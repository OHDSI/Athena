package com.odysseusinc.athena.model.athena;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "download_history")
public class DownloadHistory {
    @Id
    @SequenceGenerator(name = "download_history_pk_sequence", sequenceName = "download_history_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "download_history_pk_sequence")
    private Long id;
    @ManyToOne(optional = false, targetEntity = DownloadBundle.class)
    @JoinColumn(name = "bundle_id")
    private DownloadBundle vocabularyBundle;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "download_time")
    private Date downloadTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DownloadBundle getVocabularyBundle() {
        return vocabularyBundle;
    }

    public void setVocabularyBundle(DownloadBundle vocabularyBundle) {
        this.vocabularyBundle = vocabularyBundle;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Date getDownloadTime() {
        return downloadTime;
    }

    public void setDownloadTime(Date downloadTime) {
        this.downloadTime = downloadTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DownloadHistory that = (DownloadHistory) o;
        return id.equals(that.id) &&
                vocabularyBundle.equals(that.vocabularyBundle) &&
                userId.equals(that.userId) &&
                downloadTime.equals(that.downloadTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "DownloadHistory{" +
                "id=" + id +
                ", vocabularyBundle=" + vocabularyBundle +
                ", userId=" + userId +
                ", downloadTime=" + downloadTime +
                '}';
    }
}
