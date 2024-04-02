package dev.runtian.helpcommunity.chat.stompconnection;

import dev.runtian.helpcommunity.chat.stompconnection.repository.UserStompConnectionRepository;
import dev.runtian.helpcommunity.commons.stompconnection.UserStompConnection;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 主类测试
 */
@SpringBootTest
class MainApplicationTests {
    @Autowired
    private UserStompConnectionRepository userStompConnectionRepository;

    @Test
    void add() {
        UserStompConnection userStompConnection = UserStompConnection.builder()
                .id(123L)
                .ip("1.1.1.1")
                .port(8080)
                .online(true)
                .build();
        userStompConnectionRepository.save(userStompConnection);
    }

    @Test
    void addAnother() {
        UserStompConnection userStompConnection = UserStompConnection.builder()
                .id(1234L)
                .ip("1.1.1.2")
                .port(8080)
                .online(true)
                .build();
        userStompConnectionRepository.save(userStompConnection);
    }

    @Test
    void get() {
        System.out.println(userStompConnectionRepository.findById(String.valueOf(123L)));
        System.out.println(userStompConnectionRepository.findById(String.valueOf(1234L)));
    }

    @Test
    void update() {
        userStompConnectionRepository.save(UserStompConnection.builder()
                .id(1234L)
                .ip("1.1.1.2")
                .port(8080)
                .online(false)
                .build());
    }
}
