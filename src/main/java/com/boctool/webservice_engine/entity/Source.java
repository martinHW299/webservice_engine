package com.boctool.webservice_engine.entity;


import jakarta.persistence.*;
import jdk.jfr.Enabled;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDate;

@Entity
@Table(name = "TST_API_SOURCE")
public class Source {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "SOURCE_ID", updatable = false, nullable = false, length = 64)
    private String sourceId;
    @Column(name = "SOURCE_NAME", length = 64)
    private String sourceName;
    @Column(name = "SOURCE_URL", length = 256)
    private String sourceUrl;
    @Column(name = "SOURCE_USR", length = 64)
    private String sourceUsr;
    @Column(name = "SOURCE_PWD", length = 64)
    private String sourcePwd;
    @Column(name = "SOURCE_POOL")
    private Integer sourcePool = 2;
    @Column(name = "SOURCE_REGDATE", updatable = false)
    private LocalDate sourceRegdate = LocalDate.now();

    //default 'PE' -- AC,HI,EL,AN
    @Column(name = "SOURCE_STATUS", length = 2)
    private String sourceStatus = "PE";

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getSourceUsr() {
        return sourceUsr;
    }

    public void setSourceUsr(String sourceUsr) {
        this.sourceUsr = sourceUsr;
    }

    public String getSourcePwd() {
        return sourcePwd;
    }

    public void setSourcePwd(String sourcePwd) {
        this.sourcePwd = sourcePwd;
    }

    public Integer getSourcePool() {
        return sourcePool;
    }

    public void setSourcePool(Integer sourcePool) {
        this.sourcePool = sourcePool;
    }

    public LocalDate getSourceRegdate() {
        return sourceRegdate;
    }

    public void setSourceRegdate(LocalDate sourceRegdate) {
        this.sourceRegdate = sourceRegdate;
    }

    public String getSourceStatus() {
        return sourceStatus;
    }

    public void setSourceStatus(String sourceStatus) {
        this.sourceStatus = sourceStatus;
    }
}
