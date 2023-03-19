package com.blog.search.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
@ToString
public class KeywordSearchReqDTO {
    @NotBlank(message = "query parameter required")
    private String query;

    private String sort;

    @Min(value = 1, message = "size is less than min")
    @Max(value = 50, message = "page is more than max")
    private Integer page;

    @Min(value = 1, message = "size is less than min")
    @Max(value = 50, message = "page is more than max")
    private Integer size;

    // 검색어만 들어올 경우 기본 값 세팅
    public KeywordSearchReqDTO() {
        this.sort = "accuracy";
        this.page = 1;
        this.size = 10;
    }
}
