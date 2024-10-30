package us.hyalen.mysql_proxy.core.service;

import us.hyalen.mysql_proxy.config.enums.DBType;
import us.hyalen.mysql_proxy.core.dto.ResponseDto;
import us.hyalen.mysql_proxy.core.dto.SQLRequestDto;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public interface GenericQueryService {
    CompletableFuture<ResponseDto<Object>> executeGenericQuery(String query, DBType dbType);
    CompletableFuture<ResponseDto<Object>> executeGenericQuery(SQLRequestDto sqlRequestDto, DBType dbType);
}
