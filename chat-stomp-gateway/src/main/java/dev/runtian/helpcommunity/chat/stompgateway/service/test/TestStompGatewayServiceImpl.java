package dev.runtian.helpcommunity.chat.stompgateway.service.test;

import dev.runtian.helpcommunity.chat.commons.test.TestDto;
import dev.runtian.helpcommunity.chat.innerapi.test.TestStompGatewayService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@DubboService
@Slf4j
public class TestStompGatewayServiceImpl implements TestStompGatewayService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    public TestDto doArchitectureRoundTrip(TestDto testDto) {
        testDto.getTestMessages().add("Hello from Stomp Gateway outbound destination");
        log.error(testDto.toString());
        messagingTemplate.convertAndSendToUser(
                testDto.getTestMessages().get(0),
                "/queue/messages",
                testDto
        );
        return testDto;
    }
}
