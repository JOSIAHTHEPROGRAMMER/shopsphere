package com.app.shopsphere.dto;

import java.util.List;

import lombok.Data;

@Data
public class PagedResponse<T> {

    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean last;
}