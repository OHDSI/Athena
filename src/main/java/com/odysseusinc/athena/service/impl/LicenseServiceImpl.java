package com.odysseusinc.athena.service.impl;


import com.odysseusinc.athena.api.v1.controller.dto.LicenseExceptionDTO;
import com.odysseusinc.athena.exceptions.AlreadyExistException;
import com.odysseusinc.athena.exceptions.NotExistException;
import com.odysseusinc.athena.model.athena.DownloadBundle;
import com.odysseusinc.athena.model.athena.License;
import com.odysseusinc.athena.model.security.AthenaUser;
import com.odysseusinc.athena.service.DownloadBundleService;
import com.odysseusinc.athena.service.LicenseService;
import com.odysseusinc.athena.service.VocabularyService;
import com.odysseusinc.athena.service.mail.EmailService;
import com.odysseusinc.athena.util.extractor.LicenseStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.Optional;

@Service
@Transactional
public class LicenseServiceImpl implements LicenseService {

    private final DownloadBundleService downloadBundleService;
    private final EmailService emailService;
    private final UserService userService;
    private final VocabularyService vocabularyService;

    public LicenseServiceImpl(DownloadBundleService downloadBundleService, EmailService emailService, UserService userService, VocabularyService vocabularyService) {

        this.downloadBundleService = downloadBundleService;
        this.emailService = emailService;
        this.userService = userService;
        this.vocabularyService = vocabularyService;
    }

    @Override
    public LicenseExceptionDTO checkBundle(Long bundleId) {

        DownloadBundle bundle = downloadBundleService.get(bundleId);
        AthenaUser currentUser = userService.getCurrentUser();
        vocabularyService.checkBundleAndSharedUser(currentUser, bundle);
        vocabularyService.checkBundleVocabularies(bundle.getId(), currentUser.getId());
        return new LicenseExceptionDTO(true);

    }

    @Override
    public void checkLicense(Long id, String token) {

        final License license = vocabularyService.get(id, token);
        check(Optional.ofNullable(license));
    }

    @Override
    public void checkLicense(Long licenseId) {

        final Optional<License> license = vocabularyService.get(licenseId);
        check(license);
    }

    @Override
    public Long requestLicense(Principal principal, Integer vocabularyId) {

        AthenaUser user = userService.getUser(principal);
        License license = vocabularyService.get(user, vocabularyId);
        if (license != null) {
            throw new AlreadyExistException("License already exists");
        }
        Long licenseId = vocabularyService.requestLicense(user, vocabularyId);
        emailService.sendLicenseRequestToAdmins(vocabularyService.get(licenseId).get());
        return licenseId;
    }

    private void check(Optional<License> license) {
        if (license == null || !license.isPresent()) {
            throw new NotExistException("License does not exist or has already been declined", License.class);
        } else if (LicenseStatus.APPROVED == license.get().getStatus()) {
            throw new AlreadyExistException("License has already been approved");
        }
    }
}