package com.odysseusinc.athena.api.v1.controller.dto.vocabulary;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public class VocabularyReleaseVersionDTO {
    private final Integer value; //TODO DEV rename it to the id or code.
    private final String label;
    private final boolean current;
}
