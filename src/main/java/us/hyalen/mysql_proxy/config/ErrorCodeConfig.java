package us.hyalen.mysql_proxy.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Data
@Configuration
@ConfigurationProperties(prefix = "standard")
@PropertySource("classpath:i18n/standard-codes.properties")
public class ErrorCodeConfig {
    private String badRequestCode;
    private Integer badRequestHttp;
    private String badRequestMessage;

    private String badGatewayCode;
    private Integer badGatewayHttp;
    private String badGatewayMessage;

    private String internalServerCode;
    private Integer intervalServerHttp;
    private String internalServerMessage;

    private String notFoundCode;
    private Integer notFoundHttp;
    private String notFoundMessage;

    private String badSqlGrammarCode;
    private Integer badSqlGrammarHttp;
    private String badSqlGrammarMessage;

    private String globalErrorCode;
    private Integer globalErrorHttp;
    private String globalErrorMessage;
}