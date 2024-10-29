package com.boctool.webservice_engine.repository;

import com.boctool.webservice_engine.entity.Response;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResponseRepository extends JpaRepository<Response, String> {
}
