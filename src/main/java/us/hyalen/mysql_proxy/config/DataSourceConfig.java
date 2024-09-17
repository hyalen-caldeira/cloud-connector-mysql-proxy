package us.hyalen.mysql_proxy.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DataSourceConfig {

    @Autowired
    private Environment env;

    @Bean
    public DataSource dynamicDataSource() {
        Map<Object, Object> dataSourceMap = new HashMap<>();

        // Get all the data source keys defined in the application.properties
        String[] dataSourceKeys = env.getProperty("datasources.keys", "").split(",");

        for (String key : dataSourceKeys) {
            // Trim and skip empty keys
            key = key.trim();

            if (!key.isEmpty()) {
                // Create the DataSource using the properties
                String url = env.getProperty("datasources." + key + ".url");
                String username = env.getProperty("datasources." + key + ".username");
                String password = env.getProperty("datasources." + key + ".password");
                String driverClassName = env.getProperty("datasources." + key + ".driver-class-name");

                DataSource dataSource = DataSourceBuilder.create()
                        .url(url)
                        .username(username)
                        .password(password)
                        .driverClassName(driverClassName)
                        .build();

                // Add the data source to the map with the key
                dataSourceMap.put(key.toUpperCase(), dataSource);
            }
        }

        // Create a RoutingDataSource to switch between the data sources
        AbstractRoutingDataSource routingDataSource = new AbstractRoutingDataSource() {
            @Override
            protected Object determineCurrentLookupKey() {
                return DataSourceContextHolder.getDataSourceKey();
            }
        };

        routingDataSource.setTargetDataSources(dataSourceMap);

        // Set a default data source if necessary
        if (dataSourceMap.containsKey("MYSQL"))
            routingDataSource.setDefaultTargetDataSource(dataSourceMap.get("MYSQL"));

        routingDataSource.afterPropertiesSet(); // Ensure routingDataSource is correctly initialized

        return routingDataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(dynamicDataSource());
    }
}
