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

import java.sql.SQLException;
import java.util.AbstractList;
import java.util.Iterator;
import java.util.Map;

class ResultSetList extends AbstractList<Object> {
    private ResultSetWrapper rs;
    private Iterator<Object> itr;
    private Object current;
    private Map<String, Object> sameParentCriteria;
    private boolean advanceCursor;

    ResultSetList(ResultSetWrapper rs, Map<String, Object> criteria, boolean advanceCursor){
        this.rs = rs;
        this.sameParentCriteria = criteria;
        this.advanceCursor = advanceCursor;
    }

    public Object get() {
        if (this.itr == null) {
            this.itr = iterator();
            if (!this.itr.hasNext()) {
                return null;
            }
            this.current = this.itr.next();
        }
        return this.current;
    }

    @Override
    public Object get(int index) {
        return this.rs;
    }

    @Override
    public Iterator<Object> iterator() {
        final Iterator<Object> real = super.iterator();
        return new Iterator<Object>() {
            // this just makes sure that before advancing the cursor next() has been called
            @Override
            public boolean hasNext() {
                try {
                    if (ResultSetList.this.rs.isClosed()) {
                        return false;
                    }
                    boolean hasNext = ResultSetList.this.advanceCursor ? ResultSetList.this.rs.next() : true;
                    if (!hasNext) {
                        ResultSetList.this.rs.close();
                    }

                    // there is a new row, is this part of same parent?
                    if (hasNext && ResultSetList.this.sameParentCriteria != null) {
                        if (isPartOfSameParent()) {
                            ResultSetList.this.advanceCursor = true;
                            return true;
                        } else {
                            ResultSetList.this.advanceCursor = false;
                            // if advanceCursor controls drilling in, this one controls
                            // going back in nested results
                            ResultSetList.this.rs.setIgnoreNext(true);
                            return false;
                        }
                    }
                    ResultSetList.this.advanceCursor = true;
                    return hasNext;
                } catch (SQLException e) {
                    throw new RuntimeException("Failed to walk the results");
                }
            }
            @Override
            public Object next() {
                return real.next();
            }
        };
    }

    private boolean isPartOfSameParent() throws SQLException {
        for (Map.Entry<String, Object> entry : this.sameParentCriteria.entrySet()) {
            Object rowVal = this.rs.getObject(entry.getKey());
            if (!rowVal.equals(entry.getValue())){
                return false;
            }
        }

        return true;
    }

    @Override
    public int size() {
        return Integer.MAX_VALUE;
    }
}
