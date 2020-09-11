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

class ResultSetList extends AbstractList<Object> {
    private ResultSetWrapper rs;
    private Iterator<Object> itr;
    private Object current;
    private boolean advanceCursor;

    ResultSetList(ResultSetWrapper rs, boolean advanceCursor){
        this.rs = rs;
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
                    ResultSetList.this.advanceCursor = true;
                    return hasNext;
                } catch (SQLException e) {
                    throw new RuntimeException("Failed to walk the results", e);
                }
            }
            @Override
            public Object next() {
                return real.next();
            }
        };
    }

    @Override
    public int size() {
        return Integer.MAX_VALUE;
    }
}
