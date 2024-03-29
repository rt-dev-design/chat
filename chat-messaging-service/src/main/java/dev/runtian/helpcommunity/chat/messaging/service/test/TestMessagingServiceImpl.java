package dev.runtian.helpcommunity.chat.messaging.service.test;

import dev.runtian.helpcommunity.chat.commons.test.TestDto;
import dev.runtian.helpcommunity.chat.innerapi.test.TestChatAndMessageService;
import dev.runtian.helpcommunity.chat.innerapi.test.TestMessagingService;
import dev.runtian.helpcommunity.chat.innerapi.test.TestStompConnectionService;
import dev.runtian.helpcommunity.chat.innerapi.test.TestStompGatewayService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.rpc.cluster.specifyaddress.Address;
import org.apache.dubbo.rpc.cluster.specifyaddress.UserSpecifiedAddressUtil;
import org.springframework.stereotype.Component;

@Component
@DubboService
@Slf4j
public class TestMessagingServiceImpl implements TestMessagingService {

    @DubboReference(check = false)
    private TestStompConnectionService testStompConnectionService;

    @DubboReference(check = false)
    private TestChatAndMessageService testChatAndMessageService;

    @DubboReference(check = false)
    private TestStompGatewayService testStompGatewayService;

    @Override
    public TestDto doArchitectureRoundTrip(TestDto testDto) {
        testDto.getTestMessages().add("Hello from Messaging");
        log.info(testDto.toString());
        TestDto dtoBackFromStompConnection = testStompConnectionService.doArchitectureRoundTrip(testDto);
        log.info(dtoBackFromStompConnection.toString());
        TestDto dtoBackFromChatAndMessage = testChatAndMessageService.doArchitectureRoundTrip(dtoBackFromStompConnection);
        log.info(dtoBackFromChatAndMessage.toString());

        String ip = testDto.getTestMessages().get(3);
        int port = "9111".equals(testDto.getTestMessages().get(4)) ? 9222 : 9111;
        log.error("ip = " + ip);
        log.error("port = " + String.valueOf(port));
        ReferenceConfig<TestStompGatewayService> referenceConfig = new ReferenceConfig<>();
        referenceConfig.setInterface(TestStompGatewayService.class);
        TestStompGatewayService demo = referenceConfig.get();
        UserSpecifiedAddressUtil.setAddress(new Address(ip, port, true));
        demo.doArchitectureRoundTrip(dtoBackFromChatAndMessage);
        return dtoBackFromChatAndMessage;
    }
}
