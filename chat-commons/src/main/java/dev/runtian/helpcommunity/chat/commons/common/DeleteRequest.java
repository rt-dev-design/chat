package dev.runtian.helpcommunity.chat.commons.common;

import java.io.Serializable;
import lombok.Data;

/**
 * 通用的删除请求
 * 指定欲删除数据资源的 id
 */
@Data
public class DeleteRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}