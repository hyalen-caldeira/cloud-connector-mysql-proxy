package us.hyalen.mysql_proxy.core.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import us.hyalen.mysql_proxy.config.enums.DBType;
import us.hyalen.mysql_proxy.core.ResourceNotFoundException;

import java.util.List;
import java.util.Map;

@Service
public class GenericQueryServiceImpl implements GenericQueryService {
    private final JdbcTemplate mysqlJdbcTemplate;
    private final JdbcTemplate singleStoreJdbcTemplate;
    // private final JdbcTemplate otherJdbcTemplate;

    public GenericQueryServiceImpl(
            @Qualifier("mysqlJdbcTemplate") JdbcTemplate mysqlJdbcTemplate,
            @Qualifier("singleStoreJdbcTemplate") JdbcTemplate singleStoreJdbcTemplate
            //, @Qualifier("otherJdbcTemplate") JdbcTemplate otherJdbcTemplate
    ) {
        this.mysqlJdbcTemplate = mysqlJdbcTemplate;
        this.singleStoreJdbcTemplate = singleStoreJdbcTemplate;
        // this.otherJdbcTemplate = otherJdbcTemplate;
    }

    @Override
    public Object executeGenericQuery(String query, DBType dbType) {
        JdbcTemplate jdbcTemplate = decideJdbcTemplate(dbType);

        query = query.trim().toUpperCase();  // Normalize query

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
    }

    private JdbcTemplate decideJdbcTemplate(DBType dbType) {
        switch (dbType) {
            case MYSQL:
                return mysqlJdbcTemplate;
            case SINGLE_STORE:
                return singleStoreJdbcTemplate;
            // case OTHER:
            //     return otherJdbcTemplate;
            default:
                throw new IllegalArgumentException("Invalid DB type: " + dbType);
        }
    }
}
