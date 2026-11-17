package org.csu.lumtrace.lumtracediagnosis.dto.response;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页响应
 */
@Data
public class PageResult<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long total;
    private Integer page;
    private Integer size;
    private List<T> records;

    public static <T> PageResult<T> of(List<T> records, long total, int page, int size) {
        PageResult<T> r = new PageResult<>();
        r.setRecords(records);
        r.setTotal(total);
        r.setPage(page);
        r.setSize(size);
        return r;
    }
}
