package us.hyalen.mysql_proxy.core.service;

import us.hyalen.mysql_proxy.config.enums.DBType;

public interface GenericQueryService {
    Object executeGenericQuery(String query, DBType dbType);

}
