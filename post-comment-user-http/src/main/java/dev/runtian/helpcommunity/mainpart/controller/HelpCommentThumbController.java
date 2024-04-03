package dev.runtian.helpcommunity.mainpart.controller;

import dev.runtian.helpcommunity.commons.exception.BusinessException;
import dev.runtian.helpcommunity.commons.general.BaseResponse;
import dev.runtian.helpcommunity.commons.general.ErrorCode;
import dev.runtian.helpcommunity.commons.general.ResultUtils;
import dev.runtian.helpcommunity.commons.helpcommnet.HelpCommentThumbAddRequest;
import dev.runtian.helpcommunity.commons.user.User;
import dev.runtian.helpcommunity.innerapi.helpcomment.HelpCommentThumbService;
import dev.runtian.helpcommunity.innerapi.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 用户点赞评论接口，控件类，bean
 * 用于点赞和取消点赞
 * 注册到 MVC 的 /api/help_comment_thumb/**
 */
@RestController
@RequestMapping("/help_comment_thumb")
@Slf4j
public class HelpCommentThumbController {

    @Resource
    private HelpCommentThumbService helpCommentThumbService;

    @Resource
    private UserService userService;

    /**
     * 点赞/取消点赞端点 doThumb
     *
     * this(service),
     * helpCommentAddRequest(DTO),
     * request(Servlet, user)
     *
     * result 评论点赞数的变化量
     * 或者抛异常
     *
     * /helpComment_thumb/
     */
    @PostMapping("/")
    public BaseResponse<Integer> doThumb(
            @RequestBody HelpCommentThumbAddRequest helpCommentThumbAddRequest,
            HttpServletRequest request
    ) {
        // 校验参数和登录状态
        if (helpCommentThumbAddRequest == null || helpCommentThumbAddRequest.getId() == null || helpCommentThumbAddRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        final User loginUser = userService.getLoginUser(request);
        // 调业务执行点赞事务
        long helpCommentId = helpCommentThumbAddRequest.getId();
        int result = helpCommentThumbService.doHelpCommentThumb(helpCommentId, loginUser);
        return ResultUtils.success(result);
    }

}
