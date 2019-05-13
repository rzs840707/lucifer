package com.iscas.dao;

import com.iscas.bean.result.DetectResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetectResultDAO extends JpaRepository<DetectResult, String> {

    @Query("select r from DetectResult r where r.id = :id")
    public List<DetectResult> findAllById(@Param("id") String id);
}
