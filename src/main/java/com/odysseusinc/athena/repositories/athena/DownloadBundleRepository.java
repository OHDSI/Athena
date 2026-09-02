/*
 *
 * Copyright 2018 Odysseus Data Services, inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Company: Odysseus Data Services, Inc.
 * Product Owner/Architecture: Gregory Klebanov
 * Authors: Pavel Grafkin, Vitaly Koulakov, Maria Pozhidaeva
 * Created: April 4, 2018
 *
 */

package com.odysseusinc.athena.repositories.athena;

import com.odysseusinc.athena.model.athena.DownloadBundle;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import jakarta.persistence.PersistenceContext;

import com.odysseusinc.athena.util.DownloadBundleStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface DownloadBundleRepository extends JpaRepository<DownloadBundle, Long> {

    @Query(nativeQuery = true, value = "SELECT user_id FROM download_bundle WHERE id = :id")
    Long getUserId(@Param("id") Long bundleId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "UPDATE download_bundle SET status = 'ARCHIVED' WHERE uuid = :uuid")
    void archiveByUuid(@Param("uuid") String uuid);

    @Query(nativeQuery = true, value = "SELECT * FROM download_bundle WHERE created < :before AND status != 'ARCHIVED'")
    Set<DownloadBundle> findBefore(@Param("before") Date before);

    List<DownloadBundle> findByUserId(Long userId, Sort sort);

    DownloadBundle findByUuid(String uuid);

    List<DownloadBundle> findByStatus(DownloadBundleStatus status);

    boolean existsByStatus(DownloadBundleStatus status);

    @Query(nativeQuery = true, value = """
            SELECT *
            FROM download_bundle
            WHERE status = 'PENDING'
            ORDER BY created, id
            FOR UPDATE SKIP LOCKED
            LIMIT 1
            """)
    Optional<DownloadBundle> findNextPendingForUpdate();

    @Query(nativeQuery = true, value = """
            SELECT *
            FROM download_bundle
            WHERE id = :bundleId
              AND status = 'PENDING'
            FOR UPDATE
            """)
    Optional<DownloadBundle> findPendingByIdForUpdate(@Param("bundleId") long bundleId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = """
            UPDATE download_bundle
            SET generation_heartbeat_at = :heartbeat
            WHERE id = :bundleId
              AND status = 'GENERATING'
              AND generation_worker = :workerId
            """)
    int heartbeatGenerating(@Param("bundleId") long bundleId,
                            @Param("workerId") String workerId,
                            @Param("heartbeat") Date heartbeat);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = """
            UPDATE download_bundle
            SET status = 'READY',
                generation_heartbeat_at = :finishedAt,
                generation_worker = NULL,
                generation_failure = NULL
            WHERE id = :bundleId
              AND status = 'GENERATING'
              AND generation_worker = :workerId
            """)
    int markReadyGenerating(@Param("bundleId") long bundleId,
                            @Param("workerId") String workerId,
                            @Param("finishedAt") Date finishedAt);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = """
            UPDATE download_bundle
            SET status = 'FAILED',
                generation_heartbeat_at = :finishedAt,
                generation_worker = NULL,
                generation_failure = :failure
            WHERE id = :bundleId
              AND status = 'GENERATING'
              AND generation_worker = :workerId
            """)
    int markFailedGenerating(@Param("bundleId") long bundleId,
                             @Param("workerId") String workerId,
                             @Param("finishedAt") Date finishedAt,
                             @Param("failure") String failure);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = """
            UPDATE download_bundle
            SET status = 'PENDING',
                generation_started_at = NULL,
                generation_heartbeat_at = NULL,
                generation_worker = NULL
            WHERE status = 'GENERATING'
              AND generation_heartbeat_at < :staleBefore
              AND generation_attempts < :maxAttempts
            """)
    int requeueStaleGenerating(@Param("staleBefore") Date staleBefore,
                               @Param("maxAttempts") int maxAttempts);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = """
            UPDATE download_bundle
            SET status = 'FAILED',
                generation_failure = 'Generation interrupted too many times',
                generation_worker = NULL
            WHERE status = 'GENERATING'
              AND generation_heartbeat_at < :staleBefore
              AND generation_attempts >= :maxAttempts
            """)
    int failExhaustedGenerating(@Param("staleBefore") Date staleBefore,
                                @Param("maxAttempts") int maxAttempts);
}
