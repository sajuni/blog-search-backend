package com.blog.search.service;

import com.blog.search.dto.KeywordSearchReqDTO;
import com.blog.search.dto.KeywordSearchResDTO;
import com.blog.search.dto.TopTenResDTO;
import com.blog.search.entity.TopTen;
import com.blog.search.repository.TopTenRepository;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.OptimisticLockException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

@Service
@RequiredArgsConstructor
public class SearchService {

    @Value("${restApiKey}")
    private String restApiKey;

    @Value("${kakaoSearch}")
    private String host;

    private final TopTenRepository topTenRepository;

    @Transactional
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
            increaseViewCountByOne(req.getQuery());
            return new KeywordSearchResDTO(jsonObject, req);

        } catch (MalformedURLException ex) {
            throw new RuntimeException("URL 생성 중 오류가 발생했습니다." + ex);
        } catch (IOException ex) {
            throw new RuntimeException("API 요청 중 오류가 발생했습니다." + ex);
        } catch (Exception ex) {
            throw new RuntimeException("키워드 검생 중 오류가 발생했습니다." + ex);
        }
    }

    @Transactional
    public void increaseViewCountByOne(String searchKeyword) {
        Optional<TopTen> topTenObj = topTenRepository.findBySearchKeyword(searchKeyword);

        if (topTenObj.isPresent()) {
            int retryCount = 0;
            while (retryCount < 10) {
                TopTen topTen = topTenObj.get();
                try {
                    topTen.setViewCount(topTen.getViewCount() + 1);
                    topTenRepository.save(topTen);
                    break;
                } catch (OptimisticLockException e) {
                    topTenObj = topTenRepository.findBySearchKeyword(searchKeyword);
                    TopTen currentTopTen = topTenObj.get();
                    if (currentTopTen.getVersion() > topTen.getVersion()) {
                        retryCount++;
                        System.out.println("리트라이 실행 중");
                        LockSupport.parkNanos(TimeUnit.MICROSECONDS.toNanos(200));
                    }
                }
            }
            if (retryCount == 10) {
                throw new RuntimeException("동시성 이슈 발생, 재시도 횟수 초과");
            }
        } else {
            topTenRepository.save(new TopTen(searchKeyword));
        }
    }

    public TopTenResDTO getTopTenList() {
        List<TopTen> topTen = topTenRepository.findTop10ByOrderByViewCountDesc();
        return new TopTenResDTO(topTen);
    }
}
