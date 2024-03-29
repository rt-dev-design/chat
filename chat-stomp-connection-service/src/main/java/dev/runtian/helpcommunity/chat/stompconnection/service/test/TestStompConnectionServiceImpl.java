package dev.runtian.helpcommunity.chat.stompconnection.service.test;

import dev.runtian.helpcommunity.chat.commons.test.TestDto;
import dev.runtian.helpcommunity.chat.innerapi.test.TestStompConnectionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Component;

@Component
@DubboService
@Slf4j
public class TestStompConnectionServiceImpl implements TestStompConnectionService {
    @Override
    public TestDto doArchitectureRoundTrip(TestDto testDto) {
        testDto.getTestMessages().add("Hello from Stomp Connection");
        log.info(testDto.toString());
        return testDto;
    }
}
