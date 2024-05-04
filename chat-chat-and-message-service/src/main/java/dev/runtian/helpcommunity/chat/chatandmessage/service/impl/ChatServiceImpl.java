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
import dev.runtian.helpcommunity.commons.message.Message;
import dev.runtian.helpcommunity.commons.message.MessageAddRequest;
import dev.runtian.helpcommunity.commons.user.User;
import dev.runtian.helpcommunity.innerapi.chat.ChatService;
import dev.runtian.helpcommunity.innerapi.chat.MessageService;
import dev.runtian.helpcommunity.innerapi.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 对话服务实现类ChatServiceImpl，bean
 * 会被Spring Boot进行AOP代理，开启事务
 * 会被Dubbo处理，生成RPC服务端
 * 注入了RPC客户端和其他服务bean
 * 实现了接口ChatService，继承了MyBatisPlus的ServiceImpl
 */
@Service
@DubboService
@Slf4j
public class ChatServiceImpl extends ServiceImpl<ChatMapper, Chat>
    implements ChatService {

    @DubboReference(check = false)
    private UserService userService;

    @Resource
    private MessageService messageService;

    /**
     * 检查某用户是否有未读消息
     * 查询这个用户参与的所有对话，若其中1个存在未读，则为真，否则为假
     *
     * this ChatServiceImpl
     * userId
     *
     * true/false
     *
     * 或者抛异常
     */
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public boolean checkIfThereIsUnreadForUser(long userId) throws BusinessException {
        // id的校验在控件中，这里暂无需再校验
        QueryWrapper<Chat> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userxId", userId).or().eq("useryId", userId);
        List<Chat> chatList = this.getBaseMapper().selectList(queryWrapper);
        for (Chat chat : chatList) {
            if (getUserLastPresentTimeOnChat(userId, chat).before(chat.getLastMessageTime()))
                return true;
        }
        return false;
    }

    /**
     * 给定userId和chat（其中有2个user），返回该user的LastPresentTime
     */
    public Date getUserLastPresentTimeOnChat(Long userId, Chat chat) {
        return userId.equals(chat.getUserxId()) ? chat.getUsexLastPresentTime() : chat.getUseryLastPresentTime();
    }

    /**
     * 给定userId和chat（其中有2个user），返回另一userId
     */
    public Long getTheOtherUsersId(Long userId, Chat chat) {
        return userId.equals(chat.getUserxId()) ? chat.getUseryId() : chat.getUserxId();
    }

    @Override
    public ChatVO getChatVO(Chat chat, User user) {
        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("chatId", chat.getId());
        queryWrapper.orderByDesc("createTime");
        queryWrapper.last("LIMIT 1");
        Message message = messageService.getOne(queryWrapper);
        String lastMessage = message == null ? "" : message.getContent();
        return ChatVO.builder()
                .id(chat.getId())
                .theOtherUser(userService.getUserVO(userService.getById(getTheOtherUsersId(user.getId(), chat))))
                .lastMessageTime(chat.getLastMessageTime())
                .lastMessage(lastMessage)
                .thereAreNewMessages(getUserLastPresentTimeOnChat(user.getId(), chat).before(chat.getLastMessageTime()))
                .build();
    }

    // 这里先偷懒调一下上个方法吧（）
    @Override
    public Page<ChatVO> getChatVOPage(Page<Chat> chatPage, User user) {
        Page<ChatVO> ret = new Page<>(chatPage.getCurrent(), chatPage.getSize(), chatPage.getTotal());
        List<Chat> chatList = chatPage.getRecords();
        List<ChatVO> chatVOList = new ArrayList<>();
        for (Chat chat : chatList) {
            chatVOList.add(getChatVO(chat, user));
        }
        ret.setRecords(chatVOList);
        return ret;
    }

    @Override
    public Boolean updateLastPresentTime(UpdateLastPresentTimeDTO updateLastPresentTimeDTO, User user) throws BusinessException {
        Long id = updateLastPresentTimeDTO.getId();
        Long thisUsersId = updateLastPresentTimeDTO.getThisUsersId();
        Long theOtherUsersId = updateLastPresentTimeDTO.getTheOtherUsersId();
        Chat chat = null;
        if (id != null && id > 0) chat = this.getById(id);
        else {
            chat = this.getBaseMapper().selectOne(ChatService.getChatQueryWrapperFromRequest(
                    ChatQueryRequest.builder()
                            .thisUsersId(thisUsersId)
                            .theOtherUsersId(theOtherUsersId)
                            .build()
            ));
        }
        if (chat == null) throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "对话暂不存在，无需更新最后出现时间");
        if (chat.getUserxId().equals(user.getId())) {
            chat.setUsexLastPresentTime(new Date());
        } else if (chat.getUseryId().equals(user.getId())) {
            chat.setUseryLastPresentTime(new Date());
        }
        boolean result = this.updateById(chat);
        if (!result) throw new BusinessException(ErrorCode.OPERATION_ERROR);
        return true;
    }

    /**
     * 存储消息业务storeMessage
     * 给定一条消息，
     * 关联到相应对话，存储到数据库
     * 存储成功后，再更新对话的最新消息时间和用户的最后出现时间
     * 开启事务，因为会和查询对话、消息的业务并发
     *
     * this ChatServiceImpl
     * MessageAddRequest
     *
     * void
     *
     * BusinessException
     */
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void storeMessage(MessageAddRequest messageAddRequest) throws BusinessException {
        Long chatId = messageAddRequest.getChatId();
        // 查询或创建对话
        Chat chat = null;
        if (chatId != null && chatId > 0) {
            chat = this.getById(chatId);
            if (chat == null)
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "会话不存在");
        }
        else {
            chat = this.getOne(ChatService.getChatQueryWrapperFromRequest(
                    ChatQueryRequest.builder()
                            .thisUsersId(messageAddRequest.getSenderId())
                            .theOtherUsersId(messageAddRequest.getRecipientId())
                            .build()
            ));
            if (chat == null) {
                chat = Chat.builder()
                        .userxId(messageAddRequest.getSenderId())
                        .useryId(messageAddRequest.getRecipientId())
                        .build();
                if (!this.save(chat))
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "创建会话失败");
            }
        }

        // 关联对话并存储
        Message message = Message.builder()
                .type(messageAddRequest.getType())
                .content(messageAddRequest.getContent())
                .senderId(messageAddRequest.getSenderId())
                .chatId(chat.getId())
                .build();
        if (!messageService.save(message))
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "存储消息失败");

        // 更新对话中的时间戳
        message = messageService.getById(message.getId());
        if (message == null)
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "查询新插入的消息时出错");
        chat.setLastMessageTime(message.getCreateTime());
        setUserLastPresentTimeOnChatToNow(chat, messageAddRequest.getSenderId());
        if (!this.updateById(chat))
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "更新最新消息时间或用户最后出现时间失败");
    }

    /**
     * 分页查询对话
     * 开启事务，因为会和存储消息的业务并发
     */
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Page<Chat> page(Page<Chat> page, ChatQueryRequest chatQueryRequest) {
        return this.page(page, ChatService.getChatQueryWrapperFromRequest(chatQueryRequest));
    }

    public void setUserLastPresentTimeOnChatToNow(Chat chat, Long userId) {
        if (userId.equals(chat.getUserxId())) {
            chat.setUsexLastPresentTime(new Date());
        }
        else if (userId.equals(chat.getUseryId())) {
            chat.setUseryLastPresentTime(new Date());
        }
    }
}




