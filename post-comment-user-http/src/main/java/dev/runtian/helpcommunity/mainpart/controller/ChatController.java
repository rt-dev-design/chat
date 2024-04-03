package dev.runtian.helpcommunity.mainpart.controller;

import co.elastic.clients.elasticsearch.nodes.Http;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import dev.runtian.helpcommunity.commons.chat.Chat;
import dev.runtian.helpcommunity.commons.chat.ChatQueryRequest;
import dev.runtian.helpcommunity.commons.chat.ChatVO;
import dev.runtian.helpcommunity.commons.chat.UpdateLastPresentTimeDTO;
import dev.runtian.helpcommunity.commons.exception.BusinessException;
import dev.runtian.helpcommunity.commons.exception.ThrowUtils;
import dev.runtian.helpcommunity.commons.general.BaseResponse;
import dev.runtian.helpcommunity.commons.general.ErrorCode;
import dev.runtian.helpcommunity.commons.general.IdRequest;
import dev.runtian.helpcommunity.commons.general.ResultUtils;
import dev.runtian.helpcommunity.commons.post.Post;
import dev.runtian.helpcommunity.commons.user.User;
import dev.runtian.helpcommunity.innerapi.chat.ChatService;
import dev.runtian.helpcommunity.innerapi.test.TestChatAndMessageService;
import dev.runtian.helpcommunity.innerapi.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/chat")
@Slf4j
public class ChatController {
    @DubboReference(check = false)
    private ChatService chatService;

    @Resource
    private UserService userService;

    @DubboReference(check = false)
    private TestChatAndMessageService testChatAndMessageService;

    @PostMapping("/unread")
    public BaseResponse<Boolean> checkIfThereIsUnreadForUser(@RequestBody IdRequest idRequest) {
        if (idRequest == null || idRequest.getId() == null || idRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求id不合法");
        }
        return ResultUtils.success(chatService.checkIfThereIsUnreadForUser(idRequest.getId()));
    }

    @PostMapping("/chat-vos-page")
    public BaseResponse<Page<ChatVO>> listChatVosOnPage(
            @RequestBody ChatQueryRequest chatQueryRequest,
            HttpServletRequest request
    ) {
        long current = chatQueryRequest.getCurrent();
        long size = chatQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chat> chatPage = chatService.page(new Page<>(current, size),
                chatService.getChatQueryWrapperFromRequest(chatQueryRequest));
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(chatService.getChatVOPage(chatPage, loginUser));
    }

    @PostMapping("/update-last-present-time")
    public BaseResponse<Integer> updateLastPresentTime(
            @RequestBody UpdateLastPresentTimeDTO updateLastPresentTimeDTO,
            HttpServletRequest request
    ) {
        if (updateLastPresentTimeDTO == null ||
                updateLastPresentTimeDTO.getId() == null || updateLastPresentTimeDTO.getId() <= 0 ||
                updateLastPresentTimeDTO.getThisUsersId() == null || updateLastPresentTimeDTO.getThisUsersId() <= 0
        ) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        if (!user.getId().equals(updateLastPresentTimeDTO.getThisUsersId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(chatService.updateLastPresentTime(updateLastPresentTimeDTO, loginUser));
    }
}
