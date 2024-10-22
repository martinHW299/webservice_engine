package com.boctool.webservice_engine.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
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
    @Column(name = "WEBSERVICE_TEXT")
    private String webserviceText;
    @Column(name = "WEBSERVICE_PARAMS")
    private String webserviceParams;
    @Column(name = "WEBSERVICE_STATUS", length = 2)
    private String webserviceStatus = "PE"; //AC,HI,EL,AN
    @Column(name = "WEBSERVICE_CREATION_UID")
    private Integer webserviceCreationUid = 0;
    @Column(name = "WEBSERVICE_CREATION_DATE")
    private LocalDate webserviceCreationDate = LocalDate.now();
    @Column(name = "WEBSERVICE_CHANGE_UID")
    private Integer webserviceChangeUid = 0;
    @Column(name = "WEBSERVICE_CHANGE_DATE")
    private Date webserviceChangeDate;
    @Column(name = "WEBSERVICE_DESCRIPTION")
    private String webserviceDescription;
}
