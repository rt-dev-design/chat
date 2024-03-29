package dev.runtian.helpcommunity.chat.httpgateway.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 开启权限校验拦截器的注解
 * 被注解的方法执行时会先被代理,检验发起请求的用户是否有对应角色
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthCheck {
    /**
     * 必须有某个角色
     */
    String mustRole() default "";
}

