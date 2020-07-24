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
package io.graphqlcrud.model;

public class Attribute {
    private final String name;
    private final boolean isNullable;
    private final int type;

    public Attribute(String name, int type, boolean isNullable) {
        this.name = name;
        this.type = type;
        this.isNullable = isNullable;
    }

    public boolean isNullable() {
        return isNullable;
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Attribute [name=" + name + ", isNullable=" + isNullable + ", type=" + type + "]";
    }
}
