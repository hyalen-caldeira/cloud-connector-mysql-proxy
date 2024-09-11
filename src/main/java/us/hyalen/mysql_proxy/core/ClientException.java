package us.hyalen.mysql_proxy.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import us.hyalen.mysql_proxy.core.dto.ErrorDto;

import java.util.List;

public class ClientException extends RuntimeException {
    private final List<ErrorDto> errorDtos;
    private final HttpStatus httpStatus;

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public ClientException(List<ErrorDto> errorDtos, HttpStatus httpStatus) {
        this.errorDtos = errorDtos;
        this.httpStatus = httpStatus;
    }

    public List<ErrorDto> getErrorDtos() {
        return errorDtos;
    }

    public static ClientException create(List<ErrorDto> errors, HttpStatus httpStatus) {
        switch (httpStatus) {
            case NOT_FOUND:
                return new ClientException(errors, HttpStatus.NOT_FOUND);
            case BAD_REQUEST:
                return new ClientException(errors, HttpStatus.BAD_REQUEST);
            case UNAUTHORIZED:
                return new ClientException(errors, HttpStatus.UNAUTHORIZED);
            default:
                return new ClientException(errors, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}