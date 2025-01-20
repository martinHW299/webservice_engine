package com.tigo.api.ws_engine.repository;

import com.tigo.api.ws_engine.entity.Source;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SourceRepository extends JpaRepository<Source, UUID> {
    List<Source> findSourceByStatus(String status);
}
