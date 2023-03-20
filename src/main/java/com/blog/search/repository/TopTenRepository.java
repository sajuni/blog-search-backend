package com.blog.search.repository;

import com.blog.search.entity.TopTen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.util.List;
import java.util.Optional;

@Repository
public interface TopTenRepository extends JpaRepository<TopTen, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value ="1000")})
    Optional<TopTen> findBySearchKeyword(String searchKeyword);

    List<TopTen> findTop10ByOrderByViewCountDesc();
}

