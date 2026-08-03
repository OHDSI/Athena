/*
 *
 * Copyright 2026 Odysseus Data Services, Inc. (EPAM Systems company)
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
 * Created: July 30, 2026
 *
 */

package com.odysseusinc.athena.service.impl;

import org.junit.Test;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.sql.Types;

import static org.junit.Assert.assertEquals;

/**
 * Locks the byte-level output of {@link AthenaCSVWriter}.
 * <p>
 * Vocabulary bundles are consumed by OMOP tooling, so the delimited format is a contract.
 * The expected text below is <b>not</b> hand-written: it was produced by running the
 * previous implementation — {@code AthenaCSVWriter} plus
 * {@code AthenaResultSetHelperService} on top of the vendored {@code opencsv.jar} — on
 * JDK 8, before that jar was removed. Anything that changes these bytes changes
 * every generated vocabulary file.
 * <p>
 * Three of these behaviours are surprising enough to be worth stating, since all three
 * would be easy to "fix" and thereby break the format:
 * <ul>
 *   <li>Headers are the raw driver column names, <b>not</b> upper-cased. The old code built
 *       an upper-cased {@code titles} array that the vendored writer then ignored.</li>
 *   <li>{@code BigDecimal("1.50")} is written {@code 1.5} — trailing zeros are dropped by
 *       the {@code DecimalFormat} path.</li>
 *   <li>A value containing the separator is written through unescaped, corrupting the row.
 *       Asserted here so the flaw is documented rather than accidental.</li>
 * </ul>
 * JUnit 4 on purpose.
 */
public class AthenaCsvWriterGoldenTest {

    private static final String[] NAMES =
            {"c_int", "c_long", "c_str", "c_num", "c_date", "c_ts", "c_bool"};
    private static final int[] TYPES = {Types.INTEGER, Types.BIGINT, Types.VARCHAR, Types.NUMERIC,
            Types.DATE, Types.TIMESTAMP, Types.BOOLEAN};

    private static final Object[][] ROWS = {
            {42, 9007199254740993L, "plain", new BigDecimal("1.50"),
                    Date.valueOf("2026-07-30"), Timestamp.valueOf("2026-07-30 01:02:03.0"), Boolean.TRUE},
            {-1, 0L, "with\ttab and \"quote\"", new BigDecimal("0.000001"),
                    Date.valueOf("1970-01-01"), Timestamp.valueOf("1999-12-31 23:59:59.123"), Boolean.FALSE},
            {null, null, null, null, null, null, null},
    };

    /** Captured from the pre-migration implementation on JDK 8. */
    private static final String GOLDEN =
            "c_int\tc_long\tc_str\tc_num\tc_date\tc_ts\tc_bool\n"
          + "42\t9007199254740993\tplain\t1.5\t20260730\t2026-07-30 01:02:03\ttrue\n"
          + "-1\t0\twith\ttab and \"quote\"\t0.000001\t19700101\t1999-12-31 23:59:59.123\tfalse\n"
          + "\t\t\t\t\t\t\n";

    @Test
    public void resultSetOutputIsByteIdenticalToThePreviousImplementation() throws Exception {

        File out = File.createTempFile("athena-csv-golden", ".csv");
        out.deleteOnExit();

        try (AthenaCSVWriter writer = new AthenaCSVWriter(out.getAbsolutePath(), '\t')) {
            int rows = writer.writeAll(resultSet(), true, false);
            writer.flush(true);
            assertEquals("data rows written (header excluded)", ROWS.length, rows);
        }

        assertEquals(GOLDEN, new String(Files.readAllBytes(out.toPath()), StandardCharsets.UTF_8));
    }

    /** The path used by the concept-search and statistics exports. */
    @Test
    public void writesPlainRowsWithoutQuotingOrEscaping() throws Exception {

        File out = File.createTempFile("athena-csv-rows", ".csv");
        out.deleteOnExit();

        try (AthenaCSVWriter writer = new AthenaCSVWriter(out.getAbsolutePath(), '\t')) {
            writer.writeNext(new String[]{"a", "b"}, false);
            writer.writeAll(java.util.Arrays.asList(
                    new String[]{"1", null},
                    new String[]{"has\ttab", "x"}));
            writer.flush(true);
        }

        assertEquals("a\tb\n1\t\nhas\ttab\tx\n",
                new String(Files.readAllBytes(out.toPath()), StandardCharsets.UTF_8));
    }

    /**
     * A NUMERIC wider than a double must survive the export intact. The previous
     * implementation narrowed every decimal through {@code Number.doubleValue()} before
     * formatting, silently truncating anything past ~17 significant digits.
     */
    @Test
    public void wideNumericKeepsFullPrecision() throws Exception {

        BigDecimal wide = new BigDecimal("12345678901234567890.12345678901234567890");
        File out = File.createTempFile("athena-csv-precision", ".csv");
        out.deleteOnExit();

        try (AthenaCSVWriter writer = new AthenaCSVWriter(out.getAbsolutePath(), '\t')) {
            writer.writeAll(singleNumericRow(wide), false, false);
            writer.flush(true);
        }

        // Every significant digit survives — a double would have rendered
        // 12345678901234567000. The trailing zero is dropped by the same
        // minimumFractionDigits=0 rule that turns 1.50 into 1.5.
        assertEquals(wide.stripTrailingZeros().toPlainString() + "\n",
                new String(Files.readAllBytes(out.toPath()), StandardCharsets.UTF_8));
    }

    @Test
    public void writesUtf8() throws Exception {

        File out = File.createTempFile("athena-csv-utf8", ".csv");
        out.deleteOnExit();

        try (AthenaCSVWriter writer = new AthenaCSVWriter(out.getAbsolutePath(), '\t')) {
            writer.writeNext(new String[]{"Ünïcodé", "χ"});
            writer.flush(true);
        }

        assertEquals("Ünïcodé\tχ\n",
                new String(Files.readAllBytes(out.toPath()), StandardCharsets.UTF_8));
    }

    /** One NUMERIC column, one row. */
    private static ResultSet singleNumericRow(Object value) {

        final int[] row = {-1};
        InvocationHandler handler = (proxy, method, args) -> {
            switch (method.getName()) {
                case "getMetaData":
                    return singleColumnMetaData();
                case "next":
                    return ++row[0] < 1;
                case "isClosed":
                    return false;
                case "wasNull":
                    return false;
                case "close":
                    return null;
                case "getObject":
                    return value;
                default:
                    return defaultFor(method);
            }
        };
        return (ResultSet) Proxy.newProxyInstance(
                AthenaCsvWriterGoldenTest.class.getClassLoader(),
                new Class<?>[]{ResultSet.class}, handler);
    }

    private static ResultSetMetaData singleColumnMetaData() {

        InvocationHandler handler = (proxy, method, args) -> {
            switch (method.getName()) {
                case "getColumnCount":
                    return 1;
                case "getColumnType":
                    return Types.NUMERIC;
                case "getColumnName":
                case "getColumnLabel":
                    return "amount_value";
                default:
                    return defaultFor(method);
            }
        };
        return (ResultSetMetaData) Proxy.newProxyInstance(
                AthenaCsvWriterGoldenTest.class.getClassLoader(),
                new Class<?>[]{ResultSetMetaData.class}, handler);
    }

    // --- a ResultSet stub; implementing the interface by hand would be ~200 methods ------

    private static final boolean[] LAST_WAS_NULL = {false};

    private static ResultSet resultSet() {

        final int[] row = {-1};
        InvocationHandler handler = (proxy, method, args) -> {
            String name = method.getName();
            switch (name) {
                case "getMetaData":
                    return metaData();
                case "next":
                    return ++row[0] < ROWS.length;
                case "isClosed":
                    return false;
                case "wasNull":
                    return LAST_WAS_NULL[0];
                case "close":
                    return null;
                default:
                    break;
            }
            if (name.equals("getObject") || name.equals("getString")
                    || name.equals("getTimestamp") || name.equals("getDate")) {
                Object value = ROWS[row[0]][((Integer) args[0]) - 1];
                LAST_WAS_NULL[0] = value == null;
                if (value == null) {
                    return null;
                }
                return name.equals("getString") ? String.valueOf(value) : value;
            }
            return defaultFor(method);
        };
        return (ResultSet) Proxy.newProxyInstance(
                AthenaCsvWriterGoldenTest.class.getClassLoader(),
                new Class<?>[]{ResultSet.class}, handler);
    }

    private static ResultSetMetaData metaData() {

        InvocationHandler handler = (proxy, method, args) -> {
            switch (method.getName()) {
                case "getColumnCount":
                    return NAMES.length;
                case "getColumnType":
                    return TYPES[((Integer) args[0]) - 1];
                case "getColumnName":
                case "getColumnLabel":
                    return NAMES[((Integer) args[0]) - 1];
                default:
                    return defaultFor(method);
            }
        };
        return (ResultSetMetaData) Proxy.newProxyInstance(
                AthenaCsvWriterGoldenTest.class.getClassLoader(),
                new Class<?>[]{ResultSetMetaData.class}, handler);
    }

    private static Object defaultFor(Method method) {

        Class<?> type = method.getReturnType();
        if (type == boolean.class) {
            return false;
        }
        if (type == int.class) {
            return 0;
        }
        if (type == long.class) {
            return 0L;
        }
        return null;
    }
}
