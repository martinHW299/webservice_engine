package com.boctool.webservice_engine.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

import java.util.Date;

@Entity
@Table(name = "TST_API_RESPONSE")
public class Response {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "RESPONSE_ID", updatable = false, nullable = false)
    private String responseId;

    @CreationTimestamp
    @Temporal(TemporalType.DATE)
    @Column(name = "RESPONSE_REGDATE")
    private Date responseRegdate;

    @Column(name = "RESPONSE_RUNTIME")
    private Double responseRuntime;

    @Column(name = "RESPONSE_CODE")
    private Integer responseCode;

    @Column(name = "RESPONSE_MESSAGE", length = 2000)
    private String responseMessage;

//    @Column(name = "RESPONSE_DATA", length = 4000)
//    private String responseData;

    @Column(name = "RESPONSE_SOURCE_ID")
    private String responseSourceId;

    @Column(name = "RESPONSE_QUERY_ID")
    private String responseQueryId;

    @Column(name = "RESPONSE_QUERY_MD5")
    private String responseQueryMd5;

    @Column(name = "RESPONSE_QUERY_TEXT", length = 4000)
    private String responseQueryText;

    @Column(name = "RESPONSE_QUERY_PARAMS", length = 4000)
    private String responseQueryParams;

    @Column(name = "RESPONSE_QUERY_VALUES", length = 4000)
    private String responseQueryValues;

    public String getResponseId() {
        return responseId;
    }

    public void setResponseId(String responseId) {
        this.responseId = responseId;
    }

    public Date getResponseRegdate() {
        return responseRegdate;
    }

    public void setResponseRegdate(Date responseRegdate) {
        this.responseRegdate = responseRegdate;
    }

    public Double getResponseRuntime() {
        return responseRuntime;
    }

    public void setResponseRuntime(Double responseRuntime) {
        this.responseRuntime = responseRuntime;
    }

    public Integer getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(Integer responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

//    public String getResponseData() {
//        return responseData;
//    }
//
//    public void setResponseData(String responseData) {
//        this.responseData = responseData;
//    }

    public String getResponseSourceId() {
        return responseSourceId;
    }

    public void setResponseSourceId(String responseSourceId) {
        this.responseSourceId = responseSourceId;
    }

    public String getResponseQueryId() {
        return responseQueryId;
    }

    public void setResponseQueryId(String responseQueryId) {
        this.responseQueryId = responseQueryId;
    }

    public String getResponseQueryMd5() {
        return responseQueryMd5;
    }

    public void setResponseQueryMd5(String responseQueryMd5) {
        this.responseQueryMd5 = responseQueryMd5;
    }

    public String getResponseQueryText() {
        return responseQueryText;
    }

    public void setResponseQueryText(String responseQueryText) {
        this.responseQueryText = responseQueryText;
    }

    public String getResponseQueryParams() {
        return responseQueryParams;
    }

    public void setResponseQueryParams(String responseQueryParams) {
        this.responseQueryParams = responseQueryParams;
    }

    public String getResponseQueryValues() {
        return responseQueryValues;
    }

    public void setResponseQueryValues(String responseQueryValues) {
        this.responseQueryValues = responseQueryValues;
    }
}