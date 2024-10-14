package com.boctool.webservice_engine.repository;

import com.boctool.webservice_engine.entity.Response;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResponseRepository extends JpaRepository<Response, String> {
}
