package io.graphqlcrud;

public class Annotations {
    public String kind;
    public String primaryField;
    public  String foreignField;

    public void setAnnotation(String kind, String primaryField, String foreignField) {
        this.kind = kind;
        this.primaryField = primaryField;
        this.foreignField = foreignField;
    }

    public String getKind() {
        return kind;
    }

    public String getPrimaryField() {
        return primaryField;
    }

    public String getForeignField() { return foreignField; }
}