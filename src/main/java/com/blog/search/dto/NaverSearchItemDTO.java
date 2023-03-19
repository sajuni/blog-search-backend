package com.blog.search.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.json.JSONObject;

@Getter
@Setter
@NoArgsConstructor
public class NaverSearchItemDTO {
    private String title;
    private String bloggername;
    private String description;
    private String link;
    private String bloggerlink;
    private String postdate;

    public NaverSearchItemDTO(JSONObject jsonObject) {
        this.title = jsonObject.getString("title");
        this.bloggername = jsonObject.getString("bloggername");
        this.description = jsonObject.getString("description");
        this.link = jsonObject.getString("link");
        this.bloggerlink = jsonObject.getString("bloggerlink");
        this.postdate = jsonObject.getString("postdate");
    }
}
