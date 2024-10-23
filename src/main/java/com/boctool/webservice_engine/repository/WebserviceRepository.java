package com.boctool.webservice_engine.repository;

import com.boctool.webservice_engine.entity.Webservice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WebserviceRepository extends JpaRepository<Webservice, String> {
    Webservice findByWebserviceMd5(String md5);
    boolean existsByWebserviceId(String id);
    Webservice findByWebserviceId(String id);
}
