package dev.runtian.helpcommunity.innerapi.chat;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import dev.runtian.helpcommunity.commons.message.Message;
import dev.runtian.helpcommunity.commons.message.MessageQueryRequest;
import dev.runtian.helpcommunity.commons.message.MessageVO;
import dev.runtian.helpcommunity.commons.user.User;

import javax.servlet.http.HttpServletRequest;

/**
* @author rt
* @description 针对表【message(聊天消息)】的数据库操作Service
* @createDate 2024-03-30 16:21:57
*/
public interface MessageService extends IService<Message> {
    QueryWrapper<Message> getMessageQueryWrapperFromRequest(MessageQueryRequest messageQueryRequest);

    Page<MessageVO> getMessageVOPage(Page<Message> messagePage, User user);
}
