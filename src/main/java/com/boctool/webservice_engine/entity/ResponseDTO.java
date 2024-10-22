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
public class ResponseDTO {
    private int code;
    private String status;
    private String message;
    private int rowCount;
    private double runtime;
    private Object data;
}
