package us.hyalen.mysql_proxy.config.enums;

public enum DBType {
    MYSQL,
    SINGLE_STORE,
    OTHER_DB;  // Add more as needed

    public static DBType fromString(String dbType) {
        try {
            return DBType.valueOf(dbType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new UnsupportedOperationException("Unsupported database type: " + dbType);
        }
    }
}