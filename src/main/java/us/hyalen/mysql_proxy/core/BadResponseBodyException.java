package us.hyalen.mysql_proxy.core;

public class BadResponseBodyException extends RuntimeException {
    public BadResponseBodyException(String message) {
        super(message);
    }
}