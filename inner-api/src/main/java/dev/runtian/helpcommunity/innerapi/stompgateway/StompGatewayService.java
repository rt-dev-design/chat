package dev.runtian.helpcommunity.innerapi.stompgateway;

import dev.runtian.helpcommunity.commons.message.MessageAddRequest;

public interface StompGatewayService {
    void doSendMessage(MessageAddRequest messageAddRequest);
}
