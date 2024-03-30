package dev.runtian.helpcommunity.chat.chatandmessage.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.runtian.helpcommunity.chat.chatandmessage.mapper.MessageMapper;
import dev.runtian.helpcommunity.chat.commons.chat.Message;
import dev.runtian.helpcommunity.chat.innerapi.chat.MessageService;
import org.springframework.stereotype.Service;

/**
* @author rt
* @description 针对表【message(聊天消息)】的数据库操作Service实现
* @createDate 2024-03-30 16:21:57
*/
@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message>
    implements MessageService {

}




