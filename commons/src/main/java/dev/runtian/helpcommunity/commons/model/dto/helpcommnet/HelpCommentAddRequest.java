package dev.runtian.helpcommunity.commons.model.dto.helpcommnet;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 创建评论请求
 * 数据类，请求体实例的模板
 * 大多数时候会由前端传过来，用于控件、服务方法的参数中
 */
@Data
public class HelpCommentAddRequest implements Serializable {

    private Long postId;

    /**
     * 内容
     */
    private String content;


    private static final long serialVersionUID = 1L;
}