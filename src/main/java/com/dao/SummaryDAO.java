package com.iscas.dao;

import com.iscas.bean.result.Summary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SummaryDAO extends JpaRepository<Summary, Integer> {
    public Summary findById(@Param("id") String id);

    public void deleteById(@Param("id") String id);
}
