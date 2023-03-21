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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

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

    private final TopTenRepository topTenRepository;

    private final RestTemplate restTemplate;

    private final EntityManager entityManager;

    @Transactional
    public KeywordSearchResDTO getKeywordSearch(KeywordSearchReqDTO req) {
        try {
            String searchUrl = "/v2/search/blog";
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
            String sort = req.getSort().equals("accuracy") ? "sim" : "date";
            String url = naverHost + searchUrl + "?query=" + req.getQuery() + "&sort=" + sort
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

    // 비관적 락 로직(정확성 위주)
    @Transactional
    public synchronized void increaseViewCountByOne(String searchKeyword) {
        try {
            Optional<TopTen> topTen = topTenRepository.findBySearchKeyword(searchKeyword);
            topTen.ifPresentOrElse(
                    // 조회 수 증가
                    topTenObj -> {
                        topTenObj.setViewCount(topTenObj.getViewCount() + 1);
                        topTenRepository.save(topTenObj);
                    },
                    // 데이터 추가
                    () -> {
                        topTenRepository.save(new TopTen(searchKeyword));
                        entityManager.flush();
                    }
            );
        } catch (DataIntegrityViolationException e) {
            // 예외 처리
            throw new DataIntegrityViolationException("Failed to increase view count for search keyword: " + searchKeyword);
        }
    }

    public TopTenResDTO getTopTenList() {
        List<TopTen> topTen = topTenRepository.findTop10ByOrderByViewCountDesc();
        return new TopTenResDTO(topTen);
    }

}