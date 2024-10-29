package com.boctool.webservice_engine.repository;

import com.boctool.webservice_engine.entity.Webservice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WebserviceRepository extends JpaRepository<Webservice, String> {
    Optional<Webservice> findByWebserviceMd5(String md5);
    boolean existsByWebserviceId(String id);
    Optional<Webservice> findByWebserviceId(String id);
}
