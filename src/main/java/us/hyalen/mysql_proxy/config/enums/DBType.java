package us.hyalen.mysql_proxy.config.enums;

public enum DBType {
    MYSQL,
    SINGLESTORE_DEV,
    SINGLESTORE_QA;

    public static DBType fromString(String dbType) {
        try {
            return DBType.valueOf(dbType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new UnsupportedOperationException("Unsupported database type: " + dbType);
        }
    }
}