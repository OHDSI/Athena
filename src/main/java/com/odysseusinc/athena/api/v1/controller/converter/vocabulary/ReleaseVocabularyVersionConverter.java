package com.odysseusinc.athena.api.v1.controller.converter.vocabulary;

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
 * Author: Yaroslav Molodkov
 * Created: December 7, 2023
 *
 */

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The ReleaseVocabularyVersionConverter is designed to convert version formats between the old and new representations.
 * The old format is utilized in the database (e.g., in SQL queries such as 'SELECT vocabulary_version FROM vocabulary WHERE vocabulary_id = ''None''),
 * while the new format is intended for display and presentation purposes.
 */
public class ReleaseVocabularyVersionConverter {

    private static final ThreadLocal<SimpleDateFormat> NEW_DATE_FORMAT = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyyMMdd"));
    private static final ThreadLocal<SimpleDateFormat> OLD_DATE_FORMAT = ThreadLocal.withInitial(() -> new SimpleDateFormat("dd-MMM-yy"));

    private static final String CDM_VERSION = "5";

    public static String toOldFormat(Integer id) {
        if (id == null) {
            return null;
        }
        String stringToConvert = String.valueOf(id);
        if (stringToConvert.matches("\\d{8}")) {
            try {
                Date date = NEW_DATE_FORMAT.get().parse(stringToConvert);
                return String.format("v%s %s", CDM_VERSION, OLD_DATE_FORMAT.get().format(date).toUpperCase());
            } catch (ParseException e) {
                throw new IllegalArgumentException("Error parsing date: " + stringToConvert, e);
            }
        } else {
            throw new IllegalArgumentException("Invalid vocabulary version format: " + stringToConvert);
        }
    }

    public static String toNewFormat(Integer id) {
        if (id == null) {
            return null;
        }
        String stringToConvert = String.valueOf(id);
        if (stringToConvert.matches("\\d{8}")) {
            return String.format("v%s", stringToConvert);
        } else {
            throw new IllegalArgumentException("Invalid vocabulary version format: " + stringToConvert);
        }
    }

    public static Integer fromOldToId(String newFormatted) {
        if (newFormatted == null) {
            return null;
        }
        String[] parts = newFormatted.split(" ");
        if (parts.length == 2) {
            try {
                Date date = OLD_DATE_FORMAT.get().parse(parts[1]);
                return Integer.parseInt(NEW_DATE_FORMAT.get().format(date));
            } catch (ParseException e) {
                throw new IllegalArgumentException("Error formatting date: " + parts[1], e);
            }
        } else {
            throw new IllegalArgumentException("Invalid formatted version: " + newFormatted);
        }
    }


    public static Integer fromNewToId(String newFormatted) {
        if (newFormatted.matches("v\\d{8}")) {
            return Integer.parseInt(newFormatted.substring(1));
        } else {
            throw new IllegalArgumentException("Invalid formatted version: " + newFormatted);
        }
    }

    public static String fromOldToNew(String oldFormatted) {
        return toNewFormat(
                fromOldToId(oldFormatted)
        );
    }
}
