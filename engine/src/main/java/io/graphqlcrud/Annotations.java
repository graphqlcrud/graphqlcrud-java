package io.graphqlcrud;

public class Annotations {
    public String kind;
    public String field;

    public void setRelationshipAnnotation(String kind, String field) {
        this.kind = kind;
        this.field = field;
    }

    public String getRelationshipAnnotation() {
        return " @" + kind + "(field: \'" + field + "\') ";
    }
}