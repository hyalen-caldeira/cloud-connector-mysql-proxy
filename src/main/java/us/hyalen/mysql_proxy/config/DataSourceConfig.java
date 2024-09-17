package us.hyalen.mysql_proxy.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {
    @Value("${mysql.datasource.url}")
    private String mysqlUrl;
    @Value("${mysql.datasource.username}")
    private String mysqlUsername;
    @Value("${mysql.datasource.password}")
    private String mysqlPassword;
    @Value("${mysql.datasource.driver-class-name}")
    private String mysqlDriverClassName;

    @Value("${singlestore.datasource.url}")
    private String singleStoreUrl;

    @Value("${singlestore.datasource.username}")
    private String singleStoreUsername;

    @Value("${singlestore.datasource.password}")
    private String singleStorePassword;

    @Value("${singlestore.datasource.driver-class-name}")
    private String singleStoreDriverClassName;

    @Bean(name = "mysqlDataSource")
    public DataSource mysqlDataSource() {
        return DataSourceBuilder.create()
                .url(mysqlUrl)
                .username(mysqlUsername)
                .password(mysqlPassword)
                .driverClassName(mysqlDriverClassName)
                .build();
    }

    @Bean(name = "singleStoreDataSource")
    public DataSource singlestoreDataSource() {
        return DataSourceBuilder.create()
                .url(singleStoreUrl) // Update with SingleStore DB URL
                .username(singleStoreUsername) // Update with SingleStore username
                .password(singleStorePassword) // Update with SingleStore password
                .driverClassName(singleStoreDriverClassName)
                .build();
    }

    // Bean(name = "otherDataSource")
    // public DataSource otherDataSource() {
    //     return DataSourceBuilder.create()
    //             .url(otherUrl)
    //             .username(otherUsername)
    //             .password(otherPassword)
    //             .driverClassName(otherDriverClassName)
    //             .build();

    @Bean(name = "mysqlJdbcTemplate")
    public JdbcTemplate mysqlJdbcTemplate(@Qualifier("mysqlDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "singleStoreJdbcTemplate")
    public JdbcTemplate singleStoreJdbcTemplate(@Qualifier("singleStoreDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    // @Bean(name = "otherJdbcTemplate")
    // public JdbcTemplate otherJdbcTemplate(@Qualifier("otherDataSource") DataSource dataSource) {
    //     return new JdbcTemplate(dataSource);
    // }
}
