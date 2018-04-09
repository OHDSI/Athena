/*
 *
 * Copyright 2018 Observational Health Data Sciences and Informatics
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

import com.odysseusinc.athena.model.athena.Notification;
import java.util.List;
import java.util.Optional;
import javax.persistence.PersistenceContext;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
@PersistenceContext(unitName = "athenaEntityManagerFactory")
public interface NotificationRepository extends CrudRepository<Notification, Long> {

    List<Notification> findByUserId(Long userId);

    @Query(nativeQuery = true, value =
            "SELECT * FROM notifications WHERE user_id = :userId AND vocabulary_id_v4 = :vocabularyId")
    Optional<Notification> findByUserIdAndVocabularyV4Id(@Param("userId") Long userId,
                                                         @Param("vocabularyId") Integer vocabularyId);
}