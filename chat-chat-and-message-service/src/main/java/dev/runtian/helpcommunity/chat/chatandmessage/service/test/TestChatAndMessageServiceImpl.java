package dev.runtian.helpcommunity.chat.chatandmessage.service.test;

import dev.runtian.helpcommunity.commons.test.TestDto;
import dev.runtian.helpcommunity.innerapi.test.TestChatAndMessageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Component;

@Component
@DubboService
@Slf4j
public class TestChatAndMessageServiceImpl implements TestChatAndMessageService {
    @Override
    public TestDto doArchitectureRoundTrip(TestDto testDto) {
        testDto.getTestMessages().add("Hello from Chat And Message");
        log.info(testDto.toString());
        return testDto;
    }
}
