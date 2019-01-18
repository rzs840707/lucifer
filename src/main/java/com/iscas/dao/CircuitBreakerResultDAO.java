package com.iscas.dao;

import com.iscas.bean.result.CircuitBreakResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CircuitBreakerResultDAO extends JpaRepository<CircuitBreakResult, String> {

    @Query("select cb from CircuitBreakResult cb where cb.id = :id")
    public List<CircuitBreakResult> findAllById(@Param("id")String id);

    public int countAllById(@Param("id") String id);
}
