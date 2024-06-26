package dev.runtian.helpcommunity.mainpart.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import dev.runtian.helpcommunity.commons.exception.BusinessException;
import dev.runtian.helpcommunity.commons.general.BaseResponse;
import dev.runtian.helpcommunity.commons.general.ErrorCode;
import dev.runtian.helpcommunity.commons.general.ResultUtils;
import dev.runtian.helpcommunity.commons.postthumb.PostThumbAddRequest;
import dev.runtian.helpcommunity.commons.user.User;
import dev.runtian.helpcommunity.innerapi.post.PostThumbService;
import dev.runtian.helpcommunity.innerapi.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户点赞帖子接口，控件类，bean
 * 用于点赞和取消点赞
 * 注册到 MVC 的 /api/post_thumb/**
 */
@RestController
@RequestMapping("/post_thumb")
@Slf4j
public class PostThumbController {

    @Resource
    private PostThumbService postThumbService;

    @Resource
    private UserService userService;

    /**
     * 点赞/取消点赞端点 doThumb
     *
     * this(service),
     * postAddRequest(DTO),
     * request(Servlet, user)
     *
     * result 贴子点赞数的变化量
     * 或者抛异常
     *
     * /post_thumb/
     */
    @PostMapping("/")
    public BaseResponse<Integer> doThumb(
            @RequestBody PostThumbAddRequest postThumbAddRequest,
            HttpServletRequest request
    ) {
        // 校验参数和登录状态
        if (postThumbAddRequest == null || postThumbAddRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        final User loginUser = userService.getLoginUser(request);
        // 调业务执行点赞事务
        long postId = postThumbAddRequest.getId();
        int result = postThumbService.doPostThumb(postId, loginUser);
        return ResultUtils.success(result);
    }

}
