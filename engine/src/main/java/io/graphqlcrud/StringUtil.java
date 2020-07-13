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

public class StringUtil {

    public static String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
    
    public static String plural(String str) {
        // stupid pluralizer, but can use a better one, 
        // need to find a simple library
        if (str.toLowerCase().endsWith("s")) {
            return str.substring(0, str.length()-1)+"es";
        } else if (str.toLowerCase().endsWith("y")) {
            return str.substring(0, str.length()-1)+"ies";
        } else {
            return str + "s";
        }
    }    
}
