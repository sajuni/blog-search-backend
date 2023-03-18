package com.blog.search.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.json.JSONObject;

@Setter
@Getter
@NoArgsConstructor
public class SearchItemDTO {
    private String blogname;
    private String datetime;
    private String thumbnail;
    private String contents;
    private String title;
    private String url;

    public SearchItemDTO(JSONObject jsonObject) {
        blogname = jsonObject.getString("blogname");
        datetime = jsonObject.getString("datetime");
        thumbnail = jsonObject.getString("thumbnail");
        contents = jsonObject.getString("contents");
        title = jsonObject.getString("title");
        url = jsonObject.getString("url");
    }
}
