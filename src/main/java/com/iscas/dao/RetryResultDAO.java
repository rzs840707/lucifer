package com.iscas.dao;


import com.iscas.bean.result.DetectResult;
import com.iscas.bean.result.RetryResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RetryResultDAO extends JpaRepository<RetryResult,String> {

    @Query("select r from RetryResult r where r.id = :id")
    public List<RetryResult> findAllById(@Param("id") String id);

    public int countAllById(@Param("id") String id);
}
