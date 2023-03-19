package com.blog.search.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KeywordSearchResDTO {
    private List<Object> content = new ArrayList<>();
    private Integer page;
    private Integer size;
    private Integer total_elements;
    private Boolean is_end;
    private String sort;
    private String searchKeyword;
    private String flag;

    public KeywordSearchResDTO(JSONObject jsonObject, KeywordSearchReqDTO req, String flag) {
        this.page = req.getPage();
        this.size = req.getSize();
        this.sort = req.getSort();
        this.searchKeyword = req.getQuery();
        this.flag = flag + "Api";

        if (flag.equals("kakao")) {
            JSONObject meta = jsonObject.getJSONObject("meta");
            this.total_elements = meta.getInt("total_count");
            this.is_end = meta.getBoolean("is_end");

            JSONArray jsonArray = jsonObject.getJSONArray("documents");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject documentObj = jsonArray.getJSONObject(i);
                KakaoSearchItemDTO searchItemDTO = new KakaoSearchItemDTO(documentObj);
                this.content.add(searchItemDTO);
            }
        } else {
            this.total_elements = jsonObject.getInt("total");
            JSONArray jsonArray = jsonObject.getJSONArray("items");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject itemObj = jsonArray.getJSONObject(i);
                NaverSearchItemDTO searchItemDTO = new NaverSearchItemDTO(itemObj);
                this.content.add(searchItemDTO);
            }
        }
    }

}
