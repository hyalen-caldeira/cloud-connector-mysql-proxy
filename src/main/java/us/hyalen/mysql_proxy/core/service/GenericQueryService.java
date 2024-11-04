package us.hyalen.mysql_proxy.core.service;

import us.hyalen.mysql_proxy.config.enums.DBType;
import us.hyalen.mysql_proxy.core.dto.ResponseDto;

import java.util.concurrent.CompletableFuture;

public interface GenericQueryService {
    CompletableFuture<ResponseDto<Object>> executeGenericQuery(String query, DBType dbType);
}
