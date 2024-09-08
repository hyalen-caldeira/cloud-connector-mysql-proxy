package us.hyalen.mysql_proxy.core.dto;

import us.hyalen.mysql_proxy.model.BaseModel;

/**
 * Base Data Transfer Object
 * <p>Note: Rule of thumb: Every model <b>MUST</b> inherit from the Dto. It will give us a common base that will
 * help in future in injecting common functionality as well as in building non-intrusive logics such as aspects</p>
 */
public abstract class Dto extends BaseModel {

}