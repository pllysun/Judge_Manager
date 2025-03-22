package com.dong.judge.model.vo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 分页结果封装类
 */
@Data
public class PageResult<T> {
    // Getters and Setters
    /**
     * 总记录数
     */
    private long total;
    
    /**
     * 总页数
     */
    private int totalPages;
    
    /**
     * 当前页码
     */
    private int pageNum;
    
    /**
     * 每页大小
     */
    private int pageSize;
    
    /**
     * 当前页数据
     */
    private List<T> list;
    
    /**
     * 是否有下一页
     */
    private boolean hasNext;
    
    /**
     * 是否有上一页
     */
    private boolean hasPrevious;
    
    /**
     * 从Spring Data的Page对象创建PageResult
     *
     * @param page Spring Data分页对象
     * @param list 当前页数据列表
     * @param <T> 数据类型
     * @return 分页结果对象
     */
    public static <T> PageResult<T> fromPage(Page<?> page, List<T> list) {
        PageResult<T> result = new PageResult<>();
        result.setTotal(page.getTotalElements());
        result.setTotalPages(page.getTotalPages());
        result.setPageNum(page.getNumber() + 1); // Spring Data页码从0开始，需要加1
        result.setPageSize(page.getSize());
        result.setList(list);
        result.setHasNext(page.hasNext());
        result.setHasPrevious(page.hasPrevious());
        return result;
    }

}