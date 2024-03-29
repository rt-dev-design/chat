package dev.runtian.helpcommunity.chat.commons.model.dto.user;

import dev.runtian.helpcommunity.chat.commons.common.PageRequest;
import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户查询请求
 * 数据类，实例模板
 * 跟贴子查询请求以及其他查询请求一样，会在业务层动态判空并生成 sql, 因此这些参数对于前端都是可选的
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserQueryRequest extends PageRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 开放平台id
     */
    private String unionId;

    /**
     * 公众号openId
     */
    private String mpOpenId;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin/ban
     */
    private String userRole;

    private static final long serialVersionUID = 1L;
}