package dev.runtian.helpcommunity.commons.test;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class TestDto implements Serializable {

    List<String> testMessages;

    private static final long serialVersionUID = 1L;
}
