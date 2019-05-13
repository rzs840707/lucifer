package com.iscas.dao;

import com.iscas.bean.result.InjectResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InjectResultDAO extends JpaRepository<InjectResult, String> {

    @Query("select ir from InjectResult ir where ir.id = :id")
    public List<InjectResult> findAllById(@Param("id") String id);
}
