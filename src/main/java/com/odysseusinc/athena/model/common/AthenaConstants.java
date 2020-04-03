package com.odysseusinc.athena.model.common;

import java.time.format.DateTimeFormatter;

public class AthenaConstants {

    private AthenaConstants() {
    }

    public static final String  COMMON_DATE_FORMAT = "dd-MMM-yyyy";
    public static final String OMOP_VOCABULARY_ID = "None";

    public static final DateTimeFormatter COMMON_DATE_FORMATTER = DateTimeFormatter.ofPattern(COMMON_DATE_FORMAT);
}
