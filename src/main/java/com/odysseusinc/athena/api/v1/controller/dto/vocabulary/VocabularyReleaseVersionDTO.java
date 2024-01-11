package com.odysseusinc.athena.api.v1.controller.dto.vocabulary;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class VocabularyReleaseVersionDTO {
    private final Integer value;
    private final String label;
    private final boolean current;

    //TODO DEV this suppose to be process on the front-end
    public String getLabel() {
        if (current) {
            return String.format("%s(current)", label);
        }
        return label;
    }
}
