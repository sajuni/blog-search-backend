package com.blog.search.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@Entity
@NoArgsConstructor
public class TopTen {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String searchKeyword;

    @Column
    private Long viewCount;

//    낙관적 락 로직
//    @Version
//    private Long version;

    public TopTen(String keyword) {
        this.searchKeyword = keyword;
        this.viewCount = 1L;
    }

    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }
}
