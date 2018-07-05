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

import com.lmax.disruptor.dsl.Disruptor;
import com.opencsv.ResultSetHelperService;

import javax.xml.bind.DatatypeConverter;
import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class AthenaResultSetHelperService extends ResultSetHelperService implements Closeable {

    private ResultSet rs;
    private Method xmlStr;

    class Row {
        public Object[] value;
        public boolean seq;

        public void set(Object[] value, boolean seq) {
            this.value = value;
            this.seq = seq;
        }
    }

    Disruptor<Row> disruptor;

    /**
     * Default Constructor.
     */
    public AthenaResultSetHelperService(ResultSet res, int fetchSize) throws SQLException {
        super(res, fetchSize);

        long sec = System.nanoTime();
        rs = res;
        rs.setFetchSize(fetchSize);
        RESULT_FETCH_SIZE = fetchSize;
        try {
            rs.setFetchDirection(ResultSet.FETCH_FORWARD);
        } catch (Exception e) {
        }
        ResultSetMetaData metadata = rs.getMetaData();
        columnCount = metadata.getColumnCount();
        columnNames = new String[columnCount];
        columnTypes = new String[columnCount];
        columnClassName = new String[columnCount];
        columnTypesI = new int[columnCount];
        for (int i = 0; i < metadata.getColumnCount(); i++) {
            int type = metadata.getColumnType(i + 1);
            String value;
            switch (type) {
                case Types.JAVA_OBJECT:
                    value = "object";
                    break;
                case Types.BOOLEAN:
                    value = "boolean";
                    break;
                case Types.DECIMAL:
                case Types.DOUBLE:
                case Types.FLOAT:
                case Types.REAL:
                case Types.NUMERIC:
                    value = "double";
                    break;
                case Types.BIGINT:
                    value = "long";
                    break;
                case Types.BIT:
                case Types.INTEGER:
                case Types.TINYINT:
                case Types.SMALLINT:
                    value = "int";
                    break;
                case Types.TIME:
                    value = "date";
                    break;
                case Types.DATE:
                    value = "date";
                    break;
                case Types.TIMESTAMP:
                case -100:
                    value = "timestamp";
                    break;
                case -101:
                case -102:
                    //case Types.TIME_WITH_TIMEZONE:
                    //case Types.TIMESTAMP_WITH_TIMEZONE:
                    value = "timestamptz";
                    break;
                case Types.BINARY:
                case Types.VARBINARY:
                case Types.LONGVARBINARY:
                    value = "raw";
                    break;
                case Types.CLOB:
                case Types.NCLOB:
                    value = "clob";
                    break;
                case Types.BLOB:
                    value = "blob";
                    break;
                default:
                    value = "string";
            }
            columnTypesI[i] = type;
            columnTypes[i] = value.intern();
            columnNames[i] = metadata.getColumnName(i + 1).intern();
        }
        cost += System.nanoTime() - sec;
    }

    public AthenaResultSetHelperService(ResultSet res) throws SQLException {
        this(res, RESULT_FETCH_SIZE);
    }

    /**
     * Get all the column values from the result set.
     *
     * @param trim             - values should have white spaces trimmed.
     * @param dateFormatString - format String for dates.
     * @param timeFormatString - format String for timestamps.
     * @return - String array containing all the column values.
     * @throws SQLException - thrown by the result set.
     * @throws IOException  - thrown by the result set.
     */
    public Object[] getColumnValues(boolean trim, String dateFormatString, String timeFormatString) throws SQLException, IOException {
        long sec = System.nanoTime();
        if (rs == null || rs.isClosed() || !rs.next()) {
            if (rs != null) rs.close();
            cost += System.nanoTime() - sec;
            return null;
        }
        if (rowObject == null) rowObject = new Object[columnCount];
        Object o;
        for (int i = 0; i < columnCount; i++) {
            if (columnClassName[i] == null) {
                o = rs.getObject(i + 1);
                if (o != null) columnClassName[i] = o.getClass().getName();
            }
            switch (columnTypes[i]) {
                case "timestamptz":
                case "timestamp":
                    o = rs.getTimestamp(i + 1);
                    break;
                case "raw":
                    o = rs.getString(i + 1);
                    break;
                case "blob":
                    Blob bl = rs.getBlob(i + 1);
                    if (bl != null) {
                        o = DatatypeConverter.printHexBinary(bl.getBytes(1, (int) bl.length()));
                        bl.free();
                    } else o = null;
                    break;
                case "clob":
                    Clob c = rs.getClob(i + 1);
                    if (c != null) {
                        o = c.getSubString(1, (int) c.length());
                        c.free();
                    } else o = null;
                    break;
                default:
                    o = rs.getObject(i + 1);
                    if (!rs.wasNull() && columnTypesI[i] == 2009) {//Oracle XMLType
                        try {
                            if (xmlStr == null) xmlStr = o.getClass().getDeclaredMethod("getStringVal");
                            o = xmlStr.invoke(o);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
            }
            if (o != null && rs.wasNull()) o = null;
            if (disruptor == null) rowObject[i] = getColumnValue(o, i, trim, dateFormatString, timeFormatString);
            else rowObject[i] = o;
        }
        cost += System.nanoTime() - sec;
        return rowObject;
    }

    @Override
    public void close() {
        try {
            if (rs != null && !rs.isClosed()) rs.close();
            if (disruptor != null) disruptor.shutdown();
        } catch (Exception e) {
        }
        disruptor = null;
    }

    private Object getColumnValue(Object o, int colIndex, boolean trim, String dateFormatString, String timestampFormatString) throws SQLException, IOException {
        if (o == null) return null;
        String str;
        switch (columnTypes[colIndex]) {
            case "object":
                str = handleObject(o);
                break;
            case "boolean":
                return o;
            case "int":
                return ((Number) o).intValue();
            case "long":
                return o.toString();
            case "double":
                Double d = ((Number) o).doubleValue();
                switch (o.getClass().getSimpleName()) {
                    case "Double":
                    case "Float":
                        return formatDouble(d);
                    case "BigInteger":
                        Integer i = ((BigInteger) o).intValue();
                        if (o.toString().equals(new BigInteger(d.toString()))) return i;
                        if (o.toString().equals(new BigInteger(d.toString()))) return d;
                        return o;
                    case "BigDecimal":
                        return formatDouble(d);
                    default:
                        return d;
                }
            case "date":
            case "time":
                str = handleDate((Date) o, dateFormatString);
                break;
            case "timestamp":
                str = handleTimestamp((Timestamp) o, timestampFormatString);
                if (columnClassName[colIndex].startsWith("oracle.sql.DATE")) {
                    int pos = str.lastIndexOf('.');
                    if (pos > 0) str = str.substring(0, pos - 1);
                }
                break;
            case "timestamptz":
                str = handleTimestampTZ((Timestamp) o, timestampFormatString);
                break;
            case "longraw":
                str = DatatypeConverter.printHexBinary((byte[]) o);
                break;
            default:
                str = o.toString();
        }
        return trim ? str.trim() : str;
    }

    public Object formatDouble(Double value) {

        DecimalFormatSymbols unusualSymbols = new DecimalFormatSymbols();
        unusualSymbols.setDecimalSeparator('.');
        DecimalFormat format = new DecimalFormat();
        format.setMaximumFractionDigits(50);
        format.setGroupingUsed(false);
        format.setDecimalFormatSymbols(unusualSymbols);
        return format.format(value);
    }
}