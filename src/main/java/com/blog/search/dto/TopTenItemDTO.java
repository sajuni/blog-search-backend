package com.blog.search.dto;

import com.blog.search.entity.TopTen;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TopTenItemDTO {
    private Long viewCount;
    private String searchKeyword;

    public TopTenItemDTO(TopTen topTen) {
        viewCount = topTen.getViewCount();
        searchKeyword = topTen.getSearchKeyword();
    }
}
