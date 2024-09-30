package us.hyalen.mysql_proxy.core.web;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import us.hyalen.mysql_proxy.config.enums.DBType;
import us.hyalen.mysql_proxy.core.dto.ResponseDto;
import us.hyalen.mysql_proxy.core.dto.SQLRequestDto;
import us.hyalen.mysql_proxy.core.service.GenericQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/api/generic-query")
public class GenericQueryController {
    private static final Logger log = LoggerFactory.getLogger(GenericQueryController.class);
    private final GenericQueryService service;

    public GenericQueryController(GenericQueryService genericQueryService) {
        this.service = genericQueryService;
    }

    @GetMapping(value = "/execute-query", produces = "application/json")
    public CompletableFuture<ResponseEntity<? extends ResponseDto<? extends Object>>> executeQuery(
            @RequestParam String query,
            @RequestHeader("DB-Type") DBType dbType) {

        log.info("Received request to execute query with DB-Type: {}", dbType);

        return service.executeGenericQuery(query, dbType)
                .thenApply(result -> {
                    log.debug("Query result: {}", result);
                    if (result instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> list = (List<Map<String, Object>>) result;
                        return ok().contentType(MediaType.APPLICATION_JSON).body(ResponseDto.forSuccess(list));
                    }

                    return ok().contentType(MediaType.APPLICATION_JSON).body(ResponseDto.forSuccess(result));
                }).exceptionally(ex -> {
                    log.error("Error occurred while executing query: {}", ex.getMessage());
                    // Optionally: rethrow or transform the exception
                    throw new RuntimeException(ex); // Let the global handler catch it
                });
    }

    @PostMapping(value = "/execute-query", produces = "application/json")
    public CompletableFuture<ResponseEntity<ResponseDto<Object>>> executeQuery(
            @RequestBody SQLRequestDto sqlRequestDto,
            @RequestHeader("DB-Type") DBType dbType) {

        log.info("Received POST request with SQLRequestDto: {}", sqlRequestDto);

        return service.executeGenericQuery(sqlRequestDto, dbType)
                .thenApply(result -> {
                    log.debug("POST request result: {}", result);
                    return ok().contentType(MediaType.APPLICATION_JSON).body(ResponseDto.forSuccess(result));
                }).exceptionally(ex -> {
                    log.error("Error occurred while executing query: {}", ex.getMessage());
                    // Optionally: rethrow or transform the exception
                    throw new RuntimeException(ex); // Let the global handler catch it
                });
    }
}
