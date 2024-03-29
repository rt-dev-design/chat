package dev.runtian.helpcommunity.chat.commons.model.dto.postfavour;

import java.io.Serializable;
import lombok.Data;

/**
 * 帖子收藏 / 取消收藏请求
 * 数据类，作为请求实例的模板，DTO
 * 改变 id 为 postId 的贴子的该用户的收藏状态
 */
@Data
public class PostFavourAddRequest implements Serializable {

    /**
     * 帖子 id
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}