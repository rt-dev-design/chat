package dev.runtian.helpcommunity.chat.commons.model.dto.user;

import java.io.Serializable;
import lombok.Data;

/**
 * 用户创建请求，给管理员用的
 * 密码会有默认值
 */
@Data
public class UserAddRequest implements Serializable {

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户头像 URL
     */
    private String userAvatar;

    /**
     * 用户角色: user, admin
     */
    private String userRole;

    private static final long serialVersionUID = 1L;
}