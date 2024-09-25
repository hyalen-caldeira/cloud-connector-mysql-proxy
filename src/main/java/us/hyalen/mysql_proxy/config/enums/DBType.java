package us.hyalen.mysql_proxy.config.enums;

public enum DBType {
    MYSQL_DEV,
    MYSQL_QA,
    MYSQL_PROD,
    SINGLESTORE_DEV,
    SINGLESTORE_QA,
    SINGLESTORE_PROD,
    REDSHIFT_DEV,
    REDSHIFT_QA,
    REDSHIFT_PROD,
    OTHER_DEV,
    OTHER_QA,
    OTHER_PROD;

    public static DBType fromString(String dbType) {
        try {
            return DBType.valueOf(dbType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new UnsupportedOperationException("Unsupported database type: " + dbType);
        }
    }
}