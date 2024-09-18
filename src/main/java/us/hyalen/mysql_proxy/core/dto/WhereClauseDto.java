package us.hyalen.mysql_proxy.core.dto;

public class WhereClauseDto {
    private String column; // e.g., "department"
    private String operator; // e.g., "="
    private String value; // e.g., "IT"

    // Getters and setters

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}