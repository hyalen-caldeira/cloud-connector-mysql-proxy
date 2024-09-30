package us.hyalen.mysql_proxy.core.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import us.hyalen.mysql_proxy.config.DataSourceContextHolder;
import us.hyalen.mysql_proxy.config.enums.DBType;
import us.hyalen.mysql_proxy.core.FallbackException;
import us.hyalen.mysql_proxy.core.ResourceNotFoundException;
import us.hyalen.mysql_proxy.core.dto.SQLRequestDto;
import us.hyalen.mysql_proxy.core.dto.WhereClauseDto;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class GenericQueryServiceImpl implements GenericQueryService {
    private static final Logger logger = LoggerFactory.getLogger(GenericQueryServiceImpl.class);
    private final JdbcTemplate jdbcTemplate;
    @Autowired
    private Environment env;

    public GenericQueryServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @CircuitBreaker(name = "#root.args[1].name()", fallbackMethod = "fallbackExecuteGenericQuery")
    @TimeLimiter(name = "#root.args[1].name()") // Dynamic TimeLimiter based on DBType
    public CompletableFuture<Object> executeGenericQuery(String query, DBType dbType) {
        logger.info("Executing query: {}", query);
        logger.debug("Using DB type: {}", dbType);

        // Asynchronous execution using CompletableFuture
        return CompletableFuture.supplyAsync(() -> {
            // Set the data source key dynamically based on DBType
            DataSourceContextHolder.setDataSourceKey(dbType.name());
            logger.debug("Data source key set to: {}", dbType.name());

            String normalizedQuery = query.trim().toUpperCase();  // Normalize query
            normalizedQuery = enforceQueryLimit(normalizedQuery); // Ensure query has a LIMIT clause
            logger.debug("Normalized query: {}", normalizedQuery);

            try {
                if (normalizedQuery.startsWith("SELECT")) {
                    logger.info("Executing SELECT query...");
                    List<Map<String, Object>> result = jdbcTemplate.queryForList(normalizedQuery);
                    logger.debug("Query result: {}", result);

                    if (result.isEmpty()) {
                        logger.warn("No data found for the query: {}", normalizedQuery);
                        throw new ResourceNotFoundException("No data found for the query: " + normalizedQuery);
                    }

                    logger.info("SELECT query executed successfully.");
                    return result;
                } else if (normalizedQuery.startsWith("UPDATE") || normalizedQuery.startsWith("DELETE") || normalizedQuery.startsWith("INSERT")) {
                    logger.info("Executing DML query (UPDATE/DELETE/INSERT)...");
                    int rowsAffected = jdbcTemplate.update(normalizedQuery);
                    logger.debug("Rows affected: {}", rowsAffected);
                    return rowsAffected;
                } else {
                    logger.info("Executing DDL or other type of query...");
                    jdbcTemplate.execute(normalizedQuery);
                    logger.info("Query executed successfully.");
                    return null;
                }
            } finally {
                logger.debug("Clearing data source key to avoid affecting other operations.");
                DataSourceContextHolder.clearDataSourceKey();
            }
        });
    }

    @Override
    public CompletableFuture<Object> executeGenericQuery(SQLRequestDto sqlRequestDto, DBType dbType) {
        logger.info("Constructing SQL query from DTO.");

        // Build the query from the SQLRequestDto
        String query = buildQueryFromDto(sqlRequestDto);
        logger.debug("Constructed query: {}", query);

        // Delegate to the existing executeGenericQuery method with the constructed query
        return executeGenericQuery(query, dbType);
    }

    private String buildQueryFromDto(SQLRequestDto sqlRequestDto) {
        StringBuilder queryBuilder = new StringBuilder();
        logger.info("Building query for operation: {}", sqlRequestDto.getOperation());

        switch (sqlRequestDto.getOperation().toUpperCase()) {
            case "SELECT":
                logger.debug("Building SELECT query.");
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
                logger.debug("Building INSERT query.");
                queryBuilder.append("INSERT INTO ")
                        .append(sqlRequestDto.getTableName())
                        .append(" (");

                if (sqlRequestDto.getColumns() != null && !sqlRequestDto.getColumns().isEmpty()) {
                    queryBuilder.append(String.join(", ", sqlRequestDto.getColumns()));
                    queryBuilder.append(") VALUES ");
                }

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
                logger.debug("Building UPDATE query.");
                queryBuilder.append("UPDATE ")
                        .append(sqlRequestDto.getTableName())
                        .append(" SET ");

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
                logger.debug("Building DELETE query.");
                queryBuilder.append("DELETE FROM ").append(sqlRequestDto.getTableName());

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
                logger.debug("Building UPSERT query.");
                queryBuilder.append("INSERT INTO ")
                        .append(sqlRequestDto.getTableName())
                        .append(" (");

                if (sqlRequestDto.getColumns() != null && !sqlRequestDto.getColumns().isEmpty()) {
                    queryBuilder.append(String.join(", ", sqlRequestDto.getColumns()));
                    queryBuilder.append(") VALUES ");
                }

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
                logger.error("Unsupported operation: {}", sqlRequestDto.getOperation());
                throw new UnsupportedOperationException("Unsupported operation: " + sqlRequestDto.getOperation());
        }

        return queryBuilder.toString();
    }

    public CompletableFuture<Object> fallbackExecuteGenericQuery(String query, DBType dbType, Throwable throwable) {
        String errorMessage = String.format("Service temporarily unavailable for DBType: %s. Cause: %s", dbType, throwable.getMessage());
        logger.error("Fallback triggered for DBType: {} due to: {}", dbType, throwable.getMessage());
        throw new FallbackException(errorMessage, HttpStatus.SERVICE_UNAVAILABLE);
    }

    private String enforceQueryLimit(String query) {
        String currentDbKey = DataSourceContextHolder.getDataSourceKey().toLowerCase();
        String limitPropertyKey = "datasource." + currentDbKey + ".query.limit";
        int maxRecords = Integer.parseInt(env.getProperty(limitPropertyKey, "100"));  // Default to 100 if not set

        // Check if it's a SELECT query
        if (query.trim().toUpperCase().startsWith("SELECT")) {
            // Convert to uppercase for consistent comparison
            String upperCaseQuery = query.toUpperCase();

            // Check if there's already a LIMIT clause
            int limitIndex = upperCaseQuery.lastIndexOf("LIMIT");

            if (limitIndex == -1) {
                // If there's no LIMIT clause, append it
                return query + " LIMIT " + maxRecords;
            } else {
                // Extract the current LIMIT value
                String queryAfterLimit = query.substring(limitIndex);
                String[] limitParts = queryAfterLimit.split("\\s+");

                if (limitParts.length >= 2) {
                    try {
                        // Parse the limit value
                        int currentLimit = Integer.parseInt(limitParts[1].trim());

                        // If the current limit is greater than maxRecords, replace it
                        if (currentLimit > maxRecords) {
                            return query.substring(0, limitIndex) + " LIMIT " + maxRecords;
                        }
                    } catch (NumberFormatException e) {
                        // If parsing the limit fails, we can enforce maxRecords as a fallback
                        return query.substring(0, limitIndex) + " LIMIT " + maxRecords;
                    }
                }
            }
        }

        // Return the original query if it's not a SELECT or no limit needs to be enforced
        return query;
    }
}
