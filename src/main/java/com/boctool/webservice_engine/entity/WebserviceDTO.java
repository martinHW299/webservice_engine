package com.boctool.webservice_engine.entity;

import lombok.*;

import java.util.Map;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebserviceDTO {
    private String webservice;
    Map<String, String> parameters;
    private String code;
    private String name;
    private String description;
    private int userCode;
}
