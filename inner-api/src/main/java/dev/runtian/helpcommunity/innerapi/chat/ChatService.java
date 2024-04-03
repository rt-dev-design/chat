package dev.runtian.helpcommunity.innerapi.chat;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import dev.runtian.helpcommunity.commons.chat.Chat;
import dev.runtian.helpcommunity.commons.chat.ChatQueryRequest;
import dev.runtian.helpcommunity.commons.chat.ChatVO;
import dev.runtian.helpcommunity.commons.chat.UpdateLastPresentTimeDTO;
import dev.runtian.helpcommunity.commons.exception.BusinessException;
import dev.runtian.helpcommunity.commons.user.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
* @author rt
* @description 针对表【chat(对话表)】的数据库操作Service
* @createDate 2024-03-30 16:09:26
*/
public interface ChatService extends IService<Chat> {
    boolean checkIfThereIsUnreadForUser(long userId) throws BusinessException;

    QueryWrapper<Chat> getChatQueryWrapperFromRequest(ChatQueryRequest chatQueryRequest);

    ChatVO getChatVO(Chat chat, User user);

    Page<ChatVO> getChatVOPage(Page<Chat> chatPage, User user);

    Integer updateLastPresentTime(UpdateLastPresentTimeDTO updateLastPresentTimeDTO, User user);


}
