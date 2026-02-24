package com.mcp.common.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * 分页响应
 *
 * @param <T> 数据类型
 * @author MCP Platform
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    /**
     * 当前页码
     */
    private int page;

    /**
     * 每页大小
     */
    private int size;

    /**
     * 总记录数
     */
    private long total;

    /**
     * 总页数
     */
    private int totalPages;

    /**
     * 数据列表
     */
    private List<T> records;

    /**
     * 是否有下一页
     */
    public boolean hasNext() {
        return page < totalPages;
    }

    /**
     * 是否有上一页
     */
    public boolean hasPrevious() {
        return page > 1;
    }

    /**
     * 创建空分页结果
     */
    public static <T> PageResult<T> empty(int page, int size) {
        return PageResult.<T>builder()
                .page(page)
                .size(size)
                .total(0)
                .totalPages(0)
                .records(Collections.emptyList())
                .build();
    }

    /**
     * 从列表创建分页结果
     */
    public static <T> PageResult<T> of(List<T> records, long total, int page, int size) {
        int totalPages = (int) Math.ceil((double) total / size);
        return PageResult.<T>builder()
                .page(page)
                .size(size)
                .total(total)
                .totalPages(totalPages)
                .records(records)
                .build();
    }
}
