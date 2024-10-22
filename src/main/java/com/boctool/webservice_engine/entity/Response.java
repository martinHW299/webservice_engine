package com.boctool.webservice_engine.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "TST_WSE_RESPONSE")
public class Response {
    @Id
    @Column(name = "RESPONSE_ID", updatable = false, nullable = false)
    private String responseId = UUID.randomUUID().toString();
    @Column(name = "RESPONSE_INIT_DATE")
    private Date responseInitDate;
    @Column(name = "RESPONSE_END_DATE")
    private Date responseEndDate;
    @Column(name = "RESPONSE_RUNTIME")
    private Double responseRuntime;
    @Column(name = "RESPONSE_STATUS", length = 2)
    private String responseStatus = "PE"; //AC,HI,EL,AN
    @Column(name = "RESPONSE_CODE")
    private Integer responseCode;
    @Column(name = "RESPONSE_MESSAGE", length = 2000)
    private String responseMessage;
    @Column(name = "RESPONSE_SOURCE_ID")
    private String responseSourceId;
    @Column(name = "RESPONSE_WEBSERVICE_ID")
    private String responseWebserviceId;
    @Column(name = "RESPONSE_WEBSERVICE_MD5")
    private String responseWebserviceMd5;
    @Column(name = "RESPONSE_WEBSERVICE_TEXT", length = 4000)
    private String responseWebserviceText;
    @Column(name = "RESPONSE_WEBSERVICE_PARAMS", length = 4000)
    private String responseWebserviceParams;
    @Column(name = "RESPONSE_WEBSERVICE_VALUES", length = 4000)
    private String responseWebserviceValues;
}