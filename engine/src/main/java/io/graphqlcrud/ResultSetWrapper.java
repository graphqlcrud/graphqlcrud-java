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
import java.util.Calendar;
import java.util.Map;

public class ResultSetWrapper implements ResultSet {
    private ResultSet delegate;
    private boolean ignoreNext;
    
    public ResultSetWrapper(ResultSet rs) {
        this.delegate = rs;
    }

    public boolean absolute(int arg0) throws SQLException {
        return delegate.absolute(arg0);
    }

    public void afterLast() throws SQLException {
        delegate.afterLast();
    }

    public void beforeFirst() throws SQLException {
        delegate.beforeFirst();
    }

    public void cancelRowUpdates() throws SQLException {
        delegate.cancelRowUpdates();
    }

    public void clearWarnings() throws SQLException {
        delegate.clearWarnings();
    }

    public void close() throws SQLException {
        delegate.close();
    }

    public void deleteRow() throws SQLException {
        delegate.deleteRow();
    }

    public int findColumn(String arg0) throws SQLException {
        return delegate.findColumn(arg0);
    }

    public boolean first() throws SQLException {
        return delegate.first();
    }

    public Array getArray(int arg0) throws SQLException {
        return delegate.getArray(arg0);
    }

    public Array getArray(String arg0) throws SQLException {
        return delegate.getArray(arg0);
    }

    public InputStream getAsciiStream(int arg0) throws SQLException {
        return delegate.getAsciiStream(arg0);
    }

    public InputStream getAsciiStream(String arg0) throws SQLException {
        return delegate.getAsciiStream(arg0);
    }

    public BigDecimal getBigDecimal(int arg0, int arg1) throws SQLException {
        return delegate.getBigDecimal(arg0, arg1);
    }

    public BigDecimal getBigDecimal(int arg0) throws SQLException {
        return delegate.getBigDecimal(arg0);
    }

    public BigDecimal getBigDecimal(String arg0, int arg1) throws SQLException {
        return delegate.getBigDecimal(arg0, arg1);
    }

    public BigDecimal getBigDecimal(String arg0) throws SQLException {
        return delegate.getBigDecimal(arg0);
    }

    public InputStream getBinaryStream(int arg0) throws SQLException {
        return delegate.getBinaryStream(arg0);
    }

    public InputStream getBinaryStream(String arg0) throws SQLException {
        return delegate.getBinaryStream(arg0);
    }

    public Blob getBlob(int arg0) throws SQLException {
        return delegate.getBlob(arg0);
    }

    public Blob getBlob(String arg0) throws SQLException {
        return delegate.getBlob(arg0);
    }

    public boolean getBoolean(int arg0) throws SQLException {
        return delegate.getBoolean(arg0);
    }

    public boolean getBoolean(String arg0) throws SQLException {
        return delegate.getBoolean(arg0);
    }

    public byte getByte(int arg0) throws SQLException {
        return delegate.getByte(arg0);
    }

    public byte getByte(String arg0) throws SQLException {
        return delegate.getByte(arg0);
    }

    public byte[] getBytes(int arg0) throws SQLException {
        return delegate.getBytes(arg0);
    }

    public byte[] getBytes(String arg0) throws SQLException {
        return delegate.getBytes(arg0);
    }

    public Reader getCharacterStream(int arg0) throws SQLException {
        return delegate.getCharacterStream(arg0);
    }

    public Reader getCharacterStream(String arg0) throws SQLException {
        return delegate.getCharacterStream(arg0);
    }

    public Clob getClob(int arg0) throws SQLException {
        return delegate.getClob(arg0);
    }

    public Clob getClob(String arg0) throws SQLException {
        return delegate.getClob(arg0);
    }

    public int getConcurrency() throws SQLException {
        return delegate.getConcurrency();
    }

    public String getCursorName() throws SQLException {
        return delegate.getCursorName();
    }

    public Date getDate(int arg0, Calendar arg1) throws SQLException {
        return delegate.getDate(arg0, arg1);
    }

    public Date getDate(int arg0) throws SQLException {
        return delegate.getDate(arg0);
    }

    public Date getDate(String arg0, Calendar arg1) throws SQLException {
        return delegate.getDate(arg0, arg1);
    }

    public Date getDate(String arg0) throws SQLException {
        return delegate.getDate(arg0);
    }

    public double getDouble(int arg0) throws SQLException {
        return delegate.getDouble(arg0);
    }

    public double getDouble(String arg0) throws SQLException {
        return delegate.getDouble(arg0);
    }

    public int getFetchDirection() throws SQLException {
        return delegate.getFetchDirection();
    }

    public int getFetchSize() throws SQLException {
        return delegate.getFetchSize();
    }

    public float getFloat(int arg0) throws SQLException {
        return delegate.getFloat(arg0);
    }

    public float getFloat(String arg0) throws SQLException {
        return delegate.getFloat(arg0);
    }

    public int getHoldability() throws SQLException {
        return delegate.getHoldability();
    }

    public int getInt(int arg0) throws SQLException {
        return delegate.getInt(arg0);
    }

    public int getInt(String arg0) throws SQLException {
        return delegate.getInt(arg0);
    }

    public long getLong(int arg0) throws SQLException {
        return delegate.getLong(arg0);
    }

    public long getLong(String arg0) throws SQLException {
        return delegate.getLong(arg0);
    }

    public ResultSetMetaData getMetaData() throws SQLException {
        return delegate.getMetaData();
    }

    public Reader getNCharacterStream(int arg0) throws SQLException {
        return delegate.getNCharacterStream(arg0);
    }

    public Reader getNCharacterStream(String arg0) throws SQLException {
        return delegate.getNCharacterStream(arg0);
    }

    public NClob getNClob(int arg0) throws SQLException {
        return delegate.getNClob(arg0);
    }

    public NClob getNClob(String arg0) throws SQLException {
        return delegate.getNClob(arg0);
    }

    public String getNString(int arg0) throws SQLException {
        return delegate.getNString(arg0);
    }

    public String getNString(String arg0) throws SQLException {
        return delegate.getNString(arg0);
    }

    public <T> T getObject(int arg0, Class<T> arg1) throws SQLException {
        return delegate.getObject(arg0, arg1);
    }

    public Object getObject(int arg0, Map<String, Class<?>> arg1) throws SQLException {
        return delegate.getObject(arg0, arg1);
    }

    public Object getObject(int arg0) throws SQLException {
        return delegate.getObject(arg0);
    }

    public <T> T getObject(String arg0, Class<T> arg1) throws SQLException {
        return delegate.getObject(arg0, arg1);
    }

    public Object getObject(String arg0, Map<String, Class<?>> arg1) throws SQLException {
        return delegate.getObject(arg0, arg1);
    }

    public Object getObject(String arg0) throws SQLException {
        return delegate.getObject(arg0);
    }

    public Ref getRef(int arg0) throws SQLException {
        return delegate.getRef(arg0);
    }

    public Ref getRef(String arg0) throws SQLException {
        return delegate.getRef(arg0);
    }

    public int getRow() throws SQLException {
        return delegate.getRow();
    }

    public RowId getRowId(int arg0) throws SQLException {
        return delegate.getRowId(arg0);
    }

    public RowId getRowId(String arg0) throws SQLException {
        return delegate.getRowId(arg0);
    }

    public SQLXML getSQLXML(int arg0) throws SQLException {
        return delegate.getSQLXML(arg0);
    }

    public SQLXML getSQLXML(String arg0) throws SQLException {
        return delegate.getSQLXML(arg0);
    }

    public short getShort(int arg0) throws SQLException {
        return delegate.getShort(arg0);
    }

    public short getShort(String arg0) throws SQLException {
        return delegate.getShort(arg0);
    }

    public Statement getStatement() throws SQLException {
        return delegate.getStatement();
    }

    public String getString(int arg0) throws SQLException {
        return delegate.getString(arg0);
    }

    public String getString(String arg0) throws SQLException {
        return delegate.getString(arg0);
    }

    public Time getTime(int arg0, Calendar arg1) throws SQLException {
        return delegate.getTime(arg0, arg1);
    }

    public Time getTime(int arg0) throws SQLException {
        return delegate.getTime(arg0);
    }

    public Time getTime(String arg0, Calendar arg1) throws SQLException {
        return delegate.getTime(arg0, arg1);
    }

    public Time getTime(String arg0) throws SQLException {
        return delegate.getTime(arg0);
    }

    public Timestamp getTimestamp(int arg0, Calendar arg1) throws SQLException {
        return delegate.getTimestamp(arg0, arg1);
    }

    public Timestamp getTimestamp(int arg0) throws SQLException {
        return delegate.getTimestamp(arg0);
    }

    public Timestamp getTimestamp(String arg0, Calendar arg1) throws SQLException {
        return delegate.getTimestamp(arg0, arg1);
    }

    public Timestamp getTimestamp(String arg0) throws SQLException {
        return delegate.getTimestamp(arg0);
    }

    public int getType() throws SQLException {
        return delegate.getType();
    }

    public URL getURL(int arg0) throws SQLException {
        return delegate.getURL(arg0);
    }

    public URL getURL(String arg0) throws SQLException {
        return delegate.getURL(arg0);
    }

    public InputStream getUnicodeStream(int arg0) throws SQLException {
        return delegate.getUnicodeStream(arg0);
    }

    public InputStream getUnicodeStream(String arg0) throws SQLException {
        return delegate.getUnicodeStream(arg0);
    }

    public SQLWarning getWarnings() throws SQLException {
        return delegate.getWarnings();
    }

    public void insertRow() throws SQLException {
        delegate.insertRow();
    }

    public boolean isAfterLast() throws SQLException {
        return delegate.isAfterLast();
    }

    public boolean isBeforeFirst() throws SQLException {
        return delegate.isBeforeFirst();
    }

    public boolean isClosed() throws SQLException {
        return delegate.isClosed();
    }

    public boolean isFirst() throws SQLException {
        return delegate.isFirst();
    }

    public boolean isLast() throws SQLException {
        return delegate.isLast();
    }

    public boolean isWrapperFor(Class<?> arg0) throws SQLException {
        return delegate.isWrapperFor(arg0);
    }

    public boolean last() throws SQLException {
        return delegate.last();
    }

    public void moveToCurrentRow() throws SQLException {
        delegate.moveToCurrentRow();
    }

    public void moveToInsertRow() throws SQLException {
        delegate.moveToInsertRow();
    }

    public boolean next() throws SQLException {
        if (this.ignoreNext) {
            this.ignoreNext = false;
            return true;
        }
        return delegate.next();
    }

    public boolean previous() throws SQLException {
        return delegate.previous();
    }

    public void refreshRow() throws SQLException {
        delegate.refreshRow();
    }

    public boolean relative(int arg0) throws SQLException {
        return delegate.relative(arg0);
    }

    public boolean rowDeleted() throws SQLException {
        return delegate.rowDeleted();
    }

    public boolean rowInserted() throws SQLException {
        return delegate.rowInserted();
    }

    public boolean rowUpdated() throws SQLException {
        return delegate.rowUpdated();
    }

    public void setFetchDirection(int arg0) throws SQLException {
        delegate.setFetchDirection(arg0);
    }

    public void setFetchSize(int arg0) throws SQLException {
        delegate.setFetchSize(arg0);
    }

    public <T> T unwrap(Class<T> arg0) throws SQLException {
        return delegate.unwrap(arg0);
    }

    public void updateArray(int arg0, Array arg1) throws SQLException {
        delegate.updateArray(arg0, arg1);
    }

    public void updateArray(String arg0, Array arg1) throws SQLException {
        delegate.updateArray(arg0, arg1);
    }

    public void updateAsciiStream(int arg0, InputStream arg1, int arg2) throws SQLException {
        delegate.updateAsciiStream(arg0, arg1, arg2);
    }

    public void updateAsciiStream(int arg0, InputStream arg1, long arg2) throws SQLException {
        delegate.updateAsciiStream(arg0, arg1, arg2);
    }

    public void updateAsciiStream(int arg0, InputStream arg1) throws SQLException {
        delegate.updateAsciiStream(arg0, arg1);
    }

    public void updateAsciiStream(String arg0, InputStream arg1, int arg2) throws SQLException {
        delegate.updateAsciiStream(arg0, arg1, arg2);
    }

    public void updateAsciiStream(String arg0, InputStream arg1, long arg2) throws SQLException {
        delegate.updateAsciiStream(arg0, arg1, arg2);
    }

    public void updateAsciiStream(String arg0, InputStream arg1) throws SQLException {
        delegate.updateAsciiStream(arg0, arg1);
    }

    public void updateBigDecimal(int arg0, BigDecimal arg1) throws SQLException {
        delegate.updateBigDecimal(arg0, arg1);
    }

    public void updateBigDecimal(String arg0, BigDecimal arg1) throws SQLException {
        delegate.updateBigDecimal(arg0, arg1);
    }

    public void updateBinaryStream(int arg0, InputStream arg1, int arg2) throws SQLException {
        delegate.updateBinaryStream(arg0, arg1, arg2);
    }

    public void updateBinaryStream(int arg0, InputStream arg1, long arg2) throws SQLException {
        delegate.updateBinaryStream(arg0, arg1, arg2);
    }

    public void updateBinaryStream(int arg0, InputStream arg1) throws SQLException {
        delegate.updateBinaryStream(arg0, arg1);
    }

    public void updateBinaryStream(String arg0, InputStream arg1, int arg2) throws SQLException {
        delegate.updateBinaryStream(arg0, arg1, arg2);
    }

    public void updateBinaryStream(String arg0, InputStream arg1, long arg2) throws SQLException {
        delegate.updateBinaryStream(arg0, arg1, arg2);
    }

    public void updateBinaryStream(String arg0, InputStream arg1) throws SQLException {
        delegate.updateBinaryStream(arg0, arg1);
    }

    public void updateBlob(int arg0, Blob arg1) throws SQLException {
        delegate.updateBlob(arg0, arg1);
    }

    public void updateBlob(int arg0, InputStream arg1, long arg2) throws SQLException {
        delegate.updateBlob(arg0, arg1, arg2);
    }

    public void updateBlob(int arg0, InputStream arg1) throws SQLException {
        delegate.updateBlob(arg0, arg1);
    }

    public void updateBlob(String arg0, Blob arg1) throws SQLException {
        delegate.updateBlob(arg0, arg1);
    }

    public void updateBlob(String arg0, InputStream arg1, long arg2) throws SQLException {
        delegate.updateBlob(arg0, arg1, arg2);
    }

    public void updateBlob(String arg0, InputStream arg1) throws SQLException {
        delegate.updateBlob(arg0, arg1);
    }

    public void updateBoolean(int arg0, boolean arg1) throws SQLException {
        delegate.updateBoolean(arg0, arg1);
    }

    public void updateBoolean(String arg0, boolean arg1) throws SQLException {
        delegate.updateBoolean(arg0, arg1);
    }

    public void updateByte(int arg0, byte arg1) throws SQLException {
        delegate.updateByte(arg0, arg1);
    }

    public void updateByte(String arg0, byte arg1) throws SQLException {
        delegate.updateByte(arg0, arg1);
    }

    public void updateBytes(int arg0, byte[] arg1) throws SQLException {
        delegate.updateBytes(arg0, arg1);
    }

    public void updateBytes(String arg0, byte[] arg1) throws SQLException {
        delegate.updateBytes(arg0, arg1);
    }

    public void updateCharacterStream(int arg0, Reader arg1, int arg2) throws SQLException {
        delegate.updateCharacterStream(arg0, arg1, arg2);
    }

    public void updateCharacterStream(int arg0, Reader arg1, long arg2) throws SQLException {
        delegate.updateCharacterStream(arg0, arg1, arg2);
    }

    public void updateCharacterStream(int arg0, Reader arg1) throws SQLException {
        delegate.updateCharacterStream(arg0, arg1);
    }

    public void updateCharacterStream(String arg0, Reader arg1, int arg2) throws SQLException {
        delegate.updateCharacterStream(arg0, arg1, arg2);
    }

    public void updateCharacterStream(String arg0, Reader arg1, long arg2) throws SQLException {
        delegate.updateCharacterStream(arg0, arg1, arg2);
    }

    public void updateCharacterStream(String arg0, Reader arg1) throws SQLException {
        delegate.updateCharacterStream(arg0, arg1);
    }

    public void updateClob(int arg0, Clob arg1) throws SQLException {
        delegate.updateClob(arg0, arg1);
    }

    public void updateClob(int arg0, Reader arg1, long arg2) throws SQLException {
        delegate.updateClob(arg0, arg1, arg2);
    }

    public void updateClob(int arg0, Reader arg1) throws SQLException {
        delegate.updateClob(arg0, arg1);
    }

    public void updateClob(String arg0, Clob arg1) throws SQLException {
        delegate.updateClob(arg0, arg1);
    }

    public void updateClob(String arg0, Reader arg1, long arg2) throws SQLException {
        delegate.updateClob(arg0, arg1, arg2);
    }

    public void updateClob(String arg0, Reader arg1) throws SQLException {
        delegate.updateClob(arg0, arg1);
    }

    public void updateDate(int arg0, Date arg1) throws SQLException {
        delegate.updateDate(arg0, arg1);
    }

    public void updateDate(String arg0, Date arg1) throws SQLException {
        delegate.updateDate(arg0, arg1);
    }

    public void updateDouble(int arg0, double arg1) throws SQLException {
        delegate.updateDouble(arg0, arg1);
    }

    public void updateDouble(String arg0, double arg1) throws SQLException {
        delegate.updateDouble(arg0, arg1);
    }

    public void updateFloat(int arg0, float arg1) throws SQLException {
        delegate.updateFloat(arg0, arg1);
    }

    public void updateFloat(String arg0, float arg1) throws SQLException {
        delegate.updateFloat(arg0, arg1);
    }

    public void updateInt(int arg0, int arg1) throws SQLException {
        delegate.updateInt(arg0, arg1);
    }

    public void updateInt(String arg0, int arg1) throws SQLException {
        delegate.updateInt(arg0, arg1);
    }

    public void updateLong(int arg0, long arg1) throws SQLException {
        delegate.updateLong(arg0, arg1);
    }

    public void updateLong(String arg0, long arg1) throws SQLException {
        delegate.updateLong(arg0, arg1);
    }

    public void updateNCharacterStream(int arg0, Reader arg1, long arg2) throws SQLException {
        delegate.updateNCharacterStream(arg0, arg1, arg2);
    }

    public void updateNCharacterStream(int arg0, Reader arg1) throws SQLException {
        delegate.updateNCharacterStream(arg0, arg1);
    }

    public void updateNCharacterStream(String arg0, Reader arg1, long arg2) throws SQLException {
        delegate.updateNCharacterStream(arg0, arg1, arg2);
    }

    public void updateNCharacterStream(String arg0, Reader arg1) throws SQLException {
        delegate.updateNCharacterStream(arg0, arg1);
    }

    public void updateNClob(int arg0, NClob arg1) throws SQLException {
        delegate.updateNClob(arg0, arg1);
    }

    public void updateNClob(int arg0, Reader arg1, long arg2) throws SQLException {
        delegate.updateNClob(arg0, arg1, arg2);
    }

    public void updateNClob(int arg0, Reader arg1) throws SQLException {
        delegate.updateNClob(arg0, arg1);
    }

    public void updateNClob(String arg0, NClob arg1) throws SQLException {
        delegate.updateNClob(arg0, arg1);
    }

    public void updateNClob(String arg0, Reader arg1, long arg2) throws SQLException {
        delegate.updateNClob(arg0, arg1, arg2);
    }

    public void updateNClob(String arg0, Reader arg1) throws SQLException {
        delegate.updateNClob(arg0, arg1);
    }

    public void updateNString(int arg0, String arg1) throws SQLException {
        delegate.updateNString(arg0, arg1);
    }

    public void updateNString(String arg0, String arg1) throws SQLException {
        delegate.updateNString(arg0, arg1);
    }

    public void updateNull(int arg0) throws SQLException {
        delegate.updateNull(arg0);
    }

    public void updateNull(String arg0) throws SQLException {
        delegate.updateNull(arg0);
    }

    public void updateObject(int arg0, Object arg1, int arg2) throws SQLException {
        delegate.updateObject(arg0, arg1, arg2);
    }

    public void updateObject(int columnIndex, Object x, SQLType targetSqlType, int scaleOrLength) throws SQLException {
        delegate.updateObject(columnIndex, x, targetSqlType, scaleOrLength);
    }

    public void updateObject(int columnIndex, Object x, SQLType targetSqlType) throws SQLException {
        delegate.updateObject(columnIndex, x, targetSqlType);
    }

    public void updateObject(int arg0, Object arg1) throws SQLException {
        delegate.updateObject(arg0, arg1);
    }

    public void updateObject(String arg0, Object arg1, int arg2) throws SQLException {
        delegate.updateObject(arg0, arg1, arg2);
    }

    public void updateObject(String columnLabel, Object x, SQLType targetSqlType, int scaleOrLength)
            throws SQLException {
        delegate.updateObject(columnLabel, x, targetSqlType, scaleOrLength);
    }

    public void updateObject(String columnLabel, Object x, SQLType targetSqlType) throws SQLException {
        delegate.updateObject(columnLabel, x, targetSqlType);
    }

    public void updateObject(String arg0, Object arg1) throws SQLException {
        delegate.updateObject(arg0, arg1);
    }

    public void updateRef(int arg0, Ref arg1) throws SQLException {
        delegate.updateRef(arg0, arg1);
    }

    public void updateRef(String arg0, Ref arg1) throws SQLException {
        delegate.updateRef(arg0, arg1);
    }

    public void updateRow() throws SQLException {
        delegate.updateRow();
    }

    public void updateRowId(int arg0, RowId arg1) throws SQLException {
        delegate.updateRowId(arg0, arg1);
    }

    public void updateRowId(String arg0, RowId arg1) throws SQLException {
        delegate.updateRowId(arg0, arg1);
    }

    public void updateSQLXML(int arg0, SQLXML arg1) throws SQLException {
        delegate.updateSQLXML(arg0, arg1);
    }

    public void updateSQLXML(String arg0, SQLXML arg1) throws SQLException {
        delegate.updateSQLXML(arg0, arg1);
    }

    public void updateShort(int arg0, short arg1) throws SQLException {
        delegate.updateShort(arg0, arg1);
    }

    public void updateShort(String arg0, short arg1) throws SQLException {
        delegate.updateShort(arg0, arg1);
    }

    public void updateString(int arg0, String arg1) throws SQLException {
        delegate.updateString(arg0, arg1);
    }

    public void updateString(String arg0, String arg1) throws SQLException {
        delegate.updateString(arg0, arg1);
    }

    public void updateTime(int arg0, Time arg1) throws SQLException {
        delegate.updateTime(arg0, arg1);
    }

    public void updateTime(String arg0, Time arg1) throws SQLException {
        delegate.updateTime(arg0, arg1);
    }

    public void updateTimestamp(int arg0, Timestamp arg1) throws SQLException {
        delegate.updateTimestamp(arg0, arg1);
    }

    public void updateTimestamp(String arg0, Timestamp arg1) throws SQLException {
        delegate.updateTimestamp(arg0, arg1);
    }

    public boolean wasNull() throws SQLException {
        return delegate.wasNull();
    }

    public void setIgnoreNext(boolean ignoreNext) {
        this.ignoreNext = ignoreNext;
    }

}
