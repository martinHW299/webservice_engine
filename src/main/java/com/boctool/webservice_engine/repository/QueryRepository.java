package com.boctool.webservice_engine.repository;

import com.boctool.webservice_engine.entity.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QueryRepository extends JpaRepository<Query, String> {
    Query findQueryByQueryMd5(String md5);
    boolean existsQueryByQueryMd5(String md5);
    boolean existsByQueryId(String id);
    Query findQueryByQueryId(String id);
}
