package com.odysseusinc.athena.repositories.athena;

import com.odysseusinc.athena.model.athena.DownloadHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DownloadHistoryRepository extends JpaRepository<DownloadHistory, Long> {
}
