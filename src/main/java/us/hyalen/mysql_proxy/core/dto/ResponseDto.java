package us.hyalen.mysql_proxy.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import us.hyalen.mysql_proxy.core.dto.enums.ResponseStatus;

import java.util.Arrays;
import java.util.List;

/**
 * A standard response structure that is proposed as the standard API response model for microservice implementations.
 * It includes 3 fields, namely {@link #status}, {@link #data} and {@link #errors} and the response for a single atomic operation can be
 * constructed with the combination of these 3 fields.
 * <p>
 * Node: Emphasis must be given to the idea of `atomic` operation mentioned above. This Response DTO is mostly
 * recommended for atomic operation where the `status` represents its outcome of being {@code SUCCESS}, {@code ERROR}
 * or {@code PARTIAL} (partially successful.
 * </p>
 * <p>
 * If the service operation is successful and returns a response, the status must be set to `SUCCESS` and data field
 * must be populated.
 * </p>
 * <p><pre>
 * {
 *     "status" : "SUCCESS"
 *     "data" : {
 *         "key1" : "value1",
 *         "key2" : "value2"
 *     }
 * }
 * </pre></p>
 * <p>
 * If the service operation is failed due to an unexpected exception the `status` must indicate `ERROR` and `errors`
 * section must contain the error details (error `code` and the `message`).
 *
 * </p>
 * <p><pre>
 * {
 *     "status" : "ERROR",
 *     "errors" : [{
 *         "code" : "customer.001",
 *         "message" : "Unable to create Customer or Customer already exists"
 *     }]
 * }
 * </pre></p>
 * <p>
 * In certain scenarios, an operation may parially succeed and the consumer must still be responded with the details
 * of the net outcome. In such cases, developers must set `status to `PARTIAL` indicating the partial success.
 * When the `status` is set to `PARTIAL` developer must ensure that enough details are presented to the consumer
 * on both what succeeded and failed.
 * </p>
 * <p><pre>
 * {
 *     "status" : "PARTIAL",
 *     "data" : {
 *         "id" : "001",
 *         "ssn" : "xxx-xx-2121",
 *         "email" : "abc@def.com"
 *     },
 *     "errors" : [{
 *         "code" : "customer.001",
 *         "message" : "Unable to update SSN number"
 *     }]
 * }
 * </pre></p>
 */
@Getter
@Setter
@AllArgsConstructor
@Builder
public class ResponseDto<T> extends Dto {
    private ResponseStatus status;
    private T data;
    private List<ErrorDto> errors;

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
