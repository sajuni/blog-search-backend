package com.blog.search.repository;

import com.blog.search.entity.TopTen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TopTenRepository extends JpaRepository<TopTen, Long> {
    Optional<TopTen> findBySearchKeyword(String searchKeyword);

    List<TopTen> findTop10ByOrderByViewCountDesc();
}

