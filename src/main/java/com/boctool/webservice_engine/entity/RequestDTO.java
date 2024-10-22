package com.boctool.webservice_engine.entity;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestDTO {
    String webserviceId;
    String sourceId;
    Map<String, Object> parameters;
    int maxRows;
}
