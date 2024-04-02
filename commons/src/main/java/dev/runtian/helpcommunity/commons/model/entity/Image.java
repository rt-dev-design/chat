package dev.runtian.helpcommunity.commons.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 贴子包含的图片的信息
 */
@TableName(value ="image")
@Data
@Builder
public class Image implements Serializable {
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
     * 属于哪个贴子
     */
    private Long postId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}