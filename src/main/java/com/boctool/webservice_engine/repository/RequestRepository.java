package com.boctool.webservice_engine.repository;

import com.boctool.webservice_engine.entity.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface RequestRepository extends JpaRepository<Request, String> {
}
