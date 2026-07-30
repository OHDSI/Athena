package com.odysseusinc.athena.model.common;

import java.time.format.DateTimeFormatter;

public class AthenaConstants {

    private AthenaConstants() {
    }

    public static final String  COMMON_DATE_FORMAT = "dd-MMM-yyyy";
    public static final String DEFAULT_TEMPLATE_NAME = "index";
    public static final String OMOP_RELEASE_VOCABULARY_ID = "None";

    public static final long SEC_MS = 1000L;
    public static final long FIVE_SEC_MS = 5 * SEC_MS;
    public static final long MIN_MS = 60 * SEC_MS;

    public static final DateTimeFormatter COMMON_DATE_FORMATTER = DateTimeFormatter.ofPattern(COMMON_DATE_FORMAT);

}
