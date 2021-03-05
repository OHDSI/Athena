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

import com.odysseusinc.athena.model.athena.VocabularyConversion;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VocabularyConversionRepository extends JpaRepository<VocabularyConversion, Integer> {

    String MISSING_LICENSES = "SELECT * FROM vocabulary_conversion WHERE "
            + "available IS NOT NULL AND "
            + "vocabulary_id_v4 != 4" ; //id of cpt4 vocabulary

    String MISSING_LICENSES_FOR_USER = MISSING_LICENSES + " AND "
            + "vocabulary_id_v4 NOT IN "
            + "(SELECT vocabulary_id_v4 FROM licenses AS lic "
            + "where "
            + "(status IN ('APPROVED') OR :withoutPending IS TRUE) AND "
            + "lic.user_id = :userId)";

    @Query(nativeQuery = true, value = MISSING_LICENSES_FOR_USER)
    List<VocabularyConversion> unavailableVocabularies(@Param("userId") Long userId,
                                                       @Param("withoutPending") Boolean withoutPending);

    @Query(nativeQuery = true, value = MISSING_LICENSES)
    List<VocabularyConversion> unavailableVocabularies();

    VocabularyConversion findByIdV4(Integer idV4);

    VocabularyConversion findByIdV5(String vocabularyCode);

    List<VocabularyConversion> findByOmopReqIsNull(Sort sort);

    List<VocabularyConversion> findByOmopReqIsNotNull();

    List<VocabularyConversion> findByLatestUpdateIsNotNull();

    @Query(nativeQuery = true,
            value = "SELECT VOCABULARY_ID_V5 FROM vocabulary_conversion WHERE "
                    + " VOCABULARY_ID_V4 IN :v4Ids")
    List<String> findIdsV5ByIdsV4(@Param("v4Ids") List<Long> idV4s);

}