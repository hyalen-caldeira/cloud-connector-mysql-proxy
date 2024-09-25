package us.hyalen.mysql_proxy.core.dto;

import java.util.List;

public class SQLRequestDto {
    private String operation; // e.g., "SELECT", "INSERT", "UPSERT", etc.
    private String tableName; // e.g., "employees"
    private List<String> columns; // Used for INSERT and SELECT
    private List<List<String>> values; // Each inner list represents a row of values
    private List<WhereClauseDto> whereClause; // Conditions for WHERE clause

    // New field for UPSERT (onDuplicateUpdate)
    private List<String> onDuplicateUpdateColumns; // Columns to be updated on duplicate key
    private List<String> onDuplicateUpdateValues;  // Corresponding values to be updated

    // Getters and setters
    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public List<List<String>> getValues() {
        return values;
    }

    public void setValues(List<List<String>> values) {
        this.values = values;
    }

    public List<WhereClauseDto> getWhereClause() {
        return whereClause;
    }

    public void setWhereClause(List<WhereClauseDto> whereClause) {
        this.whereClause = whereClause;
    }

    // New getters and setters for onDuplicateUpdate
    public List<String> getOnDuplicateUpdateColumns() {
        return onDuplicateUpdateColumns;
    }

    public void setOnDuplicateUpdateColumns(List<String> onDuplicateUpdateColumns) {
        this.onDuplicateUpdateColumns = onDuplicateUpdateColumns;
    }

    public List<String> getOnDuplicateUpdateValues() {
        return onDuplicateUpdateValues;
    }

    public void setOnDuplicateUpdateValues(List<String> onDuplicateUpdateValues) {
        this.onDuplicateUpdateValues = onDuplicateUpdateValues;
    }
}