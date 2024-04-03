package dev.runtian.helpcommunity.innerapi.test;


import dev.runtian.helpcommunity.commons.exception.BusinessException;
import dev.runtian.helpcommunity.commons.test.TestDto;
import dev.runtian.helpcommunity.commons.user.User;

import javax.servlet.http.HttpServletRequest;

public interface TestChatAndMessageService {
    TestDto doArchitectureRoundTrip(TestDto testDto);

}
