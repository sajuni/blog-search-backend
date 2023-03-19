package com.blog.search.controller;

import com.blog.search.dto.KeywordSearchReqDTO;
import com.blog.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class SearchController{

    @Autowired
    private final SearchService searchService;

    @GetMapping("/search")
    public ResponseEntity<?> getSearchKeyword(@RequestBody @Valid KeywordSearchReqDTO reqParam) {
        try {
            return new ResponseEntity<>(searchService.getKeywordSearch(reqParam), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("키워드 검색 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/topten")
    public ResponseEntity<?> getTopTenList() {
        try {
            return new ResponseEntity<>(searchService.getTopTenList(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("인기 키워드 목록 조회 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
