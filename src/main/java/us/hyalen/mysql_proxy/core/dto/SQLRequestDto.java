package us.hyalen.mysql_proxy.core.dto;

import java.util.List;

public class SQLRequestDto {
    private String operation; // e.g., "SELECT", "INSERT", etc.
    private String tableName; // e.g., "employees"
    private List<String> columns; // Used for SELECT or UPDATE
    private List<ColumnValueDto> values; // Used for INSERT or UPDATE
    private List<WhereClauseDto> whereClause; // Conditions for WHERE clause

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

    public List<ColumnValueDto> getValues() {
        return values;
    }

    public void setValues(List<ColumnValueDto> values) {
        this.values = values;
    }

    public List<WhereClauseDto> getWhereClause() {
        return whereClause;
    }

    public void setWhereClause(List<WhereClauseDto> whereClause) {
        this.whereClause = whereClause;
    }
}

/*
{
  "operation": "SELECT",
  "tableName": "MyTable",
  "columns": ["*"]
}

// Response
{
    "status": "SUCCESS",
    "data": [
        {
            "id": 1,
            "first_name": "John",
            "middle_name": "Michael",
            "last_name": "Doe"
        },
        {
            "id": 2,
            "first_name": "Jane",
            "middle_name": null,
            "last_name": "Smith"
        },
        {
            "id": 3,
            "first_name": "Alice",
            "middle_name": "Marie",
            "last_name": "Johnson"
        },
        {
            "id": 4,
            "first_name": "Bob",
            "middle_name": "David",
            "last_name": "Brown"
        },
        {
            "id": 5,
            "first_name": "Charlie",
            "middle_name": "Edward",
            "last_name": "Davis"
        },
        {
            "id": 6,
            "first_name": "EMMA",
            "middle_name": "GRACE",
            "last_name": "WILSON"
        },
        {
            "id": 7,
            "first_name": "OLIVIA",
            "middle_name": null,
            "last_name": "SMITH"
        },
        {
            "id": 8,
            "first_name": "LIAM",
            "middle_name": "JAMES",
            "last_name": "BROWN"
        },
        {
            "id": 9,
            "first_name": "SOPHIA",
            "middle_name": "ISABELLA",
            "last_name": "DAVIS"
        },
        {
            "id": 10,
            "first_name": "NOAH",
            "middle_name": "ALEXANDER",
            "last_name": "JOHNSON"
        }
    ],
    "errors": null
}
 */

/*
{
  "operation": "SELECT",
  "tableName": "MyTable",
  "columns": ["*"],
  "whereClause": [
    {
      "column": "first_name",
      "operator": "=",
      "value": "Jane"
    }
  ]
}

// Response
{
    "status": "SUCCESS",
    "data": [
        {
            "id": 2,
            "first_name": "Jane",
            "middle_name": null,
            "last_name": "Smith"
        }
    ],
    "errors": null
}
*/


/*
{
  "operation": "INSERT",
  "tableName": "MyTable",
  "values": [
    {
      "column": "first_name",
      "value": "Emma"
    },
    {
      "column": "middle_name",
      "value": "Grace"
    },
    {
      "column": "last_name",
      "value": "Wilson"
    },
    {
      "column": "first_name",
      "value": "Olivia"
    },
    {
      "column": "middle_name",
      "value": null
    },
    {
      "column": "last_name",
      "value": "Smith"
    },
    {
      "column": "first_name",
      "value": "Liam"
    },
    {
      "column": "middle_name",
      "value": "James"
    },
    {
      "column": "last_name",
      "value": "Brown"
    },
    {
      "column": "first_name",
      "value": "Sophia"
    },
    {
      "column": "middle_name",
      "value": "Isabella"
    },
    {
      "column": "last_name",
      "value": "Davis"
    },
    {
      "column": "first_name",
      "value": "Noah"
    },
    {
      "column": "middle_name",
      "value": "Alexander"
    },
    {
      "column": "last_name",
      "value": "Johnson"
    }
  ]
}

// Response
{
    "status": "SUCCESS",
    "data": 5,
    "errors": null
}
 */