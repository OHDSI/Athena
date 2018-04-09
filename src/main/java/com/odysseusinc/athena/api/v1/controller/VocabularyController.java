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

package com.odysseusinc.athena.api.v1.controller;

import static com.odysseusinc.athena.util.CDMVersion.getByValue;
import static com.odysseusinc.athena.util.CDMVersion.notExist;
import static com.odysseusinc.athena.util.extractor.LicenseStatus.APPROVED;
import static java.lang.System.currentTimeMillis;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import com.odysseusinc.athena.api.v1.controller.converter.ConverterUtils;
import com.odysseusinc.athena.api.v1.controller.dto.CustomPageImpl;
import com.odysseusinc.athena.api.v1.controller.dto.PageDTO;
import com.odysseusinc.athena.api.v1.controller.dto.VocabularyForNotificationDTO;
import com.odysseusinc.athena.api.v1.controller.dto.vocabulary.AcceptDTO;
import com.odysseusinc.athena.api.v1.controller.dto.vocabulary.AddingUserLicensesDTO;
import com.odysseusinc.athena.api.v1.controller.dto.vocabulary.DownloadBundleDTO;
import com.odysseusinc.athena.api.v1.controller.dto.vocabulary.LicenseRequestDTO;
import com.odysseusinc.athena.api.v1.controller.dto.vocabulary.UserLicensesDTO;
import com.odysseusinc.athena.api.v1.controller.dto.vocabulary.UserVocabularyDTO;
import com.odysseusinc.athena.api.v1.controller.dto.vocabulary.VocabularyDTO;
import com.odysseusinc.athena.exceptions.AlreadyExistException;
import com.odysseusinc.athena.exceptions.NotExistException;
import com.odysseusinc.athena.exceptions.PermissionDeniedException;
import com.odysseusinc.athena.model.athena.DownloadBundle;
import com.odysseusinc.athena.model.athena.License;
import com.odysseusinc.athena.model.athena.Notification;
import com.odysseusinc.athena.model.security.AthenaUser;
import com.odysseusinc.athena.service.DownloadBundleService;
import com.odysseusinc.athena.service.VocabularyService;
import com.odysseusinc.athena.service.impl.UserService;
import com.odysseusinc.athena.service.mail.LicenseRequestSender;
import com.odysseusinc.athena.service.writer.FileHelper;
import com.odysseusinc.athena.util.extractor.LicenseStatus;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.Principal;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api
@RestController
@RequestMapping(value = "/api/v1/vocabularies")
public class VocabularyController {
    private static final Logger LOGGER = LoggerFactory.getLogger(VocabularyController.class);

    private VocabularyService vocabularyService;
    private LicenseRequestSender licenseRequestSender;
    private UserService userService;
    private DownloadBundleService downloadBundleService;
    private FileHelper fileHelper;
    private ConverterUtils converterUtils;

    @Autowired
    @SuppressWarnings("SpringJavaAutowiringInspection")
    public VocabularyController(VocabularyService vocabularyService,
                                UserService userService,
                                DownloadBundleService downloadBundleService,
                                FileHelper fileHelper,
                                ConverterUtils converterUtils,
                                LicenseRequestSender licenseRequestSender) {

        this.vocabularyService = vocabularyService;
        this.userService = userService;
        this.downloadBundleService = downloadBundleService;
        this.fileHelper = fileHelper;
        this.converterUtils = converterUtils;
        this.licenseRequestSender = licenseRequestSender;
    }

    @ApiOperation("Get vocabularies.")
    @RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public ResponseEntity<List<UserVocabularyDTO>> getAllForCurrentUser() throws Exception {

        return new ResponseEntity<>(vocabularyService.getAllForCurrentUser(), OK);
    }

    @ApiOperation("Save vocabularies.")
    @RequestMapping(value = "/save", method = RequestMethod.GET)
    public void save(@RequestParam(value = "cdmVersion", defaultValue = "5") float version,
                     @RequestParam(value = "ids") List<Long> idV4s,
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
        //PENDING licenses are not active
        boolean noLicence = vocabularyService.missingAvailableForDownloadingLicenses(currentUser.getId(), false).stream()
                .map(VocabularyDTO::getId)
                .map(Long::new)
                .anyMatch(idV4s::contains);

        if (noLicence) {
            throw new PermissionDeniedException("User must have licenses for all vocabularies");
        }
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
        String version = bundle.getCdmVersion().name().toLowerCase().replace(".", "_");
        String archiveName = String.format("vocabulary_download_%s_{%s}_%s.zip",
                version, uuid, Long.toString(currentTimeMillis()));

        String contentType = "application/zip, application/octet-stream";
        response.setContentType(contentType);
        response.setHeader("Content-type", contentType);
        response.setHeader("Content-Disposition",
                "attachment; filename=" + archiveName);
        response.setContentLengthLong(new File(fileHelper.getZipPath(uuid)).length());
        IOUtils.copy(new FileInputStream(fileHelper.getZipPath(uuid)), response.getOutputStream());
        response.flushBuffer();
    }

    @ApiOperation("Get download history.")
    @RequestMapping(value = "/downloads", method = RequestMethod.GET)
    public ResponseEntity<List<DownloadBundleDTO>> getDownloadHistory(Principal principal)
            throws PermissionDeniedException {

        final AthenaUser user = userService.getUser(principal);
        return new ResponseEntity<>(vocabularyService.getDownloadHistory(user.getId()), OK);
    }

    @ApiOperation("Archive download history item.")
    @RequestMapping(value = "/downloads/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Boolean> archive(@PathVariable("id") Long bundleId, Principal principal)
            throws NotExistException, IOException, PermissionDeniedException {

        final AthenaUser user = userService.getUser(principal);
        if (!user.getId().equals(downloadBundleService.getUserId(bundleId))) {
            throw new PermissionDeniedException();
        }
        downloadBundleService.archive(bundleId);
        return new ResponseEntity<>(Boolean.TRUE, OK);
    }

    @ApiOperation("Restore download history item.")
    @RequestMapping(value = "/restore/{id}", method = RequestMethod.PUT)
    public ResponseEntity restore(@PathVariable("id") Long bundleId, Principal principal)
            throws PermissionDeniedException {

        final AthenaUser user = userService.getUser(principal);
        DownloadBundle downloadBundle = downloadBundleService.get(bundleId);
        if (!user.getId().equals(downloadBundle.getUserId())) {
            throw new PermissionDeniedException();
        }
        vocabularyService.restoreDownloadBundle(downloadBundle);
        return new ResponseEntity<>(OK);
    }

    @Secured("ROLE_ADMIN")
    @ApiOperation("Get users' licenses.")
    @RequestMapping(value = "licenses", method = RequestMethod.GET)
    public ResponseEntity<CustomPageImpl<UserLicensesDTO>> getLicenses(
            @ModelAttribute PageDTO pageDTO, @RequestParam(name = "queryUser", defaultValue = "") String query,
            @RequestParam(name = "pendingOnly", defaultValue = "false") Boolean pendingOnly) {

        PageRequest pageRequest = new PageRequest(pageDTO.getPage() - 1, pageDTO.getPageSize());
        final Page<AthenaUser> users = userService.getUsersWithLicenses(pageRequest, query, pendingOnly);

        List<UserLicensesDTO> dtos = converterUtils.convertList(users.getContent(), UserLicensesDTO.class);
        CustomPageImpl<UserLicensesDTO> resultPage = new CustomPageImpl<>(dtos, pageRequest, users.getTotalElements());

        return new ResponseEntity<>(resultPage, OK);
    }

    @Secured("ROLE_ADMIN")
    @ApiOperation("Suggest licenses.")
    @RequestMapping(value = "licenses/suggest", method = RequestMethod.GET)
    public ResponseEntity<List<VocabularyDTO>> suggestLicenses(@RequestParam("userId") Long userId) {
        //PENDING licenses are added -> do not need to suggest
        final List<VocabularyDTO> vocabularies = vocabularyService.missingAvailableForDownloadingLicenses(userId, true);
        return new ResponseEntity<>(vocabularies, OK);
    }

    @Secured("ROLE_ADMIN")
    @ApiOperation("Add user's licenses.")
    @RequestMapping(value = "licenses", method = RequestMethod.POST)
    public ResponseEntity saveLicenses(@RequestBody @Valid AddingUserLicensesDTO dto) {

        vocabularyService.saveLicenses(userService.get(dto.getUserId()), dto.getVocabularyV4Ids(), APPROVED);
        return new ResponseEntity(OK);
    }

    @Secured("ROLE_ADMIN")
    @ApiOperation("Remove user's licenses.")
    @RequestMapping(value = "licenses/{id}", method = RequestMethod.DELETE)
    public ResponseEntity removeLicenses(@PathVariable("id") Long licenseId) {

        vocabularyService.deleteLicense(licenseId);
        return new ResponseEntity(OK);
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
        licenseRequestSender.sendToAdmins(vocabularyService.get(licenseId));
        return new ResponseEntity(OK);
    }

    @Secured("ROLE_ADMIN")
    @ApiOperation("Accept user's license.")
    @RequestMapping(value = "licenses/accept", method = RequestMethod.POST)
    public ResponseEntity acceptLicense(@Valid @RequestBody AcceptDTO acceptDTO)
            throws PermissionDeniedException {

        checkLicense(vocabularyService.get(acceptDTO.getId()));
        vocabularyService.acceptLicense(acceptDTO.getId(), acceptDTO.getAccepted());
        return new ResponseEntity(OK);
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

    @RequestMapping(value = "/notifications", method = POST)
    public ResponseEntity notifyAboutUpdates(
            @Valid @RequestBody VocabularyForNotificationDTO dto, Principal principal)
            throws PermissionDeniedException {

        final AthenaUser user = userService.getUser(principal);
        vocabularyService.notifyAboutUpdates(user.getId(), dto.getVocabularyV4Id(), dto.getNotify());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/notifications", method = GET)
    public ResponseEntity<List<VocabularyDTO>> getVocabulariesForNotification(Principal principal)
            throws PermissionDeniedException {

        final AthenaUser user = userService.getUser(principal);
        List<Notification> notifications = vocabularyService.getNotifications(user.getId());
        List<VocabularyDTO> vocabularyDTOs = converterUtils.convertList(notifications, VocabularyDTO.class);
        return new ResponseEntity<>(vocabularyDTOs, HttpStatus.OK);
    }
}
