package us.hyalen.mysql_proxy.core.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.web.bind.annotation.*;
import us.hyalen.mysql_proxy.config.ErrorCodeConfig;
import us.hyalen.mysql_proxy.config.enums.DBType;
import us.hyalen.mysql_proxy.core.ResourceNotFoundException;
import us.hyalen.mysql_proxy.core.dto.ErrorDto;
import us.hyalen.mysql_proxy.core.dto.QueryRequestDto;
import us.hyalen.mysql_proxy.core.dto.ResponseDto;
import us.hyalen.mysql_proxy.core.service.GenericQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/api/generic-query")
public class GenericQueryController {
    private final ErrorCodeConfig errorCodeConfig;
    private static final Logger log = LoggerFactory.getLogger(GenericQueryController.class);
    private final GenericQueryService service;

    public GenericQueryController(ErrorCodeConfig errorCodeConfig, GenericQueryService genericQueryService) {
        this.errorCodeConfig = errorCodeConfig;
        this.service = genericQueryService;
    }

    @GetMapping(value = "/execute-query", produces = "application/json")
    public CompletableFuture<ResponseEntity<? extends ResponseDto<? extends Object>>> executeQuery(
            @RequestParam String query,
            @RequestHeader("DB-Type") DBType dbType) {

        log.info("Received GET request to execute query with DB-Type: {} and query: {}", dbType, query);

        return service.executeGenericQuery(query, dbType)
                .thenApply(responseDto -> {
                    log.debug("ResponseDto received: {}", responseDto);

                    if (responseDto.isError()) {
                        ErrorDto error = responseDto.getError();
                        HttpStatus status = determineHttpStatus(error.getCode());
                        return new ResponseEntity<>(responseDto, status);
                    } else {
                        return new ResponseEntity<>(responseDto, HttpStatus.OK);
                    }
                });
    }

    @PostMapping(value = "/execute-query", produces = "application/json")
    public CompletableFuture<ResponseEntity<ResponseDto<?>>> executeQuery(
            @RequestBody QueryRequestDto queryRequestDto,
            @RequestHeader("DB-Type") DBType dbType) {

        log.info("Received POST request to execute query with DB-Type: {} and query: {}", dbType, queryRequestDto.getQuery());

        return service.executeGenericQuery(queryRequestDto.getQuery(), dbType)
                .thenApply(responseDto -> {
                    log.debug("ResponseDto received: {}", responseDto);

                    if (responseDto.isError()) {
                        ErrorDto error = responseDto.getError();
                        HttpStatus status = determineHttpStatus(error.getCode());
                        return new ResponseEntity<>(responseDto, status);
                    } else {
                        return new ResponseEntity<>(responseDto, HttpStatus.OK);
                    }
                });
    }

    private ErrorDto createErrorDto(Exception ex) {
        String errorCode;
        String errorMessage;

        if (ex instanceof ResourceNotFoundException) {
            errorCode = errorCodeConfig.getNotFoundCode();
            errorMessage = errorCodeConfig.getNotFoundMessage();
        } else if (ex instanceof BadSqlGrammarException) {
            errorCode = errorCodeConfig.getBadSqlGrammarCode();
            errorMessage = errorCodeConfig.getBadSqlGrammarMessage();
        } else if (ex instanceof TimeoutException) {
            errorCode = errorCodeConfig.getTimeoutErrorCode();
            errorMessage = errorCodeConfig.getTimeoutErrorMessage();
        } else {
            errorCode = errorCodeConfig.getGlobalErrorCode();
            errorMessage = errorCodeConfig.getGlobalErrorMessage();
        }

        return new ErrorDto(errorCode, errorMessage, ex.getMessage());
    }

    private HttpStatus determineHttpStatus(String errorCode) {
        // Map error codes to appropriate HTTP status codes
        switch (errorCode) {
            case "1004":
                return HttpStatus.NOT_FOUND;
            case "1005":
                return HttpStatus.BAD_REQUEST;
            default:
                return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }


//    @PostMapping(value = "/execute-query", produces = "application/json")
//    public CompletableFuture<ResponseEntity<ResponseDto<Object>>> executeQuery(
//            @RequestBody SQLRequestDto sqlRequestDto,
//            @RequestHeader("DB-Type") DBType dbType) {
//
//        log.info("Received POST request with SQLRequestDto: {}", sqlRequestDto);
//
//        return service.executeGenericQuery(sqlRequestDto, dbType)
//                .thenApply(result -> {
//                    log.debug("POST request result: {}", result);
//                    return ok().contentType(MediaType.APPLICATION_JSON).body(ResponseDto.forSuccess(result));
//                }).exceptionally(ex -> {
//                    log.error("Error occurred while executing query: {}", ex.getMessage());
//                    // Optionally: rethrow or transform the exception
//                    throw new RuntimeException(ex); // Let the global handler catch it
//                });
//    }
}
