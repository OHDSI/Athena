package com.odysseusinc.athena.api.v1.controller.dto.vocabulary;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class RestoreAvailabilityDTO {

    private final boolean originalVersionAvailable;
    private final String originalVersion;
    private final String currentVersion;
    private final boolean delta;
}
