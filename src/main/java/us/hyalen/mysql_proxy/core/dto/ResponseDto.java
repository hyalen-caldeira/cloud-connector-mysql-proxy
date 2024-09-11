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
    private List<ErrorDto> errors;

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

    public List<ErrorDto> getErrors() {
        return errors;
    }

    public void setErrors(List<ErrorDto> errors) {
        this.errors = errors;
    }

    // Manually implement the builder pattern
    public static class Builder<U> {
        private ResponseStatus status;
        private U data;
        private List<ErrorDto> errors;

        public Builder<U> status(ResponseStatus status) {
            this.status = status;
            return this;
        }

        public Builder<U> data(U data) {
            this.data = data;
            return this;
        }

        public Builder<U> errors(List<ErrorDto> errors) {
            this.errors = errors;
            return this;
        }

        public ResponseDto<U> build() {
            ResponseDto<U> response = new ResponseDto<>();
            response.status = this.status;
            response.data = this.data;
            response.errors = this.errors;
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

    public static <U> ResponseDto<U> forError(ErrorDto... errors) {
        return ResponseDto.<U>builder()
                .status(ResponseStatus.ERROR)
                .errors(Arrays.asList(errors))
                .build();
    }

    public static <U> ResponseDto<U> forError(List<ErrorDto> errors) {
        return forError(errors.toArray(new ErrorDto[errors.size()]));
    }

    public static <U> ResponseDto<U> forPartial(U data, ErrorDto... errors) {
        return ResponseDto.<U>builder()
                .status(ResponseStatus.PARTIAL)
                .data(data)
                .errors(Arrays.asList(errors))
                .build();
    }

    public static <U> ResponseDto<U> forPartial(U data, List<ErrorDto> errors) {
        return forPartial(data, errors.toArray(new ErrorDto[errors.size()]));
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }
}
