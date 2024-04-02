package dev.runtian.helpcommunity.common;

/**
 * 返回/响应/结果工具类
 * 给定数据或者错误,返回响应体
 * 主要用于业务层和控件层的 return
 */
public class ResultUtils {

    /**
     * 成功
     * 直接给响应数据,构造一个成功的请求体
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0, data, "ok");
    }

    /**
     * 失败
     * 直接给错误枚举,构造一个失败的响应体
     */
    public static BaseResponse error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }

    /**
     * 失败
     * 给定错误代码和错误信息,返回失败的响应体
     */
    public static BaseResponse error(int code, String message) {
        return new BaseResponse(code, null, message);
    }

    /**
     * 失败
     * 给定错误的枚举值,覆盖里面的错误信息
     */
    public static BaseResponse error(ErrorCode errorCode, String message) {
        return new BaseResponse(errorCode.getCode(), null, message);
    }
}
