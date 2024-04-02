package dev.runtian.helpcommunity.commons.model.dto.postthumb;

import java.io.Serializable;
import lombok.Data;

/**
 * 帖子点赞 / 取消点赞请求
 * 数据类，作为请求实例的模板，DTO
 * 改变 id 为 postId 的贴子的该用户的点赞状态
 */
@Data
public class PostThumbAddRequest implements Serializable {

    /**
     * 帖子 id
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}