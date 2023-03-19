package com.blog.search.dto;

import com.blog.search.entity.TopTen;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class TopTenResDTO {
    private List<TopTenItemDTO> topTenList = new ArrayList<>();

    public TopTenResDTO(List<TopTen> topTen) {
        this.topTenList = topTen.stream()
                                .map(TopTenItemDTO::new)
                                .collect(Collectors.toList());
    }
}
