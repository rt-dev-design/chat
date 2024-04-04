package dev.runtian.helpcommunity.innerapi.chat;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import dev.runtian.helpcommunity.commons.chat.Chat;
import dev.runtian.helpcommunity.commons.chat.ChatQueryRequest;
import dev.runtian.helpcommunity.commons.chat.ChatVO;
import dev.runtian.helpcommunity.commons.chat.UpdateLastPresentTimeDTO;
import dev.runtian.helpcommunity.commons.exception.BusinessException;
import dev.runtian.helpcommunity.commons.message.MessageAddRequest;
import dev.runtian.helpcommunity.commons.user.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
* @author rt
* @description 针对表【chat(对话表)】的数据库操作Service
* @createDate 2024-03-30 16:09:26
*/
public interface ChatService extends IService<Chat> {
    boolean checkIfThereIsUnreadForUser(long userId) throws BusinessException;


    ChatVO getChatVO(Chat chat, User user);

    Page<ChatVO> getChatVOPage(Page<Chat> chatPage, User user);

    Boolean updateLastPresentTime(UpdateLastPresentTimeDTO updateLastPresentTimeDTO, User user) throws BusinessException;

    void storeMessage(MessageAddRequest messageAddRequest) throws BusinessException;

    public static QueryWrapper<Chat> getChatQueryWrapperFromRequest(ChatQueryRequest chatQueryRequest) {
        QueryWrapper<Chat> queryWrapper = new QueryWrapper<>();
        if (chatQueryRequest == null) return queryWrapper;
        Long id = chatQueryRequest.getId();
        Date userxLastPresentTime = chatQueryRequest.getUsexLastPresentTime();
        Date useryLastPresentTime = chatQueryRequest.getUseryLastPresentTime();
        Date lastMessageTime = chatQueryRequest.getLastMessageTime();

        Long userxId = chatQueryRequest.getUserxId();
        Long useryId = chatQueryRequest.getUseryId();
        Long userId = chatQueryRequest.getUserId();
        Long thisUsersId = chatQueryRequest.getThisUsersId();
        Long theOtherUsersId = chatQueryRequest.getTheOtherUsersId();

        if (id != null && id > 0) queryWrapper.eq("id", id);
        if (userxLastPresentTime != null) queryWrapper.eq("userxLastPresentTime", userxLastPresentTime);
        if (useryLastPresentTime != null) queryWrapper.eq("useryLastPresentTime", useryLastPresentTime);
        if (lastMessageTime != null) queryWrapper.eq("lastMessageTime", lastMessageTime);

        if (userxId != null && userxId > 0 || useryId != null && useryId > 0) {
            if (userxId != null && userxId > 0) queryWrapper.eq("userxId", userxId);
            if (useryId != null && useryId > 0) queryWrapper.eq("useryId", useryId);
        } else if (userId != null && userId > 0) {
            queryWrapper.and(qw -> qw.eq("userxId", userId).or().eq("useryId", userId));
        } else if (thisUsersId != null && thisUsersId > 0 && theOtherUsersId != null && theOtherUsersId > 0) {
            queryWrapper.and(qw ->
                    qw.eq("userxId", thisUsersId).eq("useryId", theOtherUsersId)
                            .or().eq("userxId", theOtherUsersId).eq("useryId", thisUsersId));
        }
        return queryWrapper;
    }

    Page<Chat> page(Page<Chat> page, ChatQueryRequest chatQueryRequest);
}
