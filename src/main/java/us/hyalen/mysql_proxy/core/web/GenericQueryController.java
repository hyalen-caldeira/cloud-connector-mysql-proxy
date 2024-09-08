package us.hyalen.mysql_proxy.core.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import us.hyalen.mysql_proxy.core.dto.GenericQueryRequestDto;
import us.hyalen.mysql_proxy.core.dto.ResponseDto;
import us.hyalen.mysql_proxy.core.service.GenericQueryService;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/api/generic-query")
public class GenericQueryController {
    // instantiate the service
    private final GenericQueryService service;

    // constructor
    public GenericQueryController(GenericQueryService genericQueryService) {
        service = genericQueryService;
    }

    @GetMapping("/execute-query")
    public ResponseEntity<ResponseDto<Object>> executeQuery(@RequestParam String query) {
        var list = service.executeGenericQuery(query);

        return ok(ResponseDto.forSuccess(list));
    }

    @PostMapping("/execute-query")
    public ResponseEntity<ResponseDto<Object>> executeQuery(@RequestBody GenericQueryRequestDto request) {
        var list = service.executeGenericQuery(request.getQuery());

        return ok(ResponseDto.forSuccess(list));
    }
}

