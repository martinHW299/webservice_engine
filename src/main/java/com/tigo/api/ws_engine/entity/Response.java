package com.tigo.api.ws_engine.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "tst_wse_response")
public class Response {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "resp_id", updatable = false, nullable = false, length = 64, unique = true)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "req_id", nullable = false)
    private Request request;

    @Column(name = "init_datetime")
    private LocalDateTime initDatetime;

    @Column(name = "end_datetime")
    private LocalDateTime endDatetime;

    private Long runtime;

    @Column(name = "status_code")
    private Integer statusCode;

    private String message;
}
