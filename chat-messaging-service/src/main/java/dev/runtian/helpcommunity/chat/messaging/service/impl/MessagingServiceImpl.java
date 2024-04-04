package dev.runtian.helpcommunity.chat.messaging.service.impl;

import dev.runtian.helpcommunity.commons.message.MessageAddRequest;
import dev.runtian.helpcommunity.commons.stompconnection.UserStompConnection;
import dev.runtian.helpcommunity.innerapi.chat.ChatService;
import dev.runtian.helpcommunity.innerapi.messaging.MessagingService;
import dev.runtian.helpcommunity.innerapi.stompconnection.StompConnectionService;
import dev.runtian.helpcommunity.innerapi.stompgateway.StompGatewayService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.rpc.cluster.specifyaddress.Address;
import org.apache.dubbo.rpc.cluster.specifyaddress.UserSpecifiedAddressUtil;
import org.springframework.stereotype.Service;

@Service
@DubboService
@Slf4j
public class MessagingServiceImpl implements MessagingService {
    @DubboReference(check = false)
    private StompConnectionService stompConnectionService;

    @DubboReference(check = false)
    private ChatService chatService;

    @Override
    public void sendMessage(MessageAddRequest messageAddRequest) {
        UserStompConnection userStompConnection = stompConnectionService.getUserStompConnection(messageAddRequest.getRecipientId());
        if (userStompConnection.getOnline()) {
            String ip = userStompConnection.getIp();
            Integer port = userStompConnection.getPort();
            log.info("sending message to ip = " + ip + " port = " + port);
            ReferenceConfig<StompGatewayService> referenceConfig = new ReferenceConfig<>();
            referenceConfig.setInterface(StompGatewayService.class);
            StompGatewayService stompGatewayService = referenceConfig.get();
            UserSpecifiedAddressUtil.setAddress(new Address(ip, port, true));
            stompGatewayService.doSendMessage(messageAddRequest);
        }
        chatService.storeMessage(messageAddRequest);
    }
}
