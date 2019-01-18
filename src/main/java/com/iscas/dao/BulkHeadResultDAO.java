package com.iscas.dao;

import com.iscas.bean.result.BulkHeadResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BulkHeadResultDAO extends JpaRepository<BulkHeadResult, String> {

    @Query("select bh from BulkHeadResult bh where bh.id = :id")
    public List<BulkHeadResult> findAllById(@Param("id") String id);

    public int countAllById(@Param("id") String id);
}
