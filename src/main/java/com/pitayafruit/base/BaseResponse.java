package com.pitayafruit.base;

import lombok.Data;
import org.springframework.lang.Nullable;

/**
 * 基础返回类.
 */
@Data
public class BaseResponse<T> {

    private int code;
    private String msg;
    private T data;

    public BaseResponse() {

    }

    public BaseResponse(@Nullable T data) {
        this.code = 0;
        this.msg = "success";
        this.data = data;
    }

    public BaseResponse(String message, @Nullable T data) {
        this.code = 1;
        this.msg = message;
        this.data = data;
    }



}
