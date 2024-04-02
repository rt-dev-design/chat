package dev.runtian.helpcommunity.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import dev.runtian.helpcommunity.common.BaseResponse;
import dev.runtian.helpcommunity.common.ErrorCode;
import dev.runtian.helpcommunity.common.ResultUtils;
import dev.runtian.helpcommunity.exception.BusinessException;
import dev.runtian.helpcommunity.exception.ThrowUtils;
import dev.runtian.helpcommunity.model.dto.post.PostQueryRequest;
import dev.runtian.helpcommunity.model.dto.postfavour.PostFavourAddRequest;
import dev.runtian.helpcommunity.model.dto.postfavour.PostFavourQueryRequest;
import dev.runtian.helpcommunity.model.entity.Post;
import dev.runtian.helpcommunity.model.entity.User;
import dev.runtian.helpcommunity.model.vo.PostVO;
import dev.runtian.helpcommunity.service.PostFavourService;
import dev.runtian.helpcommunity.service.PostService;
import dev.runtian.helpcommunity.service.UserService;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户收藏贴子接口，控件类，bean
 * 用户收藏和取消收藏
 * 注册到 MVC 的 /api/post_favour/**
 */
@RestController
@RequestMapping("/post_favour")
@Slf4j  // log
public class PostFavourController {

    @Resource
    private PostFavourService postFavourService;

    @Resource
    private PostService postService;

    @Resource
    private UserService userService;

    /**
     * 收藏/取消收藏端点 doPostFavour
     *
     * this + 请求 2 件套，一个是数据/DTO/真请求体, 一个是原始请求主要用于看用户
     *
     * resultNum 收藏变化数
     *
     * /post_favour/
     */
    @PostMapping("/")
    public BaseResponse<Integer> doPostFavour(
            @RequestBody PostFavourAddRequest postFavourAddRequest,
            HttpServletRequest request
    ) {
        // 1、先做一些一般性的校验
        // 1）请求体非空，id值合法
        // 2）用户是否登录，并取到用户
        if (postFavourAddRequest == null || postFavourAddRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        final User loginUser = userService.getLoginUser(request);
        // 2、调业务正式操作
        // 这里没有上面业务上的校验
        long postId = postFavourAddRequest.getId();
        int result = postFavourService.doPostFavour(postId, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 获取我收藏的帖子的列表的端点 listMyFavourPostByPage
     *
     * 参数：this + 请求 2 件套，一个是数据/DTO/真请求体, 一个是原始请求主要用于看用户
     *
     * 返回：当前页我的收藏列表
     *
     * 映射：/post_favour/my/list/page
     */
    @PostMapping("/my/list/page")
    public BaseResponse<Page<PostVO>> listMyFavourPostByPage(
            @RequestBody PostQueryRequest postQueryRequest,
            HttpServletRequest request
    ) {
        // 1、先做一些一般性的校验
        // 1）请求体非空，id值合法
        // 2）用户是否登录，并取到用户
        // 3）限制页面大小以限制爬虫
        if (postQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        long current = postQueryRequest.getCurrent();
        long size = postQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 2、调业务正式操作
        Page<Post> postPage = postFavourService.listFavourPostByPage(new Page<>(current, size),
                postService.getQueryWrapper(postQueryRequest), loginUser.getId());
        return ResultUtils.success(postService.getPostVOPage(postPage, request));
    }

    /**
     * 获取用户收藏的帖子列表端点
     * 和上一个接口是一样的，这个一般给管理员用，或者有其他用处
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<PostVO>> listFavourPostByPage(
            @RequestBody PostFavourQueryRequest postFavourQueryRequest,
            HttpServletRequest request
    ) {
        if (postFavourQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = postFavourQueryRequest.getCurrent();
        long size = postFavourQueryRequest.getPageSize();
        Long userId = postFavourQueryRequest.getUserId();
        ThrowUtils.throwIf(size > 20 || userId == null, ErrorCode.PARAMS_ERROR);
        Page<Post> postPage = postFavourService.listFavourPostByPage(new Page<>(current, size),
                postService.getQueryWrapper(postFavourQueryRequest.getPostQueryRequest()), userId);
        return ResultUtils.success(postService.getPostVOPage(postPage, request));
    }
}
