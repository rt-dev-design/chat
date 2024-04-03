package dev.runtian.helpcommunity.commons.helpcommnet;


import dev.runtian.helpcommunity.commons.user.UserVO;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;


@Data
public class HelpCommentVO implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 贴子id
     */
    private Long postId;

    /**
     * 内容
     */
    private String content;

    /**
     * 点赞数
     */
    private Integer thumbNum;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建人信息
     */
    private UserVO user;

    /**
     * 是否已点赞
     */
    private Boolean hasThumb;

    /**
     * 是否已收藏
     */
    private Boolean hasFavour;

    /**
     * 贴子包含的图片
     */
    private List<CommentImageVO> commentImages;


    private static final long serialVersionUID = 1L;

}