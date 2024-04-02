package dev.runtian.helpcommunity.commons.exception;

import dev.runtian.helpcommunity.commons.general.ErrorCode;

/**
 * 自定义异常类，异常实例模板
 * 重载构造方法方便使用
 */
public class BusinessException extends RuntimeException {

    /**
     * 错误码，配合 enum ErrorCode 使用
     */
    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

    public int getCode() {
        return code;
    }
}
