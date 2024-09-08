package us.hyalen.mysql_proxy.core.dto;

import java.io.Serializable;

public interface ErrorCode extends Serializable {
    String getKey();

    enum Default implements ErrorCode {
        UNMAPPED_ERROR;

        @Override
        public String getKey() {
            return this.name();
        }
    }
}