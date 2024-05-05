package dev.runtian.helpcommunity.chat.chatandmessage.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.runtian.helpcommunity.chat.chatandmessage.mapper.ChatMapper;
import dev.runtian.helpcommunity.chat.chatandmessage.mapper.MessageMapper;
import dev.runtian.helpcommunity.commons.chat.Chat;
import dev.runtian.helpcommunity.commons.chat.ChatQueryRequest;
import dev.runtian.helpcommunity.commons.constant.CommonConstant;
import dev.runtian.helpcommunity.commons.message.Message;
import dev.runtian.helpcommunity.commons.message.MessageQueryRequest;
import dev.runtian.helpcommunity.commons.message.MessageVO;
import dev.runtian.helpcommunity.commons.user.User;
import dev.runtian.helpcommunity.commons.utils.SqlUtils;
import dev.runtian.helpcommunity.innerapi.chat.ChatService;
import dev.runtian.helpcommunity.innerapi.chat.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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

    @Resource
    private ChatMapper chatMapper;

    /**
     * getMessageQueryWrapperFromRequest
     * 根据前端查询请求中指定的参数，
     * 动态生成QueryWrapper，可以看作动态生成SQL里的WHERE从句
     *
     * this
     * messageQueryRequest
     *
     * queryWrapper
     *
     * no exception specified
     */
    @Override
    public QueryWrapper<Message> getMessageQueryWrapperFromRequest(MessageQueryRequest messageQueryRequest) {
        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        if (messageQueryRequest == null) return queryWrapper;
        Date beforeTime = messageQueryRequest.getBeforeTime();
        Long chatId = messageQueryRequest.getChatId();
        Long senderId = messageQueryRequest.getSenderId();
        Long recipientId = messageQueryRequest.getRecipientId();
        Long id = messageQueryRequest.getId();
        String type = messageQueryRequest.getType();
        String content = messageQueryRequest.getContent();
        String sortField = messageQueryRequest.getSortField();
        String sortOrder = messageQueryRequest.getSortOrder();

        if (beforeTime != null) {
            queryWrapper.lt("createTime", beforeTime);
        }

        if (chatId != null && chatId > 0) {
            queryWrapper.eq("chatId", chatId);
        }
        else if (recipientId != null && recipientId > 0) {
            if (senderId != null && senderId > 0) {
                Chat chat = chatMapper.selectOne(ChatService.getChatQueryWrapperFromRequest(
                        ChatQueryRequest.builder()
                                .thisUsersId(senderId)
                                .theOtherUsersId(recipientId)
                                .build()
                ));
                if (chat != null)
                    queryWrapper.eq("chatId", chat.getId());
                else
                    queryWrapper.eq("chatId", -1);
            }
        }

        if (id != null && id > 0) {
            queryWrapper.eq("id", id);
        }

        if (StringUtils.isNotBlank(type)) {
            queryWrapper.eq("type", type);
        }

        if (StringUtils.isNotBlank(content)) {
            queryWrapper.eq("content", content);
        }
        // 默认按消息创建时间降序排序，越近越靠前
        if (StringUtils.isAnyBlank(sortField, sortOrder)) {
            queryWrapper.orderBy(
                    true,
                    false,
                    "createTime");
        } else {
            queryWrapper.orderBy(
                    SqlUtils.validSortField(sortField),
                    sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                    sortField);
        }
        return queryWrapper;
    }

    public MessageVO getMessageVO(Message message) {
        return MessageVO.builder()
                .id(message.getId())
                .createTime(message.getCreateTime())
                .type(message.getType())
                .content(message.getContent())
                .chatId(message.getChatId())
                .senderId(message.getSenderId())
                .build();
    }

    @Override
    public Page<MessageVO> getMessageVOPage(Page<Message> messagePage, User user) {
        Page<MessageVO> messageVOPage = new Page<>(messagePage.getCurrent(), messagePage.getSize(), messagePage.getTotal());
        List<MessageVO> messageVOList = messagePage.getRecords().stream()
                .map(this::getMessageVO).collect(Collectors.toList());
        messageVOPage.setRecords(messageVOList);
        return messageVOPage;
    }

    @Override
    public Page<Message> page(Page<Message> page, MessageQueryRequest messageQueryRequest) {
        // 选完reverser一下，结合上面的默认按消息创建时间降序排序，选出最近的一页消息，给前端prepend
        Page<Message> messagePage = this.page(new Page<>(page.getCurrent(), page.getSize()), this.getMessageQueryWrapperFromRequest(messageQueryRequest));
        if (StringUtils.isAnyBlank(messageQueryRequest.getSortField(), messageQueryRequest.getSortOrder())) {
            Collections.reverse(messagePage.getRecords());
        }
        return messagePage;
    }
}




