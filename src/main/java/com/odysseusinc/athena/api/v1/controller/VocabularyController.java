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

package com.odysseusinc.athena.api.v1.controller;

import com.odysseusinc.athena.api.v1.controller.converter.vocabulary.VocabularyVersionConverter;
import com.odysseusinc.athena.api.v1.controller.dto.CustomPageImpl;
import com.odysseusinc.athena.api.v1.controller.dto.LicenseExceptionDTO;
import com.odysseusinc.athena.api.v1.controller.dto.PageDTO;
import com.odysseusinc.athena.api.v1.controller.dto.vocabulary.*;
import com.odysseusinc.athena.api.v1.controller.dto.vocabulary.AcceptDTO;
import com.odysseusinc.athena.api.v1.controller.dto.vocabulary.AddingUserLicensesDTO;
import com.odysseusinc.athena.api.v1.controller.dto.vocabulary.DownloadShareChangeDTO;
import com.odysseusinc.athena.api.v1.controller.dto.vocabulary.LicenseRequestDTO;
import com.odysseusinc.athena.api.v1.controller.dto.vocabulary.UserLicensesDTO;
import com.odysseusinc.athena.api.v1.controller.dto.vocabulary.VocabularyDTO;
import com.odysseusinc.athena.api.v1.controller.dto.vocabulary.VocabularyVersionDTO;
import com.odysseusinc.athena.exceptions.NotExistException;
import com.odysseusinc.athena.exceptions.PermissionDeniedException;
import com.odysseusinc.athena.exceptions.ValidationException;
import com.odysseusinc.athena.model.athena.DownloadBundle;
import com.odysseusinc.athena.model.security.AthenaUser;
import com.odysseusinc.athena.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Objects;

import static com.odysseusinc.athena.util.CDMVersion.getByValue;
import static com.odysseusinc.athena.util.CDMVersion.notExist;

@Tag(name = "VocabularyController")
@RestController
@RequestMapping("/api/v1/vocabularies")
public class VocabularyController extends AbstractVocabularyController {

    @Autowired
    private VocabularyReleaseVersionService versionService;

    @Operation(summary = "Save vocabularies.")
    @GetMapping("/save")
    public void save(@RequestParam(value = "cmdVersion", defaultValue = "5") float cmdVersion,
                     @RequestParam(value = "ids") List<Integer> idV4s,
                     @RequestParam(value = "name") String bundleName,
                     @RequestParam(value = "vocabularyVersion", required = false) Integer vocabularyVersion,
                     @RequestParam(value = "delta", defaultValue = "false") boolean delta,
                     @RequestParam(value = "deltaVersion", required = false) Integer deltaVersion) throws IOException {

        vocabularyVersion = vocabularyVersion != null ? vocabularyVersion :
                vocabularyServiceV5.getReleaseVocabularyVersionId();
        if (notExist(cmdVersion)) {
            throw new ValidationException("No supported CDM version " + cmdVersion);
        }
        if (delta && versionService.isCurrentMissingInHistory(vocabularyVersion)){
            throw new ValidationException("The current version has not been uploaded to historical data. The delta cannot be created. Please contact the administrator.");
        }
        if (delta && deltaVersion== null) {
            throw new ValidationException("The Delta version should be set.");
        }
        if (delta && !(deltaVersion < vocabularyVersion)) {
            throw new ValidationException("The Delta version should be lower than the Vocabulary version");
        }

        AthenaUser currentUser = userService.getCurrentUser();
        DownloadBundle bundle = vocabularyService.saveBundle(
                bundleName, idV4s, currentUser, getByValue(cmdVersion),
                vocabularyVersion, delta, deltaVersion
        );
        vocabularyService.saveContent(bundle, currentUser);
        LOGGER.info("Vocabulary saving is started, bundle name: {}, user id: {}", bundleName, currentUser.getId());
    }

    @Operation(summary = "Share bundle")
    @PostMapping(value = "/downloads/{id}/share")
    public ResponseEntity<Boolean> shareBundle(@PathVariable("id") Long bundleId,
                                               @RequestBody DownloadShareChangeDTO changeDTO,
                                               Principal principal) {

        final AthenaUser user = userService.getUser(principal);
        downloadBundleService.checkBundleOwner(user, bundleId);
        downloadShareService.change(bundleId, changeDTO.getEmailList(), user);

        return ResponseEntity.ok(Boolean.TRUE);
    }

    @Operation(summary = "Archive download history item.")
    @DeleteMapping("/downloads/{id}")
    public ResponseEntity<Boolean> archive(@PathVariable("id") Long bundleId, Principal principal)
            throws NotExistException {

        final AthenaUser user = userService.getUser(principal);
        if (!user.getId().equals(downloadBundleService.getUserId(bundleId))) {
            throw new PermissionDeniedException();
        }
        downloadBundleService.archive(bundleId);
        return ResponseEntity.ok(Boolean.TRUE);
    }

    @Operation(summary = "Restore download history item.")
    @PutMapping("/restore/{id}")
    public ResponseEntity<Void> restore(@PathVariable("id") Long bundleId)
            throws PermissionDeniedException {

        Objects.nonNull(bundleId);
        vocabularyService.restoreDownloadBundle(bundleId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Check bundle.")
    @GetMapping("/check/{id}")
    public LicenseExceptionDTO checkBundle(@PathVariable("id") Long bundleId)
            throws PermissionDeniedException {

        AthenaUser currentUser = userService.getCurrentUser();
        vocabularyService.checkBundleVocabularies(bundleId, currentUser.getId());
        return new LicenseExceptionDTO(true);
    }

    @Secured("ROLE_ADMIN")
    @Operation(summary = "Get users' licenses.")
    @GetMapping("licenses")
    public Page<UserLicensesDTO> getLicenses(
            @ModelAttribute PageDTO pageDTO, @RequestParam(name = "queryUser", defaultValue = "") String query,
            @RequestParam(name = "pendingOnly", defaultValue = "false") Boolean pendingOnly) {

        PageRequest pageRequest = PageRequest.of(pageDTO.getPage() - 1, pageDTO.getPageSize());
        final Page<AthenaUser> users = userService.getUsersWithLicenses(pageRequest, query, pendingOnly);

        List<UserLicensesDTO> dtos = converterUtils.convertList(users.getContent(), UserLicensesDTO.class);
        return new CustomPageImpl(dtos, pageRequest, users.getTotalElements());
    }

    @Secured("ROLE_ADMIN")
    @Operation(summary = "Suggest licenses.")
    @GetMapping("licenses/suggest")
    public List<VocabularyDTO> suggestLicenses(@RequestParam("userId") Long userId) {
        //PENDING licenses are added -> do not need to suggest
        final List<VocabularyDTO> vocabularies = vocabularyConversionService.getUnavailableVocabularies(userId, true);
        return vocabularies;
    }

    @Secured("ROLE_ADMIN")
    @Operation(summary = "Add user's licenses.")
    @PostMapping("licenses")
    public ResponseEntity<Void> saveLicenses(@RequestBody @Valid AddingUserLicensesDTO dto) {

        final AthenaUser user = userService.get(dto.getUserId());
        vocabularyService.grantLicenses(user, dto.getVocabularyV4Ids());
        return ResponseEntity.ok().build();
    }

    @Secured("ROLE_ADMIN")
    @Operation(summary = "Remove user's licenses.")
    @DeleteMapping("licenses/{id}")
    public ResponseEntity<Void> removeLicenses(@PathVariable("id") Long licenseId) {

        vocabularyService.deleteLicense(licenseId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Request user's license.")
    @PostMapping("licenses/request")
    public ResponseEntity<Void> requestLicense(Principal principal, @Valid @RequestBody LicenseRequestDTO dto) {

        licenseService.requestLicense(principal, dto.getVocabularyId());
        return ResponseEntity.ok().build();
    }

    @Secured("ROLE_ADMIN")
    @Operation(summary = "Accept user's license.")
    @PostMapping("licenses/accept")
    public ResponseEntity<Void> acceptLicense(@Valid @RequestBody AcceptDTO acceptDTO) {

        licenseService.checkLicense(acceptDTO.getId());
        vocabularyService.acceptLicense(acceptDTO.getId(), acceptDTO.getAccepted());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Accept user's license via mail.")
    @GetMapping("licenses/accept/mail")
    public void acceptLicenseViaMail(@RequestParam("id") Long id,
                                     @RequestParam("accepted") Boolean accepted,
                                     @RequestParam("token") String token,
                                     HttpServletResponse response) throws IOException {

        licenseService.checkLicense(id, token);
        vocabularyService.acceptLicense(id, accepted);
        response.sendRedirect("/admin/licenses");
    }


    //TODO DEV rename it to vocabulary-release-version
    @GetMapping("/release-version")
    public VocabularyVersionDTO releaseVersion() {

        Integer vocabularyVersionId = vocabularyServiceV5.getReleaseVocabularyVersionId();
        String vocabularyVersion = VocabularyVersionConverter.toNewFormat(vocabularyVersionId);
        return converterUtils.convert(vocabularyVersion, VocabularyVersionDTO.class);
    }

    @GetMapping("/vocabulary-release-versions")
    public List<VocabularyReleaseVersionDTO> releaseVersions() {
        return versionService.getReleaseVersions();
    }

}
