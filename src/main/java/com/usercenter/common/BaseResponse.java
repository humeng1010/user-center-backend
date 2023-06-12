package com.usercenter.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用返回类
 *
 * @param <T> data 数据类型
 * @author humeng
 */
@Data
public class BaseResponse<T> implements Serializable {

    private Integer code;

    private T data;

    private String message;

    public BaseResponse() {
    }

    public BaseResponse(Integer code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public BaseResponse(Integer code, T data) {
        this(code, data, "");
    }


    public static <T> BaseResponse<T> ok(Integer code, T data) {
        return new BaseResponse<>(code, data);
    }

    public static <T> BaseResponse<T> ok(Integer code, T data, String message) {
        return new BaseResponse<>(code, data, message);
    }

    public static <T> BaseResponse<T> error(Integer code, String message) {
        return new BaseResponse<>(code, null, message);
    }

}
