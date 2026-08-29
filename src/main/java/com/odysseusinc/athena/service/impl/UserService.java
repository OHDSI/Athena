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
import com.odysseusinc.athena.repositories.athena.AthenaRoleRepository;
import com.odysseusinc.athena.repositories.athena.AthenaUserRepository;
import com.odysseusinc.athena.security.AthenaAuthentication;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private AthenaUserRepository athenaUserRepository;
    @Autowired
    private AthenaRoleRepository athenaRoleRepository;

    @Value("${athena.security.defaultRoles}")
    private String defaultRolesValue;

    private List<AthenaRole> defaultRoles;

    private final Object monitor = new Object();
    @Value("${athena.security.saml.attributes.email}")
    private String emailAttributeName = "email";
    @Value("${athena.security.saml.attributes.first_name}")
    private String firstNameAttributeName = "firstName";
    @Value("${athena.security.saml.attributes.last_name}")
    private String lastNameAttributeName = "lastName";
    @Value("${athena.security.saml.attributes.middle_name}")
    private String middleNameAttributeName;
    @Value("${athena.security.saml.attributes.organization}")
    private String organizationAttributeName;

    /**
     * Creates or updates the local user record for someone who has just authenticated
     * against the identity provider. Driven by already-flattened assertion attributes.
     *
     * @param username   the assertion subject; with {@code origin} this is the natural
     *                   key of {@link AthenaUser}
     * @param origin     which identity provider asserted this user
     * @param attributes assertion attributes, keyed by the raw names the IdP sent
     * @throws PermissionDeniedException if {@code origin} is absent, matching the old
     *                                   behaviour of refusing to provision a user whose
     *                                   authentication method is unknown
     */
    @Transactional(transactionManager = "athenaTransactionManager")
    public AthenaUser provisionUser(String username, String origin, Map<String, String> attributes) {

        AthenaUser user = athenaUserRepository.findByUsernameIgnoreCaseAndOrigin(username, origin);
        if (user == null) {
            if (origin == null) {
                log.warn("User origin is not defined: {}", username);
                throw new PermissionDeniedException();
            }
            user = new AthenaUser();
            user.setUsername(username);
            user.setOrigin(origin);
            user.setRoles(newDefaultRoles());
        }
        // Only overwrite a field when the assertion actually carried it. The pac4j
        // implementation used UserProfileUtil.getAttribute, which returned null for an
        // absent attribute and so wiped the stored value — an IdP omitting an optional
        // attribute silently erased it. Absent now means "unchanged".
        ifPresent(attributes, emailAttributeName, user::setEmail);
        ifPresent(attributes, firstNameAttributeName, user::setFirstName);
        ifPresent(attributes, lastNameAttributeName, user::setLastName);
        ifPresent(attributes, middleNameAttributeName, user::setMiddleName);
        ifPresent(attributes, organizationAttributeName, user::setOrganization);

        return athenaUserRepository.save(user);
    }

    private static void ifPresent(Map<String, String> attributes, String name,
                                  Consumer<String> setter) {

        if (name != null && attributes.containsKey(name)) {
            setter.accept(attributes.get(name));
        }
    }

    /**
     * A fresh list per user. {@code createOrUpdateAthenaUser} handed the same
     * cached {@code List} instance to every new user, which Hibernate rejects as a
     * shared collection reference on a {@code @ManyToMany}. The role entities are
     * shared — only the collection wrapper must not be.
     */
    private List<AthenaRole> newDefaultRoles() {

        if (!StringUtils.hasText(defaultRolesValue)) {
            return new ArrayList<>();
        }
        synchronized (monitor) {
            initRoles();
            return defaultRoles == null ? new ArrayList<>() : new ArrayList<>(defaultRoles);
        }
    }

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

    private void initRoles() {

        if (defaultRoles == null) {
            if (StringUtils.hasText(defaultRolesValue)) {
                defaultRoles = athenaRoleRepository.findByNames(Arrays.asList(defaultRolesValue.split(",")));
            } else {
                defaultRoles = new ArrayList<>();
            }
        }
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
