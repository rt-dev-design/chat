package dev.runtian.helpcommunity.innerapi.test;


import dev.runtian.helpcommunity.commons.test.TestDto;

public interface TestMessagingService {
    TestDto doArchitectureRoundTrip(TestDto testDto);
}
