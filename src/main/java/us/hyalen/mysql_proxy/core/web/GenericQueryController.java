package us.hyalen.mysql_proxy.core.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import us.hyalen.mysql_proxy.config.enums.DBType;
import us.hyalen.mysql_proxy.core.dto.ErrorDto;
import us.hyalen.mysql_proxy.core.dto.QueryRequestDto;
import us.hyalen.mysql_proxy.core.dto.ResponseDto;
import us.hyalen.mysql_proxy.core.service.GenericQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/generic-query")
public class GenericQueryController {
    private static final Logger log = LoggerFactory.getLogger(GenericQueryController.class);
    private final GenericQueryService service;

    public GenericQueryController(GenericQueryService genericQueryService) {
        this.service = genericQueryService;
    }

    @GetMapping(value = "/execute-query", produces = "application/json")
    public CompletableFuture<ResponseEntity<ResponseDto<?>>> executeQuery(
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
}