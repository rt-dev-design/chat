package dev.runtian.helpcommunity.mainpart.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import dev.runtian.helpcommunity.commons.constant.UserConstant;
import dev.runtian.helpcommunity.commons.exception.BusinessException;
import dev.runtian.helpcommunity.commons.exception.ThrowUtils;
import dev.runtian.helpcommunity.commons.general.BaseResponse;
import dev.runtian.helpcommunity.commons.general.DeleteRequest;
import dev.runtian.helpcommunity.commons.general.ErrorCode;
import dev.runtian.helpcommunity.commons.general.ResultUtils;
import dev.runtian.helpcommunity.commons.helpcommnet.*;
import dev.runtian.helpcommunity.commons.post.Post;
import dev.runtian.helpcommunity.commons.post.PostQueryRequest;
import dev.runtian.helpcommunity.commons.post.PostVO;
import dev.runtian.helpcommunity.commons.user.User;
import dev.runtian.helpcommunity.innerapi.helpcomment.HelpCommentService;
import dev.runtian.helpcommunity.innerapi.user.UserService;
import dev.runtian.helpcommunity.mainpart.annotation.AuthCheck;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * HelpCommentController，评论接口/控件，用于操作评论资源
 * @RestController: 这个类会实例化为单例 bean
 * @RestController: 返回 JSON 数据，是 REST 接口
 * @RequestMapping("/helpComment"): 在 MVC 中将处理器 bean 注册和映射到 /api/file/*
 * @Slf4j 注入静态属性 log，可直接被调用并打日志
 * 属性：
 * 注入并调用用户服务和评论服务
 *
 */
@RestController
@RequestMapping("/help_omment")
@Slf4j
public class HelpCommentController {

    @Resource
    private HelpCommentService helpCommentService;

    @Resource
    private UserService userService;

    // region 增删改查

    /**
     * 增加评论端点 addHelpComment
     * 参数: this(service), helpCommentAddRequest(DTO), request(Servlet)
     * 返回: 新增评论 id, 或抛异常并返回错误信息
     * 映射: /api/helpComment/add ------ addHelpComment
     * DTO 是 MVC 框架分离出来并传参的，HttpServletRequest 是 Tomcat 容器传下来的
     */
    @PostMapping("/add")
    public BaseResponse<Long> addHelpComment(
            @RequestBody HelpCommentAddRequest helpCommentAddRequest,
            HttpServletRequest request
    ) {
        // 1、先做一些一般性的校验
        // 1）先做一些请求参数和数据的轻校验
        // 有没有请求体，有的话请求体的值是否合法
        if (helpCommentAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2）校验用户是否登录并取到用户
        User loginUser = userService.getLoginUser(request);
        // 2、开始有关业务的判断校验：
        // 根据请求 DTO 构造实体实例
        HelpComment helpComment = new HelpComment();
        // 工具类做的事：对实体中的属性，若 DTO 中存在同类型同名属性，那么将 DTO 中的复制过去
        BeanUtils.copyProperties(helpCommentAddRequest, helpComment);
        // 调用业务校验评论
        helpCommentService.validHelpComment(helpComment, true);
        // 这些和用户有关的属性和上面的校验无关
        helpComment.setUserId(loginUser.getId());
        helpComment.setThumbNum(0);
        // 3、进行正式的添加操作，并构造响应和返回
        boolean result = helpCommentService.save(helpComment);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 若存储成功，helpComment 中的 id 会被赋值，这是 MybatisPlus 做的
        long newHelpCommentId = helpComment.getId();
        return ResultUtils.success(newHelpCommentId);
    }

    /**
     * 删除评论端点 deleteHelpComment
     * 参数: this(service), deleteRequest(DTO), request(Servlet)
     * 返回: 删除是否成功的 boolean, 或抛异常并返回错误信息
     * 映射: /api/helpComment/delete ------ deleteHelpComment
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteHelpComment(
            @RequestBody DeleteRequest deleteRequest,
            HttpServletRequest request
    ) {
        // 1、先做一些一般性的校验
        // 1）先做一些请求参数和数据的轻校验
        // 有没有请求体，有的话请求体的值是否合法
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2）校验用户是否登录并取到用户
        User user = userService.getLoginUser(request);
        // 2、开始有关业务的判断校验：
        // 1）先判断评论是否存在
        long id = deleteRequest.getId();
        HelpComment oldHelpComment = helpCommentService.getById(id);
        ThrowUtils.throwIf(oldHelpComment == null, ErrorCode.NOT_FOUND_ERROR);
        // 2）然后确保仅本人或管理员可删除评论
        if (!oldHelpComment.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 3、进行正式的删除操作，并构造响应和返回
        return ResultUtils.success(helpCommentService.deleteHelpCommentAndCommentImagesByHelpCommentId(id));
    }

    @PostMapping("/delete-logically")
    public BaseResponse<Boolean> deleteHelpCommentLogically(
            @RequestBody DeleteRequest deleteRequest,
            HttpServletRequest request
    ) {
        // 1、先做一些一般性的校验
        // 1）先做一些请求参数和数据的轻校验
        // 有没有请求体，有的话请求体的值是否合法
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2）校验用户是否登录并取到用户
        User user = userService.getLoginUser(request);
        // 2、开始有关业务的判断校验：
        // 1）先判断评论是否存在
        long id = deleteRequest.getId();
        HelpComment oldHelpComment = helpCommentService.getById(id);
        ThrowUtils.throwIf(oldHelpComment == null, ErrorCode.NOT_FOUND_ERROR);
        // 2）然后确保仅本人或管理员可删除评论
        if (!oldHelpComment.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 3、进行正式的删除操作，并构造响应和返回
        return ResultUtils.success(helpCommentService.removeById(id));
    }

    /**
     * 更新评论端点 updateHelpComment, 仅管理员可用
     * 参数: this(service), deleteRequest(DTO)
     * 返回: 更新是否成功的 boolean, 或抛异常并返回错误信息
     * 映射: /api/helpComment/update ------ updateHelpComment
     * 这个具体的 @AuthCheck 注解实例会保证用户已经登录且是管理员
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateHelpComment(
            @RequestBody HelpCommentUpdateRequest helpCommentUpdateRequest
    ) {
        // 1、先做一些一般性的校验
        // 1）先做一些请求参数和数据的轻校验
        // 有没有请求体，有的话请求体的值是否合法
        if (helpCommentUpdateRequest == null || helpCommentUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2、开始有关业务的判断校验：
        // 1）先调业务中的通用评论校验方法校验参数
        // 2）判断评论是否存在
        // 将 2）放在后面是因为 2）需要查数据库
        HelpComment helpComment = new HelpComment();
        BeanUtils.copyProperties(helpCommentUpdateRequest, helpComment);
        // 参数校验
        helpCommentService.validHelpComment(helpComment, false);
        long id = helpCommentUpdateRequest.getId();
        // 判断是否存在
        HelpComment oldHelpComment = helpCommentService.getById(id);
        ThrowUtils.throwIf(oldHelpComment == null, ErrorCode.NOT_FOUND_ERROR);
        // 3、进行正式的更新操作，并构造响应和返回
        boolean result = helpCommentService.updateById(helpComment);
        return ResultUtils.success(result);
    }

    /**
     * 查询一个评论视图端点 getHelpCommentVOById
     * 参数: this(service), id, request
     * 返回: 评论视图, 或抛异常并返回错误信息
     * 映射: /api/helpComment/get/vo ------ getHelpCommentVOById
     */
    @GetMapping("/get/vo")
    public BaseResponse<HelpCommentVO> getHelpCommentVOById(
            long id, HttpServletRequest request
    ) {
        // 1、先做一些一般性的校验
        // 1）先做一些请求参数和数据的轻校验
        // 这里不会没有参数，没有参数的话会被框架先拦截掉
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2、直接进行查询，然后判断存在性并构造响应
        HelpComment helpComment = helpCommentService.getById(id);
        if (helpComment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(helpCommentService.getHelpCommentVO(helpComment, request));
    }

    /**
     * 分页获取评论列表端点 listHelpCommentByPage, 仅管理员
     * 参数: this(service), helpCommentQueryRequest(包含分页信息的DTO)
     * 返回: 评论列表页，包含了一些分页信息，或抛异常并返回错误信息
     * 映射: /api/helpComment/list/page ------ listHelpCommentByPage
     * 这个具体的 @AuthCheck 注解实例会保证用户已经登录且是管理员
     * service 的 page 方法由 MyBatisPlus 的 IService 接口提供：
     * 参数0 this, 参数1 page 分页信息, 参数2 queryWrapper 查询语句的其他信息
     * 返回 Page<T>, 数据在 records 里，类型是 T，这个返回值是个 DTO, 由 MyBatisPlus 提供，包含分页信息
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<HelpComment>> listHelpCommentByPage(
            @RequestBody HelpCommentQueryRequest helpCommentQueryRequest
    ) {
        long current = helpCommentQueryRequest.getCurrent();
        long size = helpCommentQueryRequest.getPageSize();
        Page<HelpComment> helpCommentPage = helpCommentService.page(new Page<>(current, size),
                helpCommentService.getQueryWrapper(helpCommentQueryRequest));
        return ResultUtils.success(helpCommentPage);
    }

    /**
     * 分页获取评论视图端点 listHelpCommentVOByPage, 这个是给用户看的
     * 参数: this(service), helpCommentQueryRequest(包含分页信息的DTO), request(Servlet)
     * 返回: 评论列表页，包含了一些分页信息，或抛异常并返回错误信息
     * 映射: /api/helpComment/list/page/vo ------ listHelpCommentVOByPage
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<HelpCommentVO>> listHelpCommentVOByPage(
            @RequestBody HelpCommentQueryRequest helpCommentQueryRequest,
            HttpServletRequest request
    ) {
        long current = helpCommentQueryRequest.getCurrent();
        long size = helpCommentQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<HelpComment> helpCommentPage = helpCommentService.page(new Page<>(current, size),
                helpCommentService.getQueryWrapper(helpCommentQueryRequest));
        return ResultUtils.success(helpCommentService.getHelpCommentVOPage(helpCommentPage, request));
    }

    /**
     * 分页获取当前用户创建的资源列表的端点 listMyHelpCommentVOByPage
     * 参数: this(service), helpCommentQueryRequest(包含分页信息的 DTO), request(Servlet)
     * 返回: 评论视图列表页，包含了一些分页信息，或抛异常并返回错误信息
     * 映射: /api/helpComment/my/list/page/vo ------ listMyHelpCommentVOByPage
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<HelpCommentVO>> listMyHelpCommentVOByPage(
            @RequestBody HelpCommentQueryRequest helpCommentQueryRequest,
            HttpServletRequest request
    ) {
        // 1、先做一些一般性的校验
        // 1）有没有请求体
        if (helpCommentQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2）校验用户是否登录并取到用户
        User loginUser = userService.getLoginUser(request);
        // 3、进行正式的查询操作，并构造响应和返回
        helpCommentQueryRequest.setUserId(loginUser.getId());
        long current = helpCommentQueryRequest.getCurrent();
        long size = helpCommentQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<HelpComment> helpCommentPage = helpCommentService.page(new Page<>(current, size),
                helpCommentService.getQueryWrapper(helpCommentQueryRequest));
        return ResultUtils.success(helpCommentService.getHelpCommentVOPage(helpCommentPage, request));
    }

    // endregion

    /**
     * 分页搜索（从 ES 查询，封装类）
     *
     * @param helpCommentQueryRequest
     * @param request
     * @return
     */
//    @PostMapping("/search/page/vo")
//    public BaseResponse<Page<HelpCommentVO>> searchHelpCommentVOByPage(@RequestBody HelpCommentQueryRequest helpCommentQueryRequest,
//            HttpServletRequest request) {
//        long size = helpCommentQueryRequest.getPageSize();
//        // 限制爬虫
//        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
//        Page<HelpComment> helpCommentPage = helpCommentService.searchFromEs(helpCommentQueryRequest);
//        return ResultUtils.success(helpCommentService.getHelpCommentVOPage(helpCommentPage, request));
//    }

    /**
     * 用户编辑评论的端点 editHelpComment
     * 参数: this(service), helpCommentEditRequest(DTO), request(Servlet)
     * 返回: 编辑是否成功的布尔值，或抛异常并返回错误信息
     * 映射: /api/helpComment/edit ------ editHelpComment
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editHelpComment(
            @RequestBody HelpCommentEditRequest helpCommentEditRequest,
            HttpServletRequest request
    ) {
        // 1、先做一些一般性的校验
        // 1）先做一些请求参数和数据的轻校验
        // 2）校验用户是否登录并取到用户
        if (helpCommentEditRequest == null || helpCommentEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        // 2、开始有关业务的判断校验：
        // 1）先调业务中的通用评论校验方法校验请求中的参数
        // 2）判断评论是否存在
        // 3）校验服务于业务的特殊权限
        HelpComment helpComment = new HelpComment();
        BeanUtils.copyProperties(helpCommentEditRequest, helpComment);
        // 参数校验
        helpCommentService.validHelpComment(helpComment, false);
        long id = helpCommentEditRequest.getId();
        // 判断是否存在
        HelpComment oldHelpComment = helpCommentService.getById(id);
        ThrowUtils.throwIf(oldHelpComment == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldHelpComment.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = helpCommentService.updateById(helpComment);
        return ResultUtils.success(result);
    }

    @PostMapping("/list/page/deleted/vo")
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public BaseResponse<Page<HelpCommentVO>> listDeletedHelpCommentVOByPage(
            @RequestBody HelpCommentQueryRequest helpCommentQueryRequest,
            HttpServletRequest request
    ) {
        long current = helpCommentQueryRequest.getCurrent();
        long size = helpCommentQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<HelpComment> postPage = helpCommentService.selectDeletedCommentByPage(new Page<>(current, size),
                helpCommentService.getQueryWrapper(helpCommentQueryRequest));
        return ResultUtils.success(helpCommentService.getHelpCommentVOPage(postPage, request));
    }
}
