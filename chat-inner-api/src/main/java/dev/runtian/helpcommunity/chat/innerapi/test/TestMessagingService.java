package dev.runtian.helpcommunity.chat.innerapi.test;

import dev.runtian.helpcommunity.chat.commons.test.TestDto;

public interface TestMessagingService {
    TestDto doArchitectureRoundTrip(TestDto testDto);
}
