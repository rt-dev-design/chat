package dev.runtian.helpcommunity.innerapi.test;

import dev.runtian.helpcommunity.commons.test.TestDto;

public interface TestStompGatewayService {
    TestDto doArchitectureRoundTrip(TestDto testDto);
}
