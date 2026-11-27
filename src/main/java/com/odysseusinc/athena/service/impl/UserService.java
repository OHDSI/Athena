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

package com.odysseusinc.athena.service.impl;

import com.odysseusinc.athena.exceptions.NotExistException;
import com.odysseusinc.athena.exceptions.PermissionDeniedException;
import com.odysseusinc.athena.model.security.AthenaRole;
import com.odysseusinc.athena.model.security.AthenaUser;
import com.odysseusinc.athena.repositories.athena.AthenaUserRepository;
import com.odysseusinc.athena.security.AthenaAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private AthenaUserRepository athenaUserRepository;

    public AthenaUser getCurrentUser() throws PermissionDeniedException {

        Authentication principal = SecurityContextHolder.getContext().getAuthentication();
        return getUser(principal);
    }

    public Long getCurrentUserId() throws PermissionDeniedException {

        Long userId = null;
        if (currentUserExists()) {
            AthenaUser currentUser = getCurrentUser();
            userId = currentUser.getId();
        }
        return userId;
    }

    public AthenaUser getUser(String email) {
        List<AthenaUser> users = athenaUserRepository.findByEmail(email);
        if (!users.isEmpty()) {
            return users.get(0);
        }
        return null;
    }

    public AthenaUser getUser(Principal principal) throws PermissionDeniedException {

        if (principal == null) {
            throw new PermissionDeniedException();
        }
        if (principal instanceof AthenaAuthentication) {
            AthenaUser user = ((AthenaAuthentication) principal).getPrincipal();
            if (user == null) {
                throw new NotExistException(AthenaUser.class);
            }
            return user;
        }
        throw new NotExistException(AthenaUser.class);
    }

    public boolean currentUserExists() {

        Authentication principal = SecurityContextHolder.getContext().getAuthentication();
        if (Objects.isNull(principal) || "anonymousUser".equals(principal.getPrincipal())) {
            LOGGER.debug("No current user");
            return false;
        }
        if (principal instanceof AthenaAuthentication) {
            return ((AthenaAuthentication) principal).getPrincipal() != null;
        }
        return false;
    }

    public AthenaUser get(Long userId) {

        return athenaUserRepository.findById(userId).orElse(null);
    }

    public List<AthenaUser> getAdmins() {

        return athenaUserRepository.findByRoles_name("ROLE_ADMIN");
    }

    public List<AthenaUser> suggest(String query) {

        String suggestRequest = getSuggestRequest(query);
        return athenaUserRepository.suggestUsers(suggestRequest);
    }

    /**
     * Builds the {@code SIMILAR TO} pattern used by the user search.
     * <p>
     * The query is parameterised, so this is not SQL injection — but {@code SIMILAR TO}
     * takes a <em>pattern</em>, and every term used to be interpolated raw. That let a
     * caller inject pattern syntax: unbalanced parentheses produced a 500, and nested
     * quantifiers such as {@code ((a|a)*)*} give Postgres catastrophic backtracking across
     * the whole {@code users} table from a single unauthenticated request.
     * <p>
     * Each term is therefore escaped so it matches literally. Postgres has no quoting
     * function for {@code SIMILAR TO} patterns, so metacharacters are escaped individually
     * with a backslash — its default escape character, so the queries need no
     * {@code ESCAPE} clause.
     */
    private String getSuggestRequest(String query) {

        String[] splitted = query.trim().split(" ");
        List<String> splittedList = Arrays.stream(splitted)
                .map(String::toLowerCase)
                .map(UserService::escapeSimilarTo)
                .collect(Collectors.toList());
        return "%(" + String.join("|", splittedList) + ")%";
    }

    /** Metacharacters recognised by Postgres in a {@code SIMILAR TO} pattern. */
    private static final String SIMILAR_TO_METACHARACTERS = "\\%_|*+?{}()[]";

    private static String escapeSimilarTo(String term) {

        StringBuilder escaped = new StringBuilder(term.length());
        for (char c : term.toCharArray()) {
            if (SIMILAR_TO_METACHARACTERS.indexOf(c) >= 0) {
                escaped.append('\\');
            }
            escaped.append(c);
        }
        return escaped.toString();
    }

    public Page<AthenaUser> getUsersWithLicenses(PageRequest request, String query, boolean pendingOnly) {

        String suggestRequest = getSuggestRequest(query);
        return athenaUserRepository.getUsersWithLicenses(suggestRequest, pendingOnly, request);
    }

}
