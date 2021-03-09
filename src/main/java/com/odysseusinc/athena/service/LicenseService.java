package com.odysseusinc.athena.service;

import com.odysseusinc.athena.api.v1.controller.dto.LicenseExceptionDTO;

import java.security.Principal;

public interface LicenseService {

    LicenseExceptionDTO checkBundle(Long aLong);

    void checkLicense(Long id, String token);

    void checkLicense(Long id);

    Long requestLicense(Principal principal, Integer vocabularyId);
}
