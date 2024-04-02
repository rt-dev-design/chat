package dev.runtian.helpcommunity.innerapi.stompconnection;


import dev.runtian.helpcommunity.commons.stompconnection.UserStompConnection;

public interface StompConnectionService {
    void setUserStompConnection(UserStompConnection userStompConnection);

    UserStompConnection getUserStompConnection(Long id);

    void setOffline(Long id);
}
