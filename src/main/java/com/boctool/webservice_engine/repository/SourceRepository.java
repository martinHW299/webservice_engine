package com.boctool.webservice_engine.repository;

import com.boctool.webservice_engine.entity.Source;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SourceRepository extends JpaRepository<Source, String> {
    Optional<Source> findSourceBySourceId(String sourceId);
    List<Source> findSourceBySourceStatus(String status);
    boolean existsBySourceId(String sourceId);
}
