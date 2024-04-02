package dev.runtian.helpcommunity.commons.model.dto.helpcommnet;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 更新评论请求，目前和编辑请求基本相同
 * 数据类，请求体实例的模板
 * 大多数时候会由前端传过来，用于控件、服务方法的参数中
 * 比起创建请求多了个 id
 */
@Data
public class HelpCommentUpdateRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 内容
     */
    private String content;

    private static final long serialVersionUID = 1L;
}