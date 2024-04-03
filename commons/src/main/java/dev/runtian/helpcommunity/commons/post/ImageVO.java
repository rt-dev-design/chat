package dev.runtian.helpcommunity.commons.post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 贴子包含的图片的信息视图
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageVO implements Serializable {
    /**
     * 主键，标识符
     */
    private Long id;

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

    private static final long serialVersionUID = 1L;
}