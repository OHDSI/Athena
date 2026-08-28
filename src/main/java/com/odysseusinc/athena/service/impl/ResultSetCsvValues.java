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

import java.io.Closeable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;

/**
 * Streams a {@link ResultSet} row by row, converting each row to the values written into a
 * vocabulary CSV.
 * <p>
 * Replaces {@code AthenaResultSetHelperService}, which extended a class from the
 * hand-vendored {@code opencsv.jar}. That jar calls
 * {@code sun.misc.Cleaner sun.nio.ch.DirectBuffer.cleaner()}, removed in Java 9, so it
 * cannot run on a supported JDK at all.
 *
 * <h2>The output format is a contract</h2>
 * These files are consumed by OMOP tooling, so the formatting below reproduces the old
 * pipeline byte for byte. It was captured by running the previous implementation against
 * the vendored jar on JDK 8 and is locked by {@code AthenaCsvWriterGoldenTest}. In
 * particular:
 * <ul>
 *   <li>{@code DATE} → {@code yyyyMMdd}.</li>
 *   <li>{@code TIMESTAMP} → {@code yyyy-MM-dd HH:mm:ss.S}, with a trailing {@code .0}
 *       stripped, so whole seconds render as {@code 2026-07-30 01:02:03}.</li>
 *   <li>{@code TIMESTAMP WITH TIME ZONE} → the same pattern plus {@code  X}.</li>
 *   <li>{@code BIGINT} → {@code toString()}, preserving precision beyond a double.</li>
 *   <li>Numeric/decimal → {@link DecimalFormat} with up to 50 fraction digits, grouping
 *       off and a {@code .} separator. This drops trailing zeros, so {@code 1.50} is
 *       written as {@code 1.5} — deliberate, and different from what plain opencsv does.</li>
 *   <li>{@code null} → an empty field.</li>
 * </ul>
 */
class ResultSetCsvValues implements Closeable {

    /** Matches the old {@code DEFAULT_TIMESTAMP_FORMAT}. */
    private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss.S";
    /** Athena overrode the default {@code yyyy-MM-dd} with this. */
    private static final String DATE_FORMAT = "yyyyMMdd";

    private static final String TYPE_OBJECT = "object";
    private static final String TYPE_BOOLEAN = "boolean";
    private static final String TYPE_DOUBLE = "double";
    private static final String TYPE_LONG = "long";
    private static final String TYPE_INT = "int";
    private static final String TYPE_DATE = "date";
    private static final String TYPE_TIMESTAMP = "timestamp";
    private static final String TYPE_TIMESTAMPTZ = "timestamptz";
    private static final String TYPE_RAW = "raw";
    private static final String TYPE_CLOB = "clob";
    private static final String TYPE_BLOB = "blob";
    private static final String TYPE_STRING = "string";

    private static final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();

    private final ResultSet rs;
    private final int columnCount;
    private final String[] columnNames;
    private final String[] columnTypes;
    private final Object[] row;

    ResultSetCsvValues(ResultSet rs) throws SQLException {

        this.rs = rs;
        ResultSetMetaData metaData = rs.getMetaData();
        this.columnCount = metaData.getColumnCount();
        this.columnNames = new String[columnCount];
        this.columnTypes = new String[columnCount];
        this.row = new Object[columnCount];

        for (int i = 0; i < columnCount; i++) {
            columnNames[i] = metaData.getColumnName(i + 1);
            columnTypes[i] = typeOf(metaData.getColumnType(i + 1));
        }
    }

    /**
     * Column names exactly as the driver reports them.
     * <p>
     * Note they are <em>not</em> upper-cased. The old {@code AthenaCSVWriter.writeAll}
     * built an upper-cased {@code titles} array, but the vendored {@code CSVWriter}'s
     * {@code writeColumnNames()} ignored it and wrote the raw metadata names — so the
     * upper-casing never had any effect. Preserved as-is: changing it now would alter the
     * header of every generated vocabulary file.
     */
    String[] columnNames() {

        return columnNames;
    }

    /**
     * @return the next row's values, or {@code null} at the end of the result set
     */
    Object[] nextRow(boolean trim) throws SQLException {

        if (rs == null || rs.isClosed() || !rs.next()) {
            return null;
        }
        for (int i = 0; i < columnCount; i++) {
            row[i] = value(i, trim);
        }
        return row;
    }

    private Object value(int index, boolean trim) throws SQLException {

        int column = index + 1;
        Object raw;
        switch (columnTypes[index]) {
            case TYPE_TIMESTAMP:
            case TYPE_TIMESTAMPTZ:
                raw = rs.getTimestamp(column);
                break;
            case TYPE_RAW:
                raw = rs.getString(column);
                break;
            case TYPE_BLOB:
                Blob blob = rs.getBlob(column);
                if (blob == null) {
                    raw = null;
                } else {
                    raw = printHexBinary(blob.getBytes(1, (int) blob.length()));
                    blob.free();
                }
                break;
            case TYPE_CLOB:
                Clob clob = rs.getClob(column);
                if (clob == null) {
                    raw = null;
                } else {
                    raw = clob.getSubString(1, (int) clob.length());
                    clob.free();
                }
                break;
            default:
                raw = rs.getObject(column);
        }
        if (raw != null && rs.wasNull()) {
            raw = null;
        }
        return format(raw, index, trim);
    }

    private Object format(Object raw, int index, boolean trim) {

        if (raw == null) {
            return null;
        }
        String text;
        switch (columnTypes[index]) {
            case TYPE_BOOLEAN:
                return raw;
            case TYPE_INT:
                return ((Number) raw).intValue();
            case TYPE_LONG:
                // toString, not longValue: keeps precision a double would lose
                return raw.toString();
            case TYPE_DOUBLE:
                return formatNumber((Number) raw);
            case TYPE_DATE:
                text = new SimpleDateFormat(DATE_FORMAT).format((java.util.Date) raw);
                break;
            case TYPE_TIMESTAMP:
                text = new SimpleDateFormat(TIMESTAMP_FORMAT).format((Timestamp) raw);
                // whole seconds came out as ".0"; the old code trimmed that
                if (text.endsWith(".0")) {
                    text = text.substring(0, text.length() - 2);
                }
                break;
            case TYPE_TIMESTAMPTZ:
                text = new SimpleDateFormat(TIMESTAMP_FORMAT + " X").format((Timestamp) raw);
                break;
            case TYPE_OBJECT:
            default:
                text = String.valueOf(raw);
        }
        return trim ? text.trim() : text;
    }

    /**
     * Grouping off, '.' as the separator, up to 50 fraction digits — so trailing zeros are
     * dropped and nothing is rendered in scientific notation. A new formatter per call
     * because {@link DecimalFormat} is not thread safe.
     * <p>
     * {@link BigDecimal} and {@link BigInteger} are handed to {@code format(Object)},
     * which formats them exactly. The previous implementation called
     * {@code ((Number) value).doubleValue()} first, silently truncating any NUMERIC wider
     * than a double's ~17 significant digits — a real loss in a data export. Values that
     * fit in a double are unaffected, so ordinary columns keep their existing output.
     */
    private static String formatNumber(Number value) {

        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        DecimalFormat format = new DecimalFormat();
        format.setMaximumFractionDigits(50);
        format.setGroupingUsed(false);
        format.setDecimalFormatSymbols(symbols);

        if (value instanceof BigDecimal || value instanceof BigInteger) {
            return format.format(value);
        }
        return format.format(value.doubleValue());
    }

    private static String printHexBinary(byte[] bytes) {

        char[] hex = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int b = bytes[i] & 0xFF;
            hex[i * 2] = HEX_DIGITS[b >>> 4];
            hex[i * 2 + 1] = HEX_DIGITS[b & 0x0F];
        }
        return new String(hex);
    }

    private static String typeOf(int sqlType) {

        switch (sqlType) {
            case Types.JAVA_OBJECT:
                return TYPE_OBJECT;
            case Types.BOOLEAN:
                return TYPE_BOOLEAN;
            case Types.DECIMAL:
            case Types.DOUBLE:
            case Types.FLOAT:
            case Types.REAL:
            case Types.NUMERIC:
                return TYPE_DOUBLE;
            case Types.BIGINT:
                return TYPE_LONG;
            case Types.BIT:
            case Types.INTEGER:
            case Types.TINYINT:
            case Types.SMALLINT:
                return TYPE_INT;
            case Types.TIME:
            case Types.DATE:
                return TYPE_DATE;
            case Types.TIMESTAMP:
            case -100:
                return TYPE_TIMESTAMP;
            // 2014 is the standard JDBC code; -101/-102 are Oracle vendor codes that the
            // original mapping listed while omitting the standard one.
            case Types.TIMESTAMP_WITH_TIMEZONE:
            case -101:
            case -102:
                return TYPE_TIMESTAMPTZ;
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                return TYPE_RAW;
            case Types.CLOB:
            case Types.NCLOB:
                return TYPE_CLOB;
            case Types.BLOB:
                return TYPE_BLOB;
            default:
                return TYPE_STRING;
        }
    }

    @Override
    public void close() {

        try {
            if (rs != null && !rs.isClosed()) {
                rs.close();
            }
        } catch (SQLException ignored) {
            // closing a result set that is already gone is not actionable
        }
    }
}
