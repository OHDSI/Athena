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

import static com.odysseusinc.athena.util.CDMVersion.getByValue;
import static com.odysseusinc.athena.util.CDMVersion.notExist;
import static com.odysseusinc.athena.util.extractor.LicenseStatus.APPROVED;
import static java.lang.System.currentTimeMillis;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import com.odysseusinc.athena.api.v1.controller.converter.ConverterUtils;
import com.odysseusinc.athena.api.v1.controller.dto.CustomPageImpl;
import com.odysseusinc.athena.api.v1.controller.dto.LicenseExceptionDTO;
import com.odysseusinc.athena.api.v1.controller.dto.PageDTO;
import com.odysseusinc.athena.api.v1.controller.dto.vocabulary.AcceptDTO;
import com.odysseusinc.athena.api.v1.controller.dto.vocabulary.AddingUserLicensesDTO;
import com.odysseusinc.athena.api.v1.controller.dto.vocabulary.DownloadBundleDTO;
import com.odysseusinc.athena.api.v1.controller.dto.vocabulary.DownloadShareChangeDTO;
import com.odysseusinc.athena.api.v1.controller.dto.vocabulary.LicenseRequestDTO;
import com.odysseusinc.athena.api.v1.controller.dto.vocabulary.UserLicensesDTO;
import com.odysseusinc.athena.api.v1.controller.dto.vocabulary.UserVocabularyDTO;
import com.odysseusinc.athena.api.v1.controller.dto.vocabulary.VocabularyDTO;
import com.odysseusinc.athena.api.v1.controller.dto.vocabulary.VocabularyVersionDTO;
import com.odysseusinc.athena.exceptions.AlreadyExistException;
import com.odysseusinc.athena.exceptions.NotExistException;
import com.odysseusinc.athena.exceptions.PermissionDeniedException;
import com.odysseusinc.athena.model.athena.DownloadBundle;
import com.odysseusinc.athena.model.athena.License;
import com.odysseusinc.athena.model.security.AthenaUser;
import com.odysseusinc.athena.service.DownloadBundleService;
import com.odysseusinc.athena.service.DownloadShareService;
import com.odysseusinc.athena.service.VocabularyConversionService;
import com.odysseusinc.athena.service.VocabularyService;
import com.odysseusinc.athena.service.impl.UserService;
import com.odysseusinc.athena.service.mail.EmailService;
import com.odysseusinc.athena.service.writer.FileHelper;
import com.odysseusinc.athena.util.extractor.LicenseStatus;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.Principal;
import java.util.List;

import static com.odysseusinc.athena.util.CDMVersion.getByValue;
import static com.odysseusinc.athena.util.CDMVersion.notExist;
import static com.odysseusinc.athena.util.extractor.LicenseStatus.APPROVED;
import static java.lang.System.currentTimeMillis;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Api
@RestController
@RequestMapping(value = "/api/v1/vocabularies")
public class VocabularyController {
    private static final Logger LOGGER = LoggerFactory.getLogger(VocabularyController.class);

    private final ConverterUtils converterUtils;
    private final DownloadBundleService downloadBundleService;
    private final DownloadShareService downloadShareService;
    private final EmailService emailService;
    private final FileHelper fileHelper;
    private final UserService userService;
    private final VocabularyConversionService vocabularyConversionService;
    private final VocabularyService vocabularyService;

    @Autowired
    public VocabularyController(ConverterUtils converterUtils, DownloadBundleService downloadBundleService, DownloadShareService downloadShareService, EmailService emailService, FileHelper fileHelper, UserService userService, VocabularyConversionService vocabularyConversionService, VocabularyService vocabularyService) {
        this.converterUtils = converterUtils;
        this.downloadBundleService = downloadBundleService;
        this.downloadShareService = downloadShareService;
        this.emailService = emailService;
        this.fileHelper = fileHelper;
        this.userService = userService;
        this.vocabularyConversionService = vocabularyConversionService;
        this.vocabularyService = vocabularyService;
    }

    @ApiOperation("Get vocabularies.")
    @RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public ResponseEntity<List<UserVocabularyDTO>> getAllForCurrentUser() {

        return ResponseEntity.ok(vocabularyService.getAllForCurrentUser());
    }

    @ApiOperation("Save vocabularies.")
    @RequestMapping(value = "/save", method = RequestMethod.GET)
    public void save(@RequestParam(value = "cdmVersion", defaultValue = "5") float version,
                     @RequestParam(value = "ids") List<Integer> idV4s,
                     @RequestParam(value = "name") String bundleName,
                     HttpServletResponse response) throws IOException, PermissionDeniedException {

        if (notExist(version)) {
            response.sendError(SC_BAD_REQUEST, "No supported version " + version);
            return;
        }
        if (isEmpty(bundleName)) {
            response.sendError(SC_BAD_REQUEST, "Name cannot be empty");
            return;
        }
        AthenaUser currentUser = userService.getCurrentUser();
        DownloadBundle bundle = vocabularyService.saveBundle(bundleName, idV4s, currentUser, getByValue(version));
        vocabularyService.saveContent(bundle, currentUser);
        LOGGER.info("Vocabulary saving is started, bundle name: {}, user id: {}", bundleName, currentUser.getId());
    }

    @ApiOperation("Get zip.")
    @RequestMapping(value = "/zip/{uuid}", method = RequestMethod.GET)
    public void getAllFiles(
            @PathVariable("uuid") String uuid,
            HttpServletResponse response) throws NotExistException, IOException {

        DownloadBundle bundle = vocabularyService.getDownloadBundle(uuid);
        vocabularyService.checkBundleVocabularies(bundle, bundle.getUserId());
        String version = bundle.getCdmVersion().name().toLowerCase().replace(".", "_");
        String archiveName = String.format("vocabulary_download_%s_{%s}_%s.zip",
                version, uuid, Long.toString(currentTimeMillis()));

        String contentType = "application/zip, application/octet-stream";
        response.setContentType(contentType);
        response.setHeader("Content-type", contentType);
        response.setHeader("Content-Disposition",
                "attachment; filename=" + archiveName);
        response.setContentLengthLong(new File(fileHelper.getZipPath(uuid)).length());
        try(FileInputStream is = new FileInputStream(fileHelper.getZipPath(uuid))){
            IOUtils.copy(is, response.getOutputStream());
        }
        response.flushBuffer();
    }

    @ApiOperation("Get download history.")
    @RequestMapping(value = "/downloads", method = RequestMethod.GET)
    public List<DownloadBundleDTO> getDownloadHistory(Principal principal)
            throws PermissionDeniedException {

        final AthenaUser user = userService.getUser(principal);
        return vocabularyService.getDownloadHistory(user);
    }

    @ApiOperation("Share bundle")
    @PostMapping(value = "/downloads/{id}/share")
    public ResponseEntity<Boolean> shareBundle(@PathVariable("id") Long bundleId,
                                               @RequestBody DownloadShareChangeDTO changeDTO,
                                               Principal principal)
            throws PermissionDeniedException {

        final AthenaUser user = userService.getUser(principal);
        DownloadBundle bundle = downloadBundleService.get(bundleId);
        if (!user.getId().equals(bundle.getUserId())) {
            throw new PermissionDeniedException();
        }

        downloadShareService.change(bundle, changeDTO.getEmailList(), user);

        return ResponseEntity.ok(Boolean.TRUE);
    }

    @ApiOperation("Archive download history item.")
    @RequestMapping(value = "/downloads/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Boolean> archive(@PathVariable("id") Long bundleId, Principal principal)
            throws NotExistException, PermissionDeniedException {

        final AthenaUser user = userService.getUser(principal);
        if (!user.getId().equals(downloadBundleService.getUserId(bundleId))) {
            throw new PermissionDeniedException();
        }
        downloadBundleService.archive(bundleId);
        return ResponseEntity.ok(Boolean.TRUE);
    }

    @ApiOperation("Restore download history item.")
    @RequestMapping(value = "/restore/{id}", method = RequestMethod.PUT)
    public ResponseEntity restore(@PathVariable("id") Long bundleId)
            throws PermissionDeniedException {

        DownloadBundle downloadBundle = downloadBundleService.get(bundleId);
        vocabularyService.restoreDownloadBundle(downloadBundle);
        return ResponseEntity.ok().build();
    }

    @ApiOperation("Check bundle.")
    @RequestMapping(value = "/check/{id}", method = RequestMethod.GET)
    public LicenseExceptionDTO checkBundle(@PathVariable("id") Long bundleId)
            throws PermissionDeniedException {

        DownloadBundle bundle = downloadBundleService.get(bundleId);
        AthenaUser currentUser = userService.getCurrentUser();
        vocabularyService.checkBundleAndSharedUser(currentUser, bundle);
        vocabularyService.checkBundleVocabularies(bundle, currentUser.getId());
        return new LicenseExceptionDTO(true);
    }

    @Secured("ROLE_ADMIN")
    @ApiOperation("Get users' licenses.")
    @RequestMapping(value = "licenses", method = RequestMethod.GET)
    public Page<UserLicensesDTO> getLicenses(
            @ModelAttribute PageDTO pageDTO, @RequestParam(name = "queryUser", defaultValue = "") String query,
            @RequestParam(name = "pendingOnly", defaultValue = "false") Boolean pendingOnly) {

        PageRequest pageRequest = new PageRequest(pageDTO.getPage() - 1, pageDTO.getPageSize());
        final Page<AthenaUser> users = userService.getUsersWithLicenses(pageRequest, query, pendingOnly);

        List<UserLicensesDTO> dtos = converterUtils.convertList(users.getContent(), UserLicensesDTO.class);
        return new CustomPageImpl(dtos, pageRequest, users.getTotalElements());
    }

    @Secured("ROLE_ADMIN")
    @ApiOperation("Suggest licenses.")
    @RequestMapping(value = "licenses/suggest", method = RequestMethod.GET)
    public List<VocabularyDTO> suggestLicenses(@RequestParam("userId") Long userId) {
        //PENDING licenses are added -> do not need to suggest
        final List<VocabularyDTO> vocabularies = vocabularyConversionService.getUnavailableVocabularies(userId, true);
        return vocabularies;
    }

    @Secured("ROLE_ADMIN")
    @ApiOperation("Add user's licenses.")
    @RequestMapping(value = "licenses", method = RequestMethod.POST)
    public ResponseEntity saveLicenses(@RequestBody @Valid AddingUserLicensesDTO dto) {

        vocabularyService.saveLicenses(userService.get(dto.getUserId()), dto.getVocabularyV4Ids(), APPROVED);
        return ResponseEntity.ok().build();
    }

    @Secured("ROLE_ADMIN")
    @ApiOperation("Remove user's licenses.")
    @RequestMapping(value = "licenses/{id}", method = RequestMethod.DELETE)
    public ResponseEntity removeLicenses(@PathVariable("id") Long licenseId) {

        vocabularyService.deleteLicense(licenseId);
        return ResponseEntity.ok().build();
    }

    @ApiOperation("Request user's license.")
    @RequestMapping(value = "licenses/request", method = RequestMethod.POST)
    public ResponseEntity requestLicense(Principal principal, @Valid @RequestBody LicenseRequestDTO dto)
            throws PermissionDeniedException {

        AthenaUser user = userService.getUser(principal);
        License license = vocabularyService.get(user, dto.getVocabularyId());
        if (license != null) {
            throw new AlreadyExistException("License already exists");
        }
        Long licenseId = vocabularyService.requestLicenses(user, dto.getVocabularyId());
        emailService.sendLicenseRequestToAdmins(vocabularyService.get(licenseId));
        return ResponseEntity.ok().build();
    }

    @Secured("ROLE_ADMIN")
    @ApiOperation("Accept user's license.")
    @RequestMapping(value = "licenses/accept", method = RequestMethod.POST)
    public ResponseEntity acceptLicense(@Valid @RequestBody AcceptDTO acceptDTO)
            throws PermissionDeniedException {

        checkLicense(vocabularyService.get(acceptDTO.getId()));
        vocabularyService.acceptLicense(acceptDTO.getId(), acceptDTO.getAccepted());
        return ResponseEntity.ok().build();
    }

    @ApiOperation("Accept user's license via mail.")
    @RequestMapping(value = "licenses/accept/mail", method = RequestMethod.GET)
    public void acceptLicenseViaMail(@RequestParam("id") Long id,
                                     @RequestParam("accepted") Boolean accepted,
                                     @RequestParam("token") String token,
                                     HttpServletResponse response)
            throws PermissionDeniedException, IOException {

        checkLicense(vocabularyService.get(id, token));
        vocabularyService.acceptLicense(id, accepted);
        response.sendRedirect("/admin/licenses");
    }

    private void checkLicense(License license) {

        if (license == null) {
            throw new NotExistException("License does not exist or has already been declined", License.class);
        } else if (LicenseStatus.APPROVED == license.getStatus()) {
            throw new AlreadyExistException("License has already been approved");
        }
    }

    @GetMapping(value = "/release-version")
    public VocabularyVersionDTO releaseVersion() {

        String vocabularyVersion = vocabularyService.getOMOPVocabularyVersion();

        return converterUtils.convert(vocabularyVersion, VocabularyVersionDTO.class);
    }
}
