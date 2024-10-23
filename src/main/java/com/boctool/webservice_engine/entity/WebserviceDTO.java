package com.boctool.webservice_engine.entity;

import lombok.*;

import java.util.Map;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebserviceDTO {
    String webservice;
    Map<String, String> parameters;
}
