package dev.runtian.helpcommunity.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import dev.runtian.helpcommunity.annotation.AuthCheck;
import dev.runtian.helpcommunity.common.BaseResponse;
import dev.runtian.helpcommunity.common.DeleteRequest;
import dev.runtian.helpcommunity.common.ErrorCode;
import dev.runtian.helpcommunity.common.ResultUtils;
import dev.runtian.helpcommunity.constant.UserConstant;
import dev.runtian.helpcommunity.exception.BusinessException;
import dev.runtian.helpcommunity.exception.ThrowUtils;
import dev.runtian.helpcommunity.model.dto.file.UploadFileRequest;
import dev.runtian.helpcommunity.model.dto.post.PostAddRequest;
import dev.runtian.helpcommunity.model.dto.post.PostEditRequest;
import dev.runtian.helpcommunity.model.dto.post.PostQueryRequest;
import dev.runtian.helpcommunity.model.dto.post.PostUpdateRequest;
import dev.runtian.helpcommunity.model.entity.Post;
import dev.runtian.helpcommunity.model.entity.User;
import dev.runtian.helpcommunity.model.vo.PostVO;
import dev.runtian.helpcommunity.service.PostService;
import dev.runtian.helpcommunity.service.UserService;
import java.util.List;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * PostController，贴子接口/控件，用于操作贴子资源
 * @RestController: 这个类会实例化为单例 bean
 * @RestController: 返回 JSON 数据，是 REST 接口
 * @RequestMapping("/post"): 在 MVC 中将处理器 bean 注册和映射到 /api/file/*
 * @Slf4j 注入静态属性 log，可直接被调用并打日志
 * 属性：
 * 注入并调用用户服务和贴子服务
 *
 */
@RestController
@RequestMapping("/post")
@Slf4j
public class PostController {

    @Resource
    private PostService postService;

    @Resource
    private UserService userService;

    // region 增删改查

    /**
     * 增加贴子端点 addPost
     * 参数: this(service), postAddRequest(DTO), request(Servlet)
     * 返回: 新增贴子 id, 或抛异常并返回错误信息
     * 映射: /api/post/add ------ addPost
     * DTO 是 MVC 框架分离出来并传参的，HttpServletRequest 是 Tomcat 容器传下来的
     */
    @PostMapping("/add")
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public BaseResponse<Long> addPost(
            @RequestBody PostAddRequest postAddRequest,
            HttpServletRequest request
    ) {
        // 1、先做一些一般性的校验
        // 1）先做一些请求参数和数据的轻校验
        // 有没有请求体，有的话请求体的值是否合法
        if (postAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2）校验用户是否登录并取到用户
        User loginUser = userService.getLoginUser(request);
        // 2、开始有关业务的判断校验：
        // 根据请求 DTO 构造实体实例
        Post post = new Post();
        // 工具类做的事：对实体中的属性，若 DTO 中存在同类型同名属性，那么将 DTO 中的复制过去
        BeanUtils.copyProperties(postAddRequest, post);
        // 单独复制一下 tags, 这是上面的工具类不能做的
        List<String> tags = postAddRequest.getTags();
        if (tags != null) {
            post.setTags(JSONUtil.toJsonStr(tags));
        }
        // 调用业务校验贴子
        postService.validPost(post, true);
        // 这些和用户有关的属性和上面的校验无关
        post.setUserId(loginUser.getId());
        post.setFavourNum(0);
        post.setThumbNum(0);
        // 3、进行正式的添加操作，并构造响应和返回
        boolean result = postService.save(post);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 若存储成功，post 中的 id 会被赋值，这是 MybatisPlus 做的
        long newPostId = post.getId();
        return ResultUtils.success(newPostId);
    }

    /**
     * 删除贴子端点 deletePost
     * 参数: this(service), deleteRequest(DTO), request(Servlet)
     * 返回: 删除是否成功的 boolean, 或抛异常并返回错误信息
     * 映射: /api/post/delete ------ deletePost
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePost(
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
        // 1）先判断贴子是否存在
        long id = deleteRequest.getId();
        Post oldPost = postService.getById(id);
        ThrowUtils.throwIf(oldPost == null, ErrorCode.NOT_FOUND_ERROR);
        // 2）然后确保仅本人或管理员可删除贴子
        if (!oldPost.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 3、进行正式的删除操作，并构造响应和返回
        // boolean b = postService.removeById(id);
        return ResultUtils.success(postService.deletePostAndImagesByPostId(id));
    }

    @PostMapping("/delete-post-logically")
    public BaseResponse<Boolean> deletePostLogically(
            @RequestBody DeleteRequest deleteRequest,
            HttpServletRequest request
    ) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        // 2、开始有关业务的判断校验：
        // 1）先判断贴子是否存在
        long id = deleteRequest.getId();
        Post oldPost = postService.getById(id);
        ThrowUtils.throwIf(oldPost == null, ErrorCode.NOT_FOUND_ERROR);
        // 2）然后确保仅本人或管理员可删除贴子
        if (!oldPost.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        return ResultUtils.success(postService.deletePostWithoutDeletingOtherThings(id));
    }

    /**
     * 更新贴子端点 updatePost, 仅管理员可用
     * 参数: this(service), deleteRequest(DTO)
     * 返回: 更新是否成功的 boolean, 或抛异常并返回错误信息
     * 映射: /api/post/update ------ updatePost
     * 这个具体的 @AuthCheck 注解实例会保证用户已经登录且是管理员
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updatePost(
            @RequestBody PostUpdateRequest postUpdateRequest
    ) {
        // 1、先做一些一般性的校验
        // 1）先做一些请求参数和数据的轻校验
        // 有没有请求体，有的话请求体的值是否合法
        if (postUpdateRequest == null || postUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2、开始有关业务的判断校验：
        // 1）先调业务中的通用贴子校验方法校验参数
        // 2）判断贴子是否存在
        // 将 2）放在后面是因为 2）需要查数据库
        Post post = new Post();
        BeanUtils.copyProperties(postUpdateRequest, post);
        List<String> tags = postUpdateRequest.getTags();
        if (tags != null) {
            post.setTags(JSONUtil.toJsonStr(tags));
        }
        // 参数校验
        postService.validPost(post, false);
        long id = postUpdateRequest.getId();
        // 判断是否存在
        Post oldPost = postService.getById(id);
        ThrowUtils.throwIf(oldPost == null, ErrorCode.NOT_FOUND_ERROR);
        // 3、进行正式的更新操作，并构造响应和返回
        boolean result = postService.updateById(post);
        return ResultUtils.success(result);
    }

    /**
     * 查询一个贴子视图端点 getPostVOById
     * 参数: this(service), id, request
     * 返回: 贴子视图, 或抛异常并返回错误信息
     * 映射: /api/post/get/vo ------ getPostVOById
     */
    @GetMapping("/get/vo")
    public BaseResponse<PostVO> getPostVOById(
            long id, HttpServletRequest request
    ) {
        // 1、先做一些一般性的校验
        // 1）先做一些请求参数和数据的轻校验
        // 这里不会没有参数，没有参数的话会被框架先拦截掉
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2、直接进行查询，然后判断存在性并构造响应
        Post post = postService.getById(id);
        if (post == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(postService.getPostVO(post, request));
    }

    /**
     * 分页获取贴子列表端点 listPostByPage, 仅管理员
     * 参数: this(service), postQueryRequest(包含分页信息的DTO)
     * 返回: 贴子列表页，包含了一些分页信息，或抛异常并返回错误信息
     * 映射: /api/post/list/page ------ listPostByPage
     * 这个具体的 @AuthCheck 注解实例会保证用户已经登录且是管理员
     * service 的 page 方法由 MyBatisPlus 的 IService 接口提供：
     * 参数0 this, 参数1 page 分页信息, 参数2 queryWrapper 查询语句的其他信息
     * 返回 Page<T>, 数据在 records 里，类型是 T，这个返回值是个 DTO, 由 MyBatisPlus 提供，包含分页信息
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Post>> listPostByPage(
            @RequestBody PostQueryRequest postQueryRequest
    ) {
        long current = postQueryRequest.getCurrent();
        long size = postQueryRequest.getPageSize();
        Page<Post> postPage = postService.page(new Page<>(current, size),
                postService.getQueryWrapper(postQueryRequest));
        return ResultUtils.success(postPage);
    }

    /**
     * 分页获取贴子视图端点 listPostVOByPage, 这个是给用户看的
     * 参数: this(service), postQueryRequest(包含分页信息的DTO), request(Servlet)
     * 返回: 贴子列表页，包含了一些分页信息，或抛异常并返回错误信息
     * 映射: /api/post/list/page/vo ------ listPostVOByPage
     */
    @PostMapping("/list/page/vo")
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public BaseResponse<Page<PostVO>> listPostVOByPage(
            @RequestBody PostQueryRequest postQueryRequest,
            HttpServletRequest request
    ) {
        long current = postQueryRequest.getCurrent();
        long size = postQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Post> postPage = postService.page(new Page<>(current, size),
                postService.getQueryWrapper(postQueryRequest));
        return ResultUtils.success(postService.getPostVOPage(postPage, request));
    }

    @PostMapping("/list/page/deleted/vo")
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public BaseResponse<Page<PostVO>> listDeletedPostVOByPage(
            @RequestBody PostQueryRequest postQueryRequest,
            HttpServletRequest request
    ) {
        long current = postQueryRequest.getCurrent();
        long size = postQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Post> postPage = postService.selectDeletedPostsByPage(new Page<>(current, size),
                postService.getQueryWrapper(postQueryRequest));
        return ResultUtils.success(postService.getPostVOPage(postPage, request));
    }

    /**
     * 分页获取当前用户创建的资源列表的端点 listMyPostVOByPage
     * 参数: this(service), postQueryRequest(包含分页信息的 DTO), request(Servlet)
     * 返回: 贴子视图列表页，包含了一些分页信息，或抛异常并返回错误信息
     * 映射: /api/post/my/list/page/vo ------ listMyPostVOByPage
     */
    @PostMapping("/my/list/page/vo")
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public BaseResponse<Page<PostVO>> listMyPostVOByPage(
            @RequestBody PostQueryRequest postQueryRequest,
            HttpServletRequest request
    ) {
        // 1、先做一些一般性的校验
        // 1）有没有请求体
        if (postQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2）校验用户是否登录并取到用户
        User loginUser = userService.getLoginUser(request);
        // 3、进行正式的查询操作，并构造响应和返回
        postQueryRequest.setUserId(loginUser.getId());
        long current = postQueryRequest.getCurrent();
        long size = postQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Post> postPage = postService.page(new Page<>(current, size),
                postService.getQueryWrapper(postQueryRequest));
        return ResultUtils.success(postService.getPostVOPage(postPage, request));
    }

    /**
     * 从ES分页搜索
     *
     * postQueryRequest 查询请求DTO实例，提供搜索条件
     * request 请求，主要提供用户信息
     * this
     *
     * 贴子视图页面数据
     *
     * /api/search/page/vo
     */
    @PostMapping("/search/page/vo")
    public BaseResponse<Page<PostVO>> searchPostVOByPage(
            @RequestBody PostQueryRequest postQueryRequest,
            HttpServletRequest request
    ) {
        long size = postQueryRequest.getPageSize();
        // 限制爬虫一次读取太多数据
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 调用ES搜索业务
        Page<Post> postPage = postService.searchFromEs(postQueryRequest);
        // 转换为视图数据，并查询当前用户是否点赞和收藏
        return ResultUtils.success(postService.getPostVOPage(postPage, request));
    }

    /**
     * 用户编辑贴子的端点 editPost
     * 参数: this(service), postEditRequest(DTO), request(Servlet)
     * 返回: 编辑是否成功的布尔值，或抛异常并返回错误信息
     * 映射: /api/post/edit ------ editPost
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editPost(
            @RequestBody PostEditRequest postEditRequest,
            HttpServletRequest request
    ) {
        // 1、先做一些一般性的校验
        // 1）先做一些请求参数和数据的轻校验
        // 2）校验用户是否登录并取到用户
        if (postEditRequest == null || postEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        // 2、开始有关业务的判断校验：
        // 1）先调业务中的通用贴子校验方法校验请求中的参数
        // 2）判断贴子是否存在
        // 3）校验服务于业务的特殊权限
        Post post = new Post();
        BeanUtils.copyProperties(postEditRequest, post);
        List<String> tags = postEditRequest.getTags();
        if (tags != null) {
            post.setTags(JSONUtil.toJsonStr(tags));
        }
        // 参数校验
        postService.validPost(post, false);
        long id = postEditRequest.getId();
        // 判断是否存在
        Post oldPost = postService.getById(id);
        ThrowUtils.throwIf(oldPost == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldPost.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = postService.updateById(post);
        return ResultUtils.success(result);
    }

    @PostMapping("/restore-deleted-post")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> restoreDeletedPost(
            @RequestBody DeleteRequest deleteRequest
    ) {
        if (deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0)
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(postService.restoreDeletedPost(deleteRequest.getId()));
    }
}
