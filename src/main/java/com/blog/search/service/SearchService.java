package com.blog.search.service;

import com.blog.search.dto.KeywordSearchReqDTO;
import com.blog.search.dto.KeywordSearchResDTO;
import com.blog.search.dto.TopTenResDTO;
import com.blog.search.entity.TopTen;
import com.blog.search.repository.TopTenRepository;
import lombok.RequiredArgsConstructor;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.persistence.OptimisticLockException;
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

    @Value("${kakaoSearchHost}")
    private String kakaoHost;

    @Value("${naverSearchHost}")
    private String naverHost;

    @Value("${XNaverClientId}")
    private String naverClientId;

    @Value("${XNaverClientSecret}")
    private String naverClientSecret;

    @Value("${retryMax}")
    private int retryMax;

    private final TopTenRepository topTenRepository;

    private final RestTemplate restTemplate;

    @Transactional
    public KeywordSearchResDTO getKeywordSearch(KeywordSearchReqDTO req) {
        try {
            String searchUrl = "/v2/search/blog";
//            String encodedQuery = stringEncoder(req.getQuery());
            String url = kakaoHost + searchUrl + "?query=" + req.getQuery() + "&sort=" + req.getSort()
                    + "&page=" + req.getPage() + "&size=" + req.getSize();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "KakaoAK " + restApiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            JSONObject jsonObject = new JSONObject(response.getBody());

            increaseViewCountByOne(req.getQuery());

            return new KeywordSearchResDTO(jsonObject, req, "kakao");
        } catch (RestClientException e) {
            return getNaverApiSearch(req); // 실패 시 naverapi 호출
        } catch (JSONException e) {
            throw new JSONException("제이슨 파싱 중 에러가 발생했습니다.");
        }
    }

    public KeywordSearchResDTO getNaverApiSearch(KeywordSearchReqDTO req) {
        try {
            String searchUrl = "/v1/search/blog.json";
            String encodedQuery = stringEncoder(req.getQuery()); // 네이버는 검색 시 한글 인코딩
            String sort = req.getSort().equals("accuracy") ? "sim" : "date";
            String url = naverHost + searchUrl + "?query=" + encodedQuery + "&sort=" + sort
                    + "&start=" + req.getPage() + "&display=" + req.getSize();

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Naver-Client-Id", naverClientId);
            headers.set("X-Naver-Client-Secret", naverClientSecret);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            JSONObject jsonObject = new JSONObject(response.getBody());
            return new KeywordSearchResDTO(jsonObject, req, "naver");
        } catch (RestClientException e) {
            throw new RuntimeException("Api 요청 중 에러가 발생했습니다.");
        }
    }

    @Transactional
    public void increaseViewCountByOne(String searchKeyword) {
        Optional<TopTen> topTenObj = topTenRepository.findBySearchKeyword(searchKeyword);

        if (topTenObj.isPresent()) {
            int retryCount = 0;
            while (retryCount < retryMax) {
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
                        LockSupport.parkNanos(TimeUnit.MICROSECONDS.toNanos(200));
                    }
                }
            }
            if (retryCount == retryMax) {
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

    public String stringEncoder(String param) {
        return URLEncoder.encode(param, StandardCharsets.UTF_8);
    }
}
