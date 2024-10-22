package com.boctool.webservice_engine.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "TST_WSE_SOURCE")
public class Source {
    @Id
    @Column(name = "SOURCE_ID", updatable = false, nullable = false, length = 64)
    private String sourceId = UUID.randomUUID().toString();
    @Column(name = "SOURCE_NAME")
    private String sourceName;
    @Column(name = "SOURCE_URL")
    private String sourceUrl;
    @Column(name = "SOURCE_USR")
    private String sourceUsr;
    @Column(name = "SOURCE_PWD")
    private String sourcePwd;
    @Column(name = "SOURCE_POOL")
    private Integer sourcePool;
    @Column(name = "SOURCE_CREATION_DATE", updatable = false)
    private LocalDate sourceCreationDate = LocalDate.now();
    @Column(name = "SOURCE_STATUS", length = 2)
    private String sourceStatus = "PE";//AC,HI,EL,AN
    @Column(name = "SOURCE_TIMEOUT")
    private long sourceTimeout = 20000;
    @Column(name = "SOURCE_IDLETIMEOUT")
    private long sourceIdleTimeout = 300000;
    @Column(name = "SOURCE_MAXLIFETIME")
    private long sourceMaxLifetime = 900000;
    @Column(name = "SOURCE_MINIDLE")
    private int sourceMinIdle = 1;
}
