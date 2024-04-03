package dev.runtian.helpcommunity.commons.helpcommnet;

import lombok.Data;

import java.io.Serializable;

/**
 * 评论点赞/取消点赞请求
 * 数据类，作为请求实例的模板，DTO
 * 改变 id 为 helpCommentId 的评论的该用户的点赞状态
 */
@Data
public class HelpCommentThumbAddRequest implements Serializable {

    /**
     * 评论 id
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}