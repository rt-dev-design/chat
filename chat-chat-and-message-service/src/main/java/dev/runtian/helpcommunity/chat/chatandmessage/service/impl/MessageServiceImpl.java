package dev.runtian.helpcommunity.chat.chatandmessage.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.runtian.helpcommunity.chat.chatandmessage.mapper.MessageMapper;
import dev.runtian.helpcommunity.commons.message.Message;
import dev.runtian.helpcommunity.commons.message.MessageQueryRequest;
import dev.runtian.helpcommunity.commons.message.MessageVO;
import dev.runtian.helpcommunity.commons.user.User;
import dev.runtian.helpcommunity.innerapi.chat.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

/**
* @author rt
* @description 针对表【message(聊天消息)】的数据库操作Service实现
* @createDate 2024-03-30 16:21:57
*/
@Service
@DubboService
@Slf4j
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message>
    implements MessageService {

    @Override
    public QueryWrapper<Message> getMessageQueryWrapperFromRequest(MessageQueryRequest messageQueryRequest) {
        return null;
    }

    @Override
    public Page<MessageVO> getMessageVOPage(Page<Message> messagePage, User user) {
        return null;
    }
}




