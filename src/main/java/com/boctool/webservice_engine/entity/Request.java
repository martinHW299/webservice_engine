package com.boctool.webservice_engine.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDate;
import java.util.Date;

@Entity
@Table(name = "TST_WSE_REQUEST")
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "REQUEST_ID", updatable = false, nullable = false)
    private String requestId;
    @Column(name = "REQUEST_QUERY_ID")
    private String requestQueryId;
    @Column(name = "REQUEST_QUERY_VALUES", length = 2000)
    private String requestQueryValues;
    @Column(name = "REQUEST_SOURCE_ID")
    private String requestSourceId;
    @Column(name = "REQUEST_STATUS", nullable = false)
    private String requestStatus = "PE";//AC,HI,EL,AN
    @CreationTimestamp
    @Temporal(TemporalType.DATE)
    @Column(name = "REQUEST_REGDATE", updatable = false)
    private Date requestRegdate;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getRequestQueryId() {
        return requestQueryId;
    }

    public void setRequestQueryId(String requestQueryId) {
        this.requestQueryId = requestQueryId;
    }

    public String getRequestQueryValues() {
        return requestQueryValues;
    }

    public void setRequestQueryValues(String requestQueryValues) {
        this.requestQueryValues = requestQueryValues;
    }

    public String getRequestSourceId() {
        return requestSourceId;
    }

    public void setRequestSourceId(String requestSourceId) {
        this.requestSourceId = requestSourceId;
    }

    public String getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(String requestStatus) {
        this.requestStatus = requestStatus;
    }

    public Date getRequestRegdate() {
        return requestRegdate;
    }

    public void setRequestRegdate(Date requestRegdate) {
        this.requestRegdate = requestRegdate;
    }
}
