package com.blog.search.service;

import com.blog.search.dto.KeywordSearchReqDTO;
import com.blog.search.dto.KeywordSearchResDTO;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@Transactional
public class SearchService {

    @Value("${restApiKey}")
    private String restApiKey;

    @Value("${kakaoSearch}")
    private String host;

    public KeywordSearchResDTO getKeywordSearch(KeywordSearchReqDTO req) {
        String searchUrl = "/v2/search/blog";
        try {
            URL url = new URL(host + searchUrl + "?query=" + URLEncoder.encode(req.getQuery(), StandardCharsets.UTF_8) + "&sort=" + req.getSort()
                        + "&page=" + req.getPage() + "&size=" + req.getSize());
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Authorization", "KakaoAK " + restApiKey);

            BufferedReader br;
            br = new BufferedReader(new InputStreamReader(con.getInputStream()));

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            br.close();
            con.disconnect();

            JSONObject jsonObject = new JSONObject(sb.toString());
            return new KeywordSearchResDTO(jsonObject, req.getPage(), req.getSize());

        } catch (Exception ex) {
            System.out.println("테스트: " + ex.getMessage());
        }
        return new KeywordSearchResDTO();
    }

}
