package com.blog.search.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class KeywordSearchResDTO {
    private List<SearchItemDTO> content = new ArrayList<>();
    private Integer page;
    private Integer size;
    private Integer total_elements;
    private Boolean is_end;
    private String sort;
    private String searchKeyword;

    public KeywordSearchResDTO(JSONObject jsonObject, KeywordSearchReqDTO req) {
        JSONObject meta = jsonObject.getJSONObject("meta");
        this.total_elements = meta.getInt("total_count");
        this.is_end = meta.getBoolean("is_end");
        this.page = req.getPage();
        this.size = req.getSize();
        this.sort = req.getSort();
        this.searchKeyword = req.getQuery();

        JSONArray jsonArray = jsonObject.getJSONArray("documents");
        for(int i = 0; i < jsonArray.length(); i++) {
            JSONObject documentObj = jsonArray.getJSONObject(i);
            SearchItemDTO searchItemDTO = new SearchItemDTO(documentObj);
            this.content.add(searchItemDTO);
        }
    }
}
