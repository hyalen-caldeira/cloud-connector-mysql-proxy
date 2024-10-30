package us.hyalen.mysql_proxy.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import us.hyalen.mysql_proxy.core.BadResponseBodyException;
import us.hyalen.mysql_proxy.core.FallbackException;
import us.hyalen.mysql_proxy.core.ResourceNotFoundException;
import us.hyalen.mysql_proxy.core.dto.ErrorDto;
import us.hyalen.mysql_proxy.core.dto.ResponseDto;
import java.util.concurrent.TimeoutException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    private final ErrorCodeConfig errorCodeConfig;

    public GlobalExceptionHandler(ErrorCodeConfig errorCodeConfig) {
        this.errorCodeConfig = errorCodeConfig;
    }

//    @ResponseBody
//    @ExceptionHandler(ClientException.class)
//    public ResponseEntity<ResponseDto<Void>> clientException(ClientException e) {
//        return new ResponseEntity<>(ResponseDto.forError(e.getErrorDtos()), e.getHttpStatus());
//    }

    @ResponseBody
    @ExceptionHandler(BadResponseBodyException.class)
    public ResponseEntity<ResponseDto<Void>> badResponseBodyException(BadResponseBodyException e) {
        ErrorDto errorDto =
                new ErrorDto(
                        errorCodeConfig.getBadGatewayCode(),
                        errorCodeConfig.getBadGatewayMessage(),
                        e.getMessage()
                );

        return new ResponseEntity<>(
                ResponseDto.forError(errorDto),
                HttpStatus.valueOf(errorCodeConfig.getBadGatewayHttp())
        );
    }

    @ResponseBody
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ResponseDto<Void>> handleResourceNotFoundException(ResourceNotFoundException e, WebRequest request) {
        ErrorDto errorDto =
                new ErrorDto(
                        errorCodeConfig.getNotFoundCode(),
                        errorCodeConfig.getNotFoundMessage(),
                        e.getMessage()
                );

        return new ResponseEntity<>(
                ResponseDto.forError(errorDto),
                HttpStatus.NOT_FOUND);
    }

    @ResponseBody
    @ExceptionHandler(BadSqlGrammarException.class)
    public ResponseEntity<ResponseDto<Void>> badSqlGrammarException(Exception ex, WebRequest request) {
        ErrorDto errorDto =
                new ErrorDto(
                        errorCodeConfig.getBadSqlGrammarCode(),
                        errorCodeConfig.getBadSqlGrammarMessage(),
                        ex.getMessage()
                );

        return new ResponseEntity<>(
                ResponseDto.forError(errorDto),
                HttpStatus.valueOf(errorCodeConfig.getBadSqlGrammarHttp())
        );
    }

    @ResponseBody
    @ExceptionHandler(FallbackException.class)
    public ResponseEntity<ResponseDto<Void>> handleFallbackException(FallbackException e) {
        ErrorDto errorDto = new ErrorDto(
                errorCodeConfig.getFallbackErrorCode(),
                errorCodeConfig.getFallbackErrorMessage(),
                e.getMessage()
        );

        return new ResponseEntity<>(
                ResponseDto.forError(errorDto),
                e.getHttpStatus()
        );
    }

    @ResponseBody
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDto<Void>> handleGlobalException(Exception ex, WebRequest request) {
        ErrorDto errorDto =
                new ErrorDto(
                        errorCodeConfig.getGlobalErrorCode(),
                        errorCodeConfig.getGlobalErrorMessage(),
                        ex.getMessage()
                );

        return new ResponseEntity<>(
                ResponseDto.forError(errorDto),
                HttpStatus.valueOf(errorCodeConfig.getGlobalErrorHttp())
        );
    }

    @ResponseBody
    @ExceptionHandler(TimeoutException.class)
    public ResponseEntity<ResponseDto<Void>> handleTimeoutException(TimeoutException e) {
        ErrorDto errorDto = new ErrorDto(
                errorCodeConfig.getTimeoutErrorCode(),
                errorCodeConfig.getTimeoutErrorMessage(),
                e.getMessage()
        );

        return new ResponseEntity<>(
                ResponseDto.forError(errorDto),
                HttpStatus.valueOf(errorCodeConfig.getTimeoutErrorHttp())
        );
    }
}