package dev.runtian.helpcommunity.chat.stompgateway.controller.test;


import dev.runtian.helpcommunity.chat.commons.test.TestDto;
import dev.runtian.helpcommunity.chat.innerapi.test.TestMessagingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.utils.NetUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
public class TestStompGatewayController {

    @DubboReference(check = false)
    private TestMessagingService testMessagingService;

    @Autowired
    private Environment environment;

    @MessageMapping("/test/chat")
    public void processMessage(@Payload TestDto testDto) {
        testDto.getTestMessages().add("Hello from Stomp Gateway app destination");
        testDto.getTestMessages().add(NetUtils.getLocalHost());
        testDto.getTestMessages().add(environment.getProperty("dubbo.protocol.port"));
        log.info(testDto.toString());
        testMessagingService.doArchitectureRoundTrip(testDto);
    }
}
