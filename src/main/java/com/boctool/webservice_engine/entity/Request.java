package com.boctool.webservice_engine.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "TST_WSE_REQUEST")
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "REQUEST_ID", updatable = false, nullable = false)
    private String requestId = UUID.randomUUID().toString();
    @Column(name = "REQUEST_WEBSERVICE_ID")
    private String requestWebserviceId;
    @Column(name = "REQUEST_WEBSERVICE_VALUES", length = 4000)
    private String requestWebserviceValues;
    @Column(name = "REQUEST_SOURCE_ID")
    private String requestSourceId;
    @Column(name = "REQUEST_STATUS")
    private String requestStatus = "PE";//AC,HI,EL,AN
    @Column(name = "REQUEST_CREATION_DATE")
    private LocalDate requestCreationDate = LocalDate.now();
    @Column(name = "REQUEST_UID")
    private Integer requestUid;
    @Column(name = "REQUEST_SID")
    private Integer requestSid;
    @Column(name = "REQUEST_TOKEN")
    private String requestToken;
}
