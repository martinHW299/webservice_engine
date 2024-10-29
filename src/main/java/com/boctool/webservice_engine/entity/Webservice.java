package com.boctool.webservice_engine.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.UUID;


@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "TST_WSE_WEBSERVICE")
public class Webservice {

    @Id
    @Column(name = "WEBSERVICE_ID", updatable = false, nullable = false, length = 64)
    private String webserviceId = UUID.randomUUID().toString();
    @Column(name = "WEBSERVICE_MD5")
    private String webserviceMd5;
    @Column(name = "WEBSERVICE_CODE")
    private String webserviceCode;
    @Column(name = "WEBSERVICE_NAME")
    private String webserviceName;
    @Column(name = "WEBSERVICE_TEXT", length = 4000)
    private String webserviceText;
    @Column(name = "WEBSERVICE_PARAMS", length = 4000)
    private String webserviceParams;
    @Column(name = "WEBSERVICE_STATUS", length = 2)
    private String webserviceStatus = "PE"; //AC,HI,EL,AN
    @Column(name = "WEBSERVICE_CREATION_UID")
    private Integer webserviceCreationUid = 0;

    @CreatedDate
    @Column(name = "WEBSERVICE_CREATION_DATE", updatable = false)
    private LocalDateTime webserviceCreationDate;

    @Column(name = "WEBSERVICE_CHANGE_UID")
    private Integer webserviceChangeUid = 0;

    @LastModifiedDate
    @Column(name = "WEBSERVICE_CHANGE_DATE")
    private LocalDateTime webserviceChangeDate;

    @Column(name = "WEBSERVICE_DESCRIPTION", length = 4000)
    private String webserviceDescription;
}

