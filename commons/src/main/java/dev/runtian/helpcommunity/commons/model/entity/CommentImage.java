package dev.runtian.helpcommunity.commons.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评论包含的图片的信息
 * @TableName comment_image
 */
@TableName(value ="comment_image")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentImage implements Serializable {
    /**
     * 主键，标识符
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 逻辑删除
     */
    @TableLogic
    private Integer isDelete;

    /**
     * 图片云端地址
     */
    private String url;

    /**
     * 可选的跳转地址
     */
    private String nextUrl;

    /**
     * 宽度
     */
    private Integer width;

    /**
     * 高度
     */
    private Integer height;

    /**
     * 是否为处理过的缩略图
     */
    private Integer isThumbnail;

    /**
     * 属于哪个评论
     */
    private Long commentId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}