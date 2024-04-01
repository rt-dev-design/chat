package dev.runtian.helpcommunity.chat.commons.general;

import dev.runtian.helpcommunity.chat.commons.constant.CommonConstant;
import lombok.Data;

/**
 * 分页请求
 * 数据类，作为分页请求体的模板
 * 需要分页的请求的请求体数据类可用继承并扩展这个类，就会将自身变成一个分页请求体
 * 当前页面和页面大小有默认值
 */
@Data
public class PageRequest {

    /**
     * 当前页号
     */
    private int current = 1;

    /**
     * 页面大小
     */
    private int pageSize = 10;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序顺序（默认升序）
     */
    private String sortOrder = CommonConstant.SORT_ORDER_ASC;
}
