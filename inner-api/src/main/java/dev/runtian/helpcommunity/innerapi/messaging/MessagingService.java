package dev.runtian.helpcommunity.innerapi.messaging;

import dev.runtian.helpcommunity.commons.message.MessageAddRequest;

public interface MessagingService {
    void sendMessage(MessageAddRequest messageAddRequest);
}
