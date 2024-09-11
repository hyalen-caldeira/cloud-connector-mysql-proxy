package us.hyalen.mysql_proxy.config;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import us.hyalen.mysql_proxy.core.BadResponseBodyException;
import us.hyalen.mysql_proxy.core.ClientException;
import us.hyalen.mysql_proxy.core.ResourceNotFoundException;
import us.hyalen.mysql_proxy.core.dto.ErrorDto;
import us.hyalen.mysql_proxy.core.dto.ResponseDto;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    public GlobalExceptionHandler(ErrorCodeConfig errorCodeConfig) {
        this.errorCodeConfig = errorCodeConfig;
    }

    private final ErrorCodeConfig errorCodeConfig;

    @ResponseBody
    @ExceptionHandler(ClientException.class)
    public ResponseEntity<ResponseDto<Void>> clientException(ClientException e) {
        return new ResponseEntity<>(ResponseDto.forError(e.getErrorDtos()), e.getHttpStatus());
    }

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

    // Handle specific exceptions
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
}