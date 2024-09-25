package us.hyalen.mysql_proxy.core.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import us.hyalen.mysql_proxy.config.DataSourceContextHolder;
import us.hyalen.mysql_proxy.config.enums.DBType;
import us.hyalen.mysql_proxy.core.ResourceNotFoundException;
import us.hyalen.mysql_proxy.core.dto.SQLRequestDto;
import us.hyalen.mysql_proxy.core.dto.WhereClauseDto;

import java.util.List;
import java.util.Map;

@Service
public class GenericQueryServiceImpl implements GenericQueryService {
    private final JdbcTemplate jdbcTemplate;

    public GenericQueryServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Object executeGenericQuery(String query, DBType dbType) {
        // Set the data source key dynamically based on DBType
        DataSourceContextHolder.setDataSourceKey(dbType.name());

        query = query.trim().toUpperCase();  // Normalize query

        try {
            if (query.startsWith("SELECT")) {
                // Execute SELECT query and return result
                List<Map<String, Object>> result = jdbcTemplate.queryForList(query);

                // throw ResourceNotFoundException if result is empty
                if (result.isEmpty())
                    throw new ResourceNotFoundException("No data found for the query: " + query);

                return result;
            } else if (query.startsWith("UPDATE") || query.startsWith("DELETE") || query.startsWith("INSERT")) {
                // Execute DML (Data Manipulation Language) query and return affected rows
                int rowsAffected = jdbcTemplate.update(query);

                return rowsAffected;
            } else {
                // For any other type of query (like DDL), just execute it
                jdbcTemplate.execute(query);

                return null;
            }
        } finally {
            // Clear the data source key to avoid affecting other operations
            DataSourceContextHolder.clearDataSourceKey();
        }
    }

    @Override
    public Object executeGenericQuery(SQLRequestDto sqlRequestDto, DBType dbType) {
        // Construct the SQL query string
        String query = buildQueryFromDto(sqlRequestDto);

        return executeGenericQuery(query, dbType);
    }

    private String buildQueryFromDto(SQLRequestDto sqlRequestDto) {
        StringBuilder queryBuilder = new StringBuilder();

        switch (sqlRequestDto.getOperation().toUpperCase()) {
            case "SELECT":
                queryBuilder.append("SELECT ");

                if (sqlRequestDto.getColumns() != null && !sqlRequestDto.getColumns().isEmpty())
                    queryBuilder.append(String.join(", ", sqlRequestDto.getColumns()));
                else
                    queryBuilder.append("*");

                queryBuilder.append(" FROM ").append(sqlRequestDto.getTableName());

                if (sqlRequestDto.getWhereClause() != null && !sqlRequestDto.getWhereClause().isEmpty()) {
                    queryBuilder.append(" WHERE ");

                    for (int i = 0; i < sqlRequestDto.getWhereClause().size(); i++) {
                        WhereClauseDto whereClause = sqlRequestDto.getWhereClause().get(i);
                        queryBuilder.append(whereClause.getColumn())
                                .append(" ")
                                .append(whereClause.getOperator())
                                .append(" '")
                                .append(whereClause.getValue())
                                .append("'");

                        if (i < sqlRequestDto.getWhereClause().size() - 1)
                            queryBuilder.append(" AND ");
                    }
                }
                break;

            case "INSERT":
                queryBuilder.append("INSERT INTO ")
                        .append(sqlRequestDto.getTableName())
                        .append(" (");

                // Add column names
                if (sqlRequestDto.getColumns() != null && !sqlRequestDto.getColumns().isEmpty()) {
                    queryBuilder.append(String.join(", ", sqlRequestDto.getColumns()));
                    queryBuilder.append(") VALUES ");
                }

                // Add values
                for (int i = 0; i < sqlRequestDto.getValues().size(); i++) {
                    List<String> rowValues = sqlRequestDto.getValues().get(i);
                    queryBuilder.append("(");

                    for (int j = 0; j < rowValues.size(); j++) {
                        String value = rowValues.get(j);
                        queryBuilder.append(value != null ? "'" + value + "'" : "NULL");

                        if (j < rowValues.size() - 1)
                            queryBuilder.append(", ");
                    }

                    queryBuilder.append(")");

                    if (i < sqlRequestDto.getValues().size() - 1)
                        queryBuilder.append(", ");
                }
                break;

            case "UPDATE":
                queryBuilder.append("UPDATE ")
                        .append(sqlRequestDto.getTableName())
                        .append(" SET ");

                // Add columns to be updated and their new values
                if (sqlRequestDto.getColumns() != null && !sqlRequestDto.getColumns().isEmpty() &&
                        sqlRequestDto.getValues() != null && sqlRequestDto.getValues().size() == 1) {

                    List<String> valuesToUpdate = sqlRequestDto.getValues().get(0);

                    for (int i = 0; i < sqlRequestDto.getColumns().size(); i++) {
                        queryBuilder.append(sqlRequestDto.getColumns().get(i))
                                .append(" = ")
                                .append(valuesToUpdate.get(i) != null ? "'" + valuesToUpdate.get(i) + "'" : "NULL");

                        if (i < sqlRequestDto.getColumns().size() - 1)
                            queryBuilder.append(", ");
                    }
                }

                // Add WHERE clause if present
                if (sqlRequestDto.getWhereClause() != null && !sqlRequestDto.getWhereClause().isEmpty()) {
                    queryBuilder.append(" WHERE ");

                    for (int i = 0; i < sqlRequestDto.getWhereClause().size(); i++) {
                        WhereClauseDto whereClause = sqlRequestDto.getWhereClause().get(i);

                        queryBuilder.append(whereClause.getColumn())
                                .append(" ")
                                .append(whereClause.getOperator())
                                .append(" '")
                                .append(whereClause.getValue())
                                .append("'");

                        if (i < sqlRequestDto.getWhereClause().size() - 1)
                            queryBuilder.append(" AND ");
                    }
                }
                break;

            case "DELETE":
                queryBuilder.append("DELETE FROM ").append(sqlRequestDto.getTableName());

                // Add WHERE clause if present
                if (sqlRequestDto.getWhereClause() != null && !sqlRequestDto.getWhereClause().isEmpty()) {
                    queryBuilder.append(" WHERE ");

                    for (int i = 0; i < sqlRequestDto.getWhereClause().size(); i++) {
                        WhereClauseDto whereClause = sqlRequestDto.getWhereClause().get(i);

                        queryBuilder.append(whereClause.getColumn())
                                .append(" ")
                                .append(whereClause.getOperator())
                                .append(" '")
                                .append(whereClause.getValue())
                                .append("'");

                        if (i < sqlRequestDto.getWhereClause().size() - 1)
                            queryBuilder.append(" AND ");
                    }
                }
                break;

            case "UPSERT":
                queryBuilder.append("INSERT INTO ")
                        .append(sqlRequestDto.getTableName())
                        .append(" (");

                // Add column names
                if (sqlRequestDto.getColumns() != null && !sqlRequestDto.getColumns().isEmpty()) {
                    queryBuilder.append(String.join(", ", sqlRequestDto.getColumns()));
                    queryBuilder.append(") VALUES ");
                }

                // Add values
                for (int i = 0; i < sqlRequestDto.getValues().size(); i++) {
                    List<String> rowValues = sqlRequestDto.getValues().get(i);
                    queryBuilder.append("(");

                    for (int j = 0; j < rowValues.size(); j++) {
                        String value = rowValues.get(j);
                        queryBuilder.append(value != null ? "'" + value + "'" : "NULL");

                        if (j < rowValues.size() - 1) {
                            queryBuilder.append(", ");
                        }
                    }

                    queryBuilder.append(")");

                    if (i < sqlRequestDto.getValues().size() - 1) {
                        queryBuilder.append(", ");
                    }
                }

                queryBuilder.append(" ON DUPLICATE KEY UPDATE ");

                // Add columns and values for the update
                if (sqlRequestDto.getOnDuplicateUpdateColumns() != null &&
                        !sqlRequestDto.getOnDuplicateUpdateColumns().isEmpty() &&
                        sqlRequestDto.getOnDuplicateUpdateValues() != null) {

                    List<String> updateColumns = sqlRequestDto.getOnDuplicateUpdateColumns();
                    List<String> updateValues = sqlRequestDto.getOnDuplicateUpdateValues();

                    for (int i = 0; i < updateColumns.size(); i++) {
                        queryBuilder.append(updateColumns.get(i))
                                .append(" = ")
                                .append(updateValues.get(i) != null ? "'" + updateValues.get(i) + "'" : "NULL");

                        if (i < updateColumns.size() - 1) {
                            queryBuilder.append(", ");
                        }
                    }
                } else {
                    throw new IllegalArgumentException("Missing columns/values for ON DUPLICATE KEY UPDATE");
                }
                break;

            default:
                throw new UnsupportedOperationException("Unsupported operation: " + sqlRequestDto.getOperation());
        }

        return queryBuilder.toString();
    }
}
