package us.hyalen.mysql_proxy.core.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import us.hyalen.mysql_proxy.config.DataSourceContextHolder;
import us.hyalen.mysql_proxy.config.enums.DBType;
import us.hyalen.mysql_proxy.core.ResourceNotFoundException;
import us.hyalen.mysql_proxy.core.dto.SQLRequestDto;
import us.hyalen.mysql_proxy.core.dto.WhereClauseDto;
import us.hyalen.mysql_proxy.core.dto.ColumnValueDto;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

                if (sqlRequestDto.getColumns() != null && !sqlRequestDto.getColumns().isEmpty()) {
                    queryBuilder.append(String.join(", ", sqlRequestDto.getColumns()));
                } else {
                    queryBuilder.append("*");
                }

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
                        if (i < sqlRequestDto.getWhereClause().size() - 1) {
                            queryBuilder.append(" AND ");
                        }
                    }
                }
                break;

            case "INSERT":
                queryBuilder.append("INSERT INTO ")
                        .append(sqlRequestDto.getTableName())
                        .append(" (");

                // Get unique column names
                if (sqlRequestDto.getValues() != null && !sqlRequestDto.getValues().isEmpty()) {
                    List<String> uniqueColumns = sqlRequestDto.getValues().stream()
                            .map(ColumnValueDto::getColumn)
                            .distinct()
                            .collect(Collectors.toList());

                    // Add column names
                    queryBuilder.append(String.join(", ", uniqueColumns));
                    queryBuilder.append(") VALUES ");

                    // Group values into rows
                    int numColumns = uniqueColumns.size();
                    for (int i = 0; i < sqlRequestDto.getValues().size(); i += numColumns) {
                        queryBuilder.append("(");
                        for (int j = 0; j < numColumns; j++) {
                            String value = sqlRequestDto.getValues().get(i + j).getValue();
                            queryBuilder.append(value != null ? "'" + value + "'" : "NULL");
                            if (j < numColumns - 1) {
                                queryBuilder.append(", ");
                            }
                        }
                        queryBuilder.append(")");
                        if (i + numColumns < sqlRequestDto.getValues().size()) {
                            queryBuilder.append(", ");
                        }
                    }
                }
                break;

            // Add cases for UPDATE and DELETE here

            default:
                throw new UnsupportedOperationException("Unsupported operation: " + sqlRequestDto.getOperation());
        }

        return queryBuilder.toString();
    }
}
