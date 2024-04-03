package dev.runtian.helpcommunity.chat.stompgateway.controller.chat;

import cn.hutool.core.bean.BeanUtil;
import dev.runtian.helpcommunity.commons.exception.BusinessException;
import dev.runtian.helpcommunity.commons.general.ErrorCode;
import dev.runtian.helpcommunity.commons.general.IdRequest;
import dev.runtian.helpcommunity.commons.message.MessageAddRequest;
import dev.runtian.helpcommunity.commons.stompconnection.UserStompConnection;
import dev.runtian.helpcommunity.commons.test.TestDto;
import dev.runtian.helpcommunity.innerapi.messaging.MessagingService;
import dev.runtian.helpcommunity.innerapi.stompconnection.StompConnectionService;
import dev.runtian.helpcommunity.innerapi.stompgateway.StompGatewayService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.common.utils.NetUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;


@Controller
@DubboService
@Slf4j
public class ChatAppStompController implements StompGatewayService {
    @Autowired
    private Environment environment;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @DubboReference(check = false)
    private MessagingService messagingService;

    @DubboReference(check = false)
    private StompConnectionService stompConnectionService;

    @MessageMapping("/user-connect")
    public void setUserOnline(@Payload IdRequest idRequest) {
        if (idRequest == null || idRequest.getId() == null || idRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户下线消息中的请求id不合法");
        }
        String ip = NetUtils.getLocalHost();
        String portString = environment.getProperty("dubbo.protocol.port");
        if (StringUtils.isAnyBlank(ip, portString)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取网关IP和端口时出错");
        }
        Integer port = Integer.parseInt(portString);
        stompConnectionService.setUserStompConnection(
                UserStompConnection.builder()
                        .id(idRequest.getId())
                        .ip(ip)
                        .port(port)
                        .build()
        );
    }

    @MessageMapping("/user-disconnect")
    public void setUserOffline(@Payload IdRequest idRequest) {
          if (idRequest == null || idRequest.getId() == null || idRequest.getId() <= 0) {
              throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户下线消息中的请求id不合法");
          }
          stompConnectionService.setOffline(idRequest.getId());
    }

    @MessageMapping("/chat")
    public void sendMessage(@Payload MessageAddRequest messageAddRequest) {
        if (messageAddRequest == null || messageAddRequest.getSenderId() == null || messageAddRequest.getRecipientId() == null)
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        messagingService.sendMessage(messageAddRequest);
    }

    @Override
    public void doSendMessage(MessageAddRequest messageAddRequest) {
        messagingTemplate.convertAndSendToUser(
                messageAddRequest.getRecipientId().toString(),
                "/queue/messages",
                messageAddRequest
        );
    }
}
