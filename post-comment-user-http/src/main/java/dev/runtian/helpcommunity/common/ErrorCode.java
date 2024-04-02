package dev.runtian.helpcommunity.common;

/**
 * 自定义错误码
 *
 * enum 也是类,其写法与类的写法相似
 * 差不多是,声明一些变量,声明和定义构造方法,声明和定义其他的方法
 * 但是 enum 还可以声明并构造一系列常量
 * 以 NAME1(ctorarg1, ctorarg2...), NAME2(...), ...; 的形式
 */
public enum ErrorCode {

    SUCCESS(0, "ok"),
    PARAMS_ERROR(40000, "请求参数错误"),
    NOT_LOGIN_ERROR(40100, "未登录"),
    NO_AUTH_ERROR(40101, "无权限"),
    NOT_FOUND_ERROR(40400, "请求数据不存在"),
    FORBIDDEN_ERROR(40300, "禁止访问"),
    SYSTEM_ERROR(50000, "系统内部异常"),
    OPERATION_ERROR(50001, "操作失败");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 信息
     */
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
