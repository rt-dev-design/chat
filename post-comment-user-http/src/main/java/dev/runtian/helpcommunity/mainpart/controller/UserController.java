package dev.runtian.helpcommunity.mainpart.controller;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import dev.runtian.helpcommunity.commons.constant.UserConstant;
import dev.runtian.helpcommunity.commons.exception.BusinessException;
import dev.runtian.helpcommunity.commons.exception.ThrowUtils;
import dev.runtian.helpcommunity.commons.file.UploadFileRequest;
import dev.runtian.helpcommunity.commons.general.BaseResponse;
import dev.runtian.helpcommunity.commons.general.DeleteRequest;
import dev.runtian.helpcommunity.commons.general.ErrorCode;
import dev.runtian.helpcommunity.commons.general.ResultUtils;
import dev.runtian.helpcommunity.commons.user.*;
import dev.runtian.helpcommunity.commons.utils.HttpClientUtils;
import dev.runtian.helpcommunity.commons.wxminiprogram.WxMiniProgramLoginRequest;
import dev.runtian.helpcommunity.innerapi.user.UserService;
import dev.runtian.helpcommunity.mainpart.annotation.AuthCheck;

import java.util.List;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dev.runtian.helpcommunity.mainpart.config.WxOpenConfig;
import dev.runtian.helpcommunity.mainpart.service.impl.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * UserController，用户接口/控件，用于操作贴子资源
 * @RestController: 这个类会实例化为单例 bean, 返回 JSON 数据，是 REST 接口
 * @RequestMapping("/post"): 在 MVC 中注册并映射到 /api/user/*
 * 属性：注入并调用用户服务和微信用户服务
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private WxOpenConfig wxOpenConfig;

    public static final String WX_LOGIN_URL = "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code";

    /**
     * 用户注册端点 userRegister
     * 参数: this(service), userRegisterRequest(DTO)
     * 返回: 新增用户 id, 或抛异常并返回错误信息
     * 映射: /user/register
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        // 校验请求本身和请求中的数据是否为空，然后调业务
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            return null;
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录端点 userLogin
     * 参数: this(service), userLoginRequest(DTO), request(servlet, 用于传给业务创建session)
     * 返回: 登录前端所需要的用户视图, 或抛异常并返回错误信息
     * 映射: /user/login
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(
            @RequestBody UserLoginRequest userLoginRequest,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request, response);
        return ResultUtils.success(loginUserVO);
    }

    @PostMapping("/login/wx-mini")
    public BaseResponse<LoginUserVO> userLoginByWxMiniProgram(
            HttpServletRequest request,
            @RequestBody WxMiniProgramLoginRequest wxMiniProgramLoginRequest
    ) {
        try {
            if (wxMiniProgramLoginRequest == null)
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            String code = wxMiniProgramLoginRequest.getCode();
            String avatar = wxMiniProgramLoginRequest.getAvatar();
            String nickname = wxMiniProgramLoginRequest.getNickname();
            if (StringUtils.isAnyBlank(code, avatar, nickname))
                throw new BusinessException(ErrorCode.PARAMS_ERROR);

            String wxServiceRequestUrl = String.format(WX_LOGIN_URL,
                    wxOpenConfig.getAppId(),
                    wxOpenConfig.getAppSecret(),
                    code);
            String data = HttpClientUtils.doGet(wxServiceRequestUrl);
            log.info("微信接口服务登录接口 jscode2session 请求结果：" + data);

            //解析返回的json字符串属性	类型	说明
            JSONObject jsonObject = JSONUtil.parseObj(data);
            String openId = (String) jsonObject.get("openid");
            String sessionKey = (String) jsonObject.get("session_key");
            if (StringUtils.isAnyBlank(openId, sessionKey)) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR);
            }
            return ResultUtils.success(userService.userLoginByWxMiniProgram(openId, avatar, nickname, request));
        } catch (Exception e) {
            log.error("UserLoginByWxMiniProgramJsCode error", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "登录失败，系统错误");
        }
    }


    /**
     * 用户注销端点 userLogout
     * 参数: this(service), request(servlet, 用于传给业务创建session)
     * 返回: 注销是否成功的布尔值, 或抛异常并返回错误信息
     * 映射: /user/logout
     * 注意，是 Post
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(user));
    }

    /**
     * 创建用户端点 addUser
     * 参数: this(service), DTO 和 request
     * 返回: 新建用户的 id, 或抛异常并返回错误信息
     * 映射: /user/add
     * 权限校验：@AuthCheck 实例会保证用户已经登录且是管理员
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(
            @RequestBody UserAddRequest userAddRequest,
            HttpServletRequest request
    ) {
        // 1、先做一些一般性的校验
        // 1）请求体非空
        // 2）用户是否登录（注解加代理）
        if (userAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2、无业务校验，直接正式操作
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        // 默认密码 12345678
        String defaultPassword = "12345678";
        String encryptPassword = DigestUtils.md5DigestAsHex((UserServiceImpl.SALT + defaultPassword).getBytes());
        user.setUserPassword(encryptPassword);
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());
    }

    /**
     * 删除用户端点 deleteUser
     * 参数: this(service), DTO 和 request
     * 返回: true, 或抛异常并返回错误信息
     * 映射: /user/delete
     * 权限校验：@AuthCheck 实例会保证用户已经登录且是管理员
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(
            @RequestBody DeleteRequest deleteRequest,
            HttpServletRequest request
    ) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * 更新用户端点 updateUser
     * 参数: this(service), DTO 和 request
     * 返回: true, 或抛异常并返回错误信息
     * 映射: /user/update
     * 权限校验：@AuthCheck 实例会保证用户已经登录且是管理员
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(
            @RequestBody UserUpdateRequest userUpdateRequest,
            HttpServletRequest request
    ) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取用户端点 getUserById, 仅管理员
     * 参数: this(service), id 和 request
     * 返回: user, 或抛异常并返回错误信息
     * 映射: /user/get
     * 权限校验：@AuthCheck 实例会保证用户已经登录且是管理员
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    /**
     * 根据 id 获取用户视图包装
     * 复用获取实体类的方法
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id, HttpServletRequest request) {
        BaseResponse<User> response = getUserById(id, request);
        User user = response.getData();
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 分页获取用户列表端点 listUserByPage, 仅管理员
     * 参数: this(service), userQueryRequest(DTO, 参数可变) 和 request(servlet)
     * 返回: user page, 或抛异常并返回错误信息
     * 映射: /user/list/page
     * 权限校验：@AuthCheck 实例会保证用户已经登录且是管理员
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<User>> listUserByPage(
            @RequestBody UserQueryRequest userQueryRequest,
            HttpServletRequest request
    ) {
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest));
        return ResultUtils.success(userPage);
    }

    /**
     * 分页获取用户封装列表
     * 利用了分业获取实体列表的端点和业务层实体转视图的方法
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest,
            HttpServletRequest request) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = new Page<>(current, size, userPage.getTotal());
        List<UserVO> userVO = userService.getUserVO(userPage.getRecords());
        userVOPage.setRecords(userVO);
        return ResultUtils.success(userVOPage);
    }

    // endregion

    /**
     * 更新个人信息端点 updateMyUser
     * 参数: this(service), userUpdateMyRequest(DTO, 参数可变) 和 request(取用户信息，取 id)
     * 返回: true, 或抛异常并返回错误信息
     * 映射: /user/update/my
     */
    @PostMapping("/update/my")
    public BaseResponse<Boolean> updateMyUser(
            @RequestBody UserUpdateMyRequest userUpdateMyRequest,
            HttpServletRequest request
    ) {
        if (userUpdateMyRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        User user = new User();
        BeanUtils.copyProperties(userUpdateMyRequest, user);
        // 注意写 id
        user.setId(loginUser.getId());
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    @GetMapping("/get/login-or-null")
    public BaseResponse<LoginUserVO> getLoginUserOrNull(
            HttpServletRequest request) {
        User user = userService.getLoginUserPermitNull(request);
        return ResultUtils.success(userService.getLoginUserVO(user));
    }

    @PostMapping("/upload-avatar")
    public BaseResponse<String> uploadAvatar(
            @RequestPart("file") MultipartFile multipartFile,
            UploadFileRequest uploadFileRequest,
            HttpServletRequest request
    ) {
        userService.validateUploadAvatarRequest(multipartFile, uploadFileRequest);
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userService.uploadAvatar(multipartFile, uploadFileRequest, loginUser));
    }

}
