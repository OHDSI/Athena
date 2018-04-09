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

package com.odysseusinc.athena.service.impl;

import com.opencsv.CSVWriter;
import com.opencsv.FileBuffer;
import com.opencsv.ResultSetHelperService;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AthenaCSVWriter extends CSVWriter {

    public AthenaCSVWriter(Writer writer, char separator) {

        super(writer, separator, NO_QUOTE_CHARACTER);
    }

    public AthenaCSVWriter(String file, char separator) throws IOException {

        super(file, separator, NO_QUOTE_CHARACTER, NO_ESCAPE_CHARACTER, DEFAULT_LINE_END);
        buffer = new FileBuffer(INITIAL_BUFFER_SIZE, file, "csv", StandardCharsets.UTF_8.name());
    }

    protected ResultSetHelperService getResultSetHelperService(java.sql.ResultSet rs) throws SQLException {

        ResultSetHelperService.IS_TRIM = false;
        ResultSetHelperService resultSetHelperService = new AthenaResultSetHelperService(rs);
        resultSetHelperService.setDefaultDateFormat("yyyyMMdd");
        return resultSetHelperService;
    }

    protected CSVWriter add(String str) throws IOException {
        buffer.write(str.getBytes(StandardCharsets.UTF_8));
        int len = str.length();
        lineWidth += len;
        return this;
    }

    @Override
    public int writeAll(ResultSet rs, boolean includeColumnNames, boolean trim) throws Exception {

        try (CSVWriter c = this; ResultSetHelperService resultService = getResultSetHelperService(rs)) {
            this.resultService = resultService;
            titles = new String[resultService.columnNames.length];
            for (int i = 0; i < titles.length; i++) {
                titles[i] = resultService.columnNames[i].trim().toUpperCase();
            }
            if (includeColumnNames) {
                writeColumnNames();
            }
            Object[] values;
            while ((values = resultService.getColumnValues(trim)) != null) {
                writeNext(values);
            }

            return totalRows;
        }
    }


}
