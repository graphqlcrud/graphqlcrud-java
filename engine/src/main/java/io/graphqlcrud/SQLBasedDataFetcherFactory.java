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
import java.util.Arrays;
import java.util.List;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetcherFactory;
import graphql.schema.DataFetcherFactoryEnvironment;
import graphql.schema.DataFetchingEnvironment;

public class SQLBasedDataFetcherFactory implements DataFetcherFactory<List<String>> {
    @Override
    public DataFetcher<List<String>> get(DataFetcherFactoryEnvironment environment) {
        return new DataFetcher<List<String>>() {
            @Override
            public List<String> get(DataFetchingEnvironment environment) throws Exception {
                // TODO: How to fix this for SQL
                return Arrays.asList("blah");
            }
        };
    }
}
