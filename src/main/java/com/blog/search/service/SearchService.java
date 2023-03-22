package com.blog.search.service;

import com.blog.search.dto.KeywordSearchReqDTO;
import com.blog.search.dto.KeywordSearchResDTO;
import com.blog.search.dto.TopTenResDTO;
import com.blog.search.entity.TopTen;
import com.blog.search.repository.TopTenRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.persistence.EntityManager;
import java.time.Duration;
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
    @Value("${kakaoCacheKey}")
    private String kakaoCacheKey;
    @Value("${naverCacheKey}")
    private String naverCacheKey;
    @Value("${timeOut}")
    private Long timeOut;

    private final TopTenRepository topTenRepository;
    private final RestTemplate restTemplate;
    private final EntityManager entityManager;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public KeywordSearchResDTO getKeywordSearch(KeywordSearchReqDTO req) {

        try {
            increaseViewCountByOne(req.getQuery());

            String cacheKey = kakaoCacheKey + ":" + req.getQuery() + ":" + req.getPage() + ":" + req.getSize() + ":" + req.getSort();
            String cachedResult = (String) redisTemplate.opsForValue().get(cacheKey);
            if (cachedResult != null) {
                return objectMapper.readValue(cachedResult, KeywordSearchResDTO.class);
            }

            String searchUrl = "/v2/search/blog";
            String url = kakaoHost + searchUrl + "?query=" + req.getQuery() + "&sort=" + req.getSort()
                    + "&page=" + req.getPage() + "&size=" + req.getSize();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "KakaoAK " + restApiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            JSONObject jsonObject = new JSONObject(response.getBody());

            KeywordSearchResDTO res = new KeywordSearchResDTO(jsonObject, req, "kakao");
            redisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(res), Duration.ofMinutes(timeOut));
            return res;

        } catch (RestClientException e) {
            return getNaverApiSearch(req); // 실패 시 naverapi 호출
        } catch (JSONException e) {
            throw new JSONException("제이슨 파싱 중 에러가 발생했습니다.");
        } catch (JsonProcessingException e) {
            throw new JSONException("JSON 처리 중 에러가 발생했습니다.");
        }
    }

    public KeywordSearchResDTO getNaverApiSearch(KeywordSearchReqDTO req) {
        try {
            String cacheKey = naverCacheKey + ":" + req.getQuery() + ":" + req.getPage() + ":" + req.getSize() + ":" + req.getSort();
            String cachedResult = (String) redisTemplate.opsForValue().get(cacheKey);
            if (cachedResult != null) {
                return objectMapper.readValue(cachedResult, KeywordSearchResDTO.class);
            }

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

            KeywordSearchResDTO res = new KeywordSearchResDTO(jsonObject, req, "naver");
            redisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(res), Duration.ofMinutes(timeOut));
            return res;

        } catch (RestClientException e) {
            throw new RuntimeException("Api 요청 중 에러가 발생했습니다.");
        } catch (JsonProcessingException e) {
            throw new JSONException("JSON 처리 중 에러가 발생했습니다.");
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