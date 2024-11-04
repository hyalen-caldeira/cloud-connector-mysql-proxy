package us.hyalen.mysql_proxy.core.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import us.hyalen.mysql_proxy.config.DataSourceContextHolder;
import us.hyalen.mysql_proxy.config.ErrorCodeConfig;
import us.hyalen.mysql_proxy.config.enums.DBType;
import us.hyalen.mysql_proxy.core.FallbackException;
import us.hyalen.mysql_proxy.core.ResourceNotFoundException;
import us.hyalen.mysql_proxy.core.dto.ErrorDto;
import us.hyalen.mysql_proxy.core.dto.ResponseDto;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

@Service
public class GenericQueryServiceImpl implements GenericQueryService {
    private final ErrorCodeConfig errorCodeConfig;
    private static final Logger logger = LoggerFactory.getLogger(GenericQueryServiceImpl.class);
    private final JdbcTemplate jdbcTemplate;
    @Autowired
    private Environment env;

    public GenericQueryServiceImpl(ErrorCodeConfig errorCodeConfig, JdbcTemplate jdbcTemplate) {
        this.errorCodeConfig = errorCodeConfig;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @CircuitBreaker(
            name = "#root.args[1].name()",
            fallbackMethod = "fallbackExecuteGenericQuery")
    @TimeLimiter(name = "#root.args[1].name()") // Dynamic TimeLimiter based on DBType
    public CompletableFuture<ResponseDto<Object>> executeGenericQuery(String query, DBType dbType) {
        logger.info("Executing query: {}", query);
        logger.debug("Using DB type: {}", dbType);

        return CompletableFuture.supplyAsync(() -> {
            // Set the data source key dynamically based on DBType
            DataSourceContextHolder.setDataSourceKey(dbType.name());
            logger.debug("Data source key set to: {}", dbType.name());

            String normalizedQuery = query.trim();
            normalizedQuery = enforceQueryLimit(normalizedQuery);
            logger.debug("Normalized query: {}", normalizedQuery);

            try {
                if (normalizedQuery.toUpperCase().startsWith("SELECT")) {
                    logger.info("Executing SELECT query...");
                    List<Map<String, Object>> result = jdbcTemplate.queryForList(normalizedQuery);
                    logger.debug("Query result: {}", result);

                    if (result.isEmpty()) {
                        logger.warn("No data found for the query: {}", normalizedQuery);
                        throw new ResourceNotFoundException("No data found for the query: " + normalizedQuery);
                    }

                    logger.info("SELECT query executed successfully.");
                    return ResponseDto.forSuccess(result);
                } else if (normalizedQuery.toUpperCase().startsWith("UPDATE") ||
                        normalizedQuery.toUpperCase().startsWith("DELETE") ||
                        normalizedQuery.toUpperCase().startsWith("INSERT")) {
                    logger.info("Executing DML query (UPDATE/DELETE/INSERT)...");
                    int rowsAffected = jdbcTemplate.update(normalizedQuery);
                    logger.debug("Rows affected: {}", rowsAffected);
                    return ResponseDto.forSuccess(rowsAffected);
                } else if (normalizedQuery.toUpperCase().startsWith("CALL")) {
                    logger.info("Executing stored procedure call using CallableStatement...");
                    return executeStoredProcedure(normalizedQuery);
                } else {
                    throw new BadSqlGrammarException("Unsupported query type", normalizedQuery, new SQLException("Unsupported query type"));
                }
            } catch (ResourceNotFoundException | BadSqlGrammarException ex) {
                logger.error("Caught exception: {}", ex.getMessage());
                return ResponseDto.forError(createErrorDto(ex));
            } finally {
                logger.debug("Clearing data source key to avoid affecting other operations.");
                DataSourceContextHolder.clearDataSourceKey();
            }
        });
    }

    // Add logs and error handling for stored procedure execution
    private ResponseDto<Object> executeStoredProcedure(String query) {
        logger.debug("Executing stored procedure: {}", query);
        return jdbcTemplate.execute((Connection conn) -> {
            try (CallableStatement callableStatement = conn.prepareCall(query)) {

                int inputParamCount = countInputParameters(query);
                int outputCursorCount = countOutputParameters(query);
                logger.debug("Total input {} output {} parameters count.", inputParamCount, outputCursorCount);

                for (int i = 1; i <= outputCursorCount; i++) {
                    logger.debug("Registering output parameter at index: {}", i);
                    callableStatement.registerOutParameter(i, Types.REF_CURSOR); // Oracle-specific type
                }

                logger.info("Executing stored procedurea after setting output parameters and before executing: {}", query);
                callableStatement.execute();

                logger.info("Stored procedure executed successfully.");

                List<List<Map<String, Object>>> allResults = new ArrayList<>();

                // Iterate through the output parameters to retrieve each cursor
                for (int i = 1; i <= outputCursorCount; i++) {
                    logger.debug("Getting output parameter at index: {}", i);

                    try (ResultSet resultSet = (ResultSet) callableStatement.getObject(i)) {
                        if (resultSet != null) {
                            logger.debug("Found ResultSet for cursor at index: {}", i);
                            List<Map<String, Object>> result = mapResultSet(resultSet);
                            logger.debug("Stored procedure result for cursor {}: {}", i, result);

                            if (result.isEmpty()) {
                                logger.warn("No data found for the procedure call cursor at index: {}", i);
                            }

                            allResults.add(result);
                        } else {
                            logger.warn("No ResultSet found for cursor at index: {}", i);
                        }
                    }
                }

                // Check if we have any data at all
                if (allResults.isEmpty()) {
                    logger.warn("No data found for the procedure call: {}", query);
                    return ResponseDto.forError(createErrorDto(new ResourceNotFoundException("No data found for the procedure call: " + query)));
                }

                logger.info("Stored procedure executed successfully with results.");
                return ResponseDto.forSuccess(allResults);

            } catch (SQLException e) {
                logger.error("Error executing stored procedure: {}", e.getMessage());
                throw new RuntimeException("Error executing stored procedure", e);
            }
        });
    }

    private int countInputParameters(String query) {
        // Assuming input parameters are those provided directly in the procedure call.
        int startIndex = query.indexOf('(');
        int endIndex = query.indexOf(')');

        if (startIndex != -1 && endIndex != -1) {
            String paramString = query.substring(startIndex + 1, endIndex);
            String[] params = paramString.split(",");
            int inputParamCount = 0;

            for (String param : params) {
                param = param.trim();
                // Assuming '?' represents output params, others are input params
                if (!param.equals("?")) {
                    inputParamCount++;
                }
            }

            return inputParamCount;
        }
        return 0;
    }
    
    private int countOutputParameters(String query) {
        return query.length() - query.replace("?", "").length();
    }

    private List<Map<String, Object>> mapResultSet(ResultSet resultSet) throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();


        logger.debug("Mapping result set with {} columns", columnCount);
        while (resultSet.next()) {
            logger.debug("Mapping row...");
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                row.put(metaData.getColumnName(i), resultSet.getObject(i));
            }
            logger.debug("Mapped row: {}", row);
            result.add(row);
        }

        logger.debug("Mapped result set with {} rows", result.size());
        return result;
    }

    public CompletableFuture<Object> fallbackExecuteGenericQuery(String query, DBType dbType, Throwable throwable) throws Exception {
        String message = throwable.getMessage();

        String errorMessage = String.format("Service temporarily unavailable for DBType: %s. Cause: %s", dbType, message);
        logger.error("Fallback triggered for DBType: {} due to: {}", dbType, message);
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

    private ErrorDto createErrorDto(Exception ex) {
        String errorCode;
        String errorMessage;

        if (ex instanceof ResourceNotFoundException) {
            errorCode = errorCodeConfig.getNotFoundCode();
            errorMessage = errorCodeConfig.getNotFoundMessage();
        } else if (ex instanceof BadSqlGrammarException) {
            errorCode = errorCodeConfig.getBadSqlGrammarCode();
            errorMessage = errorCodeConfig.getBadSqlGrammarMessage();
        } else if (ex instanceof TimeoutException) {
            errorCode = errorCodeConfig.getTimeoutErrorCode();
            errorMessage = errorCodeConfig.getTimeoutErrorMessage();
        } else {
            errorCode = errorCodeConfig.getGlobalErrorCode();
            errorMessage = errorCodeConfig.getGlobalErrorMessage();
        }

        return new ErrorDto(errorCode, errorMessage, ex.getMessage());
    }
}
