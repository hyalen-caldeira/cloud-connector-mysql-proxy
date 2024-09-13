package us.hyalen.mysql_proxy.core.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import us.hyalen.mysql_proxy.model.BaseModel;

import java.io.Serializable;

/**
 * Base Data Transfer Object
 * <p>Note: Rule of thumb: Every model <b>MUST</b> inherit from the Dto. It will give us a common base that will
 * help in future in injecting common functionality as well as in building non-intrusive logics such as aspects</p>
 */
@JsonSerialize
public abstract class Dto extends BaseModel implements Serializable {

}