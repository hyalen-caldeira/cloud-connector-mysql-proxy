package us.hyalen.mysql_proxy.core;

import org.springframework.http.HttpStatus;

public class FallbackException extends RuntimeException {
    private final HttpStatus httpStatus;

    public FallbackException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
