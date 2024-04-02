package dev.runtian.helpcommunity.commons.model.dto.post;

import java.io.Serializable;
import java.util.List;
import lombok.Data;

/**
 * 编辑贴子请求
 * 数据类，请求体实例的模板
 * 大多数时候会由前端传过来，用于控件、服务方法的参数中
 * 比起创建请求多了个 id
 */
@Data
public class PostEditRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 标签列表
     */
    private List<String> tags;

    private static final long serialVersionUID = 1L;
}