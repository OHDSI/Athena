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
import com.odysseusinc.athena.model.security.AthenaProfile;
import com.odysseusinc.athena.model.security.AthenaRole;
import com.odysseusinc.athena.model.security.AthenaUser;
import com.odysseusinc.athena.repositories.athena.AthenaRoleRepository;
import com.odysseusinc.athena.repositories.athena.AthenaUserRepository;
import com.odysseusinc.athena.util.UserProfileUtil;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.pac4j.core.authorization.generator.AuthorizationGenerator;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.TokenCredentials;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.creator.ProfileCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService implements ProfileCreator<TokenCredentials, CommonProfile>, AuthorizationGenerator<CommonProfile> {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private AthenaUserRepository athenaUserRepository;
    @Autowired
    private AthenaRoleRepository athenaRoleRepository;

    private static final String ATTR_AUTHENTICATION_METHOD = "authenticationMethod";

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

    @Override
    public CommonProfile create(TokenCredentials credentials, WebContext webContext) throws HttpAction {

        final CommonProfile profile = credentials.getUserProfile();
        AthenaProfile athenaProfile = new AthenaProfile();
        athenaProfile.setClientName(profile.getClientName());
        athenaProfile.setId(profile.getId());
        athenaProfile.setRemembered(profile.isRemembered());
        athenaProfile.addAttributes(profile.getAttributes());
        athenaProfile.addPermissions(profile.getPermissions());
        final String origin = getAuthenticationMethod(profile);
        AthenaUser athenaUser = createOrUpdateAthenaUser(profile.getId(), origin, athenaProfile, webContext);
        athenaProfile.setAthenaUser(athenaUser);
        athenaProfile.setToken(credentials.getToken());

        return athenaProfile;
    }

    private AthenaUser createOrUpdateAthenaUser(String username, String origin,
                                                AthenaProfile athenaProfile, WebContext webContext)
            throws HttpAction {

        AthenaUser user = athenaUserRepository.findByUsernameIgnoreCaseAndOrigin(username, origin);
        if (user == null) {
            if (origin == null) {
                log.warn("User origin is not defined: {}", username);
                throw HttpAction.unauthorized(webContext);
            }
            user = new AthenaUser();
            user.setUsername(username);
            user.setOrigin(origin);
            if (org.springframework.util.StringUtils.hasText(defaultRolesValue) && defaultRoles == null) {
                synchronized (monitor) {
                    initRoles();
                }
            }
            user.setRoles(defaultRoles);
        }
        mapAttributesToUser(user, athenaProfile);
        return athenaUserRepository.save(user);
    }

    private void mapAttributesToUser(AthenaUser user, final AthenaProfile profile) {

        user.setEmail(UserProfileUtil.getAttribute(profile, emailAttributeName));
        user.setFirstName(UserProfileUtil.getAttribute(profile, firstNameAttributeName));
        user.setLastName(UserProfileUtil.getAttribute(profile, lastNameAttributeName));
        user.setMiddleName(UserProfileUtil.getAttribute(profile, middleNameAttributeName));
        user.setOrganization(UserProfileUtil.getAttribute(profile, organizationAttributeName));
    }

    private String getAuthenticationMethod(CommonProfile profile) {

        if (profile.getAttributes().containsKey(ATTR_AUTHENTICATION_METHOD)) {
            Object attr = profile.getAttribute(ATTR_AUTHENTICATION_METHOD);
            if (attr instanceof String) {
                return (String) attr;
            } else if (attr instanceof JSONArray) {
                JSONArray array = (JSONArray) attr;
                if (!array.isEmpty()) {
                    return array.get(0).toString();
                }
            }
        }
        return null;
    }

    @Override
    public CommonProfile generate(WebContext context, CommonProfile commonProfile) {

        if (commonProfile instanceof AthenaProfile) {
            AthenaUser user = ((AthenaProfile) commonProfile).getAthenaUser();
            if (user != null) {
                List<AthenaRole> roles = user.getRoles();
                roles.forEach(role -> commonProfile.addRole(role.getName()));
            }
        } else {
            commonProfile.addRole("ROLE_USER");
        }
        return commonProfile;
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

    public AthenaUser getUserById(long id){
        AthenaUser athenaUser = athenaUserRepository.findById(id);
        return athenaUser;
    }

    public AthenaUser getUser(Principal principal) throws PermissionDeniedException {

        if (principal == null) {
            throw new PermissionDeniedException();
        }
        final AthenaProfile profile = UserProfileUtil.getProfile(principal).orElseThrow(() ->
                new NotExistException(AthenaUser.class));
        final AthenaUser user = profile.getAthenaUser();
        if (user == null) {
            throw new NotExistException(AthenaUser.class);
        }
        return user;
    }

    public boolean currentUserExists() {

        Authentication principal = SecurityContextHolder.getContext().getAuthentication();
        if (Objects.isNull(principal) || "anonymousUser".equals(principal.getPrincipal())) {
            LOGGER.debug("No current user");
            return false;
        }
        Optional<AthenaProfile> optional = UserProfileUtil.getProfile(principal);
        return optional.isPresent() && Objects.nonNull(optional.get().getAthenaUser());
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

    private String getSuggestRequest(String query) {

        String[] splitted = query.trim().split(" ");
        List<String> splittedList = Arrays.stream(splitted).map(String::toLowerCase).collect(Collectors.toList());
        return "%(" + String.join("|", splittedList) + ")%";
    }

    public Page<AthenaUser> getUsersWithLicenses(PageRequest request, String query, boolean pendingOnly) {

        String suggestRequest = getSuggestRequest(query);
        return athenaUserRepository.getUsersWithLicenses(suggestRequest, pendingOnly, request);
    }

}
