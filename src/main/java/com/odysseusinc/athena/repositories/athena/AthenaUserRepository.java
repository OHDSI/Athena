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

import com.odysseusinc.athena.model.security.AthenaUser;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

public interface AthenaUserRepository extends PagingAndSortingRepository<AthenaUser, Long> {

    String GET_USERS_WITH_LICENSES = " FROM users us WHERE "
            + "id IN (SELECT DISTINCT user_id FROM licenses where status IN ('PENDING') OR :pendingOnly IS FALSE) "
            + "AND (lower(firstname) SIMILAR TO :suggestRequest "
            + "OR lower(lastname) SIMILAR TO :suggestRequest "
            + "OR lower(middlename) SIMILAR TO :suggestRequest) ";

    @Query(nativeQuery = true, value = "SELECT * " + GET_USERS_WITH_LICENSES
            + "ORDER BY firstname, lastname, middlename" + " \n--#pageable\n",
            countQuery = "SELECT count(*) " + GET_USERS_WITH_LICENSES)
    Page<AthenaUser> getUsersWithLicenses(@Param("suggestRequest") String suggestRequest,
                                          @Param("pendingOnly") boolean pendingOnly,  Pageable pageable);

    AthenaUser findByUsernameAndOrigin(String username, String origin);

    @Query(nativeQuery = true, value =
            "SELECT * FROM users WHERE lower(firstname) SIMILAR TO :suggestRequest "
                    + "OR lower(lastname) SIMILAR TO :suggestRequest "
                    + "OR lower(middlename) SIMILAR TO :suggestRequest ORDER BY firstname, lastname, middlename")
    List<AthenaUser> suggestUsers(@Param("suggestRequest") String suggestRequest);

    List<AthenaUser> findByRoles_name(String role);

}
