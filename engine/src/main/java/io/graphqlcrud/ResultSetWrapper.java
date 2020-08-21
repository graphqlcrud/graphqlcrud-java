/*
 * Copyright 2012-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.graphqlcrud;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLType;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ResultSetWrapper implements ResultSet {
    private ResultSet delegate;
    private boolean ignoreNext;
    private ArrayList<String> colNames = new ArrayList<>();
    private Map<String, Object> previousRow = new HashMap<>();

    public ResultSetWrapper(ResultSet rs) throws SQLException {
        this.delegate = rs;
        ResultSetMetaData metadata = this.getMetaData();
        for (int i = 0; i < metadata.getColumnCount(); i++) {
            this.colNames.add(metadata.getColumnName(i+1));
        }
    }

    @Override
    public boolean absolute(int arg0) throws SQLException {
        return this.delegate.absolute(arg0);
    }

    @Override
    public void afterLast() throws SQLException {
        this.delegate.afterLast();
    }

    @Override
    public void beforeFirst() throws SQLException {
        this.delegate.beforeFirst();
    }

    @Override
    public void cancelRowUpdates() throws SQLException {
        this.delegate.cancelRowUpdates();
    }

    @Override
    public void clearWarnings() throws SQLException {
        this.delegate.clearWarnings();
    }

    @Override
    public void close() throws SQLException {
        this.delegate.close();
    }

    @Override
    public void deleteRow() throws SQLException {
        this.delegate.deleteRow();
    }

    @Override
    public int findColumn(String arg0) throws SQLException {
        return this.delegate.findColumn(arg0);
    }

    @Override
    public boolean first() throws SQLException {
        return this.delegate.first();
    }

    @Override
    public Array getArray(int arg0) throws SQLException {
        return this.delegate.getArray(arg0);
    }

    @Override
    public Array getArray(String arg0) throws SQLException {
        return this.delegate.getArray(arg0);
    }

    @Override
    public InputStream getAsciiStream(int arg0) throws SQLException {
        return this.delegate.getAsciiStream(arg0);
    }

    @Override
    public InputStream getAsciiStream(String arg0) throws SQLException {
        return this.delegate.getAsciiStream(arg0);
    }

    @Override
    public BigDecimal getBigDecimal(int arg0, int arg1) throws SQLException {
        return this.delegate.getBigDecimal(arg0, arg1);
    }

    @Override
    public BigDecimal getBigDecimal(int arg0) throws SQLException {
        return this.delegate.getBigDecimal(arg0);
    }

    @Override
    public BigDecimal getBigDecimal(String arg0, int arg1) throws SQLException {
        return this.delegate.getBigDecimal(arg0, arg1);
    }

    @Override
    public BigDecimal getBigDecimal(String arg0) throws SQLException {
        return this.delegate.getBigDecimal(arg0);
    }

    @Override
    public InputStream getBinaryStream(int arg0) throws SQLException {
        return this.delegate.getBinaryStream(arg0);
    }

    @Override
    public InputStream getBinaryStream(String arg0) throws SQLException {
        return this.delegate.getBinaryStream(arg0);
    }

    @Override
    public Blob getBlob(int arg0) throws SQLException {
        return this.delegate.getBlob(arg0);
    }

    @Override
    public Blob getBlob(String arg0) throws SQLException {
        return this.delegate.getBlob(arg0);
    }

    @Override
    public boolean getBoolean(int arg0) throws SQLException {
        return this.delegate.getBoolean(arg0);
    }

    @Override
    public boolean getBoolean(String arg0) throws SQLException {
        return this.delegate.getBoolean(arg0);
    }

    @Override
    public byte getByte(int arg0) throws SQLException {
        return this.delegate.getByte(arg0);
    }

    @Override
    public byte getByte(String arg0) throws SQLException {
        return this.delegate.getByte(arg0);
    }

    @Override
    public byte[] getBytes(int arg0) throws SQLException {
        return this.delegate.getBytes(arg0);
    }

    @Override
    public byte[] getBytes(String arg0) throws SQLException {
        return this.delegate.getBytes(arg0);
    }

    @Override
    public Reader getCharacterStream(int arg0) throws SQLException {
        return this.delegate.getCharacterStream(arg0);
    }

    @Override
    public Reader getCharacterStream(String arg0) throws SQLException {
        return this.delegate.getCharacterStream(arg0);
    }

    @Override
    public Clob getClob(int arg0) throws SQLException {
        return this.delegate.getClob(arg0);
    }

    @Override
    public Clob getClob(String arg0) throws SQLException {
        return this.delegate.getClob(arg0);
    }

    @Override
    public int getConcurrency() throws SQLException {
        return this.delegate.getConcurrency();
    }

    @Override
    public String getCursorName() throws SQLException {
        return this.delegate.getCursorName();
    }

    @Override
    public Date getDate(int arg0, Calendar arg1) throws SQLException {
        return this.delegate.getDate(arg0, arg1);
    }

    @Override
    public Date getDate(int arg0) throws SQLException {
        return this.delegate.getDate(arg0);
    }

    @Override
    public Date getDate(String arg0, Calendar arg1) throws SQLException {
        return this.delegate.getDate(arg0, arg1);
    }

    @Override
    public Date getDate(String arg0) throws SQLException {
        return this.delegate.getDate(arg0);
    }

    @Override
    public double getDouble(int arg0) throws SQLException {
        return this.delegate.getDouble(arg0);
    }

    @Override
    public double getDouble(String arg0) throws SQLException {
        return this.delegate.getDouble(arg0);
    }

    @Override
    public int getFetchDirection() throws SQLException {
        return this.delegate.getFetchDirection();
    }

    @Override
    public int getFetchSize() throws SQLException {
        return this.delegate.getFetchSize();
    }

    @Override
    public float getFloat(int arg0) throws SQLException {
        return this.delegate.getFloat(arg0);
    }

    @Override
    public float getFloat(String arg0) throws SQLException {
        return this.delegate.getFloat(arg0);
    }

    @Override
    public int getHoldability() throws SQLException {
        return this.delegate.getHoldability();
    }

    @Override
    public int getInt(int arg0) throws SQLException {
        return this.delegate.getInt(arg0);
    }

    @Override
    public int getInt(String arg0) throws SQLException {
        return this.delegate.getInt(arg0);
    }

    @Override
    public long getLong(int arg0) throws SQLException {
        return this.delegate.getLong(arg0);
    }

    @Override
    public long getLong(String arg0) throws SQLException {
        return this.delegate.getLong(arg0);
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return this.delegate.getMetaData();
    }

    @Override
    public Reader getNCharacterStream(int arg0) throws SQLException {
        return this.delegate.getNCharacterStream(arg0);
    }

    @Override
    public Reader getNCharacterStream(String arg0) throws SQLException {
        return this.delegate.getNCharacterStream(arg0);
    }

    @Override
    public NClob getNClob(int arg0) throws SQLException {
        return this.delegate.getNClob(arg0);
    }

    @Override
    public NClob getNClob(String arg0) throws SQLException {
        return this.delegate.getNClob(arg0);
    }

    @Override
    public String getNString(int arg0) throws SQLException {
        return this.delegate.getNString(arg0);
    }

    @Override
    public String getNString(String arg0) throws SQLException {
        return this.delegate.getNString(arg0);
    }

    @Override
    public <T> T getObject(int arg0, Class<T> arg1) throws SQLException {
        return this.delegate.getObject(arg0, arg1);
    }

    @Override
    public Object getObject(int arg0, Map<String, Class<?>> arg1) throws SQLException {
        return this.delegate.getObject(arg0, arg1);
    }

    @Override
    public Object getObject(int arg0) throws SQLException {
        return this.delegate.getObject(arg0);
    }

    @Override
    public <T> T getObject(String arg0, Class<T> arg1) throws SQLException {
        return this.delegate.getObject(arg0, arg1);
    }

    @Override
    public Object getObject(String arg0, Map<String, Class<?>> arg1) throws SQLException {
        return this.delegate.getObject(arg0, arg1);
    }

    @Override
    public Object getObject(String arg0) throws SQLException {
        if (this.ignoreNext || isClosed()) {
            return convertIfDate(this.previousRow.get(arg0));
        }
        return convertIfDate(this.delegate.getObject(arg0));
    }

    private Object convertIfDate(Object obj) {
        if (obj instanceof Timestamp) {
            return OffsetDateTime.ofInstant(Instant.ofEpochMilli(((Timestamp)obj).getTime()), ZoneId.of("UTC"));
        } else if (obj instanceof Time) {
            return OffsetTime.ofInstant(Instant.ofEpochMilli(((Time)obj).getTime()), ZoneId.of("UTC"));
        } else if (obj instanceof Date) {
            return OffsetDateTime.ofInstant(Instant.ofEpochMilli(((Date)obj).getTime()), ZoneId.of("UTC"));
        }
        return obj;
    }

    @Override
    public Ref getRef(int arg0) throws SQLException {
        return this.delegate.getRef(arg0);
    }

    @Override
    public Ref getRef(String arg0) throws SQLException {
        return this.delegate.getRef(arg0);
    }

    @Override
    public int getRow() throws SQLException {
        return this.delegate.getRow();
    }

    @Override
    public RowId getRowId(int arg0) throws SQLException {
        return this.delegate.getRowId(arg0);
    }

    @Override
    public RowId getRowId(String arg0) throws SQLException {
        return this.delegate.getRowId(arg0);
    }

    @Override
    public SQLXML getSQLXML(int arg0) throws SQLException {
        return this.delegate.getSQLXML(arg0);
    }

    @Override
    public SQLXML getSQLXML(String arg0) throws SQLException {
        return this.delegate.getSQLXML(arg0);
    }

    @Override
    public short getShort(int arg0) throws SQLException {
        return this.delegate.getShort(arg0);
    }

    @Override
    public short getShort(String arg0) throws SQLException {
        return this.delegate.getShort(arg0);
    }

    @Override
    public Statement getStatement() throws SQLException {
        return this.delegate.getStatement();
    }

    @Override
    public String getString(int arg0) throws SQLException {
        return this.delegate.getString(arg0);
    }

    @Override
    public String getString(String arg0) throws SQLException {
        return this.delegate.getString(arg0);
    }

    @Override
    public Time getTime(int arg0, Calendar arg1) throws SQLException {
        return this.delegate.getTime(arg0, arg1);
    }

    @Override
    public Time getTime(int arg0) throws SQLException {
        return this.delegate.getTime(arg0);
    }

    @Override
    public Time getTime(String arg0, Calendar arg1) throws SQLException {
        return this.delegate.getTime(arg0, arg1);
    }

    @Override
    public Time getTime(String arg0) throws SQLException {
        return this.delegate.getTime(arg0);
    }

    @Override
    public Timestamp getTimestamp(int arg0, Calendar arg1) throws SQLException {
        return this.delegate.getTimestamp(arg0, arg1);
    }

    @Override
    public Timestamp getTimestamp(int arg0) throws SQLException {
        return this.delegate.getTimestamp(arg0);
    }

    @Override
    public Timestamp getTimestamp(String arg0, Calendar arg1) throws SQLException {
        return this.delegate.getTimestamp(arg0, arg1);
    }

    @Override
    public Timestamp getTimestamp(String arg0) throws SQLException {
        return this.delegate.getTimestamp(arg0);
    }

    @Override
    public int getType() throws SQLException {
        return this.delegate.getType();
    }

    @Override
    public URL getURL(int arg0) throws SQLException {
        return this.delegate.getURL(arg0);
    }

    @Override
    public URL getURL(String arg0) throws SQLException {
        return this.delegate.getURL(arg0);
    }

    @Override
    public InputStream getUnicodeStream(int arg0) throws SQLException {
        return this.delegate.getUnicodeStream(arg0);
    }

    @Override
    public InputStream getUnicodeStream(String arg0) throws SQLException {
        return this.delegate.getUnicodeStream(arg0);
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return this.delegate.getWarnings();
    }

    @Override
    public void insertRow() throws SQLException {
        this.delegate.insertRow();
    }

    @Override
    public boolean isAfterLast() throws SQLException {
        return this.delegate.isAfterLast();
    }

    @Override
    public boolean isBeforeFirst() throws SQLException {
        return this.delegate.isBeforeFirst();
    }

    @Override
    public boolean isClosed() throws SQLException {
        return this.delegate.isClosed();
    }

    @Override
    public boolean isFirst() throws SQLException {
        return this.delegate.isFirst();
    }

    @Override
    public boolean isLast() throws SQLException {
        return this.delegate.isLast();
    }

    @Override
    public boolean isWrapperFor(Class<?> arg0) throws SQLException {
        return this.delegate.isWrapperFor(arg0);
    }

    @Override
    public boolean last() throws SQLException {
        return this.delegate.last();
    }

    @Override
    public void moveToCurrentRow() throws SQLException {
        this.delegate.moveToCurrentRow();
    }

    @Override
    public void moveToInsertRow() throws SQLException {
        this.delegate.moveToInsertRow();
    }

    @Override
    public boolean next() throws SQLException {
        if (this.ignoreNext) {
            this.ignoreNext = false;
            return true;
        }
        if (!isBeforeFirst() && !isAfterLast()) {
            for (String col:this.colNames) {
                this.previousRow.put(col, getObject(col));
            }
        }
        return this.delegate.next();
    }

    @Override
    public boolean previous() throws SQLException {
        return this.delegate.previous();
    }

    @Override
    public void refreshRow() throws SQLException {
        this.delegate.refreshRow();
    }

    @Override
    public boolean relative(int arg0) throws SQLException {
        return this.delegate.relative(arg0);
    }

    @Override
    public boolean rowDeleted() throws SQLException {
        return this.delegate.rowDeleted();
    }

    @Override
    public boolean rowInserted() throws SQLException {
        return this.delegate.rowInserted();
    }

    @Override
    public boolean rowUpdated() throws SQLException {
        return this.delegate.rowUpdated();
    }

    @Override
    public void setFetchDirection(int arg0) throws SQLException {
        this.delegate.setFetchDirection(arg0);
    }

    @Override
    public void setFetchSize(int arg0) throws SQLException {
        this.delegate.setFetchSize(arg0);
    }

    @Override
    public <T> T unwrap(Class<T> arg0) throws SQLException {
        return this.delegate.unwrap(arg0);
    }

    @Override
    public void updateArray(int arg0, Array arg1) throws SQLException {
        this.delegate.updateArray(arg0, arg1);
    }

    @Override
    public void updateArray(String arg0, Array arg1) throws SQLException {
        this.delegate.updateArray(arg0, arg1);
    }

    @Override
    public void updateAsciiStream(int arg0, InputStream arg1, int arg2) throws SQLException {
        this.delegate.updateAsciiStream(arg0, arg1, arg2);
    }

    @Override
    public void updateAsciiStream(int arg0, InputStream arg1, long arg2) throws SQLException {
        this.delegate.updateAsciiStream(arg0, arg1, arg2);
    }

    @Override
    public void updateAsciiStream(int arg0, InputStream arg1) throws SQLException {
        this.delegate.updateAsciiStream(arg0, arg1);
    }

    @Override
    public void updateAsciiStream(String arg0, InputStream arg1, int arg2) throws SQLException {
        this.delegate.updateAsciiStream(arg0, arg1, arg2);
    }

    @Override
    public void updateAsciiStream(String arg0, InputStream arg1, long arg2) throws SQLException {
        this.delegate.updateAsciiStream(arg0, arg1, arg2);
    }

    @Override
    public void updateAsciiStream(String arg0, InputStream arg1) throws SQLException {
        this.delegate.updateAsciiStream(arg0, arg1);
    }

    @Override
    public void updateBigDecimal(int arg0, BigDecimal arg1) throws SQLException {
        this.delegate.updateBigDecimal(arg0, arg1);
    }

    @Override
    public void updateBigDecimal(String arg0, BigDecimal arg1) throws SQLException {
        this.delegate.updateBigDecimal(arg0, arg1);
    }

    @Override
    public void updateBinaryStream(int arg0, InputStream arg1, int arg2) throws SQLException {
        this.delegate.updateBinaryStream(arg0, arg1, arg2);
    }

    @Override
    public void updateBinaryStream(int arg0, InputStream arg1, long arg2) throws SQLException {
        this.delegate.updateBinaryStream(arg0, arg1, arg2);
    }

    @Override
    public void updateBinaryStream(int arg0, InputStream arg1) throws SQLException {
        this.delegate.updateBinaryStream(arg0, arg1);
    }

    @Override
    public void updateBinaryStream(String arg0, InputStream arg1, int arg2) throws SQLException {
        this.delegate.updateBinaryStream(arg0, arg1, arg2);
    }

    @Override
    public void updateBinaryStream(String arg0, InputStream arg1, long arg2) throws SQLException {
        this.delegate.updateBinaryStream(arg0, arg1, arg2);
    }

    @Override
    public void updateBinaryStream(String arg0, InputStream arg1) throws SQLException {
        this.delegate.updateBinaryStream(arg0, arg1);
    }

    @Override
    public void updateBlob(int arg0, Blob arg1) throws SQLException {
        this.delegate.updateBlob(arg0, arg1);
    }

    @Override
    public void updateBlob(int arg0, InputStream arg1, long arg2) throws SQLException {
        this.delegate.updateBlob(arg0, arg1, arg2);
    }

    @Override
    public void updateBlob(int arg0, InputStream arg1) throws SQLException {
        this.delegate.updateBlob(arg0, arg1);
    }

    @Override
    public void updateBlob(String arg0, Blob arg1) throws SQLException {
        this.delegate.updateBlob(arg0, arg1);
    }

    @Override
    public void updateBlob(String arg0, InputStream arg1, long arg2) throws SQLException {
        this.delegate.updateBlob(arg0, arg1, arg2);
    }

    @Override
    public void updateBlob(String arg0, InputStream arg1) throws SQLException {
        this.delegate.updateBlob(arg0, arg1);
    }

    @Override
    public void updateBoolean(int arg0, boolean arg1) throws SQLException {
        this.delegate.updateBoolean(arg0, arg1);
    }

    @Override
    public void updateBoolean(String arg0, boolean arg1) throws SQLException {
        this.delegate.updateBoolean(arg0, arg1);
    }

    @Override
    public void updateByte(int arg0, byte arg1) throws SQLException {
        this.delegate.updateByte(arg0, arg1);
    }

    @Override
    public void updateByte(String arg0, byte arg1) throws SQLException {
        this.delegate.updateByte(arg0, arg1);
    }

    @Override
    public void updateBytes(int arg0, byte[] arg1) throws SQLException {
        this.delegate.updateBytes(arg0, arg1);
    }

    @Override
    public void updateBytes(String arg0, byte[] arg1) throws SQLException {
        this.delegate.updateBytes(arg0, arg1);
    }

    @Override
    public void updateCharacterStream(int arg0, Reader arg1, int arg2) throws SQLException {
        this.delegate.updateCharacterStream(arg0, arg1, arg2);
    }

    @Override
    public void updateCharacterStream(int arg0, Reader arg1, long arg2) throws SQLException {
        this.delegate.updateCharacterStream(arg0, arg1, arg2);
    }

    @Override
    public void updateCharacterStream(int arg0, Reader arg1) throws SQLException {
        this.delegate.updateCharacterStream(arg0, arg1);
    }

    @Override
    public void updateCharacterStream(String arg0, Reader arg1, int arg2) throws SQLException {
        this.delegate.updateCharacterStream(arg0, arg1, arg2);
    }

    @Override
    public void updateCharacterStream(String arg0, Reader arg1, long arg2) throws SQLException {
        this.delegate.updateCharacterStream(arg0, arg1, arg2);
    }

    @Override
    public void updateCharacterStream(String arg0, Reader arg1) throws SQLException {
        this.delegate.updateCharacterStream(arg0, arg1);
    }

    @Override
    public void updateClob(int arg0, Clob arg1) throws SQLException {
        this.delegate.updateClob(arg0, arg1);
    }

    @Override
    public void updateClob(int arg0, Reader arg1, long arg2) throws SQLException {
        this.delegate.updateClob(arg0, arg1, arg2);
    }

    @Override
    public void updateClob(int arg0, Reader arg1) throws SQLException {
        this.delegate.updateClob(arg0, arg1);
    }

    @Override
    public void updateClob(String arg0, Clob arg1) throws SQLException {
        this.delegate.updateClob(arg0, arg1);
    }

    @Override
    public void updateClob(String arg0, Reader arg1, long arg2) throws SQLException {
        this.delegate.updateClob(arg0, arg1, arg2);
    }

    @Override
    public void updateClob(String arg0, Reader arg1) throws SQLException {
        this.delegate.updateClob(arg0, arg1);
    }

    @Override
    public void updateDate(int arg0, Date arg1) throws SQLException {
        this.delegate.updateDate(arg0, arg1);
    }

    @Override
    public void updateDate(String arg0, Date arg1) throws SQLException {
        this.delegate.updateDate(arg0, arg1);
    }

    @Override
    public void updateDouble(int arg0, double arg1) throws SQLException {
        this.delegate.updateDouble(arg0, arg1);
    }

    @Override
    public void updateDouble(String arg0, double arg1) throws SQLException {
        this.delegate.updateDouble(arg0, arg1);
    }

    @Override
    public void updateFloat(int arg0, float arg1) throws SQLException {
        this.delegate.updateFloat(arg0, arg1);
    }

    @Override
    public void updateFloat(String arg0, float arg1) throws SQLException {
        this.delegate.updateFloat(arg0, arg1);
    }

    @Override
    public void updateInt(int arg0, int arg1) throws SQLException {
        this.delegate.updateInt(arg0, arg1);
    }

    @Override
    public void updateInt(String arg0, int arg1) throws SQLException {
        this.delegate.updateInt(arg0, arg1);
    }

    @Override
    public void updateLong(int arg0, long arg1) throws SQLException {
        this.delegate.updateLong(arg0, arg1);
    }

    @Override
    public void updateLong(String arg0, long arg1) throws SQLException {
        this.delegate.updateLong(arg0, arg1);
    }

    @Override
    public void updateNCharacterStream(int arg0, Reader arg1, long arg2) throws SQLException {
        this.delegate.updateNCharacterStream(arg0, arg1, arg2);
    }

    @Override
    public void updateNCharacterStream(int arg0, Reader arg1) throws SQLException {
        this.delegate.updateNCharacterStream(arg0, arg1);
    }

    @Override
    public void updateNCharacterStream(String arg0, Reader arg1, long arg2) throws SQLException {
        this.delegate.updateNCharacterStream(arg0, arg1, arg2);
    }

    @Override
    public void updateNCharacterStream(String arg0, Reader arg1) throws SQLException {
        this.delegate.updateNCharacterStream(arg0, arg1);
    }

    @Override
    public void updateNClob(int arg0, NClob arg1) throws SQLException {
        this.delegate.updateNClob(arg0, arg1);
    }

    @Override
    public void updateNClob(int arg0, Reader arg1, long arg2) throws SQLException {
        this.delegate.updateNClob(arg0, arg1, arg2);
    }

    @Override
    public void updateNClob(int arg0, Reader arg1) throws SQLException {
        this.delegate.updateNClob(arg0, arg1);
    }

    @Override
    public void updateNClob(String arg0, NClob arg1) throws SQLException {
        this.delegate.updateNClob(arg0, arg1);
    }

    @Override
    public void updateNClob(String arg0, Reader arg1, long arg2) throws SQLException {
        this.delegate.updateNClob(arg0, arg1, arg2);
    }

    @Override
    public void updateNClob(String arg0, Reader arg1) throws SQLException {
        this.delegate.updateNClob(arg0, arg1);
    }

    @Override
    public void updateNString(int arg0, String arg1) throws SQLException {
        this.delegate.updateNString(arg0, arg1);
    }

    @Override
    public void updateNString(String arg0, String arg1) throws SQLException {
        this.delegate.updateNString(arg0, arg1);
    }

    @Override
    public void updateNull(int arg0) throws SQLException {
        this.delegate.updateNull(arg0);
    }

    @Override
    public void updateNull(String arg0) throws SQLException {
        this.delegate.updateNull(arg0);
    }

    @Override
    public void updateObject(int arg0, Object arg1, int arg2) throws SQLException {
        this.delegate.updateObject(arg0, arg1, arg2);
    }

    @Override
    public void updateObject(int columnIndex, Object x, SQLType targetSqlType, int scaleOrLength) throws SQLException {
        this.delegate.updateObject(columnIndex, x, targetSqlType, scaleOrLength);
    }

    @Override
    public void updateObject(int columnIndex, Object x, SQLType targetSqlType) throws SQLException {
        this.delegate.updateObject(columnIndex, x, targetSqlType);
    }

    @Override
    public void updateObject(int arg0, Object arg1) throws SQLException {
        this.delegate.updateObject(arg0, arg1);
    }

    @Override
    public void updateObject(String arg0, Object arg1, int arg2) throws SQLException {
        this.delegate.updateObject(arg0, arg1, arg2);
    }

    @Override
    public void updateObject(String columnLabel, Object x, SQLType targetSqlType, int scaleOrLength)
            throws SQLException {
        this.delegate.updateObject(columnLabel, x, targetSqlType, scaleOrLength);
    }

    @Override
    public void updateObject(String columnLabel, Object x, SQLType targetSqlType) throws SQLException {
        this.delegate.updateObject(columnLabel, x, targetSqlType);
    }

    @Override
    public void updateObject(String arg0, Object arg1) throws SQLException {
        this.delegate.updateObject(arg0, arg1);
    }

    @Override
    public void updateRef(int arg0, Ref arg1) throws SQLException {
        this.delegate.updateRef(arg0, arg1);
    }

    @Override
    public void updateRef(String arg0, Ref arg1) throws SQLException {
        this.delegate.updateRef(arg0, arg1);
    }

    @Override
    public void updateRow() throws SQLException {
        this.delegate.updateRow();
    }

    @Override
    public void updateRowId(int arg0, RowId arg1) throws SQLException {
        this.delegate.updateRowId(arg0, arg1);
    }

    @Override
    public void updateRowId(String arg0, RowId arg1) throws SQLException {
        this.delegate.updateRowId(arg0, arg1);
    }

    @Override
    public void updateSQLXML(int arg0, SQLXML arg1) throws SQLException {
        this.delegate.updateSQLXML(arg0, arg1);
    }

    @Override
    public void updateSQLXML(String arg0, SQLXML arg1) throws SQLException {
        this.delegate.updateSQLXML(arg0, arg1);
    }

    @Override
    public void updateShort(int arg0, short arg1) throws SQLException {
        this.delegate.updateShort(arg0, arg1);
    }

    @Override
    public void updateShort(String arg0, short arg1) throws SQLException {
        this.delegate.updateShort(arg0, arg1);
    }

    @Override
    public void updateString(int arg0, String arg1) throws SQLException {
        this.delegate.updateString(arg0, arg1);
    }

    @Override
    public void updateString(String arg0, String arg1) throws SQLException {
        this.delegate.updateString(arg0, arg1);
    }

    @Override
    public void updateTime(int arg0, Time arg1) throws SQLException {
        this.delegate.updateTime(arg0, arg1);
    }

    @Override
    public void updateTime(String arg0, Time arg1) throws SQLException {
        this.delegate.updateTime(arg0, arg1);
    }

    @Override
    public void updateTimestamp(int arg0, Timestamp arg1) throws SQLException {
        this.delegate.updateTimestamp(arg0, arg1);
    }

    @Override
    public void updateTimestamp(String arg0, Timestamp arg1) throws SQLException {
        this.delegate.updateTimestamp(arg0, arg1);
    }

    @Override
    public boolean wasNull() throws SQLException {
        return this.delegate.wasNull();
    }

    public void setIgnoreNext(boolean ignoreNext) {
        this.ignoreNext = ignoreNext;
    }

}
