package com.blog.search.controller;

import com.blog.search.dto.KeywordSearchReqDTO;
import com.blog.search.service.SearchService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@AllArgsConstructor
@RequestMapping("/api")
@RestController
public class SearchController {

    @Autowired
    private final SearchService searchService;

    @GetMapping("/search")
    public ResponseEntity<?> getSearchKeyword(@RequestBody @Valid KeywordSearchReqDTO reqParam) {
        return new ResponseEntity<>(searchService.getKeywordSearch(reqParam), HttpStatus.OK);
    }



}
