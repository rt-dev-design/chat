package dev.runtian.helpcommunity.chat.httpgateway.controller;

import dev.runtian.helpcommunity.chat.commons.test.TestDto;
import dev.runtian.helpcommunity.chat.innerapi.test.TestChatAndMessageService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
public class TestController {
    @DubboReference
    private TestChatAndMessageService testChatAndMessageService;

    @PostMapping("/hello")
    public TestDto hello(@RequestBody TestDto testDto) {
        testDto.getTestMessages().add("Hello from Http Gateway");
        return testChatAndMessageService.doArchitectureRoundTrip(testDto);
    }
}
