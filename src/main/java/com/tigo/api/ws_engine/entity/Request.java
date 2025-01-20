package com.tigo.api.ws_engine.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;


@Data
@Entity
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "tst_wse_request")
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "req_id", updatable = false, nullable = false, length = 64, unique = true)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ws_id", nullable = false)
    private Webservice webservice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "src_id", nullable = false)
    private Source source;

    private String uid;

    private String sid;

    @Column(name = "parameters_value")
    private String parametersValue;

    @Column(name = "ws_built")
    private Boolean wsBuilt;

    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToOne(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    private Response response;
}

