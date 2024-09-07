package com.boctool.webservice_engine.repository;

import com.boctool.webservice_engine.entity.Source;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SourceRepository extends JpaRepository<Source, String> {
    Source findSourceBySourceId(String sourceId);
    List<Source> findSourceBySourceStatus(String status);
}
