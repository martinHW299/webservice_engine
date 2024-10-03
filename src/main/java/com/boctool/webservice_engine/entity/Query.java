package com.boctool.webservice_engine.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

import java.util.Date;

@Entity
@Table(name = "TST_API_QUERY")
public class Query {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "QUERY_ID", updatable = false, nullable = false, length = 64)
    private String queryId;
    @Column(name = "QUERY_MD5", nullable = false, updatable = false, length = 64)
    private String queryMd5;
    @Column(name = "QUERY_TEXT", length = 4000)
    private String queryText;
    @Column(name = "QUERY_PARAMS", length = 4000)
    private String queryParams;
    @Column(name = "QUERY_STATUS", length = 2)
    private String queryStatus = "PE"; //AC,HI,EL,AN
    @CreationTimestamp
    @Temporal(TemporalType.DATE)
    @Column(name = "QUERY_REGDATE")
    private Date queryRegdate;

    public String getQueryId() {
        return queryId;
    }

    public void setQueryId(String queryId) {
        this.queryId = queryId;
    }

    public String getQueryMd5() {
        return queryMd5;
    }

    public void setQueryMd5(String queryMd5) {
        this.queryMd5 = queryMd5;
    }

    public String getQueryText() {
        return queryText;
    }

    public void setQueryText(String queryText) {
        this.queryText = queryText;
    }

    public String getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(String queryParams) {
        this.queryParams = queryParams;
    }

    public String getQueryStatus() {
        return queryStatus;
    }

    public void setQueryStatus(String queryStatus) {
        this.queryStatus = queryStatus;
    }

    public Date getQueryRegdate() {
        return queryRegdate;
    }

    public void setQueryRegdate(Date queryRegdate) {
        this.queryRegdate = queryRegdate;
    }
}
