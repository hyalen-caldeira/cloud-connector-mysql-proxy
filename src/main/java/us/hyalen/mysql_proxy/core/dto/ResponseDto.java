package us.hyalen.mysql_proxy.core.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import us.hyalen.mysql_proxy.core.dto.enums.ResponseStatus;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

@JsonSerialize
public class ResponseDto<T> extends Dto implements Serializable {
    private static final long serialVersionUID = 1L;
    private ResponseStatus status;
    private T data;
    private ErrorDto error;

    public ResponseStatus getStatus() {
        return status;
    }

    public void setStatus(ResponseStatus status) {
        this.status = status;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public ErrorDto getError() {
        return error;
    }

    public void setError(ErrorDto error) {
        this.error = error;
    }

    /**
     * Checks if the response contains an error.
     * @return true if there is an error, false otherwise.
     */
    public boolean isError() {
        return error != null || ResponseStatus.ERROR.equals(status);
    }

    // Builder pattern implementation
    public static class Builder<U> {
        private ResponseStatus status;
        private U data;
        private ErrorDto error;

        public Builder<U> status(ResponseStatus status) {
            this.status = status;
            return this;
        }

        public Builder<U> data(U data) {
            this.data = data;
            return this;
        }

        public Builder<U> error(ErrorDto error) {
            this.error = error;
            return this;
        }

        public ResponseDto<U> build() {
            ResponseDto<U> response = new ResponseDto<>();
            response.status = this.status;
            response.data = this.data;
            response.error = this.error;
            return response;
        }
    }

    // Static factory methods
    public static <U> Builder<U> builder() {
        return new Builder<>();
    }

    public static <U> ResponseDto<U> forSuccess(U data) {
        return ResponseDto.<U>builder()
                .status(ResponseStatus.SUCCESS)
                .data(data)
                .build();
    }

    public static <U> ResponseDto<U> forError(ErrorDto error) {
        return ResponseDto.<U>builder()
                .status(ResponseStatus.ERROR)
                .error(error)
                .build();
    }

    public static <U> ResponseDto<U> forPartial(U data, ErrorDto error) {
        return ResponseDto.<U>builder()
                .status(ResponseStatus.PARTIAL)
                .data(data)
                .error(error)
                .build();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }
}

