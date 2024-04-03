package dev.runtian.helpcommunity.chat.chatandmessage.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.runtian.helpcommunity.chat.chatandmessage.mapper.ChatMapper;
import dev.runtian.helpcommunity.commons.chat.Chat;
import dev.runtian.helpcommunity.commons.chat.ChatQueryRequest;
import dev.runtian.helpcommunity.commons.chat.ChatVO;
import dev.runtian.helpcommunity.commons.chat.UpdateLastPresentTimeDTO;
import dev.runtian.helpcommunity.commons.exception.BusinessException;
import dev.runtian.helpcommunity.commons.general.ErrorCode;
import dev.runtian.helpcommunity.commons.user.User;
import dev.runtian.helpcommunity.innerapi.chat.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

/**
* @author rt
* @description 针对表【chat(对话表)】的数据库操作Service实现
* @createDate 2024-03-30 16:09:26
*/
@Service
@DubboService
@Slf4j
public class ChatServiceImpl extends ServiceImpl<ChatMapper, Chat>
    implements ChatService {

    @Override
    public boolean checkIfThereIsUnreadForUser(long userId) throws BusinessException {
        if (userId == 2L) throw new BusinessException(ErrorCode.OPERATION_ERROR);
        return false;
    }

    @Override
    public QueryWrapper<Chat> getChatQueryWrapperFromRequest(ChatQueryRequest chatQueryRequest) {
        return null;
    }

    @Override
    public ChatVO getChatVO(Chat chat, User user) {
        return null;
    }

    @Override
    public Page<ChatVO> getChatVOPage(Page<Chat> chatPage, User user) {
        return null;
    }

    @Override
    public Integer updateLastPresentTime(UpdateLastPresentTimeDTO updateLastPresentTimeDTO, User user) {
        return null;
    }
}




