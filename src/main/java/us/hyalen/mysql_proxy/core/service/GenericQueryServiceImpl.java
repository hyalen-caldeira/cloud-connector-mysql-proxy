package us.hyalen.mysql_proxy.core.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import us.hyalen.mysql_proxy.core.ResourceNotFoundException;

import java.util.List;
import java.util.Map;

@Service
public class GenericQueryServiceImpl implements GenericQueryService {
    private final JdbcTemplate jdbcTemplate;

    public GenericQueryServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Object executeGenericQuery(String query) {
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
}
