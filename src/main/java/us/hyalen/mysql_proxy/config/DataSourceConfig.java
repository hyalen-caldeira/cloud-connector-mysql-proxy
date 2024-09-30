package us.hyalen.mysql_proxy.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private static final Logger logger = LoggerFactory.getLogger(DataSourceConfig.class);

    @Autowired
    private Environment env;

    @Bean
    public DataSource dynamicDataSource() {
        logger.info("Starting dynamicDataSource initialization...");

        Map<Object, Object> dataSourceMap = new HashMap<>();

        // Get all the data source keys defined in the application.properties
        String[] dataSourceKeys = env.getProperty("datasource.keys", "").split(",");

        logger.info("Found {} data sources to initialize.", dataSourceKeys.length);

        for (String key : dataSourceKeys) {
            // Trim and skip empty keys
            key = key.trim();
            if (!key.isEmpty()) {
                try {
                    logger.info("Initializing data source for key: {}", key);

                    // Use HikariCP for each data source
                    HikariConfig hikariConfig = new HikariConfig();
                    String url = env.getProperty("datasource." + key + ".url");
                    String username = env.getProperty("datasource." + key + ".username");
                    String password = env.getProperty("datasource." + key + ".password");
                    String driverClassName = env.getProperty("datasource." + key + ".driver-class-name");

                    logger.debug("Setting JDBC URL: {}", url);
                    logger.debug("Setting Username: {}", username);
                    logger.debug("Setting Driver Class: {}", driverClassName);

                    hikariConfig.setJdbcUrl(url);
                    hikariConfig.setUsername(username);
                    hikariConfig.setPassword(password);
                    hikariConfig.setDriverClassName(driverClassName);

                    // Optional HikariCP configurations (tuning)
                    hikariConfig.setMaximumPoolSize(10);
                    hikariConfig.setMinimumIdle(2);
                    hikariConfig.setIdleTimeout(30000); // 30 seconds
                    hikariConfig.setConnectionTimeout(30000); // 30 seconds

                    logger.info("Creating HikariCP DataSource for key: {}", key);
                    HikariDataSource hikariDataSource = new HikariDataSource(hikariConfig);

                    // Add the data source to the map with the key
                    dataSourceMap.put(key.toUpperCase(), hikariDataSource);
                    logger.info("Successfully initialized data source for key: {}", key);

                } catch (Exception e) {
                    logger.error("Failed to initialize datasource for key: {}", key, e);
                }
            }
        }

        logger.info("Creating routingDataSource with {} data sources.", dataSourceMap.size());

        // Create a RoutingDataSource to switch between the data sources
        AbstractRoutingDataSource routingDataSource = new AbstractRoutingDataSource() {
            @Override
            protected Object determineCurrentLookupKey() {
                String currentDbKey = DataSourceContextHolder.getDataSourceKey();
                logger.debug("Determining current data source key " + currentDbKey);

                if (currentDbKey == null) {
                    logger.warn("No data source key is set, returning null.");
                }
                return currentDbKey;
            }
        };

        routingDataSource.setTargetDataSources(dataSourceMap);

        // Set a default data source if necessary
        if (dataSourceMap.containsKey("H2_DEV")) {
            logger.info("Setting default data source to H2_DEV");
            routingDataSource.setDefaultTargetDataSource(dataSourceMap.get("H2_DEV"));
        } else {
            logger.warn("No default data source found in the map. Default data source will not be set.");
        }

        routingDataSource.afterPropertiesSet(); // Ensure routingDataSource is correctly initialized

        logger.info("dynamicDataSource initialization completed.");

        return routingDataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate() {
        logger.info("Creating JdbcTemplate using dynamicDataSource.");
        return new JdbcTemplate(dynamicDataSource());
    }
}
