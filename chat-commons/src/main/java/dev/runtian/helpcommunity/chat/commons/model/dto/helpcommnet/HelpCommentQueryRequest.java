package dev.runtian.helpcommunity.chat.commons.model.dto.helpcommnet;

import dev.runtian.helpcommunity.chat.commons.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 查询评论请求
 * 数据类，请求体实例的模板
 * 继承了分页请求体，引导前端分页请求资源
 * 配合业务层中的 getQueryWrapper 使用，使得所有属性变得“可选”
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class HelpCommentQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * id
     */
    private Long notId;

    /**
     * 搜索词
     */
    private String searchText;


    /**
     * 内容
     */
    private String content;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 所附属的贴子
     */
    private Long postId;


    private static final long serialVersionUID = 1L;
}