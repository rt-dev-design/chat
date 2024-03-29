package dev.runtian.helpcommunity.chat.httpgateway.aop;

import dev.runtian.helpcommunity.chat.httpgateway.annotation.AuthCheck;
import dev.runtian.helpcommunity.common.ErrorCode;
import dev.runtian.helpcommunity.exception.BusinessException;
import dev.runtian.helpcommunity.model.entity.User;
import dev.runtian.helpcommunity.model.enums.UserRoleEnum;
import dev.runtian.helpcommunity.chat.httpgateway.service.UserService;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 进行权限校验的 AOP 切面,或者说代理,或者说拦截器
 */
@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserService userService;

    /**
     * 对于注解了 @AuthCheck 的方法进行拦截
     * 主要校验是否封号,以及是否有管理员权限
     *
     * @param joinPoint 连接点,即要执行的方法
     * @param authCheck 注解实例,即方法源码上的那个注解信息
     * @return joinPoint, 即原方法的返回值,放行才会这么做
     * @exception BusinessException 校验不通过则根据不同原因抛各种异常
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        // 获取当前登录用户信息
        User loginUser = userService.getLoginUser(request);
        // 必须有权限才通过,否则抛异常
        if (StringUtils.isNotBlank(mustRole)) {
            UserRoleEnum mustUserRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
            if (mustUserRoleEnum == null) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
            String userRole = loginUser.getUserRole();
            // 如果被封号，直接拒绝
            if (UserRoleEnum.BAN.equals(mustUserRoleEnum)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
            // 必须有管理员权限
            if (UserRoleEnum.ADMIN.equals(mustUserRoleEnum)) {
                if (!mustRole.equals(userRole)) {
                    throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
                }
            }
        }
        // 通过权限校验，放行
        return joinPoint.proceed();
    }
}

