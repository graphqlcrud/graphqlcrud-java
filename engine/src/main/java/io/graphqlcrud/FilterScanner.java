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

import graphql.language.ObjectField;
import graphql.language.ObjectValue;

public class FilterScanner<C> {
    private FilterBuilder<C> visitor;
    enum Clause {and, or, not}

    class WrappedCondition {
        C condition;
        Clause clause = Clause.and;
        WrappedCondition(C c, Clause clause){
            this.condition = c;
            this.clause = clause;
        }
    }

    public FilterScanner(FilterBuilder<C> visitor) {
        this.visitor = visitor;
    }

    public WrappedCondition scan(ObjectValue filterInput, Clause wrappedClause) {

        // first lets walk the naked filter fields for example, that are not "and/or/not"
        // foo : {
        //     eq: "bar"
        // }
        WrappedCondition prevCondition = null;
        for (ObjectField field : filterInput.getObjectFields()) {
            if (field.getName().equals("and") || field.getName().equals("or") || field.getName().equals("not")) {
                continue;
            } else {
                ObjectValue value = (ObjectValue)field.getValue();
                if (!value.getChildren().isEmpty()) {
                    ObjectField child = value.getObjectFields().get(0);
                    C condition = this.visitor.buildCondition(field.getName(), child.getName(), child.getValue());
                    if (prevCondition == null) {
                        prevCondition = new WrappedCondition(condition, wrappedClause);
                    } else {
                        condition = applyClause(prevCondition.condition, Clause.and, condition);
                        prevCondition = new WrappedCondition(condition, wrappedClause);
                    }
                }
            }
        }

        // now walk only and/or/not
        for (ObjectField field : filterInput.getObjectFields()) {
            if (field.getChildren().isEmpty()) {
                continue;
            }
            if (field.getName().equals("and")) {
                WrappedCondition w = scan((ObjectValue)field.getValue(), Clause.and);
                if (prevCondition == null) {
                    prevCondition = w;
                } else {
                    C condition = applyClause(prevCondition.condition, prevCondition.clause, w.condition);
                    prevCondition = new WrappedCondition(condition, w.clause);
                }
            } else if (field.getName().equals("or")) {
                WrappedCondition w = scan((ObjectValue)field.getValue(), Clause.or);
                if (prevCondition == null) {
                    prevCondition = w;
                } else {
                    C condition = applyClause(prevCondition.condition, Clause.or, w.condition);
                    prevCondition = new WrappedCondition(condition, w.clause);
                }
            } else if (field.getName().equals("not")) {
                WrappedCondition w = scan((ObjectValue)field.getValue(), Clause.and);
                if (prevCondition == null) {
                    C condition = this.visitor.not(w.condition);
                    prevCondition = new WrappedCondition(condition, Clause.and);
                } else {
                    C condition = applyClause(prevCondition.condition, prevCondition.clause, this.visitor.not(w.condition));
                    prevCondition = new WrappedCondition(condition, Clause.and);
                }
            } else {
                // we should have already handled this case above
                // ignore
            }
        }
        return prevCondition;
    }

    private C applyClause(C left, Clause clause, C right) {
        switch(clause) {
        case and:
            return this.visitor.and(left, right);
        case or:
            return this.visitor.or(left, right);
        default:
            throw new RuntimeException("Wrong clause");
        }
    }
}
