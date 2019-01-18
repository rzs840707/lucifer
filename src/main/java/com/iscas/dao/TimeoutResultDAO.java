package com.iscas.dao;

import com.iscas.bean.result.TimeoutResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimeoutResultDAO extends JpaRepository<TimeoutResult, String> {

    @Query("select r from TimeoutResult r where r.id = :id")
    public List<TimeoutResult> findAllById(@Param("id") String id);

    public int countAllById(@Param("id") String id);
}
