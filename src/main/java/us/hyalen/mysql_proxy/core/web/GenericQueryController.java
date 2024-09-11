package us.hyalen.mysql_proxy.core.web;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import us.hyalen.mysql_proxy.core.dto.GenericQueryRequestDto;
import us.hyalen.mysql_proxy.core.dto.ResponseDto;
import us.hyalen.mysql_proxy.core.service.GenericQueryService;

import java.util.List;
import java.util.Map;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/api/generic-query")
public class GenericQueryController {
    private final GenericQueryService service;

    public GenericQueryController(GenericQueryService genericQueryService) {
        service = genericQueryService;
    }

    @GetMapping(value = "/execute-query", produces = "application/json")
    public ResponseEntity<ResponseDto<Object>> executeQuery(@RequestParam String query) {
        Object result = service.executeGenericQuery(query);

        // If result is a list (SELECT query), we assume it's List<Map<String, Object>>
        if (result instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> list = (List<Map<String, Object>>) result;
            return ok(ResponseDto.forSuccess(list));
        }

        return ok(ResponseDto.forSuccess(result));
    }

    @PostMapping(value = "/execute-query", produces = "application/json")
    public ResponseEntity<ResponseDto<Object>> executeQuery(@RequestBody GenericQueryRequestDto request) {
        Object result = service.executeGenericQuery(request.getQuery());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(ResponseDto.forSuccess(result));
    }
}
