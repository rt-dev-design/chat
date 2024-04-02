package dev.runtian.helpcommunity.chat.chatandmessage.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.runtian.helpcommunity.chat.chatandmessage.mapper.ChatMapper;
import dev.runtian.helpcommunity.commons.chat.Chat;
import dev.runtian.helpcommunity.innerapi.chat.ChatService;
import org.springframework.stereotype.Service;

/**
* @author rt
* @description 针对表【chat(对话表)】的数据库操作Service实现
* @createDate 2024-03-30 16:09:26
*/
@Service
public class ChatServiceImpl extends ServiceImpl<ChatMapper, Chat>
    implements ChatService {

}




