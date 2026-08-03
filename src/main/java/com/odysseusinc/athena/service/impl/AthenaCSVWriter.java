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
 * Authors: Pavel Grafkin, Vitaly Koulakov, Maria Pozhidaeva
 * Created: April 4, 2018
 *
 */

package com.odysseusinc.athena.service.impl;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.util.List;

/**
 * Writes the delimited files Athena produces: vocabulary bundle tables, the concept search
 * export and the admin statistics export.
 * <p>
 * Previously extended {@code com.opencsv.CSVWriter} from a hand-vendored
 * {@code opencsv.jar}, which calls {@code sun.misc.Cleaner} — removed in Java 9 — and so
 * cannot run on a supported JDK. This is a direct replacement with no external
 * dependency.
 *
 * <h2>Not really CSV</h2>
 * The old configuration used {@code NO_QUOTE_CHARACTER} and {@code NO_ESCAPE_CHARACTER}, so
 * output is simply the values joined by the separator and terminated with {@code \n}, with
 * {@code null} written as an empty field. That is reproduced exactly, including the
 * consequence that <b>a value containing the separator or a newline corrupts the file</b> — a
 * concept name containing a tab silently shifts every later column. Fixing that would change
 * the format of every generated file, so it is left alone.
 * <p>
 * The byte-level output is locked by {@code AthenaCsvWriterGoldenTest}, whose expectations
 * were captured by running the previous implementation against the vendored jar on JDK 8.
 */
public class AthenaCSVWriter implements Closeable {

    private static final String LINE_END = "\n";

    private final Writer writer;
    private final char separator;
    private final boolean neutraliseFormulas;

    public AthenaCSVWriter(Writer writer, char separator) {

        this(writer, separator, false);
    }

    public AthenaCSVWriter(Writer writer, char separator, boolean neutraliseFormulas) {

        this.writer = writer;
        this.separator = separator;
        this.neutraliseFormulas = neutraliseFormulas;
    }

    public AthenaCSVWriter(String file, char separator) throws IOException {

        this(file, separator, false);
    }

    /**
     * @param neutraliseFormulas see {@link #neutralise(String)}. Pass {@code true} for report
     *                           exports that a person opens in a spreadsheet; leave it
     *                           {@code false} for the vocabulary bundle files, whose bytes are
     *                           a published contract.
     */
    public AthenaCSVWriter(String file, char separator, boolean neutraliseFormulas) throws IOException {

        this(new BufferedWriter(new OutputStreamWriter(
                Files.newOutputStream(Paths.get(file)), StandardCharsets.UTF_8)), separator,
                neutraliseFormulas);
    }

    /**
     * Writes the whole result set, optionally preceded by a header row.
     *
     * @param trim whether to trim string values; production callers pass {@code false}
     * @return the number of data rows written
     */
    public int writeAll(ResultSet rs, boolean includeColumnNames, boolean trim) throws Exception {

        int rows = 0;
        try (ResultSetCsvValues values = new ResultSetCsvValues(rs)) {
            if (includeColumnNames) {
                writeNext(values.columnNames());
            }
            Object[] row;
            while ((row = values.nextRow(trim)) != null) {
                writeNext(row);
                rows++;
            }
        }
        return rows;
    }

    public void writeAll(List<String[]> rows) throws IOException {

        for (String[] row : rows) {
            writeNext(row);
        }
    }

    public void writeNext(Object[] values) throws IOException {

        if (values == null) {
            return;
        }
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                line.append(separator);
            }
            if (values[i] != null) {
                line.append(neutraliseFormulas ? neutralise(String.valueOf(values[i])) : values[i]);
            }
        }
        line.append(LINE_END);
        writer.write(line.toString());
    }

    /**
     * Characters that make Excel, LibreOffice and Google Sheets treat a cell as a formula
     * rather than text when it appears first.
     */
    private static final String FORMULA_TRIGGERS = "=+-@\t\r";

    /**
     * Prefixing with an apostrophe is the standard neutralisation — spreadsheets read it as
     * "the rest is literal text" and do not display it. A leading {@code -} is left alone when
     * the value is a plain negative number, so numeric columns keep sorting as numbers.
     */
    private static String neutralise(String value) {

        if (value.isEmpty() || FORMULA_TRIGGERS.indexOf(value.charAt(0)) < 0) {
            return value;
        }
        if (value.charAt(0) == '-' && isNumber(value)) {
            return value;
        }
        return "'" + value;
    }

    private static boolean isNumber(String value) {

        try {
            new java.math.BigDecimal(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * @param applyQuotesToAll ignored — kept so existing call sites read unchanged. The old
     *                         writer had no quote character configured, so this had no
     *                         effect there either.
     */
    public void writeNext(Object[] values, boolean applyQuotesToAll) throws IOException {

        writeNext(values);
    }

    /**
     * @param flushUnderlying ignored — this writer keeps no secondary buffer. Retained
     *                        because callers pass {@code true}.
     */
    public void flush(boolean flushUnderlying) throws IOException {

        writer.flush();
    }

    public void flush() throws IOException {

        writer.flush();
    }

    @Override
    public void close() throws IOException {

        writer.close();
    }
}
