package dev.runtian.helpcommunity.commons.wxminiprogram;

import lombok.Data;

import java.io.Serializable;

@Data
public class WxMiniProgramLoginRequest implements Serializable {
    private String code;

    private String avatar;

    private String nickname;

    private static final long serialVersionUID = 1L;
}
