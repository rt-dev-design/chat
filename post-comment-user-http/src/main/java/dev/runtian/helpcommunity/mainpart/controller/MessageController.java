package dev.runtian.helpcommunity.mainpart.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import dev.runtian.helpcommunity.commons.chat.Chat;
import dev.runtian.helpcommunity.commons.exception.ThrowUtils;
import dev.runtian.helpcommunity.commons.general.BaseResponse;
import dev.runtian.helpcommunity.commons.general.ErrorCode;
import dev.runtian.helpcommunity.commons.general.ResultUtils;
import dev.runtian.helpcommunity.commons.message.Message;
import dev.runtian.helpcommunity.commons.message.MessageQueryRequest;
import dev.runtian.helpcommunity.commons.message.MessageVO;
import dev.runtian.helpcommunity.commons.user.User;
import dev.runtian.helpcommunity.innerapi.chat.MessageService;
import dev.runtian.helpcommunity.innerapi.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/message")
@Slf4j
public class MessageController {

    @DubboReference(check = false)
    MessageService messageService;

    @Resource
    private UserService userService;

    @PostMapping("/message-vos-page")
    public BaseResponse<Page<MessageVO>> listMessageVosOnPage(
            @RequestBody MessageQueryRequest messageQueryRequest,
            HttpServletRequest request
    ) {
        long current = messageQueryRequest.getCurrent();
        long size = messageQueryRequest.getPageSize();
        User loginUser = userService.getLoginUser(request);
        // 限制爬虫
        ThrowUtils.throwIf(size > 40, ErrorCode.PARAMS_ERROR);
        Page<Message> messagePage = messageService.page(new Page<>(current, size),
                messageService.getMessageQueryWrapperFromRequest(messageQueryRequest));
        return ResultUtils.success(messageService.getMessageVOPage(messagePage, loginUser));
    }
}
