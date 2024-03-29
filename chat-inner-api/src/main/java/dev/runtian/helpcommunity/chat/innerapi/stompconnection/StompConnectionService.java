package dev.runtian.helpcommunity.chat.innerapi.stompconnection;

import dev.runtian.helpcommunity.chat.commons.stompconnection.UserStompConnection;

public interface StompConnectionService {
    void setUserStompConnection(UserStompConnection userStompConnection);

    UserStompConnection getUserStompConnection(Long id);

    void setOffline(Long id);
}
