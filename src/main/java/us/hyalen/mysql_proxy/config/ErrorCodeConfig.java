package us.hyalen.mysql_proxy.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

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

    public String getBadRequestCode() {
        return badRequestCode;
    }

    public void setBadRequestCode(String badRequestCode) {
        this.badRequestCode = badRequestCode;
    }

    public Integer getBadRequestHttp() {
        return badRequestHttp;
    }

    public void setBadRequestHttp(Integer badRequestHttp) {
        this.badRequestHttp = badRequestHttp;
    }

    public String getBadRequestMessage() {
        return badRequestMessage;
    }

    public void setBadRequestMessage(String badRequestMessage) {
        this.badRequestMessage = badRequestMessage;
    }

    public String getBadGatewayCode() {
        return badGatewayCode;
    }

    public void setBadGatewayCode(String badGatewayCode) {
        this.badGatewayCode = badGatewayCode;
    }

    public Integer getBadGatewayHttp() {
        return badGatewayHttp;
    }

    public void setBadGatewayHttp(Integer badGatewayHttp) {
        this.badGatewayHttp = badGatewayHttp;
    }

    public String getBadGatewayMessage() {
        return badGatewayMessage;
    }

    public void setBadGatewayMessage(String badGatewayMessage) {
        this.badGatewayMessage = badGatewayMessage;
    }

    public String getInternalServerCode() {
        return internalServerCode;
    }

    public void setInternalServerCode(String internalServerCode) {
        this.internalServerCode = internalServerCode;
    }

    public Integer getIntervalServerHttp() {
        return intervalServerHttp;
    }

    public void setIntervalServerHttp(Integer intervalServerHttp) {
        this.intervalServerHttp = intervalServerHttp;
    }

    public String getInternalServerMessage() {
        return internalServerMessage;
    }

    public void setInternalServerMessage(String internalServerMessage) {
        this.internalServerMessage = internalServerMessage;
    }

    public String getNotFoundCode() {
        return notFoundCode;
    }

    public void setNotFoundCode(String notFoundCode) {
        this.notFoundCode = notFoundCode;
    }

    public Integer getNotFoundHttp() {
        return notFoundHttp;
    }

    public void setNotFoundHttp(Integer notFoundHttp) {
        this.notFoundHttp = notFoundHttp;
    }

    public String getNotFoundMessage() {
        return notFoundMessage;
    }

    public void setNotFoundMessage(String notFoundMessage) {
        this.notFoundMessage = notFoundMessage;
    }

    public String getBadSqlGrammarCode() {
        return badSqlGrammarCode;
    }

    public void setBadSqlGrammarCode(String badSqlGrammarCode) {
        this.badSqlGrammarCode = badSqlGrammarCode;
    }

    public Integer getBadSqlGrammarHttp() {
        return badSqlGrammarHttp;
    }

    public void setBadSqlGrammarHttp(Integer badSqlGrammarHttp) {
        this.badSqlGrammarHttp = badSqlGrammarHttp;
    }

    public String getBadSqlGrammarMessage() {
        return badSqlGrammarMessage;
    }

    public void setBadSqlGrammarMessage(String badSqlGrammarMessage) {
        this.badSqlGrammarMessage = badSqlGrammarMessage;
    }

    public String getGlobalErrorCode() {
        return globalErrorCode;
    }

    public void setGlobalErrorCode(String globalErrorCode) {
        this.globalErrorCode = globalErrorCode;
    }

    public Integer getGlobalErrorHttp() {
        return globalErrorHttp;
    }

    public void setGlobalErrorHttp(Integer globalErrorHttp) {
        this.globalErrorHttp = globalErrorHttp;
    }

    public String getGlobalErrorMessage() {
        return globalErrorMessage;
    }

    public void setGlobalErrorMessage(String globalErrorMessage) {
        this.globalErrorMessage = globalErrorMessage;
    }
}