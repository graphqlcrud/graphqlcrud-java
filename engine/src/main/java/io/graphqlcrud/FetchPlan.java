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

import java.util.HashMap;
import java.util.List;

public class FetchPlan {
    HashMap<String, List<String>> keyColumns = new HashMap<>();

    public void addUniques(String name, List<String> columns) {
        this.keyColumns.put(name, columns);
    }
    
    public List<String> getUniques(String name){
        return this.keyColumns.get(name);
    }
}
