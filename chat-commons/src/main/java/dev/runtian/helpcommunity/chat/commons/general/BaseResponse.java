package dev.runtian.helpcommunity.chat.commons.general;

import java.io.Serializable;
import lombok.Data;

/**
 * 通用返回类
 * 精髓在于重载了各种构造器便于应对各种返回的构造
 * 包括完整的,部分的和错误的
 *
 * @param <T> data 的类型
 */
@Data
public class BaseResponse<T> implements Serializable {

    private int code;

    private T data;

    private String message;

    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public BaseResponse(int code, T data) {
        this(code, data, "");
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }
}
