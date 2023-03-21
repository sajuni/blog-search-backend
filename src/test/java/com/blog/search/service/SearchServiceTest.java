package com.blog.search.service;

import com.blog.search.dto.TopTenResDTO;
import com.blog.search.entity.TopTen;
import com.blog.search.repository.TopTenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class SearchServiceTest {

    @Autowired
    private SearchService searchService;

    @Autowired
    private TopTenRepository topTenRepository;


    @Test
    @DisplayName("TopTen save 테스트")
    public void saveTopTen_test() {
        // Given
        String searchKeyword = String.valueOf(UUID.randomUUID());

        // When
        topTenRepository.save(new TopTen(searchKeyword));

        // Then
        Optional<TopTen> topTen = topTenRepository.findBySearchKeyword(searchKeyword);
        assertTrue(topTen.isPresent());
        assertEquals(1, topTen.get().getViewCount()); // save 시 viewCount 기본 값 1 세팅
    }

    @Test
    @DisplayName("조회수 증가 하는 로직 테스트")
    public void increaseViewCountByOne_test() {
        // Given
        String searchKeyword = String.valueOf(UUID.randomUUID());
        topTenRepository.save(new TopTen(searchKeyword)); // 처음 save 시 기본 값 1이 들어감

        // When
        for (int i = 0; i < 5; i++) {
            searchService.increaseViewCountByOne(searchKeyword); // 조회수 ++
        }

        // Then
        Optional<TopTen> topTen = topTenRepository.findBySearchKeyword(searchKeyword);
        assertTrue(topTen.isPresent());
        assertEquals(6, topTen.get().getViewCount());
    }


    @Test
    @DisplayName("topTenList 가져오는 로직 테스트")
    public void getTopTenList_test() {
        // Given
        String topKeyword = "";
        for (int i = 0; i < 15; i++) {
            String uuid = String.valueOf(UUID.randomUUID());
            topTenRepository.save(new TopTen(uuid));
            if(i > 3) {
                searchService.increaseViewCountByOne(uuid);
            }
            if(i == 4) {
                topKeyword = uuid;
                searchService.increaseViewCountByOne(uuid);
            }
        }

        // When
        TopTenResDTO topTenList = searchService.getTopTenList();

        // Then
        assertNotNull(topTenList);
        assertEquals(10, topTenList.getTopTenList().size());
        assertEquals(topKeyword, topTenList.getTopTenList().get(0).getSearchKeyword());
    }


    @Test
    @DisplayName("여러 쓰레드 동시 요청 테스트")
    public void concurrency_test() throws InterruptedException {
        // Given
        int threadCount = 500;
        String searchKeyword = "강남스타일";
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);

        // When
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    searchService.increaseViewCountByOne(searchKeyword);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();

        // Then
        assertNotNull(searchService.getTopTenList());
        assertEquals(topTenRepository.findBySearchKeyword(searchKeyword).get().getViewCount(), 500);
    }
}