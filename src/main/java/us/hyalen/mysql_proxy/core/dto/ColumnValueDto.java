package us.hyalen.mysql_proxy.core.dto;

public class ColumnValueDto {
    private String column; // e.g., "name"
    private String value; // e.g., "Alice Johnson"

    // Getters and setters

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}