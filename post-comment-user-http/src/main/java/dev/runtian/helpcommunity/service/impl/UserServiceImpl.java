package dev.runtian.helpcommunity.service.impl;

import static dev.runtian.helpcommunity.constant.UserConstant.USER_LOGIN_STATE;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.runtian.helpcommunity.common.ErrorCode;
import dev.runtian.helpcommunity.constant.CommonConstant;
import dev.runtian.helpcommunity.constant.FileConstant;
import dev.runtian.helpcommunity.exception.BusinessException;
import dev.runtian.helpcommunity.exception.ThrowUtils;
import dev.runtian.helpcommunity.manager.CosManager;
import dev.runtian.helpcommunity.mapper.UserMapper;
import dev.runtian.helpcommunity.model.dto.file.UploadFileRequest;
import dev.runtian.helpcommunity.model.dto.user.UserQueryRequest;
import dev.runtian.helpcommunity.model.entity.Image;
import dev.runtian.helpcommunity.model.entity.User;
import dev.runtian.helpcommunity.model.enums.FileUploadBizEnum;
import dev.runtian.helpcommunity.model.enums.UserRoleEnum;
import dev.runtian.helpcommunity.model.vo.LoginUserVO;
import dev.runtian.helpcommunity.model.vo.UserVO;
import dev.runtian.helpcommunity.service.ImageService;
import dev.runtian.helpcommunity.service.UserService;
import dev.runtian.helpcommunity.utils.SqlUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户服务实现，bean
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 盐值，混淆密码
     */
    public static final String SALT = "rt's_salt";

    @Resource
    private CosManager cosManager;
    /**
     * 用户注册业务方法 userRegister
     * 本质是一个创建操作
     *
     * this
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     *
     * @return 新用户 id
     * 或者抛异常
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验，先判空，然后逐个进行业务校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        // 2. 进行正式业务操作
        // 如果是增删改，则一般需要先拿锁
        // 新建时，在临界区先进行查询判重是常见操作
        // 然后创建插入对象并且进行正式操作
        synchronized (userAccount.intern()) {
            // 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }
            // 2. 加密
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
            // 3. 插入数据
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            return user.getId();
        }
    }

    /**
     * 用户登录业务方法 userLogin
     * 本质是从主库查询并返回，同时创建或更新 session 库的数据
     *
     * this(IService, ServiceImpl, mappers)
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param request servlet
     *
     * @return 登录后所需的用户视图
     * 或者抛异常
     */
    @Override
    public LoginUserVO userLogin(
            String userAccount,
            String userPassword,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        // 1. 校验，先判空，然后逐个进行业务校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 2. 构造查询进行正式操作
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 3. 记录用户的登录态，第二个操作是一个 createOrUpdate
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }

    /**
     * 微信用户登录业务方法 userLoginByMpOpen
     * 若自己的库还没有这个用户，则创建，同时更新 session
     * 本质是一次 insert if not exists 加上一次 insert or update
     *
     * this(IService, ServiceImpl, mappers)
     * @param wxOAuth2UserInfo 从微信返回的用户信息，由控件请求并传下来
     * @param request servlet
     *
     * @return 登录后所需的用户视图
     * 或者抛异常
     */
    @Override
    public LoginUserVO userLoginByMpOpen(WxOAuth2UserInfo wxOAuth2UserInfo, HttpServletRequest request) {
        String unionId = wxOAuth2UserInfo.getUnionId();
        String mpOpenId = wxOAuth2UserInfo.getOpenid();
        // 单机锁, insert if not exists
        synchronized (unionId.intern()) {
            // 构造语句并查询用户是否已存在
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("unionId", unionId);
            User user = this.getOne(queryWrapper);
            // 被封号，禁止登录
            if (user != null && UserRoleEnum.BAN.getValue().equals(user.getUserRole())) {
                throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "该用户已被封，禁止登录");
            }
            // 用户不存在则创建
            if (user == null) {
                user = new User();
                user.setUnionId(unionId);
                user.setMpOpenId(mpOpenId);
                user.setUserAvatar(wxOAuth2UserInfo.getHeadImgUrl());
                user.setUserName(wxOAuth2UserInfo.getNickname());
                boolean result = this.save(user);
                if (!result) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "登录失败");
                }
            }
            // 记录用户的登录态
            request.getSession().setAttribute(USER_LOGIN_STATE, user);
            return getLoginUserVO(user);
        }
    }

    /**
     * 获取当前用户业务方法 getLoginUser
     * 本质是 2 次查询，一次查 session，一次查主库
     *
     * this(IService, ServiceImpl, mappers)
     * @param request servlet request 若登录则包含用户信息
     *
     * @return user实体
     * 或者抛异常
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先查 session 并判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 然后从数据库查询并校验（追求性能且功能允许的话，可以注释掉这部分，直接走缓存）
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    /**
     * 获取当前用户业务方法 getLoginUserPermitNull，允许未登录用户
     * @return user实体或 null
     * 或者抛异常
     */
    @Override
    public User getLoginUserPermitNull(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            return null;
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        return this.getById(userId);
    }

    /**
     * 当前请求的用户是否为管理员，是一次查询，且是一次缓存查询
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return isAdmin(user);
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

    /**
     * 注销用户业务方法 userLogout
     * 本质是对缓存的 delete if exists
     *
     * this(IService, ServiceImpl, mappers)
     * @param request servlet request 若登录则包含用户信息
     *
     * @return true
     * 或者抛异常
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        if (request.getSession().getAttribute(USER_LOGIN_STATE) == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    /**
     * 数据对象的转换也有业务 bean 来完成
     */
    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVO(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    /**
     * 将查询请求实例动态转为 SQL 语句
     * 重点在于判空并拼接
     */
    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String unionId = userQueryRequest.getUnionId();
        String mpOpenId = userQueryRequest.getMpOpenId();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(StringUtils.isNotBlank(unionId), "unionId", unionId);
        queryWrapper.eq(StringUtils.isNotBlank(mpOpenId), "mpOpenId", mpOpenId);
        queryWrapper.eq(StringUtils.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StringUtils.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.like(StringUtils.isNotBlank(userName), "userName", userName);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public LoginUserVO userLoginByWxMiniProgram(
            String openId,
            String avatar,
            String nickname,
            HttpServletRequest request
    ) {
        if (StringUtils.isAnyBlank(openId, avatar, nickname)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 单机锁, insert if not exists
        synchronized (openId.intern()) {
            // 构造语句并查询用户是否已存在
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("unionId", openId);
            User user = this.getOne(queryWrapper);
            // 被封号，禁止登录
            if (user != null && UserRoleEnum.BAN.getValue().equals(user.getUserRole())) {
                throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "该用户已被封，禁止登录");
            }
            // 用户不存在则创建
            if (user == null) {
                user = new User();
                user.setUserAccount(nickname);
                user.setUserPassword("default");
                user.setUnionId(openId);
                user.setUserAvatar(avatar);
                user.setUserName(nickname);
                boolean result = this.save(user);
                if (!result) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "登录失败");
                }
            }
            // 记录用户的登录态
            request.getSession().setAttribute(USER_LOGIN_STATE, user);
            return getLoginUserVO(user);
        }
    }

    @Override
    public void validateUploadAvatarRequest(MultipartFile multipartFile, UploadFileRequest uploadFileRequest) {
        if (multipartFile == null || uploadFileRequest == null)
            throw new BusinessException(ErrorCode.PARAMS_ERROR);

        FileUploadBizEnum fileUploadBizEnum = FileUploadBizEnum.getEnumByValue(uploadFileRequest.getBiz());
        if (!FileUploadBizEnum.USER_AVATAR.equals(fileUploadBizEnum)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        long fileSize = multipartFile.getSize();
        final long ONE_M = 1024 * 1024L;
        if (fileSize > ONE_M * 10L) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过 10M");
        }

        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        if (!Arrays.asList("jpeg", "jpg", "svg", "png", "webp").contains(fileSuffix)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
        }
    }

    @Override
    public String uploadAvatar(
            MultipartFile multipartFile,
            UploadFileRequest uploadFileRequest,
            User loginUser
    ) {
        // 生成临时文件名和文件信息对象，并创建临时文件，并且用于上传云端
        String uuid = RandomStringUtils.randomAlphanumeric(8);
        String filename = uuid + "-" + multipartFile.getOriginalFilename();
        FileUploadBizEnum fileUploadBizEnum = FileUploadBizEnum.getEnumByValue(uploadFileRequest.getBiz());
        Long userId = loginUser.getId();
        String filepath = String.format("/%s/%s/%s", fileUploadBizEnum.getValue(), userId, filename);
        File file = null;
        try {
            // 上传文件到云端
            file = File.createTempFile(filepath, null);
            multipartFile.transferTo(file);
            cosManager.putObject(filepath, file);
            String url = FileConstant.COS_HOST + filepath;
            boolean result = this.update()
                    .eq("id", loginUser.getId())
                    .set("userAvatar", url)
                    .update();
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "内部错误，头像上传失败");
            // 返回可访问地址
            return url;
        } catch (Exception e) {
            log.error("file upload error, filepath = " + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            if (file != null) {
                // 删除临时文件
                boolean delete = file.delete();
                if (!delete) {
                    log.error("file delete error, filepath = {}", filepath);
                }
            }
        }
    }
}
