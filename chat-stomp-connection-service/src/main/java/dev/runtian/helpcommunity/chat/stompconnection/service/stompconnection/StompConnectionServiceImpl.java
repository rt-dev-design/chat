package dev.runtian.helpcommunity.chat.stompconnection.service.stompconnection;

import dev.runtian.helpcommunity.chat.commons.stompconnection.UserStompConnection;
import dev.runtian.helpcommunity.chat.innerapi.stompconnection.StompConnectionService;
import dev.runtian.helpcommunity.chat.stompconnection.repository.UserStompConnectionRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@DubboService
@Slf4j
public class StompConnectionServiceImpl implements StompConnectionService {
    @Autowired
    private UserStompConnectionRepository userStompConnectionRepository;

    @Override
    public void setUserStompConnection(UserStompConnection userStompConnection) {
         log.info(userStompConnection.toString());
         userStompConnection.setOnline(true);
         userStompConnectionRepository.save(userStompConnection);
    }

    @Override
    public UserStompConnection getUserStompConnection(Long id) {
        log.info(id.toString());
        return userStompConnectionRepository
                .findById(id.toString())
                .orElse(UserStompConnection.builder()
                        .online(false)
                        .build());
    }

    @Override
    public void setOffline(Long id) {
        log.info(id.toString());
        UserStompConnection userStompConnection = userStompConnectionRepository.findById(id.toString()).get();
        log.info(userStompConnection.toString());
        userStompConnection.setOnline(false);
        userStompConnectionRepository.save(userStompConnection);
    }
}
