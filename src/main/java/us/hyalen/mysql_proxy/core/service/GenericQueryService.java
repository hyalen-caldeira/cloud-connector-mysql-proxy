package us.hyalen.mysql_proxy.core.service;

import us.hyalen.mysql_proxy.config.enums.DBType;
import us.hyalen.mysql_proxy.core.dto.SQLRequestDto;

public interface GenericQueryService {
    Object executeGenericQuery(String query, DBType dbType);
    Object executeGenericQuery(SQLRequestDto sqlRequestDto, DBType dbType);
}
