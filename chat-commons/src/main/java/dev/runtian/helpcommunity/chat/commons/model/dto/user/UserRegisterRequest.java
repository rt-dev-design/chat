package dev.runtian.helpcommunity.chat.commons.model.dto.user;

import java.io.Serializable;
import lombok.Data;

/**
 * 用户注册请求体
 * 业务上，用户可以后续再上传头像等等其他信息
 */
@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;

    private String userAccount;

    private String userPassword;

    /**
     * 要求用户重复输入密码 2 次，这是第二次的那个密码
     * 前后端都应该校验 2 个密码的一致性，原因包括用户不一定通过前端注册等等
     */
    private String checkPassword;
}
